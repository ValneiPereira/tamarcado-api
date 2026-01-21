package com.tamarcado.shared.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AddressRequest(
        @NotBlank(message = "CEP é obrigatório")
        @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP inválido")
        String cep,

        @NotBlank(message = "Rua é obrigatória")
        String street,

        @NotBlank(message = "Número é obrigatório")
        String number,

        String complement,

        @NotBlank(message = "Bairro é obrigatório")
        String neighborhood,

        @NotBlank(message = "Cidade é obrigatória")
        String city,

        @NotBlank(message = "Estado é obrigatório")
        @Pattern(regexp = "[A-Z]{2}", message = "Estado deve ter 2 letras maiúsculas")
        String state
) {}