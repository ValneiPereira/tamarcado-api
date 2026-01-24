package com.tamarcado.adapter.out.persistence.impl;

import com.tamarcado.adapter.out.persistence.repository.AppointmentJpaRepository;
import com.tamarcado.application.port.out.AppointmentRepositoryPort;
import com.tamarcado.domain.model.appointment.Appointment;
import com.tamarcado.domain.model.appointment.AppointmentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AppointmentRepositoryAdapter implements AppointmentRepositoryPort {

    private final AppointmentJpaRepository jpaRepository;

    @Override
    public Appointment save(Appointment appointment) {
        return jpaRepository.save(appointment);
    }

    @Override
    public Optional<Appointment> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Appointment> findByIdWithDetails(UUID id) {
        return jpaRepository.findByIdWithDetails(id);
    }

    @Override
    public List<Appointment> findByClientId(UUID clientId) {
        return jpaRepository.findByClientId(clientId);
    }

    @Override
    public List<Appointment> findByClientIdAndStatus(UUID clientId, AppointmentStatus status) {
        return jpaRepository.findByClientIdAndStatus(clientId, status);
    }

    @Override
    public List<Appointment> findByProfessionalId(UUID professionalId) {
        return jpaRepository.findByProfessionalId(professionalId);
    }

    @Override
    public List<Appointment> findByProfessionalIdAndStatus(UUID professionalId, AppointmentStatus status) {
        return jpaRepository.findByProfessionalIdAndStatus(professionalId, status);
    }

    @Override
    public List<Appointment> findByProfessionalIdAndDate(UUID professionalId, LocalDate date) {
        return jpaRepository.findByProfessionalIdAndDate(professionalId, date);
    }

    @Override
    public List<Appointment> findByProfessionalIdAndDateAndStatus(
            UUID professionalId,
            LocalDate date,
            AppointmentStatus status
    ) {
        return jpaRepository.findByProfessionalIdAndDateAndStatus(professionalId, date, status);
    }

    @Override
    public long countByProfessionalIdAndStatus(UUID professionalId, AppointmentStatus status) {
        return jpaRepository.countByProfessionalIdAndStatus(professionalId, status);
    }

    @Override
    public List<Appointment> findByProfessionalIdAndDateRangeAndStatus(
            UUID professionalId,
            LocalDate startDate,
            LocalDate endDate,
            AppointmentStatus status
    ) {
        return jpaRepository.findByProfessionalIdAndDateRangeAndStatus(professionalId, startDate, endDate, status);
    }

    @Override
    public List<Appointment> findByClientIdAndStatusIn(UUID clientId, List<AppointmentStatus> statuses) {
        return jpaRepository.findByClientIdAndStatusIn(clientId, statuses);
    }
}
