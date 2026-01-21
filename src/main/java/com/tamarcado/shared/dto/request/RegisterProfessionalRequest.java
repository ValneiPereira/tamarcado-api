package com.tamarcado.shared.dto.request;

import com.tamarcado.domain.model.service.Category;
import com.tamarcado.domain.model.service.ServiceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RegisterProfessionalRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(min = 3, max = 150, message = "Nome deve ter entre 3 e 150 caracteres")
        String name,

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, max = 100, message = "Senha deve ter entre 6 e 100 caracteres")
        String password,

        @NotBlank(message = "Telefone é obrigatório")
        @Pattern(regexp = "\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}", message = "Telefone inválido")
        String phone,

        @NotNull(message = "Endereço é obrigatório")
        @Valid
        AddressRequest address,

        @NotNull(message = "Categoria é obrigatória")
        Category category,

        @NotNull(message = "Tipo de serviço é obrigatório")
        ServiceType serviceType,

        @NotNull(message = "Serviços são obrigatórios")
        @Size(min = 1, message = "Deve ter pelo menos um serviço")
        @Valid
        List<ServiceRequest> services
) {}