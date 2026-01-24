package com.tamarcado;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

/**
 * Versão alternativa do AbstractIntegrationTest que não requer Docker.
 * Usa H2 em memória em vez de Testcontainers.
 * Configurado para usar Rest Assured para testes de API.
 * 
 * Use esta classe quando Docker não estiver disponível.
 * 
 * Exemplo de uso:
 * class MyTest extends AbstractIntegrationTestWithoutDocker { ... }
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTestWithoutDocker {
    
    @LocalServerPort
    protected int port;
    
    @BeforeEach
    void setUpRestAssured() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.basePath = "/api/v1";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}
