package com.tamarcado.adapter.out.persistence.repository;

import com.tamarcado.domain.model.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationJpaRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(UUID userId, Boolean isRead);

    long countByUserIdAndIsReadFalse(UUID userId);

    @Query("SELECT n FROM Notification n LEFT JOIN FETCH n.user WHERE n.id = :id")
    Optional<Notification> findByIdWithUser(@Param("id") UUID id);

    @Query("SELECT n.user.id FROM Notification n WHERE n.id = :id")
    Optional<UUID> findUserIdByNotificationId(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id")
    void markAsReadById(@Param("id") UUID id);

    @Query("SELECT n.isRead FROM Notification n WHERE n.id = :id")
    Optional<Boolean> findIsReadById(@Param("id") UUID id);
}
