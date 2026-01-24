package com.tamarcado.integration.controller;

import com.tamarcado.AbstractIntegrationTestWithoutDocker;
import com.tamarcado.TestDataLoader;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class AuthControllerIntegrationTest extends AbstractIntegrationTestWithoutDocker {

    @Test
    void shouldRegisterClient() {
        Map<String, Object> request = TestDataLoader.loadRegisterClientRequest("joaoSilva");

        given()
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post("/auth/register/client")
        .then()
                .statusCode(201)
                .body("success", equalTo(true))
                .body("data.accessToken", notNullValue())
                .body("data.refreshToken", notNullValue())
                .body("data.user.id", notNullValue())
                .body("data.user.email", equalTo("joao@example.com"));
    }

    @Test
    void shouldRegisterProfessional() {
        Map<String, Object> request = TestDataLoader.loadRegisterProfessionalRequest("mariaSantos");

        given()
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post("/auth/register/professional")
        .then()
                .statusCode(201)
                .body("success", equalTo(true))
                .body("data.accessToken", notNullValue())
                .body("data.user.email", equalTo("maria@example.com"));
    }

    @Test
    void shouldLoginWithValidCredentials() {
        Map<String, Object> registerRequest = TestDataLoader.loadRegisterClientRequest("loginTest");
        given()
                .contentType(ContentType.JSON)
                .body(registerRequest)
        .when()
                .post("/auth/register/client");

        Map<String, Object> loginRequest = TestDataLoader.loadLoginRequest("loginTest");
        given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
        .when()
                .post("/auth/login")
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.accessToken", notNullValue())
                .body("data.user.email", equalTo("login@example.com"));
    }

    @Test
    void shouldFailLoginWithInvalidCredentials() {
        Map<String, Object> loginRequest = TestDataLoader.loadLoginRequest("invalid");

        given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
        .when()
                .post("/auth/login")
        .then()
                .statusCode(400);
    }
}
