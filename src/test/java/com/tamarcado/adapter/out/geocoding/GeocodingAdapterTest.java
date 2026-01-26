package com.tamarcado.adapter.out.geocoding;

import com.tamarcado.shared.dto.response.CoordinatesResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GeocodingAdapterTest {

    private GeocodingAdapter geocodingAdapter;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        geocodingAdapter = new GeocodingAdapter(builder);

        ReflectionTestUtils.setField(geocodingAdapter, "nominatimBaseUrl", "https://nominatim.openstreetmap.org");
    }

    @Test
    void shouldReturnCoordinatesFromNominatim() {
        // Arrange
        String street = "Av. Paulista";
        String number = "1000";
        String city = "São Paulo";
        String state = "SP";
        String cep = "01310-100";

        String responseJson = """
                [
                    {
                        "lat": "-23.561414",
                        "lon": "-46.6558819",
                        "display_name": "São Paulo, SP, Brasil"
                    }
                ]
                """;

        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("q=S%C3%A3o%20Paulo%20SP%20Brasil")))
                .andExpect(header("User-Agent", "tamarcado-api/1.0 (contato@tamarcado.ia.br)"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        // Act
        CoordinatesResponse response = geocodingAdapter.addressToCoordinates(street, number, city, state, cep);

        // Assert
        assertNotNull(response);
        assertEquals(-23.561414, response.latitude());
        assertEquals(-46.6558819, response.longitude());
        mockServer.verify();
    }

    @Test
    void shouldReturnEmptyCoordinatesWhenNominatimReturnsNoResults() {
        // Arrange
        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("nominatim.openstreetmap.org/search")))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        // Act
        CoordinatesResponse response = geocodingAdapter.addressToCoordinates("Rua Inexistente", "0",
                "Vila de lugar nenhum", "XX", "00000-000");

        // Assert
        assertNotNull(response);
        assertNull(response.latitude());
        assertNull(response.longitude());
    }
}
