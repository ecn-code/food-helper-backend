import unittest
from pathlib import Path
import sys

sys.path.insert(0, str(Path(__file__).resolve().parent))

from export_legacy_products_recipes_nutrition import (
    build_import_sql,
    ExportReport,
    ExportRow,
    export_rows,
    map_nutrition,
    RecipeIngredientRow,
    postgres_connection_uri,
    unwrap_dynamodb_item,
)


class ExportLegacyProductsRecipesNutritionTest(unittest.TestCase):
    def test_unwraps_dynamodb_item(self):
        item = unwrap_dynamodb_item(
            {
                "PK": {"S": "product"},
                "description": {"NULL": True},
                "nutritional_value": {
                    "L": [
                        {"L": [{"S": "Kilocalorías"}, {"S": "Kcal"}, {"S": "52"}]},
                    ]
                },
            }
        )

        self.assertEqual(item["PK"], "product")
        self.assertIsNone(item["description"])
        self.assertEqual(item["nutritional_value"], [["Kilocalorías", "Kcal", "52"]])

    def test_maps_list_nutrition_and_ignores_sugar(self):
        report = ExportReport()

        nutrition = map_nutrition(
            [
                ["Kilocalorías", "Kcal", "146.5"],
                ["protein", "g", "0.6"],
                ["Azucares", "g", "0.08"],
                ["Grasas", "g", "15.4"],
                ["Hidratos de Carbono", "g", "0.08"],
            ],
            report,
        )

        self.assertEqual(
            nutrition,
            {
                "calories": "146.50",
                "fats": "15.40",
                "proteins": "0.60",
                "carbohydrates": "0.08",
            },
        )
        self.assertEqual(report.ignored_nutrients["Azucares"], 1)

    def test_maps_dict_nutrition_and_defaults_missing_values(self):
        report = ExportReport()

        nutrition = map_nutrition(
            [
                {"name": "Kilocalorías", "value": "914.15", "unit": "Kcal"},
                {"name": "protein", "value": "54.68", "unit": "g"},
            ],
            report,
        )

        self.assertEqual(nutrition["calories"], "914.15")
        self.assertEqual(nutrition["proteins"], "54.68")
        self.assertEqual(nutrition["fats"], "0.00")
        self.assertEqual(nutrition["carbohydrates"], "0.00")
        self.assertEqual(report.defaulted_nutrients["fats"], 1)
        self.assertEqual(report.defaulted_nutrients["carbohydrates"], 1)

    def test_exports_only_products_and_recipes(self):
        products, recipes, recipe_ingredients, report = export_rows(
            [
                {
                    "PK": "product",
                    "SK": "Aceitunas negras",
                    "description": None,
                    "nutritional_value": [
                        ["Kilocalorías", "Kcal", "146.5"],
                        ["protein", "g", "0.6"],
                        ["Grasas", "g", "15.4"],
                        ["Hidratos de Carbono", "g", "0.08"],
                    ],
                },
                {
                    "PK": "recipe",
                    "SK": "Albondigas",
                    "description": "Receta",
                    "products": [
                        ["Aceitunas negras", "gr", "20"],
                        ["Tomate", "gr", "125.555"],
                    ],
                    "nutritional_value": [
                        {"name": "Kilocalorías", "value": "914.15", "unit": "Kcal"},
                        {"name": "protein", "value": "54.68", "unit": "g"},
                        {"name": "Grasas", "value": "61.64", "unit": "g"},
                        {"name": "Hidratos de Carbono", "value": "41.31", "unit": "g"},
                    ],
                },
                {"PK": "user", "SK": "elias"},
            ]
        )

        self.assertEqual(len(products), 1)
        self.assertEqual(products[0].name, "Aceitunas negras")
        self.assertEqual(products[0].description, "")
        self.assertEqual(len(recipes), 1)
        self.assertEqual(recipes[0].name, "Albondigas")
        self.assertEqual(len(recipe_ingredients), 2)
        self.assertEqual(recipe_ingredients[0].recipe_name, "Albondigas")
        self.assertEqual(recipe_ingredients[0].product_name, "Aceitunas negras")
        self.assertEqual(recipe_ingredients[0].grams, "20.00")
        self.assertEqual(recipe_ingredients[1].product_name, "Tomate")
        self.assertEqual(recipe_ingredients[1].grams, "125.56")
        self.assertEqual(report.products_exported, 1)
        self.assertEqual(report.recipes_exported, 1)
        self.assertEqual(report.recipe_ingredients_exported, 2)
        self.assertEqual(report.ignored_by_pk["user"], 1)

    def test_skips_malformed_recipe_ingredients(self):
        _, _, recipe_ingredients, report = export_rows(
            [
                {
                    "PK": "recipe",
                    "SK": "Albondigas",
                    "products": [
                        ["Aceitunas negras", "gr", "20"],
                        ["Missing grams", "gr", ""],
                        ["Malformed"],
                    ],
                    "nutritional_value": [],
                },
            ]
        )

        self.assertEqual(len(recipe_ingredients), 1)
        self.assertEqual(report.recipe_ingredients_exported, 1)
        self.assertEqual(report.skipped_recipe_ingredients, 2)
        self.assertEqual(report.missing_ingredient_products["Aceitunas negras"], 1)

    def test_normalizes_seitan_fraction_ingredients_to_recipe_name(self):
        products, recipes, recipe_ingredients, report = export_rows(
            [
                {
                    "PK": "recipe",
                    "SK": "Seitan",
                    "nutritional_value": [
                        ["Kilocalorías", "Kcal", "779.28"],
                        ["protein", "g", "94.16"],
                        ["Grasas", "g", "29.08"],
                        ["Hidratos de Carbono", "g", "21.12"],
                    ],
                    "products": [],
                },
                {
                    "PK": "recipe",
                    "SK": "Seitan con sarraceno y brocoli",
                    "nutritional_value": [],
                    "products": [["Seitan 1/4", "portions", "2"]],
                },
                {
                    "PK": "recipe",
                    "SK": "Taco de lechuga y seitan",
                    "nutritional_value": [],
                    "products": [["Seitan 1/8", "portions", "2"]],
                },
            ]
        )

        self.assertEqual(len(products), 1)
        self.assertEqual(products[0].name, "Seitan")
        self.assertEqual(products[0].calories, "779.28")
        self.assertEqual(products[0].proteins, "94.16")
        self.assertEqual(len(recipes), 3)
        self.assertEqual(recipe_ingredients[0].recipe_name, "Seitan con sarraceno y brocoli")
        self.assertEqual(recipe_ingredients[0].product_name, "Seitan")
        self.assertEqual(recipe_ingredients[0].grams, "500.00")
        self.assertEqual(recipe_ingredients[1].recipe_name, "Taco de lechuga y seitan")
        self.assertEqual(recipe_ingredients[1].product_name, "Seitan")
        self.assertEqual(recipe_ingredients[1].grams, "250.00")
        self.assertEqual(report.missing_ingredient_products["Seitan"], 0)
        self.assertEqual(report.normalized_ingredient_products["Seitan 1/4 -> Seitan"], 1)
        self.assertEqual(report.normalized_ingredient_products["Seitan 1/8 -> Seitan"], 1)
        self.assertEqual(report.synthetic_products_exported, 1)

    def test_normalizes_harina_de_maiz_capitalization(self):
        products, _, recipe_ingredients, report = export_rows(
            [
                {
                    "PK": "product",
                    "SK": "Harina de maiz",
                    "nutritional_value": [],
                },
                {
                    "PK": "recipe",
                    "SK": "Taco con tortita",
                    "nutritional_value": [],
                    "products": [["Harina de Maiz", "gr", "100"]],
                },
            ]
        )

        self.assertEqual(products[0].name, "Harina de maiz")
        self.assertEqual(recipe_ingredients[0].product_name, "Harina de maiz")
        self.assertEqual(recipe_ingredients[0].grams, "100.00")
        self.assertEqual(report.missing_ingredient_products["Harina de maiz"], 0)
        self.assertEqual(report.normalized_ingredient_products["Harina de Maiz -> Harina de maiz"], 1)

    def test_builds_postgres_import_sql(self):
        sql = build_import_sql(
            [
                ExportRow(
                    name="Harina de maiz",
                    description="Para arepa",
                    calories="332.00",
                    fats="1.20",
                    proteins="8.30",
                    carbohydrates="72.40",
                )
            ],
            [
                ExportRow(
                    name="Arepa",
                    description="Una arepa por persona",
                    calories="1392.70",
                    fats="70.50",
                    proteins="38.24",
                    carbohydrates="128.69",
                )
            ],
            [RecipeIngredientRow(recipe_name="Arepa", product_name="Harina de maiz", grams="130.00")],
        )

        self.assertIn("INSERT INTO products", sql)
        self.assertIn("INSERT INTO nutritional_values", sql)
        self.assertIn("INSERT INTO recipes", sql)
        self.assertIn("DELETE FROM recipe_products rp", sql)
        self.assertIn("INSERT INTO recipe_products", sql)
        self.assertIn("'Harina de maiz'", sql)
        self.assertIn("'Arepa'", sql)
        self.assertIn("130.00", sql)

    def test_builds_postgres_uri_from_spring_jdbc_url(self):
        uri = postgres_connection_uri(
            "jdbc:postgresql://localhost:5432/foodhelper",
            "foodhelper",
            "secret password",
        )

        self.assertEqual(uri, "postgresql://foodhelper:secret%20password@localhost:5432/foodhelper")


if __name__ == "__main__":
    unittest.main()
