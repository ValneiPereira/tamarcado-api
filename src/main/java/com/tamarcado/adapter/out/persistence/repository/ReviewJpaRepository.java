package com.tamarcado.adapter.out.persistence.repository;

import com.tamarcado.domain.model.review.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewJpaRepository extends JpaRepository<Review, UUID> {

    Optional<Review> findByAppointmentId(UUID appointmentId);

    @Query("SELECT r FROM Review r " +
           "LEFT JOIN FETCH r.appointment a " +
           "LEFT JOIN FETCH a.client " +
           "WHERE r.professional.id = :professionalId " +
           "ORDER BY r.createdAt DESC")
    Page<Review> findByProfessionalIdOrderByCreatedAtDesc(@Param("professionalId") UUID professionalId, Pageable pageable);

    @Query("SELECT r FROM Review r " +
           "LEFT JOIN FETCH r.appointment a " +
           "LEFT JOIN FETCH a.client " +
           "WHERE r.professional.id = :professionalId " +
           "ORDER BY r.createdAt DESC")
    List<Review> findByProfessionalIdOrderByCreatedAtDesc(@Param("professionalId") UUID professionalId);

    boolean existsByAppointmentId(UUID appointmentId);
}
