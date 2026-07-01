package com.eliascanalesnieto.foodhelper.domain;

import java.util.List;

public interface ProposedWeekMenuRepository {
    ProposedWeekMenu create(ProposedWeekMenu menu);

    ProposedWeekMenu findById(Long id);

    List<PlanningSummary> findAllSummaries();

    void delete(Long id);

    ProposedWeekMenu upsertDay(Long menuId, ProposedWeekMenuDay day);
}
