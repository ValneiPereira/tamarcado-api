package com.tamarcado.adapter.out.persistence.impl;

import com.tamarcado.adapter.out.persistence.repository.ProfessionalJpaRepository;
import com.tamarcado.application.port.out.ProfessionalRepositoryPort;
import com.tamarcado.domain.model.service.Category;
import com.tamarcado.domain.model.service.ServiceType;
import com.tamarcado.domain.model.user.Professional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProfessionalRepositoryAdapter implements ProfessionalRepositoryPort {

    private final ProfessionalJpaRepository jpaRepository;

    @Override
    public Professional save(Professional professional) {
        return jpaRepository.save(professional);
    }

    @Override
    public Optional<Professional> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Professional> findByIdWithDetails(UUID id) {
        return jpaRepository.findByIdWithDetails(id);
    }

    @Override
    public List<Professional> findNearbyProfessionals(
            UUID serviceId,
            Double latitude,
            Double longitude,
            Double maxDistanceKm,
            Integer limit
    ) {
        return jpaRepository.findNearbyProfessionals(serviceId, latitude, longitude, maxDistanceKm, limit);
    }

    @Override
    public List<Professional> findByCategoryAndServiceType(Category category, ServiceType serviceType) {
        return jpaRepository.findByCategoryAndServiceTypeAndActiveTrue(category, serviceType);
    }

    @Override
    public List<Professional> findByActiveTrue() {
        return jpaRepository.findByActiveTrue();
    }
}
