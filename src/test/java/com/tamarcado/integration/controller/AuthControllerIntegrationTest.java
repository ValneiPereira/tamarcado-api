package com.tamarcado.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tamarcado.AbstractIntegrationTest;
import com.tamarcado.shared.dto.request.LoginRequest;
import com.tamarcado.shared.dto.request.RegisterClientRequest;
import com.tamarcado.shared.dto.request.RegisterProfessionalRequest;
import com.tamarcado.shared.dto.request.AddressRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRegisterClient() throws Exception {
        RegisterClientRequest request = new RegisterClientRequest(
                "João Silva",
                "joao@example.com",
                "senha123",
                "11999999999",
                new AddressRequest(
                        "01310-100",
                        "Av. Paulista",
                        "1000",
                        null,
                        "Bela Vista",
                        "São Paulo",
                        "SP"
                )
        );

        mockMvc.perform(post("/api/v1/auth/register/client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.user.id").exists())
                .andExpect(jsonPath("$.data.user.email").value("joao@example.com"));
    }

    @Test
    void shouldRegisterProfessional() throws Exception {
        RegisterProfessionalRequest request = new RegisterProfessionalRequest(
                "Maria Santos",
                "maria@example.com",
                "senha123",
                "11988888888",
                new AddressRequest(
                        "01310-100",
                        "Av. Paulista",
                        "2000",
                        null,
                        "Bela Vista",
                        "São Paulo",
                        "SP"
                ),
                com.tamarcado.domain.model.service.Category.BELEZA,
                com.tamarcado.domain.model.service.ServiceType.CABELEIREIRO,
                java.util.List.of(
                        new com.tamarcado.shared.dto.request.ServiceRequest("Corte de Cabelo", java.math.BigDecimal.valueOf(50.00))
                )
        );

        mockMvc.perform(post("/api/v1/auth/register/professional")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.user.email").value("maria@example.com"));
    }

    @Test
    void shouldLoginWithValidCredentials() throws Exception {
        // Primeiro registrar um cliente
        RegisterClientRequest registerRequest = new RegisterClientRequest(
                "Teste Login",
                "login@example.com",
                "senha123",
                "11977777777",
                new AddressRequest(
                        "01310-100",
                        "Av. Paulista",
                        "3000",
                        null,
                        "Bela Vista",
                        "São Paulo",
                        "SP"
                )
        );

        mockMvc.perform(post("/api/v1/auth/register/client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)));

        // Agora fazer login
        LoginRequest loginRequest = new LoginRequest("login@example.com", "senha123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.user.email").value("login@example.com"));
    }

    @Test
    void shouldFailLoginWithInvalidCredentials() throws Exception {
        LoginRequest loginRequest = new LoginRequest("inexistente@example.com", "senha123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}
