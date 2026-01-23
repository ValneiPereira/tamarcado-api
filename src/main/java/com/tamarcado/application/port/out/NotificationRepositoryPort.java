package com.tamarcado.application.port.out;

import com.tamarcado.domain.model.notification.Notification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepositoryPort {

    Notification save(Notification notification);

    Optional<Notification> findById(UUID id);

    List<Notification> findByUserId(UUID userId);

    List<Notification> findByUserIdAndIsRead(UUID userId, Boolean isRead);

    long countByUserIdAndIsReadFalse(UUID userId);
}
