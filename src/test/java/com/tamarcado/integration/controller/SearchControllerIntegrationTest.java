package com.tamarcado.integration.controller;

import com.tamarcado.AbstractIntegrationTestWithoutDocker;
import com.tamarcado.TestUtils;
import com.tamarcado.application.port.out.ProfessionalRepositoryPort;
import com.tamarcado.application.port.out.ServiceOfferingRepositoryPort;
import com.tamarcado.application.port.out.UserRepositoryPort;
import com.tamarcado.domain.model.service.Category;
import com.tamarcado.domain.model.service.ServiceOffering;
import com.tamarcado.domain.model.service.ServiceType;
import com.tamarcado.domain.model.user.Professional;
import com.tamarcado.domain.model.user.User;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class SearchControllerIntegrationTest extends AbstractIntegrationTestWithoutDocker {

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private ProfessionalRepositoryPort professionalRepository;

    @Autowired
    private ServiceOfferingRepositoryPort serviceOfferingRepository;

    @Autowired
    private UserRepositoryPort userRepository;

    private User professional;
    private Professional prof;
    private ServiceOffering service;

    @BeforeEach
    void setUp() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        professional = testUtils.createTestProfessional("prof-" + unique + "@test.com", "Profissional Teste");
        // Usar findByIdWithDetails para garantir que o relacionamento User está carregado
        prof = professionalRepository.findByIdWithDetails(professional.getId()).orElseThrow();

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
    void shouldSearchServices() {
        given()
        .when()
                .get("/search/services")
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data", is(notNullValue()))
                .body("data.size()", greaterThanOrEqualTo(1));
    }

    @Test
    void shouldSearchServicesByCategory() {
        given()
                .queryParam("category", Category.BELEZA.name())
        .when()
                .get("/search/services")
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data", is(notNullValue()));
    }

    @Test
    void shouldSearchServicesByCategoryAndType() {
        given()
                .queryParam("category", Category.BELEZA.name())
                .queryParam("serviceType", ServiceType.CABELEIREIRO.name())
        .when()
                .get("/search/services")
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data", is(notNullValue()));
    }

    @Test
    void shouldSearchProfessionals() {
        given()
        .when()
                .get("/search/professionals")
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data", is(notNullValue()))
                .body("data.content", is(notNullValue()))
                .body("data.content", is(instanceOf(java.util.List.class)));
    }

    @Test
    void shouldSearchProfessionalsByServiceId() {
        given()
                .queryParam("serviceId", service.getId().toString())
        .when()
                .get("/search/professionals")
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data", is(notNullValue()))
                .body("data.content", is(notNullValue()))
                .body("data.content", is(instanceOf(java.util.List.class)));
    }

    @Test
    void shouldSearchProfessionalsWithDistance() {
        // Recarregar o profissional com detalhes para garantir que User e Address estão carregados
        prof = professionalRepository.findByIdWithDetails(professional.getId()).orElseThrow();
        
        // Atualizar endereço do profissional com coordenadas
        var address = prof.getUser().getAddress();
        if (address != null) {
            address.setLatitude(-23.550520);
            address.setLongitude(-46.633308);
            prof.getUser().setAddress(address);
            // Salvar o user para persistir o endereço
            userRepository.save(prof.getUser());
        }
        
        given()
                .queryParam("latitude", -23.550520)
                .queryParam("longitude", -46.633308)
        .when()
                .get("/search/professionals")
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data", is(notNullValue()))
                .body("data.content", is(notNullValue()));
    }

    @Test
    void shouldSearchProfessionalsSortedByRating() {
        given()
                .queryParam("sortBy", "rating")
        .when()
                .get("/search/professionals")
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data", is(notNullValue()))
                .body("data.content", is(notNullValue()));
    }

    @Test
    void shouldSearchProfessionalsWithPagination() {
        given()
                .queryParam("page", 0)
                .queryParam("size", 10)
        .when()
                .get("/search/professionals")
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data", is(notNullValue()))
                .body("data.content", is(notNullValue()))
                .body("data.pageable", is(notNullValue()));
                // Verifica que a paginação está presente (estrutura do Spring Data Page)
    }
}
