package com.tamarcado.integration.controller;

import com.tamarcado.AbstractIntegrationTestWithoutDocker;
import com.tamarcado.TestUtils;
import com.tamarcado.application.port.out.AppointmentRepositoryPort;
import com.tamarcado.application.port.out.ProfessionalRepositoryPort;
import com.tamarcado.application.port.out.ReviewRepositoryPort;
import com.tamarcado.application.port.out.ServiceOfferingRepositoryPort;
import com.tamarcado.domain.model.appointment.Appointment;
import com.tamarcado.domain.model.appointment.AppointmentStatus;
import com.tamarcado.domain.model.review.Review;
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

class ReviewControllerIntegrationTest extends AbstractIntegrationTestWithoutDocker {

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private ProfessionalRepositoryPort professionalRepository;

    @Autowired
    private ServiceOfferingRepositoryPort serviceOfferingRepository;

    @Autowired
    private AppointmentRepositoryPort appointmentRepository;

    @Autowired
    private ReviewRepositoryPort reviewRepository;

    private User client;
    private User otherClient;
    private User professional;
    private String clientToken;
    private String otherClientToken;
    private Professional prof;
    private ServiceOffering service;
    private Appointment completedAppointment;
    private Appointment pendingAppointment;

    @BeforeEach
    void setUp() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        client = testUtils.createTestClient("client-" + unique + "@test.com", "Cliente Teste");
        clientToken = testUtils.getAuthorizationHeader(testUtils.generateToken(client));

        String unique2 = UUID.randomUUID().toString().substring(0, 8);
        otherClient = testUtils.createTestClient("other-" + unique2 + "@test.com", "Outro Cliente");
        otherClientToken = testUtils.getAuthorizationHeader(testUtils.generateToken(otherClient));

        String unique3 = UUID.randomUUID().toString().substring(0, 8);
        professional = testUtils.createTestProfessional("prof-" + unique3 + "@test.com", "Profissional Teste");
        prof = professionalRepository.findById(professional.getId()).orElseThrow();

        // Criar serviço
        service = ServiceOffering.builder()
                .professional(prof)
                .name("Corte de Cabelo")
                .price(BigDecimal.valueOf(50.00))
                .active(true)
                .build();
        service = serviceOfferingRepository.save(service);

        // Criar agendamento completado
        completedAppointment = Appointment.builder()
                .client(client)
                .professional(prof)
                .serviceOffering(service)
                .date(LocalDate.now().minusDays(1))
                .time(LocalTime.of(14, 0))
                .status(AppointmentStatus.COMPLETED)
                .build();
        completedAppointment = appointmentRepository.save(completedAppointment);

        // Criar agendamento pendente
        pendingAppointment = Appointment.builder()
                .client(client)
                .professional(prof)
                .serviceOffering(service)
                .date(LocalDate.now().plusDays(1))
                .time(LocalTime.of(15, 0))
                .status(AppointmentStatus.PENDING)
                .build();
        pendingAppointment = appointmentRepository.save(pendingAppointment);
    }

    @Test
    void shouldCreateReview() {
        Map<String, Object> request = new HashMap<>();
        request.put("appointmentId", completedAppointment.getId().toString());
        request.put("rating", 5);
        request.put("comment", "Excelente serviço!");

        given()
                .header("Authorization", clientToken)
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post("/reviews")
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.id", notNullValue())
                .body("data.rating", equalTo(5))
                .body("data.comment", equalTo("Excelente serviço!"));
    }

    @Test
    void shouldFailCreateReviewForNonCompletedAppointment() {
        Map<String, Object> request = new HashMap<>();
        request.put("appointmentId", pendingAppointment.getId().toString());
        request.put("rating", 5);
        request.put("comment", "Excelente serviço!");

        given()
                .header("Authorization", clientToken)
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post("/reviews")
        .then()
                .statusCode(400); // Apenas agendamentos completados podem ser avaliados
    }

    @Test
    void shouldFailCreateReviewForOtherClientAppointment() {
        // Criar agendamento completado para outro cliente
        Appointment otherAppointment = Appointment.builder()
                .client(otherClient)
                .professional(prof)
                .serviceOffering(service)
                .date(LocalDate.now().minusDays(2))
                .time(LocalTime.of(16, 0))
                .status(AppointmentStatus.COMPLETED)
                .build();
        otherAppointment = appointmentRepository.save(otherAppointment);

        Map<String, Object> request = new HashMap<>();
        request.put("appointmentId", otherAppointment.getId().toString());
        request.put("rating", 5);
        request.put("comment", "Excelente serviço!");

        given()
                .header("Authorization", clientToken)
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post("/reviews")
        .then()
                .statusCode(400); // Cliente só pode avaliar seus próprios agendamentos
    }

    @Test
    void shouldFailCreateDuplicateReview() {
        // Criar uma avaliação existente
        Review existingReview = Review.builder()
                .appointment(completedAppointment)
                .professional(prof)
                .rating(4)
                .comment("Bom serviço")
                .build();
        reviewRepository.save(existingReview);

        Map<String, Object> request = new HashMap<>();
        request.put("appointmentId", completedAppointment.getId().toString());
        request.put("rating", 5);
        request.put("comment", "Excelente serviço!");

        given()
                .header("Authorization", clientToken)
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post("/reviews")
        .then()
                .statusCode(400); // Agendamento já foi avaliado
    }

    @Test
    void shouldGetReviewsByProfessional() {
        // Criar algumas avaliações
        Review review1 = Review.builder()
                .appointment(completedAppointment)
                .professional(prof)
                .rating(5)
                .comment("Excelente!")
                .build();
        reviewRepository.save(review1);

        given()
        .when()
                .get("/reviews/professionals/" + professional.getId())
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.averageRating", notNullValue())
                .body("data.totalReviews", greaterThanOrEqualTo(1))
                .body("data.reviews", notNullValue())
                .body("data.pagination", notNullValue());
    }

    @Test
    void shouldGetReviewsByProfessionalWithPagination() {
        // Criar algumas avaliações
        Review review1 = Review.builder()
                .appointment(completedAppointment)
                .professional(prof)
                .rating(5)
                .comment("Excelente!")
                .build();
        reviewRepository.save(review1);

        given()
                .queryParam("page", 1)
                .queryParam("pageSize", 5)
        .when()
                .get("/reviews/professionals/" + professional.getId())
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.pagination.page", equalTo(1))
                .body("data.pagination.pageSize", equalTo(5));
    }

    @Test
    void shouldGetMyReviews() {
        // Criar uma avaliação para o cliente
        Review review = Review.builder()
                .appointment(completedAppointment)
                .professional(prof)
                .rating(5)
                .comment("Excelente!")
                .build();
        reviewRepository.save(review);

        given()
                .header("Authorization", clientToken)
        .when()
                .get("/reviews/client/me")
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data", is(notNullValue()))
                .body("data.size()", greaterThanOrEqualTo(1));
    }

    @Test
    void shouldFailGetMyReviewsWithoutAuth() {
        given()
        .when()
                .get("/reviews/client/me")
        .then()
                .statusCode(401);
    }
}
