package com.tamarcado.application.port.out;

import com.tamarcado.domain.model.review.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepositoryPort {

    Review save(Review review);

    Optional<Review> findById(UUID id);

    Optional<Review> findByAppointmentId(UUID appointmentId);

    Page<Review> findByProfessionalId(UUID professionalId, Pageable pageable);

    List<Review> findByProfessionalId(UUID professionalId);

    boolean existsByAppointmentId(UUID appointmentId);
}