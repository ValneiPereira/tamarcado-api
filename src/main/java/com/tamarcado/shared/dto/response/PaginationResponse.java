package com.tamarcado.shared.dto.response;

public record PaginationResponse(
        Integer page,
        Integer pageSize,
        Integer totalPages,
        Long totalItems
) {}
