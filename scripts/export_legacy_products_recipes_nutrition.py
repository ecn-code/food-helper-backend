#!/usr/bin/env python3
"""Export legacy DynamoDB products and recipes with their nutrition values."""

from __future__ import annotations

import argparse
import csv
import json
import os
import re
import subprocess
from collections import Counter
from dataclasses import asdict, dataclass
from decimal import Decimal, InvalidOperation, ROUND_HALF_UP
from pathlib import Path
from urllib.parse import quote, urlparse
from typing import Any, Iterable


NUTRIENT_COLUMNS = {
    "Kilocalorias": "calories",
    "Kilocalorías": "calories",
    "Grasas": "fats",
    "protein": "proteins",
    "Proteinas": "proteins",
    "Proteínas": "proteins",
    "Hidratos de Carbono": "carbohydrates",
}

OUTPUT_COLUMNS = [
    "name",
    "description",
    "calories",
    "fats",
    "proteins",
    "carbohydrates",
]

SEITAN_PORTION_PATTERN = re.compile(r"^Seitan\s+1/([1-9][0-9]*)$", re.IGNORECASE)
INGREDIENT_NAME_ALIASES = {
    "Harina de Maiz": "Harina de maiz",
}


@dataclass(frozen=True)
class ExportRow:
    name: str
    description: str
    calories: str
    fats: str
    proteins: str
    carbohydrates: str


@dataclass(frozen=True)
class RecipeIngredientRow:
    recipe_name: str
    product_name: str
    grams: str


@dataclass
class ExportReport:
    products_exported: int = 0
    recipes_exported: int = 0
    recipe_ingredients_exported: int = 0
    synthetic_products_exported: int = 0
    lines_read: int = 0
    skipped_without_name: int = 0
    skipped_recipe_ingredients: int = 0
    ignored_by_pk: Counter[str] | None = None
    ignored_nutrients: Counter[str] | None = None
    defaulted_nutrients: Counter[str] | None = None
    missing_ingredient_products: Counter[str] | None = None
    normalized_ingredient_products: Counter[str] | None = None

    def __post_init__(self) -> None:
        if self.ignored_by_pk is None:
            self.ignored_by_pk = Counter()
        if self.ignored_nutrients is None:
            self.ignored_nutrients = Counter()
        if self.defaulted_nutrients is None:
            self.defaulted_nutrients = Counter()
        if self.missing_ingredient_products is None:
            self.missing_ingredient_products = Counter()
        if self.normalized_ingredient_products is None:
            self.normalized_ingredient_products = Counter()

    def to_json_dict(self) -> dict[str, Any]:
        return {
            "products_exported": self.products_exported,
            "recipes_exported": self.recipes_exported,
            "recipe_ingredients_exported": self.recipe_ingredients_exported,
            "synthetic_products_exported": self.synthetic_products_exported,
            "lines_read": self.lines_read,
            "skipped_without_name": self.skipped_without_name,
            "skipped_recipe_ingredients": self.skipped_recipe_ingredients,
            "ignored_by_pk": dict(sorted(self.ignored_by_pk.items())),
            "ignored_nutrients": dict(sorted(self.ignored_nutrients.items())),
            "defaulted_nutrients": dict(sorted(self.defaulted_nutrients.items())),
            "missing_ingredient_products": dict(sorted(self.missing_ingredient_products.items())),
            "normalized_ingredient_products": dict(sorted(self.normalized_ingredient_products.items())),
        }


def unwrap_dynamodb_value(value: Any) -> Any:
    if not isinstance(value, dict) or len(value) != 1:
        return value

    kind, raw_value = next(iter(value.items()))
    if kind == "S":
        return raw_value
    if kind == "N":
        return raw_value
    if kind == "BOOL":
        return raw_value
    if kind == "NULL":
        return None
    if kind == "L":
        return [unwrap_dynamodb_value(item) for item in raw_value]
    if kind == "M":
        return {key: unwrap_dynamodb_value(item) for key, item in raw_value.items()}
    return value


def unwrap_dynamodb_item(item: dict[str, Any]) -> dict[str, Any]:
    return {key: unwrap_dynamodb_value(value) for key, value in item.items()}


def read_legacy_items(path: Path) -> Iterable[dict[str, Any]]:
    with path.open("r", encoding="utf-8") as legacy_file:
        for line_number, line in enumerate(legacy_file, start=1):
            stripped = line.strip()
            if not stripped:
                continue
            try:
                payload = json.loads(stripped)
            except json.JSONDecodeError as exc:
                raise ValueError(f"Invalid JSON at line {line_number}: {exc}") from exc

            item = payload.get("Item")
            if not isinstance(item, dict):
                raise ValueError(f"Missing DynamoDB Item object at line {line_number}")
            yield unwrap_dynamodb_item(item)


