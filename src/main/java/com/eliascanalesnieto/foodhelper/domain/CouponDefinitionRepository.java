package com.eliascanalesnieto.foodhelper.domain;

import java.util.List;

public interface CouponDefinitionRepository {
    List<CouponDefinition> findAll();
    CouponDefinition findByCode(String code);
    CouponDefinition create(CouponDefinition coupon);
    CouponDefinition update(String code, CouponDefinition coupon);
    void delete(String code);
}
