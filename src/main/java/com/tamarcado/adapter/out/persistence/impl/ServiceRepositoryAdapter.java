package com.tamarcado.adapter.out.persistence.impl;

import com.tamarcado.adapter.out.persistence.jpa.ServiceJpaRepository;
import com.tamarcado.application.port.out.ServiceRepositoryPort;
import com.tamarcado.domain.model.service.Service;
import com.tamarcado.domain.model.user.Professional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ServiceRepositoryAdapter implements ServiceRepositoryPort {

    private final ServiceJpaRepository jpaRepository;

    @Override
    public Service save(Service service) {
        return jpaRepository.save(service);
    }

    @Override
    public Optional<Service> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Service> findByProfessional(Professional professional) {
        return jpaRepository.findByProfessional(professional);
    }

    @Override
    public List<Service> findByProfessionalId(UUID professionalId) {
        return jpaRepository.findByProfessionalId(professionalId);
    }

    @Override
    public List<Service> findByProfessionalIdAndActiveTrue(UUID professionalId) {
        return jpaRepository.findByProfessionalIdAndActiveTrue(professionalId);
    }

    @Override
    public boolean existsByIdAndProfessionalId(UUID serviceId, UUID professionalId) {
        return jpaRepository.existsByIdAndProfessionalId(serviceId, professionalId);
    }

    @Override
    public void delete(Service service) {
        jpaRepository.delete(service);
    }

    @Override
    public long countByProfessionalIdAndActiveTrue(UUID professionalId) {
        return jpaRepository.countByProfessionalIdAndActiveTrue(professionalId);
    }
}