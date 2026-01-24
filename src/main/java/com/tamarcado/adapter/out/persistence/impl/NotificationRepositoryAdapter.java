package com.tamarcado.adapter.out.persistence.impl;

import com.tamarcado.adapter.out.persistence.repository.NotificationJpaRepository;
import com.tamarcado.application.port.out.NotificationRepositoryPort;
import com.tamarcado.domain.model.notification.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationRepositoryPort {

    private final NotificationJpaRepository jpaRepository;

    @Override
    public Notification save(Notification notification) {
        return jpaRepository.save(notification);
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Notification> findByIdWithUser(UUID id) {
        return jpaRepository.findByIdWithUser(id);
    }

    @Override
    public Optional<UUID> findUserIdByNotificationId(UUID id) {
        return jpaRepository.findUserIdByNotificationId(id);
    }

    @Override
    public void markAsReadById(UUID id) {
        jpaRepository.markAsReadById(id);
    }

    @Override
    public Optional<Boolean> findIsReadById(UUID id) {
        return jpaRepository.findIsReadById(id);
    }

    @Override
    public List<Notification> findByUserId(UUID userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<Notification> findByUserIdAndIsRead(UUID userId, Boolean isRead) {
        return jpaRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, isRead);
    }

    @Override
    public long countByUserIdAndIsReadFalse(UUID userId) {
        return jpaRepository.countByUserIdAndIsReadFalse(userId);
    }
}
