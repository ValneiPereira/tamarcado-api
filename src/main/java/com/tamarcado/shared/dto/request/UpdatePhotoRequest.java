package com.tamarcado.shared.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdatePhotoRequest(
        @NotBlank(message = "URL da foto é obrigatória")
        @Pattern(regexp = "https?://.*", message = "URL da foto deve ser válida")
        String photoUrl
) {}
