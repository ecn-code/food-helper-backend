package com.eliascanalesnieto.foodhelper.domain;

import java.util.List;

public interface ProposedWeekMenuRepository {
    ProposedWeekMenu create(ProposedWeekMenu menu);

    ProposedWeekMenu findById(Long id);

    List<PlanningSummary> findAllSummaries();

    ProposedWeekMenu upsertDay(Long menuId, ProposedWeekMenuDay day);
}
