package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuDayPart;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuDayPartRepository;
import java.util.List;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ProposedWeekMenuDayPartService {
    private final ProposedWeekMenuDayPartRepository repository;

    @Transactional
    public ProposedWeekMenuDayPart create(String name, String description, Integer sortOrder) {
        return repository.create(toDomain(name, description, sortOrder));
    }

    @Transactional
    public ProposedWeekMenuDayPart update(Long id, String name, String description, Integer sortOrder) {
        return repository.update(id, toDomain(name, description, sortOrder));
    }

    @Transactional(readOnly = true)
    public List<ProposedWeekMenuDayPart> findAll() {
        return repository.findAll();
    }

    @Transactional
    public List<ProposedWeekMenuDayPart> reorder(List<Long> dayPartIds) {
        if (dayPartIds == null || dayPartIds.isEmpty() || dayPartIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException("Day part identifiers must be positive and non-empty");
        }
        if (new HashSet<>(dayPartIds).size() != dayPartIds.size()) {
            throw new IllegalArgumentException("Day part identifiers must be unique");
        }
        List<ProposedWeekMenuDayPart> current = repository.findAll();
        if (current.size() != dayPartIds.size() || !new HashSet<>(dayPartIds).equals(
                current.stream().map(ProposedWeekMenuDayPart::getId).collect(java.util.stream.Collectors.toSet()))) {
            throw new IllegalArgumentException("Day part identifiers must exactly match the current catalog");
        }
        java.util.Map<Long, ProposedWeekMenuDayPart> byId = current.stream().collect(java.util.stream.Collectors.toMap(
                ProposedWeekMenuDayPart::getId, value -> value));
        for (int index = 0; index < dayPartIds.size(); index++) {
            Long id = dayPartIds.get(index);
            ProposedWeekMenuDayPart part = byId.get(id);
            repository.update(id, part.toBuilder().sortOrder((index + 1) * 10).build());
        }
        return repository.findAll();
    }

    private ProposedWeekMenuDayPart toDomain(String name, String description, Integer sortOrder) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Day part name is required");
        }
        if (!StringUtils.hasText(description)) {
            throw new IllegalArgumentException("Day part description is required");
        }
        if (sortOrder == null || sortOrder < 0) {
            throw new IllegalArgumentException("Day part sort order must be zero or greater");
        }
        return ProposedWeekMenuDayPart.builder()
                .name(name.trim())
                .description(description.trim())
                .sortOrder(sortOrder)
                .build();
    }
}
