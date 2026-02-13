package com.tamarcado.shared.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateDescriptionRequest(
        @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
        String description
) {}
