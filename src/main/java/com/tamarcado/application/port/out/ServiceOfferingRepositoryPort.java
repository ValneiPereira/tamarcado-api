package com.tamarcado.application.port.out;

import com.tamarcado.domain.model.service.Category;
import com.tamarcado.domain.model.service.ServiceOffering;
import com.tamarcado.domain.model.service.ServiceType;
import com.tamarcado.domain.model.user.Professional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceOfferingRepositoryPort {

    ServiceOffering save(ServiceOffering serviceOffering);

    Optional<ServiceOffering> findById(UUID id);

    List<ServiceOffering> findByProfessional(Professional professional);

    List<ServiceOffering> findByProfessionalId(UUID professionalId);

    List<ServiceOffering> findByProfessionalIdAndActiveTrue(UUID professionalId);

    boolean existsByIdAndProfessionalId(UUID serviceOfferingId, UUID professionalId);

    void delete(ServiceOffering serviceOffering);

    long countByProfessionalIdAndActiveTrue(UUID professionalId);

    /**
     * Busca serviços ativos agrupados por categoria e tipo
     */
    List<ServiceOffering> findActiveServicesByCategoryAndType(Category category, ServiceType serviceType);
}
