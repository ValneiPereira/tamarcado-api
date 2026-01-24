package com.tamarcado.shared.dto.response;

import com.tamarcado.domain.model.service.Category;
import com.tamarcado.domain.model.service.ServiceType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ServiceSearchResponse(
        String serviceName,
        Category category,
        ServiceType serviceType,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Long professionalCount,
        List<UUID> serviceIds
) {}
