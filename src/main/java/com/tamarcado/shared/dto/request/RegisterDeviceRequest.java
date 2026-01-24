package com.tamarcado.shared.dto.request;

import com.tamarcado.domain.model.notification.DeviceToken;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterDeviceRequest(
        @NotBlank(message = "Device token é obrigatório")
        String deviceToken,

        @NotNull(message = "Platform é obrigatória")
        DeviceToken.Platform platform
) {}
