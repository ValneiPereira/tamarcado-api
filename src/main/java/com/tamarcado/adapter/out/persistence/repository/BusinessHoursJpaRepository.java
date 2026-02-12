package com.tamarcado.adapter.out.persistence.repository;

import com.tamarcado.domain.model.professional.BusinessHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BusinessHoursJpaRepository extends JpaRepository<BusinessHours, UUID> {

    List<BusinessHours> findByProfessionalIdOrderByDayOfWeek(UUID professionalId);

    void deleteByProfessionalId(UUID professionalId);
}
