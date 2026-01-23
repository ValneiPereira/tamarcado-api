package com.tamarcado.shared.dto.response;

import com.tamarcado.domain.model.notification.NotificationType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationType type,
        String title,
        String message,
        Map<String, Object> data,
        Boolean isRead,
        LocalDateTime createdAt
) {}
