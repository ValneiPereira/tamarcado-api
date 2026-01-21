package com.tamarcado.shared.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID clientId,
        String clientName,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {}
