package com.eliascanalesnieto.foodhelper.presentation;

import com.eliascanalesnieto.foodhelper.application.MediaUrlService;
import com.eliascanalesnieto.foodhelper.domain.Media;
import com.eliascanalesnieto.foodhelper.domain.NutritionalValues;
import com.eliascanalesnieto.foodhelper.domain.Product;
import com.eliascanalesnieto.foodhelper.domain.Recipe;
import com.eliascanalesnieto.foodhelper.domain.RecipeDerivedProduct;
import com.eliascanalesnieto.foodhelper.domain.RecipeIngredient;
import com.eliascanalesnieto.foodhelper.domain.StockEntry;
import com.eliascanalesnieto.foodhelper.domain.StockMovement;
import com.eliascanalesnieto.foodhelper.domain.Supermarket;
import org.springframework.beans.factory.annotation.Autowired;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class ProductApiMapper {
    @Autowired
    protected MediaUrlService mediaUrlService;

    @Mapping(target = "photo", expression = "java(toSignedPhotoUrl(product.getPhoto()))")
    public abstract ProductResponse toResponse(Product product);

    public abstract SupermarketResponse toResponse(Supermarket supermarket);

    public abstract NutritionalValuesResponse toResponse(NutritionalValues nutritionalValues);

    @Mapping(target = "photo", expression = "java(toSignedPhotoUrl(recipe.getPhoto()))")
    @Mapping(target = "products", source = "ingredients")
    public abstract RecipeResponse toResponse(Recipe recipe);

    public abstract RecipeIngredientResponse toResponse(RecipeIngredient ingredient);

    public abstract RecipeDerivedProductResponse toResponse(RecipeDerivedProduct derivedProduct);

    public abstract StockEntryResponse toResponse(StockEntry stockEntry);

    public StockMovementResponse toResponse(StockMovement movement) {
        return new StockMovementResponse(
                movement.getId(),
                movement.getProductId(),
                movement.getProductName(),
                movement.getStockEntryId(),
                movement.getMovementType().name(),
                movement.getSignedQuantity(),
                movement.getEffectiveDate(),
                movement.getRecordedAt(),
                movement.getPrice(),
                movement.getExpirationDate(),
                movement.getEntryDate()
        );
    }

    protected String toSignedPhotoUrl(Media media) {
        if (media == null) {
            return null;
        }
        return mediaUrlService.signedUrl(media);
    }
}
