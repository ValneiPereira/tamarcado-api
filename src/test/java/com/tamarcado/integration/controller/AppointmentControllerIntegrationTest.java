package com.tamarcado.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tamarcado.AbstractIntegrationTest;
import com.tamarcado.TestUtils;
import com.tamarcado.application.port.out.ProfessionalRepositoryPort;
import com.tamarcado.application.port.out.ServiceOfferingRepositoryPort;
import com.tamarcado.domain.model.service.ServiceOffering;
import com.tamarcado.domain.model.user.Professional;
import com.tamarcado.domain.model.user.User;
import com.tamarcado.shared.dto.request.CreateAppointmentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
class AppointmentControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private ServiceOfferingRepositoryPort serviceOfferingRepository;

    @Autowired
    private ProfessionalRepositoryPort professionalRepository;

    private User client;
    private User professional;
    private ServiceOffering service;
    private String clientToken;

    @BeforeEach
    void setUp() {
        // Criar cliente
        client = testUtils.createTestClient("client@test.com", "Cliente Teste");
        clientToken = testUtils.getAuthorizationHeader(testUtils.generateToken(client));

        // Criar profissional
        professional = testUtils.createTestProfessional("professional@test.com", "Profissional Teste");

        // Buscar Professional criado
        Professional prof = professionalRepository.findById(professional.getId())
                .orElseThrow();

        // Criar serviço
        service = ServiceOffering.builder()
                .professional(prof)
                .name("Corte de Cabelo")
                .price(BigDecimal.valueOf(50.00))
                .active(true)
                .build();
        service = serviceOfferingRepository.save(service);
    }

    @Test
    void shouldCreateAppointment() throws Exception {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                professional.getId(),
                service.getId(),
                LocalDate.now().plusDays(1),
                LocalTime.of(14, 0),
                "Teste de agendamento"
        );

        mockMvc.perform(post("/api/v1/appointments")
                        .header("Authorization", clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.professionalId").value(professional.getId().toString()));
    }

    @Test
    void shouldListClientAppointments() throws Exception {
        mockMvc.perform(get("/api/v1/appointments/client")
                        .header("Authorization", clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void shouldFailCreateAppointmentWithoutAuth() throws Exception {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                professional.getId(),
                service.getId(),
                LocalDate.now().plusDays(1),
                LocalTime.of(14, 0),
                null
        );

        mockMvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
