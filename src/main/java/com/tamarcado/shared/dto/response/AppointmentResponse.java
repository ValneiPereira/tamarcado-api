package com.tamarcado.shared.dto.response;

import com.tamarcado.domain.model.appointment.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        UUID professionalId,
        String professionalName,
        String professionalPhone,
        UUID clientId,
        String clientName,
        ServiceResponse service,
        LocalDate date,
        LocalTime time,
        String notes,
        AppointmentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
