package com.tamarcado.shared.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BusinessHoursItemRequest(
        @NotNull(message = "Dia da semana é obrigatório")
        @Min(value = 0, message = "Dia da semana deve ser entre 0 (Segunda) e 6 (Domingo)")
        @Max(value = 6, message = "Dia da semana deve ser entre 0 (Segunda) e 6 (Domingo)")
        Integer dayOfWeek,

        @NotNull(message = "Horário de início é obrigatório")
        String startTime,

        @NotNull(message = "Horário de fim é obrigatório")
        String endTime,

        @NotNull(message = "Status ativo é obrigatório")
        Boolean active
) {}
