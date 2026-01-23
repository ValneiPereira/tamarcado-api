package com.tamarcado.shared.dto.response;

import java.util.List;

public record ReviewListResponse(
        Double averageRating,
        Long totalReviews,
        List<ReviewResponse> reviews,
        PaginationResponse pagination
) {}
