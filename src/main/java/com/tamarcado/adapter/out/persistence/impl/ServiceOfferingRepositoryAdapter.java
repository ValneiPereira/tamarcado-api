package com.tamarcado.adapter.out.persistence.impl;

import com.tamarcado.adapter.out.persistence.repository.ServiceOfferingJpaRepository;
import com.tamarcado.application.port.out.ServiceOfferingRepositoryPort;
import com.tamarcado.domain.model.service.Category;
import com.tamarcado.domain.model.service.ServiceOffering;
import com.tamarcado.domain.model.service.ServiceType;
import com.tamarcado.domain.model.user.Professional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ServiceOfferingRepositoryAdapter implements ServiceOfferingRepositoryPort {

    private final ServiceOfferingJpaRepository jpaRepository;

    @Override
    public ServiceOffering save(ServiceOffering serviceOffering) {
        return jpaRepository.save(serviceOffering);
    }

    @Override
    public Optional<ServiceOffering> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<ServiceOffering> findByProfessional(Professional professional) {
        return jpaRepository.findByProfessional(professional);
    }

    @Override
    public List<ServiceOffering> findByProfessionalId(UUID professionalId) {
        return jpaRepository.findByProfessionalId(professionalId);
    }

    @Override
    public List<ServiceOffering> findByProfessionalIdAndActiveTrue(UUID professionalId) {
        return jpaRepository.findByProfessionalIdAndActiveTrue(professionalId);
    }

    @Override
    public boolean existsByIdAndProfessionalId(UUID serviceOfferingId, UUID professionalId) {
        return jpaRepository.existsByIdAndProfessionalId(serviceOfferingId, professionalId);
    }

    @Override
    public void delete(ServiceOffering serviceOffering) {
        jpaRepository.delete(serviceOffering);
    }

    @Override
    public long countByProfessionalIdAndActiveTrue(UUID professionalId) {
        return jpaRepository.countByProfessionalIdAndActiveTrue(professionalId);
    }

    @Override
    public List<ServiceOffering> findActiveServicesByCategoryAndType(Category category, ServiceType serviceType) {
        return jpaRepository.findActiveServicesByCategoryAndType(category, serviceType);
    }
}
