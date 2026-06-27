package com.eliascanalesnieto.foodhelper.infra;

import com.eliascanalesnieto.foodhelper.domain.Supermarket;
import com.eliascanalesnieto.foodhelper.domain.SupermarketRepository;
import com.eliascanalesnieto.foodhelper.presentation.error.DuplicateResourceException;
import com.eliascanalesnieto.foodhelper.presentation.error.ResourceNotFoundException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class JdbcSupermarketRepository implements SupermarketRepository {
    private final SupermarketCrudRepository repository;
    private final JdbcClient jdbcClient;

    @Override
    @Transactional
    public Supermarket create(Supermarket supermarket) {
        try {
            return toDomain(repository.save(new SupermarketEntity(null, supermarket.getName())));
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("Supermarket name already exists");
        }
    }

    @Override
    @Transactional
    public Supermarket update(Long id, Supermarket supermarket) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Supermarket not found");
        }
        try {
            return toDomain(repository.save(new SupermarketEntity(id, supermarket.getName())));
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("Supermarket name already exists");
        }
    }

    @Override
    public List<Supermarket> findAll() {
        return jdbcClient.sql("SELECT id, name FROM supermarkets ORDER BY LOWER(name), id")
                .query((rs, rowNum) -> Supermarket.builder().id(rs.getLong("id")).name(rs.getString("name")).build())
                .list();
    }

    @Override
    public Supermarket findById(Long id) {
        return repository.findById(id)
                .map(this::toDomain)
                .orElseThrow(() -> new ResourceNotFoundException("Supermarket not found"));
    }

    @Override
    public List<Supermarket> findByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<Long> distinctIds = new LinkedHashSet<>(ids);
        List<Supermarket> supermarkets = distinctIds.stream().map(this::findById).toList();
        return supermarkets.stream()
                .sorted(java.util.Comparator.comparing(Supermarket::getName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Supermarket::getId))
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Supermarket not found");
        }
        Long assignments = jdbcClient.sql("SELECT COUNT(*) FROM product_supermarkets WHERE supermarket_id = :id")
                .param("id", id)
                .query(Long.class)
                .single();
        if (assignments > 0) {
            throw new DuplicateResourceException("Supermarket is assigned to one or more products");
        }
        try {
            repository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("Supermarket is assigned to one or more products");
        }
    }

    private Supermarket toDomain(SupermarketEntity entity) {
        return Supermarket.builder().id(entity.id()).name(entity.name()).build();
    }
}
