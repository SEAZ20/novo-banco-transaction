package com.novobanco.transaction.domain.model;


public record PageRequest(int page, int size) {

    public PageRequest {
        if (page < 0) throw new IllegalArgumentException("La página no puede ser negativa");
        if (size < 1 || size > 100) throw new IllegalArgumentException("El tamaño debe estar entre 1 y 100");
    }

    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size);
    }
}
