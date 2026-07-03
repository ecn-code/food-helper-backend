package com.eliascanalesnieto.foodhelper.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Product create(Product product);

    Product update(Long id, Product product);

    List<Product> findAll();

    default List<Product> findPage(int offset, int limit) {
        return findPage(offset, limit, ProductSearchCriteria.empty());
    }

    List<Product> findPage(int offset, int limit, ProductSearchCriteria searchCriteria);

    default long count() {
        return count(ProductSearchCriteria.empty());
    }

    long count(ProductSearchCriteria searchCriteria);

    Product findById(Long id);

    Optional<Product> findByName(String name);

    List<Product> findByIds(Collection<Long> ids);

    Collection<Long> findProductIdsBySupermarket(Long supermarketId, Collection<Long> productIds);

    void delete(Long id);
}
