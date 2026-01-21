package com.tamarcado.adapter.out.persistence.jpa;

import com.tamarcado.domain.model.service.Service;
import com.tamarcado.domain.model.user.Professional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceJpaRepository extends JpaRepository<Service, UUID> {

    List<Service> findByProfessional(Professional professional);

    List<Service> findByProfessionalId(UUID professionalId);

    List<Service> findByProfessionalIdAndActiveTrue(UUID professionalId);

    @Query("SELECT COUNT(s) > 0 FROM Service s WHERE s.id = :serviceId AND s.professional.id = :professionalId")
    boolean existsByIdAndProfessionalId(@Param("serviceId") UUID serviceId, @Param("professionalId") UUID professionalId);

    @Query("SELECT COUNT(s) FROM Service s WHERE s.professional.id = :professionalId AND s.active = true")
    long countByProfessionalIdAndActiveTrue(@Param("professionalId") UUID professionalId);
}