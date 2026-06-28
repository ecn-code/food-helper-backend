package com.eliascanalesnieto.foodhelper.domain;

import java.time.LocalDate;

public record PlanningSummary(
        Long id,
        LocalDate startDate,
        LocalDate endDate,
        int plannedDays,
        PlanningState state,
        Long menuId
) {
}
