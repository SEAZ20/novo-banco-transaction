package com.novobanco.transaction.infrastructure.adapter.input.rest.dto.response;

import com.novobanco.transaction.domain.model.PagedResult;

import java.util.List;
import java.util.function.Function;

public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static <D, R> PagedResponse<R> from(PagedResult<D> result, Function<D, R> mapper) {
        return new PagedResponse<>(
                result.content().stream().map(mapper).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext()
        );
    }
}
