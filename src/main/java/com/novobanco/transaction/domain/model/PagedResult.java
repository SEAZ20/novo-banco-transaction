package com.novobanco.transaction.domain.model;

import java.util.List;

public record PagedResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T> PagedResult<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new PagedResult<>(content, page, size, totalElements, totalPages);
    }

    public boolean hasNext() {
        return page < totalPages - 1;
    }
}
