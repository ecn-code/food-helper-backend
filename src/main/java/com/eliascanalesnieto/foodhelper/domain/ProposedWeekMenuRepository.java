package com.eliascanalesnieto.foodhelper.domain;

public interface ProposedWeekMenuRepository {
    ProposedWeekMenu create(ProposedWeekMenu menu);

    ProposedWeekMenu findById(Long id);

    ProposedWeekMenu upsertDay(Long menuId, ProposedWeekMenuDay day);
}
