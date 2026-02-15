package com.tamarcado.integration.controller;

import com.tamarcado.AbstractIntegrationTestWithoutDocker;
import com.tamarcado.TestUtils;
import com.tamarcado.application.port.out.NotificationRepositoryPort;
import com.tamarcado.application.service.NotificationService;
import com.tamarcado.domain.model.notification.Notification;
import com.tamarcado.domain.model.notification.NotificationType;
import com.tamarcado.domain.model.user.User;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationControllerIntegrationTest extends AbstractIntegrationTestWithoutDocker {

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepositoryPort notificationRepository;

    private User otherClient;
    private String clientToken;
    private Notification notification;

    @BeforeEach
    void setUp() {
        var unique = UUID.randomUUID().toString().substring(0, 8);
        var client = testUtils.createTestClient("client-" + unique + "@test.com", "Cliente Teste");
        clientToken = testUtils.getAuthorizationHeader(testUtils.generateToken(client));

        var unique2 = UUID.randomUUID().toString().substring(0, 8);
        otherClient = testUtils.createTestClient("other-" + unique2 + "@test.com", "Outro Cliente");


        Map<String, Object> data = new HashMap<>();
        data.put("appointmentId", UUID.randomUUID().toString());
        notification = notificationService.sendNotification(
                client.getId(),
                NotificationType.APPOINTMENT_CREATED,
                "Novo Agendamento",
                "Você tem um novo agendamento",
                data
        );
    }

    @Test
    void shouldRegisterDevice() {
        Map<String, Object> request = new HashMap<>();
        request.put("deviceToken", "test-device-token-123");
        request.put("platform", "ANDROID");

        given()
                .header("Authorization", clientToken)
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post("/notifications/register-device")
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data", equalTo("Device token registrado com sucesso"));
    }

    @Test
    void shouldGetNotifications() {
        given()
                .header("Authorization", clientToken)
        .when()
                .get("/notifications")
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data", is(notNullValue()))
                .body("data", is(instanceOf(java.util.List.class)))
                .body("data.size()", greaterThanOrEqualTo(1));
    }

    @Test
    void shouldGetUnreadNotificationsOnly() {
        notification.setIsRead(false);
        notificationRepository.save(notification);

        given()
                .header("Authorization", clientToken)
                .queryParam("unreadOnly", true)
        .when()
                .get("/notifications")
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data", is(notNullValue()))
                .body("data", is(instanceOf(java.util.List.class)))
                .body("data.size()", greaterThanOrEqualTo(1));
    }

    @Test
    void shouldMarkNotificationAsRead() {

        notification.setIsRead(false);
        notificationRepository.save(notification);

        given()
                .header("Authorization", clientToken)
        .when()
                .put("/notifications/" + notification.getId() + "/read")
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data", equalTo("Notificação marcada como lida"));


        var isRead = notificationRepository.findIsReadById(notification.getId())
            .orElseThrow(() -> new RuntimeException("Notificação não encontrada"));
        assertTrue(isRead, "Notificação deveria estar marcada como lida");
    }

    @Test
    void shouldFailMarkOtherUserNotificationAsRead() {

        Map<String, Object> data = new HashMap<>();
        data.put("appointmentId", UUID.randomUUID().toString());
        Notification otherNotification = notificationService.sendNotification(
                otherClient.getId(),
                NotificationType.APPOINTMENT_CREATED,
                "Novo Agendamento",
                "Você tem um novo agendamento",
                data
        );

        given()
                .header("Authorization", clientToken)
        .when()
                .put("/notifications/" + otherNotification.getId() + "/read")
        .then()
                .statusCode(400);
    }

    @Test
    void shouldDeleteDeviceToken() {
        Map<String, Object> registerRequest = new HashMap<>();
        registerRequest.put("deviceToken", "test-device-token-to-delete");
        registerRequest.put("platform", "IOS");

        given()
                .header("Authorization", clientToken)
                .contentType(ContentType.JSON)
                .body(registerRequest)
        .when()
                .post("/notifications/register-device");

        given()
                .header("Authorization", clientToken)
        .when()
                .delete("/notifications/device/test-device-token-to-delete")
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data", equalTo("Device token removido com sucesso"));
    }

    @Test
    void shouldFailAccessOtherUserNotifications() {

        Map<String, Object> data = new HashMap<>();
        data.put("appointmentId", UUID.randomUUID().toString());
        notificationService.sendNotification(
                otherClient.getId(),
                NotificationType.APPOINTMENT_CREATED,
                "Novo Agendamento",
                "Você tem um novo agendamento",
                data
        );

        given()
                .header("Authorization", clientToken)
        .when()
                .get("/notifications")
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data", is(notNullValue()))
                .body("data", is(instanceOf(java.util.List.class)))
                .body("data.size()", equalTo(1));
    }

    @Test
    void shouldFailGetNotificationsWithoutAuth() {
        given()
        .when()
                .get("/notifications")
        .then()
                .statusCode(401);
    }
}
