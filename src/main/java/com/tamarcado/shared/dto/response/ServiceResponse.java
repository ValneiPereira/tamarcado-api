package com.tamarcado.shared.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ServiceResponse(
        UUID id,
        String name,
        BigDecimal price,
        Boolean active,
        LocalDateTime createdAt
) {}
