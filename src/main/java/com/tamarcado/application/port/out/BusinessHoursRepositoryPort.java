package com.tamarcado.application.port.out;

import com.tamarcado.domain.model.professional.BusinessHours;

import java.util.List;
import java.util.UUID;

public interface BusinessHoursRepositoryPort {

    List<BusinessHours> findByProfessionalId(UUID professionalId);

    BusinessHours save(BusinessHours businessHours);

    List<BusinessHours> saveAll(List<BusinessHours> businessHours);

    void deleteByProfessionalId(UUID professionalId);
}
