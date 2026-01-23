package com.tamarcado.adapter.out.persistence.repository;

import com.tamarcado.domain.model.notification.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceTokenJpaRepository extends JpaRepository<DeviceToken, UUID> {

    Optional<DeviceToken> findByUserIdAndDeviceToken(UUID userId, String deviceToken);

    List<DeviceToken> findByUserId(UUID userId);

    void deleteByDeviceToken(String deviceToken);
}
