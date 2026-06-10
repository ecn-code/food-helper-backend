package com.eliascanalesnieto.foodhelper.domain;

public interface ProductRepository {
    Product create(Product product);

    Product update(Long id, Product product);

    void delete(Long id);
}
