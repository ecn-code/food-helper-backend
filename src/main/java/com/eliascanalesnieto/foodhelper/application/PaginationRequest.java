package com.eliascanalesnieto.foodhelper.application;

public record PaginationRequest(int page, int size) {
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public PaginationRequest {
        if (page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }
    }

    public static PaginationRequest of(Integer page, Integer size) {
        return new PaginationRequest(
                page == null ? DEFAULT_PAGE : page,
                size == null ? DEFAULT_SIZE : size
        );
    }

    public int offset() {
        return page * size;
    }
}
