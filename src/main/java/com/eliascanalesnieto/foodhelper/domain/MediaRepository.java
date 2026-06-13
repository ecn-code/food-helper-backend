package com.eliascanalesnieto.foodhelper.domain;

public interface MediaRepository {
    Media create(Media media);

    Media findById(Long id);

    void delete(Long id);
}
