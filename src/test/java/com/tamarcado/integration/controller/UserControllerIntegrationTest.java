package com.tamarcado.integration.controller;

import com.tamarcado.AbstractIntegrationTestWithoutDocker;
import com.tamarcado.TestUtils;
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

class UserControllerIntegrationTest extends AbstractIntegrationTestWithoutDocker {

    @Autowired
    private TestUtils testUtils;

    private User client;
    private User otherClient;
    private String clientToken;
    private String otherClientToken;

    @BeforeEach
    void setUp() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        client = testUtils.createTestClient("client-" + unique + "@test.com", "Cliente Teste");
        clientToken = testUtils.getAuthorizationHeader(testUtils.generateToken(client));

        String unique2 = UUID.randomUUID().toString().substring(0, 8);
        otherClient = testUtils.createTestClient("other-" + unique2 + "@test.com", "Outro Cliente");
        otherClientToken = testUtils.getAuthorizationHeader(testUtils.generateToken(otherClient));
    }

    @Test
    void shouldGetCurrentUser() {
        given()
                .header("Authorization", clientToken)
        .when()
                .get("/users/me")
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.id", notNullValue())
                .body("data.email", equalTo(client.getEmail()))
                .body("data.name", equalTo(client.getName()))
                .body("data.userType", equalTo("CLIENT"));
    }

    @Test
    void shouldFailGetCurrentUserWithoutAuth() {
        given()
        .when()
                .get("/users/me")
        .then()
                .statusCode(401);
    }

    @Test
    void shouldUpdateCurrentUser() {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Nome Atualizado");
        request.put("email", "novoemail@test.com");
        request.put("phone", "11999999999");

        given()
                .header("Authorization", clientToken)
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put("/users/me")
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.name", equalTo("Nome Atualizado"))
                .body("data.email", equalTo("novoemail@test.com"));
    }

    @Test
    void shouldFailUpdateCurrentUserWithDuplicateEmail() {
        // Primeiro atualiza o cliente atual
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Nome Atualizado");
        request.put("email", otherClient.getEmail()); // Email já usado por outro cliente
        request.put("phone", "11999999999");

        given()
                .header("Authorization", clientToken)
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put("/users/me")
        .then()
                .statusCode(400);
    }

    @Test
    void shouldChangePassword() {
        Map<String, Object> request = new HashMap<>();
        request.put("currentPassword", "senha123");
        request.put("newPassword", "novaSenha123");
        request.put("confirmPassword", "novaSenha123");

        given()
                .header("Authorization", clientToken)
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put("/users/me/password")
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data", equalTo("Senha alterada com sucesso"));
    }

    @Test
    void shouldFailChangePasswordWithWrongCurrentPassword() {
        Map<String, Object> request = new HashMap<>();
        request.put("currentPassword", "senhaErrada");
        request.put("newPassword", "novaSenha123");
        request.put("confirmPassword", "novaSenha123");

        given()
                .header("Authorization", clientToken)
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put("/users/me/password")
        .then()
                .statusCode(400);
    }

    @Test
    void shouldFailChangePasswordWithMismatchedPasswords() {
        Map<String, Object> request = new HashMap<>();
        request.put("currentPassword", "senha123");
        request.put("newPassword", "novaSenha123");
        request.put("confirmPassword", "senhaDiferente");

        given()
                .header("Authorization", clientToken)
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put("/users/me/password")
        .then()
                .statusCode(400);
    }

    @Test
    void shouldDeleteCurrentUser() {
        given()
                .header("Authorization", clientToken)
        .when()
                .delete("/users/me")
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data", equalTo("Conta desativada com sucesso"));

        // Verificar que o usuário não consegue mais acessar após desativação
        given()
                .header("Authorization", clientToken)
        .when()
                .get("/users/me")
        .then()
                .statusCode(401); // Token pode estar inválido ou usuário inativo
    }

    @Test
    void shouldFailAccessOtherUserProfile() {
        // Tentar acessar perfil de outro usuário usando token de outro cliente
        // Como o endpoint /users/me sempre retorna o usuário autenticado,
        // este teste valida que cada token só acessa seu próprio perfil
        given()
                .header("Authorization", clientToken)
        .when()
                .get("/users/me")
        .then()
                .statusCode(200)
                .body("data.id", equalTo(client.getId().toString()));

        // Outro cliente acessa seu próprio perfil
        given()
                .header("Authorization", otherClientToken)
        .when()
                .get("/users/me")
        .then()
                .statusCode(200)
                .body("data.id", equalTo(otherClient.getId().toString()));
    }
}
