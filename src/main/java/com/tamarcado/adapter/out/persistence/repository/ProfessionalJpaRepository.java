package com.tamarcado.adapter.out.persistence.repository;

import com.tamarcado.domain.model.service.Category;
import com.tamarcado.domain.model.service.ServiceType;
import com.tamarcado.domain.model.user.Professional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfessionalJpaRepository extends JpaRepository<Professional, UUID> {

    @Query("""
            SELECT DISTINCT p FROM Professional p
            LEFT JOIN FETCH p.user u
            LEFT JOIN FETCH u.address
            WHERE p.id = :id
            """)
    Optional<Professional> findByIdWithDetails(@Param("id") UUID id);

    @Query(value = """
            SELECT p.*,
                   ST_Distance(
                       ST_MakePoint(a.longitude, a.latitude)::geography,
                       ST_MakePoint(:lng, :lat)::geography
                   ) / 1000.0 as distance_km
            FROM professionals p
            INNER JOIN users u ON u.id = p.id
            INNER JOIN addresses a ON a.id = u.address_id
            INNER JOIN service_offerings s ON s.professional_id = p.id
            WHERE s.id = :serviceId
              AND s.active = true
              AND p.active = true
              AND a.latitude IS NOT NULL
              AND a.longitude IS NOT NULL
            HAVING ST_Distance(
                       ST_MakePoint(a.longitude, a.latitude)::geography,
                       ST_MakePoint(:lng, :lat)::geography
                   ) / 1000.0 <= :maxDistanceKm
            ORDER BY distance_km ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<Professional> findNearbyProfessionals(
            @Param("serviceId") UUID serviceId,
            @Param("lat") Double latitude,
            @Param("lng") Double longitude,
            @Param("maxDistanceKm") Double maxDistanceKm,
            @Param("limit") Integer limit
    );

    List<Professional> findByCategoryAndServiceTypeAndActiveTrue(
            Category category,
            ServiceType serviceType
    );

    List<Professional> findByActiveTrue();
}
