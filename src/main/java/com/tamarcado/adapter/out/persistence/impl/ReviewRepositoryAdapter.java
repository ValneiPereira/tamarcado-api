package com.tamarcado.adapter.out.persistence.impl;

import com.tamarcado.adapter.out.persistence.repository.ReviewJpaRepository;
import com.tamarcado.application.port.out.ReviewRepositoryPort;
import com.tamarcado.domain.model.review.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReviewRepositoryAdapter implements ReviewRepositoryPort {

    private final ReviewJpaRepository jpaRepository;

    @Override
    public Review save(Review review) {
        return jpaRepository.save(review);
    }

    @Override
    public Optional<Review> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Review> findByAppointmentId(UUID appointmentId) {
        return jpaRepository.findByAppointmentId(appointmentId);
    }

    @Override
    public Page<Review> findByProfessionalId(UUID professionalId, Pageable pageable) {
        return jpaRepository.findByProfessionalIdOrderByCreatedAtDesc(professionalId, pageable);
    }

    @Override
    public List<Review> findByProfessionalId(UUID professionalId) {
        return jpaRepository.findByProfessionalIdOrderByCreatedAtDesc(professionalId);
    }

    @Override
    public boolean existsByAppointmentId(UUID appointmentId) {
        return jpaRepository.existsByAppointmentId(appointmentId);
    }

    @Override
    public long countByProfessionalId(UUID professionalId) {
        return jpaRepository.countByProfessionalId(professionalId);
    }

    @Override
    public Double calculateAverageRatingByProfessionalId(UUID professionalId) {
        return jpaRepository.calculateAverageRatingByProfessionalId(professionalId);
    }
}
