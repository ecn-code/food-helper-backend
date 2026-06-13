package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.Media;
import com.eliascanalesnieto.foodhelper.domain.MediaUpload;
import com.eliascanalesnieto.foodhelper.domain.NutritionalValues;
import com.eliascanalesnieto.foodhelper.domain.Product;
import com.eliascanalesnieto.foodhelper.domain.ProductRepository;
import com.eliascanalesnieto.foodhelper.domain.Recipe;
import com.eliascanalesnieto.foodhelper.domain.RecipeDerivedProduct;
import com.eliascanalesnieto.foodhelper.domain.RecipeIngredient;
import com.eliascanalesnieto.foodhelper.domain.RecipeRepository;
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
    public Recipe create(String name, String description, String instructions, List<RecipeIngredient> ingredients, MediaUpload photoUpload) {
        Media photo = mediaService.createOptimized(photoUpload);
        Recipe recipe = enrichRecipe(Recipe.builder()
                .name(name)
                .description(description)
                .instructions(instructions)
                .ingredients(ingredients)
                .photo(photo)
                .build());
        return recipeRepository.create(recipe);
    }

    @Transactional
    public Recipe update(Long id, String name, String description, String instructions, List<RecipeIngredient> ingredients, MediaUpload photoUpload) {
        Recipe existing = recipeRepository.findById(id);
        Media photo = photoUpload == null ? existing.getPhoto() : mediaService.createOptimized(photoUpload);
        Recipe recipe = enrichRecipe(Recipe.builder()
                .id(id)
                .name(name)
                .description(description)
                .instructions(instructions)
                .ingredients(ingredients)
                .photo(photo)
                .build());
        Recipe savedRecipe = recipeRepository.update(id, recipe);
        if (photoUpload != null && existing.getPhoto() != null) {
            mediaService.delete(existing.getPhoto().getId());
        }
        recipeRepository.findDerivedProductByRecipeId(id)
                .ifPresent(derivedProduct -> syncDerivedProduct(savedRecipe, derivedProduct));
        return attachDerivedProduct(savedRecipe);
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
    public RecipeDerivedProduct createDerivedProduct(Long recipeId, BigDecimal producedGrams, BigDecimal gramsPerUnit) {
        if (recipeRepository.findDerivedProductByRecipeId(recipeId).isPresent()) {
            throw new DuplicateResourceException("Recipe already has a derived product");
        }
        Recipe recipe = loadRecipe(recipeId);
        Product createdProduct = productRepository.create(Product.builder()
                .name(recipe.getName())
                .description(recipe.getDescription())
                .gramsPerUnit(scale(gramsPerUnit))
                .nutritionalValues(recipe.getNutritionalValues())
                .build());
        RecipeDerivedProduct linkedProduct = recipeRepository.linkDerivedProduct(
                recipeId,
                createdProduct.getId(),
                scale(producedGrams),
                scale(gramsPerUnit)
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
        return RecipeIngredient.builder()
                .productId(product.getId())
                .productName(product.getName())
                .grams(scale(ingredient.getGrams()))
                .nutritionalValues(calculateContribution(product.getNutritionalValues(), ingredient.getGrams()))
                .build();
    }

    private NutritionalValues calculateContribution(NutritionalValues nutritionalValues, BigDecimal grams) {
        return NutritionalValues.builder()
                .calories(scale(calculateValue(nutritionalValues.getCalories(), grams)))
                .carbohydrates(scale(calculateValue(nutritionalValues.getCarbohydrates(), grams)))
                .proteins(scale(calculateValue(nutritionalValues.getProteins(), grams)))
                .fats(scale(calculateValue(nutritionalValues.getFats(), grams)))
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

    private BigDecimal calculateValue(BigDecimal perHundredGramsValue, BigDecimal grams) {
        return perHundredGramsValue.multiply(grams).divide(ONE_HUNDRED, SCALE, RoundingMode.HALF_UP);
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
        return RecipeDerivedProduct.builder()
                .productId(derivedProduct.getProductId())
                .producedGrams(scale(derivedProduct.getProducedGrams()))
                .gramsPerUnit(scale(derivedProduct.getGramsPerUnit()))
                .unitsProduced(scale(derivedProduct.getProducedGrams().divide(derivedProduct.getGramsPerUnit(), SCALE, RoundingMode.HALF_UP)))
                .build();
    }

    private void syncDerivedProduct(Recipe recipe, RecipeDerivedProduct derivedProduct) {
        productRepository.update(derivedProduct.getProductId(), Product.builder()
                .name(recipe.getName())
                .description(recipe.getDescription())
                .gramsPerUnit(scale(derivedProduct.getGramsPerUnit()))
                .nutritionalValues(recipe.getNutritionalValues())
                .build());
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }
}
