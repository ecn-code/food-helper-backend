package com.eliascanalesnieto.foodhelper.infra;

import org.springframework.data.repository.CrudRepository;

public interface StockCrudRepository extends CrudRepository<StockEntryEntity, Long> {
}
