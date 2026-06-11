package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.NutritionalValues;
import com.eliascanalesnieto.foodhelper.domain.Product;
import com.eliascanalesnieto.foodhelper.domain.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository repository;

    public Product create(String name, String description, BigDecimal calories, BigDecimal carbohydrates, BigDecimal proteins, BigDecimal fats) {
        return repository.create(Product.builder()
                .name(name)
                .description(description)
                .nutritionalValues(buildNutritionalValues(calories, carbohydrates, proteins, fats))
                .build());
    }

    public Product update(Long id, String name, String description, BigDecimal calories, BigDecimal carbohydrates, BigDecimal proteins, BigDecimal fats) {
        return repository.update(id, Product.builder()
                .name(name)
                .description(description)
                .nutritionalValues(buildNutritionalValues(calories, carbohydrates, proteins, fats))
                .build());
    }

    public List<Product> findAll() {
        return repository.findAll();
    }

    public void delete(Long id) {
        repository.delete(id);
    }

    private NutritionalValues buildNutritionalValues(BigDecimal calories, BigDecimal carbohydrates, BigDecimal proteins, BigDecimal fats) {
        return NutritionalValues.builder()
                .calories(calories)
                .carbohydrates(carbohydrates)
                .proteins(proteins)
                .fats(fats)
                .build();
    }
}
