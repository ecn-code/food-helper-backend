package com.eliascanalesnieto.foodhelper.presentation;

import com.eliascanalesnieto.foodhelper.domain.NutritionalValues;
import com.eliascanalesnieto.foodhelper.domain.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductApiMapper {
    ProductResponse toResponse(Product product);

    NutritionalValuesResponse toResponse(NutritionalValues nutritionalValues);
}
