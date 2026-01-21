package com.tamarcado.application.port.out;

import com.tamarcado.domain.model.service.Service;
import com.tamarcado.domain.model.user.Professional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceRepositoryPort {

    Service save(Service service);

    Optional<Service> findById(UUID id);

    List<Service> findByProfessional(Professional professional);

    List<Service> findByProfessionalId(UUID professionalId);

    List<Service> findByProfessionalIdAndActiveTrue(UUID professionalId);

    boolean existsByIdAndProfessionalId(UUID serviceId, UUID professionalId);

    void delete(Service service);

    long countByProfessionalIdAndActiveTrue(UUID professionalId);
}