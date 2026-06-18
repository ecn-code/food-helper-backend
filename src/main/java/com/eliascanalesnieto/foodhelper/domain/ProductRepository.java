package com.eliascanalesnieto.foodhelper.domain;

import java.util.Collection;
import java.util.List;

public interface ProductRepository {
    Product create(Product product);

    Product update(Long id, Product product);

    List<Product> findAll();

    List<Product> findPage(int offset, int limit);

    long count();

    Product findById(Long id);

    List<Product> findByIds(Collection<Long> ids);

    void delete(Long id);
}
