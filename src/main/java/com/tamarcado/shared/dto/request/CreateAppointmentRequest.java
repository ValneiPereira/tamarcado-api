package com.tamarcado.shared.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CreateAppointmentRequest(
        @NotNull(message = "ID do profissional é obrigatório")
        UUID professionalId,

        @NotNull(message = "ID do serviço é obrigatório")
        UUID serviceId,

        @NotNull(message = "Data é obrigatória")
        @Future(message = "Data deve ser futura")
        LocalDate date,

        @NotNull(message = "Horário é obrigatório")
        LocalTime time,

        @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
        String notes
) {}
