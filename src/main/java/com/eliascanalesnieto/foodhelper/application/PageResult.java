package com.eliascanalesnieto.foodhelper.application;

import java.util.List;

public record PageResult<T>(List<T> items, int page, int size, long totalElements) {
    public PageResult {
        items = items == null ? List.of() : List.copyOf(items);
    }

    public int totalPages() {
        return size == 0 ? 0 : (int) ((totalElements + size - 1L) / size);
    }
}
