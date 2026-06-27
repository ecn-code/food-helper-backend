package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.Supermarket;
import com.eliascanalesnieto.foodhelper.domain.SupermarketRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SupermarketService {
    private final SupermarketRepository repository;

    @Transactional
    public Supermarket create(String name) {
        return repository.create(Supermarket.builder().name(normalizeName(name)).build());
    }

    @Transactional
    public Supermarket update(Long id, String name) {
        return repository.update(id, Supermarket.builder().name(normalizeName(name)).build());
    }

    @Transactional(readOnly = true)
    public List<Supermarket> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Supermarket findById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(id);
    }

    private String normalizeName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Supermarket name is required");
        }
        return name.trim();
    }
}
