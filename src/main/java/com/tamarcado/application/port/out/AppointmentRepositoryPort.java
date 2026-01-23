package com.tamarcado.application.port.out;

import com.tamarcado.domain.model.appointment.Appointment;
import com.tamarcado.domain.model.appointment.AppointmentStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepositoryPort {

    Appointment save(Appointment appointment);

    Optional<Appointment> findById(UUID id);

    Optional<Appointment> findByIdWithDetails(UUID id);

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

    long countByProfessionalIdAndStatus(UUID professionalId, AppointmentStatus status);

    List<Appointment> findByProfessionalIdAndDateRangeAndStatus(
            UUID professionalId,
            LocalDate startDate,
            LocalDate endDate,
            AppointmentStatus status
    );

    List<Appointment> findByClientIdAndStatusIn(UUID clientId, List<AppointmentStatus> statuses);
}