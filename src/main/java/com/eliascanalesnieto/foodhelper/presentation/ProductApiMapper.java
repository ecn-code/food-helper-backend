package com.eliascanalesnieto.foodhelper.presentation;

import com.eliascanalesnieto.foodhelper.domain.Media;
import com.eliascanalesnieto.foodhelper.domain.NutritionalValues;
import com.eliascanalesnieto.foodhelper.domain.Product;
import com.eliascanalesnieto.foodhelper.domain.Recipe;
import com.eliascanalesnieto.foodhelper.domain.RecipeDerivedProduct;
import com.eliascanalesnieto.foodhelper.domain.RecipeIngredient;
import com.eliascanalesnieto.foodhelper.domain.StockEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductApiMapper {
    ProductResponse toResponse(Product product);

    MediaResponse toResponse(Media media);

    NutritionalValuesResponse toResponse(NutritionalValues nutritionalValues);

    @Mapping(target = "products", source = "ingredients")
    RecipeResponse toResponse(Recipe recipe);

    RecipeIngredientResponse toResponse(RecipeIngredient ingredient);

    RecipeDerivedProductResponse toResponse(RecipeDerivedProduct derivedProduct);

    StockEntryResponse toResponse(StockEntry stockEntry);
}
