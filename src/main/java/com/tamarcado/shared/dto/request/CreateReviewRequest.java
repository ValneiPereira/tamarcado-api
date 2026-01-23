package com.tamarcado.shared.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateReviewRequest(
        @NotNull(message = "ID do agendamento é obrigatório")
        UUID appointmentId,

        @NotNull(message = "Avaliação é obrigatória")
        @Min(value = 1, message = "Avaliação deve ser entre 1 e 5")
        @Max(value = 5, message = "Avaliação deve ser entre 1 e 5")
        Integer rating,

        @Size(max = 1000, message = "Comentário deve ter no máximo 1000 caracteres")
        String comment
) {}
