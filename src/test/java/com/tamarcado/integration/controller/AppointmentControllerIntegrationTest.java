package com.tamarcado.integration.controller;

import com.tamarcado.AbstractIntegrationTestWithoutDocker;
import com.tamarcado.TestDataLoader;
import com.tamarcado.TestUtils;
import com.tamarcado.application.port.out.ProfessionalRepositoryPort;
import com.tamarcado.application.port.out.ServiceOfferingRepositoryPort;
import com.tamarcado.domain.model.service.ServiceOffering;
import com.tamarcado.domain.model.user.Professional;
import com.tamarcado.domain.model.user.User;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static com.tamarcado.TestDataLoader.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class AppointmentControllerIntegrationTest extends AbstractIntegrationTestWithoutDocker {

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private ServiceOfferingRepositoryPort serviceOfferingRepository;

    @Autowired
    private ProfessionalRepositoryPort professionalRepository;

    private User professional;
    private ServiceOffering service;
    private String clientToken;
    private String professionalToken;

    @BeforeEach
    void setUp() {
        Map<String, Object> clientSetup = loadAppointmentSetup("client");
        Map<String, Object> professionalSetup = loadAppointmentSetup("professional");
        Map<String, Object> serviceSetup = loadAppointmentSetup("service");

        String unique = UUID.randomUUID().toString().substring(0, 8);
        String clientName = (String) clientSetup.get("name");
        String professionalName = (String) professionalSetup.get("name");
        User client = testUtils.createTestClient("client-" + unique + "@test.com", clientName);
        clientToken = testUtils.getAuthorizationHeader(testUtils.generateToken(client));
        professional = testUtils.createTestProfessional("professional-" + unique + "@test.com",
            professionalName);
        professionalToken = testUtils.getAuthorizationHeader(testUtils.generateToken(professional));

        Professional prof = professionalRepository.findById(professional.getId()).orElseThrow();
        String serviceName = (String) serviceSetup.get("name");
        BigDecimal price = BigDecimal.valueOf(((Number) serviceSetup.get("price")).doubleValue());
        service = ServiceOffering.builder()
            .professional(prof)
            .name(serviceName)
            .price(price)
            .active(true)
            .build();
        service = serviceOfferingRepository.save(service);
    }

    @Test
    void shouldCreateAppointment() {
        Map<String, Object> request = buildCreateAppointmentRequest(
            professional.getId(), service.getId(), "default");

        given()
            .header("Authorization", clientToken)
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/appointments")
            .then()
            .statusCode(201)
            .body("success", equalTo(true))
            .body("data.id", notNullValue())
            .body("data.professionalId", equalTo(professional.getId().toString()));
    }

    @Test
    void shouldListClientAppointments() {
        given()
            .header("Authorization", clientToken)
            .when()
            .get("/appointments/client")
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("data", is(notNullValue()));
    }

    @Test
    void shouldFailCreateAppointmentWithoutAuth() {
        Map<String, Object> request = buildCreateAppointmentRequest(
            professional.getId(), service.getId(), "noNotes");

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/appointments")
            .then()
            .statusCode(401);
    }

    @Test
    void shouldGetAppointmentById() {
        String appointmentId = createAppointment();

        given()
            .header("Authorization", clientToken)
            .when()
            .get("/appointments/{id}", appointmentId)
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("data.id", equalTo(appointmentId));
    }

    @Test
    void shouldCancelAppointment() {
        String appointmentId = createAppointment();

        given()
            .header("Authorization", clientToken)
            .when()
            .delete("/appointments/{id}", appointmentId)
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("data", containsString("cancelado"));
    }

    @Test
    void shouldListProfessionalAppointments() {
        createAppointment();

        given()
            .header("Authorization", professionalToken)
            .when()
            .get("/appointments/professional")
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("data", is(notNullValue()));
    }

    @Test
    void shouldAcceptAppointment() {
        String appointmentId = createAppointment();

        given()
            .header("Authorization", professionalToken)
            .when()
            .put("/appointments/{id}/accept", appointmentId)
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("data", containsString("aceito"));
    }

    @Test
    void shouldRejectAppointment() {
        String appointmentId = createAppointment();

        given()
            .header("Authorization", professionalToken)
            .when()
            .put("/appointments/{id}/reject", appointmentId)
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("data", containsString("rejeitado"));
    }

    @Test
    void shouldCompleteAppointment() {
        String appointmentId = createAppointment();

        // Profissional aceita primeiro
        given()
            .header("Authorization", professionalToken)
            .put("/appointments/{id}/accept", appointmentId);

        // Depois completa
        given()
            .header("Authorization", professionalToken)
            .when()
            .put("/appointments/{id}/complete", appointmentId)
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("data", containsString("completado"));
    }

    private String createAppointment() {
        Map<String, Object> request = buildCreateAppointmentRequest(
            professional.getId(), service.getId(), "default");

        return given()
            .header("Authorization", clientToken)
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/appointments")
            .then()
            .statusCode(201)
            .extract()
            .path("data.id");
    }
}
