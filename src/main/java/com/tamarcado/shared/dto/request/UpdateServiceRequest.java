package com.tamarcado.shared.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateServiceRequest(
        @NotBlank(message = "Nome do serviço é obrigatório")
        @Size(min = 3, max = 150, message = "Nome deve ter entre 3 e 150 caracteres")
        String name,

        @NotNull(message = "Preço é obrigatório")
        @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
        BigDecimal price
) {}