def decimal_string(value: Any) -> str:
    if value in (None, ""):
        return "0.00"
    try:
        decimal = Decimal(str(value).strip())
    except (InvalidOperation, AttributeError) as exc:
        raise ValueError(f"Invalid numeric nutrition value: {value!r}") from exc
    return str(decimal.quantize(Decimal("0.01"), rounding=ROUND_HALF_UP))


def iter_nutrition_entries(raw_nutrition: Any) -> Iterable[tuple[str, Any]]:
    if not isinstance(raw_nutrition, list):
        return

    for entry in raw_nutrition:
        if isinstance(entry, dict):
            name = entry.get("name")
            value = entry.get("value")
        elif isinstance(entry, list) and len(entry) >= 3:
            name = entry[0]
            value = entry[2]
        else:
            continue

        if name:
            yield str(name), value


def map_nutrition(raw_nutrition: Any, report: ExportReport) -> dict[str, str]:
    values = {
        "calories": None,
        "fats": None,
        "proteins": None,
        "carbohydrates": None,
    }

    for legacy_name, value in iter_nutrition_entries(raw_nutrition):
        column = NUTRIENT_COLUMNS.get(legacy_name)
        if column is None:
            report.ignored_nutrients[legacy_name] += 1
            continue
        values[column] = decimal_string(value)

    mapped = {}
    for column, value in values.items():
        if value is None:
            report.defaulted_nutrients[column] += 1
            mapped[column] = "0.00"
        else:
            mapped[column] = value
    return mapped


def item_to_export_row(item: dict[str, Any], report: ExportReport) -> ExportRow | None:
    name = item.get("name") or item.get("SK")
    if not name:
        report.skipped_without_name += 1
        return None

    nutrition = map_nutrition(item.get("nutritional_value"), report)
    return ExportRow(
        name=str(name),
        description=str(item.get("description") or ""),
        calories=nutrition["calories"],
        fats=nutrition["fats"],
        proteins=nutrition["proteins"],
        carbohydrates=nutrition["carbohydrates"],
    )


def normalize_recipe_ingredient(product_name: str, unit: Any, amount: Any, report: ExportReport) -> tuple[str, Any]:
    aliased_name = INGREDIENT_NAME_ALIASES.get(product_name, product_name)
    if aliased_name != product_name:
        report.normalized_ingredient_products[f"{product_name} -> {aliased_name}"] += 1
        product_name = aliased_name

    match = SEITAN_PORTION_PATTERN.match(product_name.strip())
    if not match:
        return product_name, amount

    denominator = Decimal(match.group(1))
    portion_grams = Decimal("1000") / denominator
    grams = Decimal(str(amount).strip()) * portion_grams
    report.normalized_ingredient_products[f"{product_name} -> Seitan"] += 1
    return "Seitan", grams


def recipe_ingredient_rows(
    item: dict[str, Any],
    recipe_name: str,
    product_names: set[str],
    report: ExportReport,
) -> list[RecipeIngredientRow]:
    rows: list[RecipeIngredientRow] = []
    raw_products = item.get("products")
    if not isinstance(raw_products, list):
        return rows

    for raw_product in raw_products:
        if not isinstance(raw_product, list) or len(raw_product) < 3:
            report.skipped_recipe_ingredients += 1
            continue

        product_name = raw_product[0]
        grams = raw_product[2]
        if not product_name or grams in (None, ""):
            report.skipped_recipe_ingredients += 1
            continue

        product_name = str(product_name)
        product_name, grams = normalize_recipe_ingredient(product_name, raw_product[1], grams, report)
        if product_name not in product_names:
            report.missing_ingredient_products[product_name] += 1

        rows.append(
            RecipeIngredientRow(
                recipe_name=recipe_name,
                product_name=product_name,
                grams=decimal_string(grams),
            )
        )
    return rows


