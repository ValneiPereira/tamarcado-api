package com.tamarcado.adapter.out.persistence.jpa;

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

    Page<Review> findByProfessionalIdOrderByCreatedAtDesc(UUID professionalId, Pageable pageable);

    List<Review> findByProfessionalIdOrderByCreatedAtDesc(UUID professionalId);

    boolean existsByAppointmentId(UUID appointmentId);
}