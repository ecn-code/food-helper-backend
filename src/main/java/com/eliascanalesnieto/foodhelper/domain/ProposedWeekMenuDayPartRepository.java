package com.eliascanalesnieto.foodhelper.domain;

import java.util.List;

public interface ProposedWeekMenuDayPartRepository {
    ProposedWeekMenuDayPart create(ProposedWeekMenuDayPart dayPart);

    ProposedWeekMenuDayPart update(Long id, ProposedWeekMenuDayPart dayPart);

    ProposedWeekMenuDayPart findById(Long id);

    List<ProposedWeekMenuDayPart> findAll();
}
