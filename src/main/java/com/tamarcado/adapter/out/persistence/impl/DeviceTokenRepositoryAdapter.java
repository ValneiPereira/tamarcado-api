package com.tamarcado.adapter.out.persistence.impl;

import com.tamarcado.adapter.out.persistence.repository.DeviceTokenJpaRepository;
import com.tamarcado.application.port.out.DeviceTokenRepositoryPort;
import com.tamarcado.domain.model.notification.DeviceToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeviceTokenRepositoryAdapter implements DeviceTokenRepositoryPort {

    private final DeviceTokenJpaRepository jpaRepository;

    @Override
    public DeviceToken save(DeviceToken deviceToken) {
        return jpaRepository.save(deviceToken);
    }

    @Override
    public Optional<DeviceToken> findByUserIdAndDeviceToken(UUID userId, String deviceToken) {
        return jpaRepository.findByUserIdAndDeviceToken(userId, deviceToken);
    }

    @Override
    public List<DeviceToken> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public void delete(DeviceToken deviceToken) {
        jpaRepository.delete(deviceToken);
    }

    @Override
    public void deleteByDeviceToken(String deviceToken) {
        jpaRepository.deleteByDeviceToken(deviceToken);
    }
}
