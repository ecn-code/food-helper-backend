package com.eliascanalesnieto.foodhelper.presentation.error;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
