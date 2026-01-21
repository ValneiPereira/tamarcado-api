package com.tamarcado.shared.dto.response;

import com.tamarcado.domain.model.service.Category;
import com.tamarcado.domain.model.service.ServiceType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProfessionalDetailResponse(
        UUID id,
        String name,
        String email,
        String phone,
        String photo,
        Category category,
        ServiceType serviceType,
        BigDecimal averageRating,
        Integer totalRatings,
        AddressResponse address,
        Double distanceKm,
        List<ServiceResponse> services,
        List<ReviewResponse> reviews
) {}
