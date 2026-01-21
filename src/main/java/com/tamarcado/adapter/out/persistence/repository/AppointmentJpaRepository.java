package com.tamarcado.adapter.out.persistence.repository;

import com.tamarcado.domain.model.appointment.Appointment;
import com.tamarcado.domain.model.appointment.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentJpaRepository extends JpaRepository<Appointment, UUID> {

    @Query("""
            SELECT DISTINCT a FROM Appointment a
            LEFT JOIN FETCH a.client
            LEFT JOIN FETCH a.professional
            LEFT JOIN FETCH a.professional.user
            LEFT JOIN FETCH a.serviceOffering
            WHERE a.id = :id
            """)
    Optional<Appointment> findByIdWithDetails(@Param("id") UUID id);

    List<Appointment> findByClientId(UUID clientId);

    List<Appointment> findByClientIdAndStatus(UUID clientId, AppointmentStatus status);

    List<Appointment> findByProfessionalId(UUID professionalId);

    List<Appointment> findByProfessionalIdAndStatus(UUID professionalId, AppointmentStatus status);

    List<Appointment> findByProfessionalIdAndDate(UUID professionalId, LocalDate date);

    List<Appointment> findByProfessionalIdAndDateAndStatus(
            UUID professionalId,
            LocalDate date,
            AppointmentStatus status
    );

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.professional.id = :professionalId AND a.status = :status")
    long countByProfessionalIdAndStatus(@Param("professionalId") UUID professionalId, @Param("status") AppointmentStatus status);
}
