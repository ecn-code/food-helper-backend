package com.eliascanalesnieto.foodhelper.domain;

public interface NutritionalRulesRepository {
    NutritionalRules find();

    NutritionalRules save(NutritionalRules rules);
}
