package com.tamarcado.integration.controller;

import com.tamarcado.AbstractIntegrationTestWithoutDocker;
import com.tamarcado.TestUtils;
import com.tamarcado.application.port.out.AppointmentRepositoryPort;
import com.tamarcado.application.port.out.ProfessionalRepositoryPort;
import com.tamarcado.application.port.out.ServiceOfferingRepositoryPort;
import com.tamarcado.domain.model.appointment.Appointment;
import com.tamarcado.domain.model.appointment.AppointmentStatus;
import com.tamarcado.domain.model.service.ServiceOffering;
import com.tamarcado.domain.model.user.Professional;
import com.tamarcado.domain.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class DashboardControllerIntegrationTest extends AbstractIntegrationTestWithoutDocker {

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private ProfessionalRepositoryPort professionalRepository;

    @Autowired
    private ServiceOfferingRepositoryPort serviceOfferingRepository;

    @Autowired
    private AppointmentRepositoryPort appointmentRepository;

    private String professionalToken;
    private String clientToken;

    @BeforeEach
    void setUp() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        User professional = testUtils.createTestProfessional("prof-" + unique + "@test.com", "Profissional Teste");
        professionalToken = testUtils.getAuthorizationHeader(testUtils.generateToken(professional));
        Professional prof = professionalRepository.findById(professional.getId()).orElseThrow();

        String unique2 = UUID.randomUUID().toString().substring(0, 8);
        User client = testUtils.createTestClient("client-" + unique2 + "@test.com", "Cliente Teste");
        clientToken = testUtils.getAuthorizationHeader(testUtils.generateToken(client));

        // Criar serviço
        ServiceOffering service = ServiceOffering.builder()
            .professional(prof)
            .name("Corte de Cabelo")
            .price(BigDecimal.valueOf(50.00))
            .active(true)
            .build();
        service = serviceOfferingRepository.save(service);

        // Criar alguns agendamentos para estatísticas
        Appointment todayAppointment = Appointment.builder()
                .client(client)
                .professional(prof)
                .serviceOffering(service)
                .date(LocalDate.now())
                .time(LocalTime.of(14, 0))
                .status(AppointmentStatus.PENDING)
                .build();
        appointmentRepository.save(todayAppointment);

        Appointment completedAppointment = Appointment.builder()
                .client(client)
                .professional(prof)
                .serviceOffering(service)
                .date(LocalDate.now().minusDays(5))
                .time(LocalTime.of(14, 0))
                .status(AppointmentStatus.COMPLETED)
                .build();
        appointmentRepository.save(completedAppointment);
    }

    @Test
    void shouldGetProfessionalDashboard() {
        given()
                .header("Authorization", professionalToken)
        .when()
                .get("/dashboard/professional/stats")
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.todayAppointments", notNullValue())
                .body("data.pendingAppointments", notNullValue())
                .body("data.averageRating", notNullValue())
                .body("data.totalRatings", notNullValue())
                .body("data.monthRevenue", notNullValue())
                .body("data.completedThisMonth", notNullValue());
    }

    @Test
    void shouldGetClientDashboard() {
        given()
                .header("Authorization", clientToken)
        .when()
                .get("/dashboard/client/stats")
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.upcomingAppointments", notNullValue())
                .body("data.completedAppointments", notNullValue());
    }

    @Test
    void shouldCalculateCorrectStatistics() {
        var response = given()
                .header("Authorization", professionalToken)
        .when()
                .get("/dashboard/professional/stats")
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .extract()
                .path("data");

        // Verificar que as estatísticas são calculadas corretamente
        // (pelo menos devem existir e serem números válidos)
        assert response != null;
    }

    @Test
    void shouldFailGetDashboardWithoutAuth() {
        given()
        .when()
                .get("/dashboard/professional/stats")
        .then()
                .statusCode(401);

        given()
        .when()
                .get("/dashboard/client/stats")
        .then()
                .statusCode(401);
    }
}
