package com.tamarcado.application.port.out;

import com.tamarcado.domain.model.service.Category;
import com.tamarcado.domain.model.service.ServiceType;
import com.tamarcado.domain.model.user.Professional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfessionalRepositoryPort {

    Professional save(Professional professional);

    Optional<Professional> findById(UUID id);

    Optional<Professional> findByIdWithDetails(UUID id);

    List<Professional> findNearbyProfessionals(
            UUID serviceId,
            Double latitude,
            Double longitude,
            Double maxDistanceKm,
            Integer limit
    );

    List<Professional> findByCategoryAndServiceType(Category category, ServiceType serviceType);

    List<Professional> findByActiveTrue();
}