def export_rows(
    items: Iterable[dict[str, Any]],
) -> tuple[list[ExportRow], list[ExportRow], list[RecipeIngredientRow], ExportReport]:
    legacy_items = list(items)
    original_product_names = {
        str(item.get("name") or item.get("SK"))
        for item in legacy_items
        if item.get("PK") == "product" and (item.get("name") or item.get("SK"))
    }
    recipe_items_by_name = {
        str(item.get("name") or item.get("SK")): item
        for item in legacy_items
        if item.get("PK") == "recipe" and (item.get("name") or item.get("SK"))
    }
    products: list[ExportRow] = []
    recipes: list[ExportRow] = []
    recipe_ingredients: list[RecipeIngredientRow] = []
    report = ExportReport()

    for item in legacy_items:
        report.lines_read += 1
        pk = item.get("PK")
        if pk == "product":
            row = item_to_export_row(item, report)
            if row is not None:
                products.append(row)
                report.products_exported += 1
        elif pk == "recipe":
            row = item_to_export_row(item, report)
            if row is not None:
                recipes.append(row)
                product_names = original_product_names | set(recipe_items_by_name)
                ingredients = recipe_ingredient_rows(item, row.name, product_names, report)
                recipe_ingredients.extend(ingredients)
                report.recipes_exported += 1
                report.recipe_ingredients_exported += len(ingredients)
        else:
            report.ignored_by_pk[str(pk)] += 1

    referenced_names = {ingredient.product_name for ingredient in recipe_ingredients}
    synthetic_product_names = sorted((referenced_names & set(recipe_items_by_name)) - original_product_names)
    for product_name in synthetic_product_names:
        row = item_to_export_row(recipe_items_by_name[product_name], report)
        if row is not None:
            products.append(row)
            report.products_exported += 1
            report.synthetic_products_exported += 1

    return products, recipes, recipe_ingredients, report


