package com.tamarcado.adapter.out.persistence.repository;

import com.tamarcado.domain.model.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationJpaRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(UUID userId, Boolean isRead);

    long countByUserIdAndIsReadFalse(UUID userId);
}
