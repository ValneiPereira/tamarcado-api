package com.tamarcado.adapter.out.persistence.impl;

import com.tamarcado.adapter.out.persistence.repository.BusinessHoursJpaRepository;
import com.tamarcado.application.port.out.BusinessHoursRepositoryPort;
import com.tamarcado.domain.model.professional.BusinessHours;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BusinessHoursRepositoryAdapter implements BusinessHoursRepositoryPort {

    private final BusinessHoursJpaRepository jpaRepository;

    @Override
    public List<BusinessHours> findByProfessionalId(UUID professionalId) {
        return jpaRepository.findByProfessionalIdOrderByDayOfWeek(professionalId);
    }

    @Override
    public BusinessHours save(BusinessHours businessHours) {
        return jpaRepository.save(businessHours);
    }

    @Override
    public List<BusinessHours> saveAll(List<BusinessHours> businessHours) {
        return jpaRepository.saveAll(businessHours);
    }

    @Override
    public void deleteByProfessionalId(UUID professionalId) {
        jpaRepository.deleteByProfessionalId(professionalId);
    }
}
