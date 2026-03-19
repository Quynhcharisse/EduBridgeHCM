package com.sp26se041.edubridgehcm.utils;

import com.sp26se041.edubridgehcm.responses.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class PaginationUtil {

    // 👉 Build Pageable
    public static PageRequest buildPageRequest(int page, int size) {
        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("Invalid page or size");
        }
        return PageRequest.of(page, size);
    }

    // 👉 Có mapper
    public static <T, R> PageResponse<R> buildPageResponse(
            Page<T> pageData,
            Function<T, R> mapper
    ) {
        if (pageData == null) {
            return emptyPage();
        }

        if (mapper == null) {
            throw new IllegalArgumentException("Mapper cannot be null");
        }

        List<T> content = pageData.getContent();

        List<R> items = content.stream()
                .filter(Objects::nonNull)
                .map(mapper)
                .filter(Objects::nonNull)
                .toList();

        return buildMeta(pageData, items);
    }

    // 👉 Không cần mapper
    public static <T> PageResponse<T> buildPageResponse(Page<T> pageData) {

        if (pageData == null) {
            return emptyPage();
        }

        List<T> items = pageData.getContent();

        return buildMeta(pageData, items);
    }

    // 👉 Meta builder
    private static <R> PageResponse<R> buildMeta(Page<?> pageData, List<R> items) {
        return PageResponse.<R>builder()
                .items(items)
                .currentPage(pageData.getNumber())
                .pageSize(pageData.getSize())
                .totalItems(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .hasNext(pageData.hasNext())
                .hasPrevious(pageData.hasPrevious())
                .build();
    }

    // 👉 Empty fallback
    private static <T> PageResponse<T> emptyPage() {
        return PageResponse.<T>builder()
                .items(Collections.emptyList())
                .currentPage(0)
                .pageSize(0)
                .totalItems(0)
                .totalPages(0)
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }
}
