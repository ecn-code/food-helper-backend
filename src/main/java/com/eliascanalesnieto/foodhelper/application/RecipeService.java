package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.Media;
import com.eliascanalesnieto.foodhelper.domain.MediaUpload;
import com.eliascanalesnieto.foodhelper.domain.NutritionBasis;
import com.eliascanalesnieto.foodhelper.domain.NutritionalValues;
import com.eliascanalesnieto.foodhelper.domain.Product;
import com.eliascanalesnieto.foodhelper.domain.ProductRepository;
import com.eliascanalesnieto.foodhelper.domain.QuantityType;
import com.eliascanalesnieto.foodhelper.domain.Recipe;
import com.eliascanalesnieto.foodhelper.domain.RecipeDerivedProduct;
import com.eliascanalesnieto.foodhelper.domain.RecipeIngredient;
import com.eliascanalesnieto.foodhelper.domain.RecipeRepository;
import com.eliascanalesnieto.foodhelper.domain.RecipeSearchCriteria;
import com.eliascanalesnieto.foodhelper.presentation.error.DuplicateResourceException;
import com.eliascanalesnieto.foodhelper.presentation.error.ResourceNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecipeService {
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final int SCALE = 2;

    private final RecipeRepository recipeRepository;
    private final ProductRepository productRepository;
    private final MediaService mediaService;

    @Transactional
    public Recipe create(String name, String description, String instructions, BigDecimal defaultUnitsProduced, List<RecipeIngredient> ingredients, MediaUpload photoUpload) {
        Media photo = mediaService.createOptimized(photoUpload);
        Recipe recipe = enrichRecipe(Recipe.builder()
                .name(name)
                .description(description)
                .instructions(instructions)
                .defaultUnitsProduced(scaleOptional(defaultUnitsProduced))
                .ingredients(ingredients)
                .photo(photo)
                .build());
        return recipeRepository.create(recipe);
    }

    @Transactional
    public Recipe update(Long id, String name, String description, String instructions, BigDecimal defaultUnitsProduced, Boolean stockFromComposition, List<RecipeIngredient> ingredients, MediaUpload photoUpload) {
        Recipe existing = recipeRepository.findById(id);
        Media photo = photoUpload == null ? existing.getPhoto() : mediaService.createOptimized(photoUpload);
        Recipe recipe = enrichRecipe(Recipe.builder()
                .id(id)
                .name(name)
                .description(description)
                .instructions(instructions)
                .defaultUnitsProduced(scaleOptional(defaultUnitsProduced))
                .ingredients(ingredients)
                .photo(photo)
                .build());
        Recipe savedRecipe = recipeRepository.update(id, recipe);
        if (photoUpload != null && existing.getPhoto() != null) {
            mediaService.delete(existing.getPhoto().getId());
        }
        recipeRepository.findDerivedProductByRecipeId(id)
                .ifPresent(derivedProduct -> syncDerivedProduct(savedRecipe, derivedProduct, stockFromComposition));
        return attachDerivedProduct(savedRecipe);
    }

    @Transactional(readOnly = true)
    public List<Recipe> findAll() {
        return recipeRepository.findAll().stream()
                .map(this::enrichRecipe)
                .map(this::attachDerivedProduct)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResult<Recipe> findPage(PaginationRequest pagination) {
        return findPage(pagination, RecipeSearchCriteria.empty());
    }

    @Transactional(readOnly = true)
    public PageResult<Recipe> findPage(PaginationRequest pagination, RecipeSearchCriteria criteria) {
        List<Recipe> items = recipeRepository.findPage(pagination.offset(), pagination.size(), criteria).stream()
                .map(this::enrichRecipe)
                .map(this::attachDerivedProduct)
                .toList();
        return new PageResult<>(items, pagination.page(), pagination.size(), recipeRepository.count(criteria));
    }

    @Transactional(readOnly = true)
    public Recipe findById(Long id) {
        return loadRecipe(id);
    }

    @Transactional
    public void delete(Long id) {
        Recipe existing = recipeRepository.findById(id);
        recipeRepository.findDerivedProductByRecipeId(id)
                .ifPresent(derivedProduct -> productRepository.delete(derivedProduct.getProductId()));
        recipeRepository.delete(id);
        if (existing.getPhoto() != null) {
            mediaService.delete(existing.getPhoto().getId());
        }
    }

    @Transactional
    public RecipeDerivedProduct createDerivedProduct(Long recipeId, String name, BigDecimal units, Boolean stockFromComposition) {
        if (recipeRepository.findDerivedProductByRecipeId(recipeId).isPresent()) {
            throw new DuplicateResourceException("Recipe already has a derived product");
        }
        if (productRepository.findByName(name).isPresent()) {
            throw new DuplicateResourceException("Product name already exists");
        }
        Recipe recipe = loadRecipe(recipeId);
        Map<Long, Product> productsById = loadProducts(recipe.getIngredients());
        List<RecipeIngredient> composition = divideIngredients(recipe.getIngredients(), units);
        BigDecimal gramsPerUnit = calculateTotalGrams(recipe.getIngredients(), productsById).divide(units, SCALE, RoundingMode.HALF_UP);
        boolean resolvedStockFromComposition = resolveStockFromComposition(stockFromComposition, true);
        Product createdProduct = productRepository.create(Product.builder()
                .name(name)
                .description(recipe.getDescription())
                .gramsPerUnit(scale(gramsPerUnit))
                .nutritionBasis(NutritionBasis.PER_UNIT)
                .nutritionalValues(divideNutrients(recipe.getNutritionalValues(), units))
                .build());
        RecipeDerivedProduct linkedProduct = recipeRepository.saveDerivedProduct(
                recipeId,
                createdProduct.getId(),
                createdProduct.getName(),
                scale(units),
                resolvedStockFromComposition,
                composition
        );
        return completeDerivedProduct(linkedProduct);
    }

    private Recipe loadRecipe(Long id) {
        return attachDerivedProduct(enrichRecipe(recipeRepository.findById(id)));
    }

    private Recipe enrichRecipe(Recipe recipe) {
        Map<Long, Product> productsById = loadProducts(recipe.getIngredients());
        List<RecipeIngredient> enrichedIngredients = recipe.getIngredients().stream()
                .map(ingredient -> enrichIngredient(ingredient, productsById.get(ingredient.getProductId())))
                .toList();
        return recipe.toBuilder()
                .ingredients(enrichedIngredients)
                .nutritionalValues(sumNutritionalValues(enrichedIngredients))
                .build();
    }

    private Map<Long, Product> loadProducts(List<RecipeIngredient> ingredients) {
        List<Long> ids = ingredients.stream()
                .map(RecipeIngredient::getProductId)
                .distinct()
                .toList();
        List<Product> products = productRepository.findByIds(ids);
        if (products.size() != ids.size()) {
            throw new ResourceNotFoundException("One or more products were not found");
        }
        Map<Long, Product> productsById = new LinkedHashMap<>();
        products.forEach(product -> productsById.put(product.getId(), product));
        return productsById;
    }

    private RecipeIngredient enrichIngredient(RecipeIngredient ingredient, Product product) {
        validateIngredientCompatibility(ingredient, product);
        BigDecimal quantity = scale(ingredient.getQuantity());
        return RecipeIngredient.builder()
                .productId(product.getId())
                .productName(product.getName())
                .quantity(quantity)
                .quantityType(ingredient.getQuantityType())
                .nutritionalValues(calculateContribution(product.getNutritionalValues(), quantity, ingredient.getQuantityType()))
                .build();
    }

    private void validateIngredientCompatibility(RecipeIngredient ingredient, Product product) {
        if (ingredient.getQuantityType() == QuantityType.GRAMS && product.getNutritionBasis() != NutritionBasis.PER_100_GRAMS) {
            throw new IllegalArgumentException("Products measured in grams must use per-100-grams nutrition");
        }
        if (ingredient.getQuantityType() == QuantityType.UNITS && product.getNutritionBasis() != NutritionBasis.PER_UNIT) {
            throw new IllegalArgumentException("Products measured in units must use per-unit nutrition");
        }
    }

    private NutritionalValues calculateContribution(NutritionalValues nutritionalValues, BigDecimal quantity, QuantityType quantityType) {
        return switch (quantityType) {
            case GRAMS -> NutritionalValues.builder()
                    .calories(scale(calculatePerHundredValue(nutritionalValues.getCalories(), quantity)))
                    .carbohydrates(scale(calculatePerHundredValue(nutritionalValues.getCarbohydrates(), quantity)))
                    .proteins(scale(calculatePerHundredValue(nutritionalValues.getProteins(), quantity)))
                    .fats(scale(calculatePerHundredValue(nutritionalValues.getFats(), quantity)))
                    .build();
            case UNITS -> NutritionalValues.builder()
                    .calories(scale(nutritionalValues.getCalories().multiply(quantity)))
                    .carbohydrates(scale(nutritionalValues.getCarbohydrates().multiply(quantity)))
                    .proteins(scale(nutritionalValues.getProteins().multiply(quantity)))
                    .fats(scale(nutritionalValues.getFats().multiply(quantity)))
                    .build();
        };
    }

    private NutritionalValues divideNutrients(NutritionalValues nutritionalValues, BigDecimal units) {
        return NutritionalValues.builder()
                .calories(scale(nutritionalValues.getCalories().divide(units, SCALE, RoundingMode.HALF_UP)))
                .carbohydrates(scale(nutritionalValues.getCarbohydrates().divide(units, SCALE, RoundingMode.HALF_UP)))
                .proteins(scale(nutritionalValues.getProteins().divide(units, SCALE, RoundingMode.HALF_UP)))
                .fats(scale(nutritionalValues.getFats().divide(units, SCALE, RoundingMode.HALF_UP)))
                .build();
    }

    private List<RecipeIngredient> divideIngredients(List<RecipeIngredient> ingredients, BigDecimal units) {
        return ingredients.stream()
                .map(ingredient -> RecipeIngredient.builder()
                        .productId(ingredient.getProductId())
                        .productName(ingredient.getProductName())
                        .quantity(scale(ingredient.getQuantity().divide(units, SCALE, RoundingMode.HALF_UP)))
                        .quantityType(ingredient.getQuantityType())
                        .nutritionalValues(divideNutritionalContribution(ingredient.getNutritionalValues(), units))
                        .build())
                .toList();
    }

    private NutritionalValues divideNutritionalContribution(NutritionalValues nutritionalValues, BigDecimal units) {
        return NutritionalValues.builder()
                .calories(scale(nutritionalValues.getCalories().divide(units, SCALE, RoundingMode.HALF_UP)))
                .carbohydrates(scale(nutritionalValues.getCarbohydrates().divide(units, SCALE, RoundingMode.HALF_UP)))
                .proteins(scale(nutritionalValues.getProteins().divide(units, SCALE, RoundingMode.HALF_UP)))
                .fats(scale(nutritionalValues.getFats().divide(units, SCALE, RoundingMode.HALF_UP)))
                .build();
    }

    private NutritionalValues sumNutritionalValues(List<RecipeIngredient> ingredients) {
        BigDecimal calories = BigDecimal.ZERO;
        BigDecimal carbohydrates = BigDecimal.ZERO;
        BigDecimal proteins = BigDecimal.ZERO;
        BigDecimal fats = BigDecimal.ZERO;

        for (RecipeIngredient ingredient : ingredients) {
            calories = calories.add(ingredient.getNutritionalValues().getCalories());
            carbohydrates = carbohydrates.add(ingredient.getNutritionalValues().getCarbohydrates());
            proteins = proteins.add(ingredient.getNutritionalValues().getProteins());
            fats = fats.add(ingredient.getNutritionalValues().getFats());
        }

        return NutritionalValues.builder()
                .calories(scale(calories))
                .carbohydrates(scale(carbohydrates))
                .proteins(scale(proteins))
                .fats(scale(fats))
                .build();
    }

    private BigDecimal calculatePerHundredValue(BigDecimal perHundredGramsValue, BigDecimal grams) {
        return perHundredGramsValue.multiply(grams).divide(ONE_HUNDRED, SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTotalGrams(List<RecipeIngredient> ingredients, Map<Long, Product> productsById) {
        BigDecimal total = BigDecimal.ZERO;
        for (RecipeIngredient ingredient : ingredients) {
            Product product = productsById.get(ingredient.getProductId());
            total = total.add(switch (ingredient.getQuantityType()) {
                case GRAMS -> ingredient.getQuantity();
                case UNITS -> ingredient.getQuantity().multiply(product.getGramsPerUnit());
            });
        }
        return scale(total);
    }

    private Recipe attachDerivedProduct(Recipe recipe) {
        RecipeDerivedProduct derivedProduct = recipeRepository.findDerivedProductByRecipeId(recipe.getId())
                .map(this::completeDerivedProduct)
                .orElse(null);
        return recipe.toBuilder()
                .derivedProduct(derivedProduct)
                .build();
    }

    private RecipeDerivedProduct completeDerivedProduct(RecipeDerivedProduct derivedProduct) {
        Map<Long, Product> productsById = loadProducts(derivedProduct.getIngredients());
        return RecipeDerivedProduct.builder()
                .recipeId(derivedProduct.getRecipeId())
                .productId(derivedProduct.getProductId())
                .name(derivedProduct.getName())
                .unitsProduced(scale(derivedProduct.getUnitsProduced()))
                .stockFromComposition(derivedProduct.isStockFromComposition())
                .ingredients(derivedProduct.getIngredients().stream()
                        .map(ingredient -> enrichIngredient(ingredient, productsById.get(ingredient.getProductId())))
                        .toList())
                .build();
    }

    private void syncDerivedProduct(Recipe recipe, RecipeDerivedProduct derivedProduct, Boolean stockFromComposition) {
        Product existingProduct = productRepository.findById(derivedProduct.getProductId());
        BigDecimal units = derivedProduct.getUnitsProduced();
        Map<Long, Product> productsById = loadProducts(recipe.getIngredients());
        List<RecipeIngredient> composition = divideIngredients(recipe.getIngredients(), units);
        boolean resolvedStockFromComposition = resolveStockFromComposition(stockFromComposition, derivedProduct.isStockFromComposition());
        productRepository.update(derivedProduct.getProductId(), existingProduct.toBuilder()
                .name(derivedProduct.getName())
                .description(recipe.getDescription())
                .gramsPerUnit(scale(calculateTotalGrams(recipe.getIngredients(), productsById).divide(units, SCALE, RoundingMode.HALF_UP)))
                .nutritionBasis(NutritionBasis.PER_UNIT)
                .nutritionalValues(divideNutrients(recipe.getNutritionalValues(), units))
                .derivedProduct(RecipeDerivedProduct.builder()
                        .recipeId(recipe.getId())
                        .productId(derivedProduct.getProductId())
                        .name(derivedProduct.getName())
                        .unitsProduced(units)
                        .stockFromComposition(resolvedStockFromComposition)
                        .ingredients(composition)
                        .build())
                .build());
        recipeRepository.saveDerivedProduct(recipe.getId(), derivedProduct.getProductId(), derivedProduct.getName(), units, resolvedStockFromComposition, composition);
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal scaleOptional(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return scale(value);
    }

    private boolean resolveStockFromComposition(Boolean requestedValue, boolean defaultValue) {
        return requestedValue == null ? defaultValue : requestedValue;
    }
}
