package com.eliascanalesnieto.foodhelper.domain;

import java.util.Collection;
import java.util.List;

public interface SupermarketRepository {
    Supermarket create(Supermarket supermarket);

    Supermarket update(Long id, Supermarket supermarket);

    List<Supermarket> findAll();

    Supermarket findById(Long id);

    List<Supermarket> findByIds(Collection<Long> ids);

    void delete(Long id);
}
