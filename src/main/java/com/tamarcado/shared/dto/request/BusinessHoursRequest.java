package com.tamarcado.shared.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BusinessHoursRequest(
        @NotNull(message = "Lista de horários é obrigatória")
        @Valid
        List<BusinessHoursItemRequest> hours
) {}
