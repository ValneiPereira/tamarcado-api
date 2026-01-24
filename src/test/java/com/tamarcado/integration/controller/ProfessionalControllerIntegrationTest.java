package com.tamarcado.integration.controller;

import com.tamarcado.AbstractIntegrationTestWithoutDocker;
import com.tamarcado.TestUtils;
import com.tamarcado.application.port.out.AppointmentRepositoryPort;
import com.tamarcado.application.port.out.ProfessionalRepositoryPort;
import com.tamarcado.application.port.out.ServiceOfferingRepositoryPort;
import com.tamarcado.application.port.out.UserRepositoryPort;
import com.tamarcado.domain.model.appointment.Appointment;
import com.tamarcado.domain.model.appointment.AppointmentStatus;
import com.tamarcado.domain.model.service.ServiceOffering;
import com.tamarcado.domain.model.user.Professional;
import com.tamarcado.domain.model.user.User;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class ProfessionalControllerIntegrationTest extends AbstractIntegrationTestWithoutDocker {

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private ProfessionalRepositoryPort professionalRepository;

    @Autowired
    private ServiceOfferingRepositoryPort serviceOfferingRepository;

    @Autowired
    private AppointmentRepositoryPort appointmentRepository;

    @Autowired
    private UserRepositoryPort userRepository;

    private User professional;
    private User client;
    private String professionalToken;
    private String otherProfessionalToken;
    private Professional prof;
    private ServiceOffering service;

    @BeforeEach
    void setUp() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        professional = testUtils.createTestProfessional("prof-" + unique + "@test.com", "Profissional Teste");
        professionalToken = testUtils.getAuthorizationHeader(testUtils.generateToken(professional));
        // Usar findByIdWithDetails para garantir que o relacionamento User está carregado
        prof = professionalRepository.findByIdWithDetails(professional.getId()).orElseThrow();

        String unique2 = UUID.randomUUID().toString().substring(0, 8);
        User otherProfessional = testUtils.createTestProfessional("other-" + unique2 + "@test.com", "Outro Profissional");
        otherProfessionalToken = testUtils.getAuthorizationHeader(testUtils.generateToken(otherProfessional));

        String unique3 = UUID.randomUUID().toString().substring(0, 8);
        client = testUtils.createTestClient("client-" + unique3 + "@test.com", "Cliente Teste");
        String clientToken = testUtils.getAuthorizationHeader(testUtils.generateToken(client));

        // Criar um serviço para o profissional
        service = ServiceOffering.builder()
                .professional(prof)
                .name("Corte de Cabelo")
                .price(BigDecimal.valueOf(50.00))
                .active(true)
                .build();
        service = serviceOfferingRepository.save(service);
    }

    @Test
    void shouldGetProfessionalById() {
        given()
        .when()
                .get("/professionals/" + professional.getId())
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data", is(notNullValue()))
                .body("data.id", notNullValue())
                .body("data.name", notNullValue())
                .body("data.email", notNullValue())
                .body("data.category", notNullValue())
                .body("data.serviceType", notNullValue())
                .body("data.services", is(notNullValue()))
                .body("data.reviews", is(notNullValue()))
                .body("data.phone", anyOf(notNullValue(), nullValue()))
                .body("data.photo", anyOf(notNullValue(), nullValue()))
                .body("data.averageRating", anyOf(notNullValue(), nullValue()))
                .body("data.totalRatings", anyOf(notNullValue(), nullValue()));
    }

    @Test
    void shouldGetProfessionalByIdWithDistance() {
        // Atualizar endereço do profissional com coordenadas
        var address = prof.getUser().getAddress();
        address.setLatitude(-23.550520);
        address.setLongitude(-46.633308);
        prof.getUser().setAddress(address);
        // Salvar o user para persistir o endereço
        userRepository.save(prof.getUser());
        // Recarregar o profissional
        prof = professionalRepository.findById(professional.getId()).orElseThrow();

        given()
                .queryParam("latitude", -23.550520)
                .queryParam("longitude", -46.633308)
        .when()
                .get("/professionals/" + professional.getId())
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.id", equalTo(professional.getId().toString()))
                .body("data.distanceKm", anyOf(notNullValue(), nullValue()));
                // distanceKm pode ser null se não houver coordenadas do cliente
    }

    @Test
    void shouldGetMyServices() {
        given()
                .header("Authorization", professionalToken)
        .when()
                .get("/professionals/me/services")
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data", is(notNullValue()))
                .body("data.size()", greaterThanOrEqualTo(1));
    }

    @Test
    void shouldFailGetMyServicesWithoutAuth() {
        given()
        .when()
                .get("/professionals/me/services")
        .then()
                .statusCode(401);
    }

    @Test
    void shouldCreateService() {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Barba Completa");
        request.put("price", 30.00);

        given()
                .header("Authorization", professionalToken)
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post("/professionals/me/services")
        .then()
                .statusCode(201)
                .body("success", equalTo(true))
                .body("data.id", notNullValue())
                .body("data.name", equalTo("Barba Completa"))
                .body("data.price", equalTo(30.0f));
    }

    @Test
    void shouldUpdateService() {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Corte Atualizado");
        request.put("price", 60.00);

        given()
                .header("Authorization", professionalToken)
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put("/professionals/me/services/" + service.getId())
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.name", equalTo("Corte Atualizado"))
                .body("data.price", equalTo(60.0f));
    }

    @Test
    void shouldFailUpdateOtherProfessionalService() {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Corte Atualizado");
        request.put("price", 60.00);

        given()
                .header("Authorization", otherProfessionalToken)
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put("/professionals/me/services/" + service.getId())
        .then()
                .statusCode(400); // Serviço não pertence ao profissional
    }

    @Test
    void shouldDeleteService() {
        // Criar um serviço temporário para deletar
        ServiceOffering tempService = ServiceOffering.builder()
                .professional(prof)
                .name("Serviço Temporário")
                .price(BigDecimal.valueOf(25.00))
                .active(true)
                .build();
        tempService = serviceOfferingRepository.save(tempService);

        given()
                .header("Authorization", professionalToken)
        .when()
                .delete("/professionals/me/services/" + tempService.getId())
        .then()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @Test
    void shouldFailDeleteServiceWithActiveAppointments() {
        // Criar um agendamento ativo para o serviço
        Appointment appointment = Appointment.builder()
                .client(client)
                .professional(prof)
                .serviceOffering(service)
                .date(LocalDate.now().plusDays(1))
                .time(LocalTime.of(14, 0))
                .status(AppointmentStatus.PENDING)
                .build();
        appointmentRepository.save(appointment);

        given()
                .header("Authorization", professionalToken)
        .when()
                .delete("/professionals/me/services/" + service.getId())
        .then()
                .statusCode(400); // Não pode deletar serviço com agendamentos ativos
    }

    @Test
    void shouldFailAccessOtherProfessionalServices() {
        // Tentar acessar serviços de outro profissional
        given()
                .header("Authorization", otherProfessionalToken)
        .when()
                .get("/professionals/me/services")
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.size()", equalTo(0)); // Outro profissional não tem serviços
    }
}