def write_csv(path: Path, rows: list[Any], fieldnames: list[str]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8", newline="") as output_file:
        writer = csv.DictWriter(output_file, fieldnames=fieldnames)
        writer.writeheader()
        for row in rows:
            writer.writerow(asdict(row))


def write_json(path: Path, rows: list[ExportRow]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8") as output_file:
        json.dump([asdict(row) for row in rows], output_file, ensure_ascii=False, indent=2)
        output_file.write("\n")


def write_report(path: Path, report: ExportReport) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8") as output_file:
        json.dump(report.to_json_dict(), output_file, ensure_ascii=False, indent=2)
        output_file.write("\n")


def sql_string(value: str) -> str:
    return "'" + value.replace("'", "''") + "'"


def sql_numeric(value: str) -> str:
    Decimal(value)
    return value


def build_import_sql(products: list[ExportRow], recipes: list[ExportRow], recipe_ingredients: list[RecipeIngredientRow]) -> str:
    lines = [
        "-- Generated by scripts/export_legacy_products_recipes_nutrition.py",
        "BEGIN;",
        "",
    ]

    lines.append("-- Products and per-100g nutritional values")
    for product in products:
        lines.extend(
            [
                "WITH upserted_product AS (",
                "    INSERT INTO products (name, description, grams_per_unit, default_price, media_id)",
                f"    VALUES ({sql_string(product.name)}, {sql_string(product.description)}, 100.00, NULL, NULL)",
                "    ON CONFLICT (name) DO UPDATE SET",
                "        description = EXCLUDED.description,",
                "        grams_per_unit = EXCLUDED.grams_per_unit,",
                "        default_price = EXCLUDED.default_price",
                "    RETURNING id",
                ")",
                "INSERT INTO nutritional_values (product_id, calories, carbohydrates, proteins, fats)",
                (
                    "SELECT id, "
                    f"{sql_numeric(product.calories)}, "
                    f"{sql_numeric(product.carbohydrates)}, "
                    f"{sql_numeric(product.proteins)}, "
                    f"{sql_numeric(product.fats)} "
                    "FROM upserted_product"
                ),
                "ON CONFLICT (product_id) DO UPDATE SET",
                "    calories = EXCLUDED.calories,",
                "    carbohydrates = EXCLUDED.carbohydrates,",
                "    proteins = EXCLUDED.proteins,",
                "    fats = EXCLUDED.fats;",
                "",
            ]
        )

    lines.append("-- Recipes")
    for recipe in recipes:
        lines.extend(
            [
                "INSERT INTO recipes (name, description, instructions, media_id)",
                f"VALUES ({sql_string(recipe.name)}, {sql_string(recipe.description)}, '', NULL)",
                "ON CONFLICT (name) DO UPDATE SET",
                "    description = EXCLUDED.description,",
                "    instructions = EXCLUDED.instructions;",
                "",
            ]
        )

    if recipes:
        recipe_names = ", ".join(sql_string(recipe.name) for recipe in recipes)
        lines.extend(
            [
                "-- Replace ingredients for imported recipes",
                "DELETE FROM recipe_products rp",
                "USING recipes r",
                "WHERE rp.recipe_id = r.id",
                f"  AND r.name IN ({recipe_names});",
                "",
            ]
        )

    lines.append("-- Recipe ingredients")
    for ingredient in recipe_ingredients:
        lines.extend(
            [
                "INSERT INTO recipe_products (recipe_id, product_id, grams)",
                "SELECT r.id, p.id, " + sql_numeric(ingredient.grams),
                "FROM recipes r",
                "JOIN products p ON p.name = " + sql_string(ingredient.product_name),
                "WHERE r.name = " + sql_string(ingredient.recipe_name),
                "ON CONFLICT (recipe_id, product_id) DO UPDATE SET grams = EXCLUDED.grams;",
                "",
            ]
        )

    lines.extend(["COMMIT;", ""])
    return "\n".join(lines)


def write_sql(path: Path, products: list[ExportRow], recipes: list[ExportRow], recipe_ingredients: list[RecipeIngredientRow]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(build_import_sql(products, recipes, recipe_ingredients), encoding="utf-8")


def postgres_connection_uri(database_url: str | None, username: str | None, password: str | None) -> str:
    raw_url = database_url or os.getenv("SPRING_DATASOURCE_URL") or "jdbc:postgresql://localhost:5432/foodhelper"
    raw_username = username or os.getenv("SPRING_DATASOURCE_USERNAME") or "foodhelper"
    raw_password = password or os.getenv("SPRING_DATASOURCE_PASSWORD") or "foodhelper"

    if raw_url.startswith("jdbc:postgresql://"):
        parsed = urlparse("postgresql://" + raw_url.removeprefix("jdbc:postgresql://"))
    else:
        parsed = urlparse(raw_url)

    if parsed.scheme not in {"postgresql", "postgres"}:
        raise ValueError(f"Unsupported PostgreSQL URL: {raw_url}")

    host = parsed.hostname or "localhost"
    port = f":{parsed.port}" if parsed.port else ""
    path = parsed.path or "/foodhelper"
    query = f"?{parsed.query}" if parsed.query else ""
    return f"postgresql://{quote(raw_username)}:{quote(raw_password)}@{host}{port}{path}{query}"


def apply_sql(sql_path: Path, database_url: str | None, username: str | None, password: str | None) -> None:
    connection_uri = postgres_connection_uri(database_url, username, password)
    subprocess.run(
        ["psql", "--set", "ON_ERROR_STOP=1", connection_uri, "--file", str(sql_path)],
        check=True,
    )


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Export legacy DynamoDB products and recipes with calories, fats, proteins and carbohydrates."
    )
    parser.add_argument("input", type=Path, help="Path to the DynamoDB JSON-lines export")
    parser.add_argument("--output-dir", type=Path, default=Path("exports/legacy-nutrition"))
    parser.add_argument("--format", choices=("csv", "json", "sql"), default="csv")
    parser.add_argument("--apply", action="store_true", help="Apply the generated import.sql to PostgreSQL using psql")
    parser.add_argument("--database-url", help="PostgreSQL URL or Spring JDBC URL. Defaults to SPRING_DATASOURCE_URL")
    parser.add_argument("--username", help="PostgreSQL username. Defaults to SPRING_DATASOURCE_USERNAME or foodhelper")
    parser.add_argument("--password", help="PostgreSQL password. Defaults to SPRING_DATASOURCE_PASSWORD or foodhelper")
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    products, recipes, recipe_ingredients, report = export_rows(read_legacy_items(args.input))

    if args.format == "csv":
        write_csv(args.output_dir / "products.csv", products, OUTPUT_COLUMNS)
        write_csv(args.output_dir / "recipes.csv", recipes, OUTPUT_COLUMNS)
        write_csv(
            args.output_dir / "recipe_ingredients.csv",
            recipe_ingredients,
            ["recipe_name", "product_name", "grams"],
        )
    elif args.format == "json":
        write_json(args.output_dir / "products.json", products)
        write_json(args.output_dir / "recipes.json", recipes)
        write_json(args.output_dir / "recipe_ingredients.json", recipe_ingredients)

    sql_path = args.output_dir / "import.sql"
    write_sql(sql_path, products, recipes, recipe_ingredients)
    if args.apply:
        apply_sql(sql_path, args.database_url, args.username, args.password)

    write_report(args.output_dir / "report.json", report)
    print(json.dumps(report.to_json_dict(), ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
