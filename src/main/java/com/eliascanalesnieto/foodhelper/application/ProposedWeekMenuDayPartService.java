package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuDayPart;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuDayPartRepository;
import java.util.List;
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
