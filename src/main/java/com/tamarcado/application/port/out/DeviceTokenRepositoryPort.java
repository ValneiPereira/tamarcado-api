package com.tamarcado.application.port.out;

import com.tamarcado.domain.model.notification.DeviceToken;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceTokenRepositoryPort {

    DeviceToken save(DeviceToken deviceToken);

    Optional<DeviceToken> findByUserIdAndDeviceToken(UUID userId, String deviceToken);

    List<DeviceToken> findByUserId(UUID userId);

    void delete(DeviceToken deviceToken);

    void deleteByDeviceToken(String deviceToken);
}
