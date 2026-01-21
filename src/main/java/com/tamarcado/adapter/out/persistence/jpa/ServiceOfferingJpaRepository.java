package com.tamarcado.adapter.out.persistence.jpa;

import com.tamarcado.domain.model.service.ServiceOffering;
import com.tamarcado.domain.model.user.Professional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceOfferingJpaRepository extends JpaRepository<ServiceOffering, UUID> {

    List<ServiceOffering> findByProfessional(Professional professional);

    List<ServiceOffering> findByProfessionalId(UUID professionalId);

    List<ServiceOffering> findByProfessionalIdAndActiveTrue(UUID professionalId);

    @Query("SELECT COUNT(s) > 0 FROM ServiceOffering s WHERE s.id = :serviceOfferingId AND s.professional.id = :professionalId")
    boolean existsByIdAndProfessionalId(@Param("serviceOfferingId") UUID serviceOfferingId, @Param("professionalId") UUID professionalId);

    @Query("SELECT COUNT(s) FROM ServiceOffering s WHERE s.professional.id = :professionalId AND s.active = true")
    long countByProfessionalIdAndActiveTrue(@Param("professionalId") UUID professionalId);
}