package com.campus360.shared.dto;

import java.util.List;

/**
 * Standard paginated response wrapper for all list endpoints.
 * Enterprise pattern: consistent pagination metadata across the API.
 *
 * @param <T> the type of data in the page
 */
public record PageResponse<T>(
        List<T> data,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last) {

    public static <T> PageResponse<T> of(org.springframework.data.domain.Page<T> springPage) {
        return new PageResponse<>(
                springPage.getContent(),
                springPage.getNumber(),
                springPage.getSize(),
                springPage.getTotalElements(),
                springPage.getTotalPages(),
                springPage.isFirst(),
                springPage.isLast());
    }

    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = size == 0 ? 1 : (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(content, page, size, totalElements, totalPages,
                page == 0, page >= totalPages - 1);
    }
}
