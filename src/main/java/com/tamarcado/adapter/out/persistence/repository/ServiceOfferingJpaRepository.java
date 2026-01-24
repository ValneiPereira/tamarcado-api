package com.tamarcado.adapter.out.persistence.repository;

import com.tamarcado.domain.model.service.Category;
import com.tamarcado.domain.model.service.ServiceOffering;
import com.tamarcado.domain.model.service.ServiceType;
import com.tamarcado.domain.model.user.Professional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    /**
     * Busca serviços ativos agrupados por nome, categoria e tipo
     * Retorna apenas serviços de profissionais ativos
     */
    @Query("SELECT s FROM ServiceOffering s " +
           "JOIN s.professional p " +
           "JOIN p.user u " +
           "WHERE s.active = true " +
           "AND p.active = true " +
           "AND u.active = true " +
           "AND (:category IS NULL OR p.category = :category) " +
           "AND (:serviceType IS NULL OR p.serviceType = :serviceType)")
    List<ServiceOffering> findActiveServicesByCategoryAndType(
            @Param("category") Category category,
            @Param("serviceType") ServiceType serviceType
    );
}
