package com.tamarcado.shared.dto.response;

import com.tamarcado.domain.model.appointment.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentProfessionalResponse(
        UUID id,
        UUID clientId,
        String clientName,
        String clientPhone,
        Double distance,
        ServiceResponse service,
        LocalDate date,
        LocalTime time,
        String notes,
        AppointmentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
