package com.tamarcado.adapter.out.geocoding;

import com.tamarcado.adapter.out.geocoding.dto.NominatimResponse;
import com.tamarcado.adapter.out.geocoding.dto.ViaCepResponse;
import com.tamarcado.application.port.out.GeocodingPort;
import com.tamarcado.shared.dto.response.AddressResponse;
import com.tamarcado.shared.dto.response.CoordinatesResponse;
import com.tamarcado.shared.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.core.ParameterizedTypeReference;

@Slf4j
@Component
public class GeocodingAdapter implements GeocodingPort {

    public static final String COUNTRY_CODES_BR = "/search?format=json&q={q}&limit=1&countrycodes=br";
    public static final String USER_AGENT = "User-Agent";
    public static final String TAMARCADO_IA_BR = "tamarcado-api/1.0 (contato@tamarcado.ia.br)";
    private final RestClient restClient;

    @Value("${geocoding.api.viacep.base-url}")
    private String viaCepBaseUrl;

    @Value("${geocoding.api.nominatim.base-url}")
    private String nominatimBaseUrl;

    public GeocodingAdapter(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @Override
    public CoordinatesResponse addressToCoordinates(String street, String number, String city, String state,
            String cep) {
        log.debug("Convertendo endereço para coordenadas: {}, {}", city, state);
        return addressToCoordinatesNominatim(city, state);
    }

    @Override
    public AddressResponse cepToAddress(String cep) {

        log.debug("Buscando endereço por CEP: {}", cep);
        var cleanCep = cep.replaceAll("[^0-9]", "");

        try {

            var baseUrl = viaCepBaseUrl.endsWith("/") ? viaCepBaseUrl.substring(0, viaCepBaseUrl.length() - 1) : viaCepBaseUrl;
            var uri = String.format("%s/%s/json/", baseUrl, cleanCep);

            log.debug("Buscando CEP na URL: {}", uri);
            ViaCepResponse viaCepResponse = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(ViaCepResponse.class);

            if (viaCepResponse == null) {
                throw new BusinessException("Erro ao buscar endereço por CEP");
            }

            if (viaCepResponse.erro()) {
                log.warn("CEP não encontrado: {}", cep);
                throw new BusinessException("CEP não encontrado: " + cep);
            }

            return new AddressResponse(
                    viaCepResponse.cep(),
                    viaCepResponse.logradouro(),
                    null, // number - ViaCEP não fornece número
                    viaCepResponse.complemento(), // complement - ViaCEP pode fornecer
                    viaCepResponse.bairro(),
                    viaCepResponse.localidade(),
                    viaCepResponse.uf(),
                    null, // latitude - ViaCEP não fornece coordenadas
                    null // longitude - ViaCEP não fornece coordenadas
            );
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao buscar endereço por CEP: {}", cep, e);
            throw new BusinessException("Erro ao buscar endereço por CEP: " + e.getMessage());
        }
    }

    private CoordinatesResponse addressToCoordinatesNominatim(String city, String state) {
        try {

            var query = String.format("%s %s Brasil", city, state);

            log.debug("Buscando coordenadas no Nominatim (Simplificado): {}", query);
            var responseType = new ParameterizedTypeReference<java.util.List<NominatimResponse>>() {
            };

            var results = restClient.get()
                    .uri(nominatimBaseUrl + COUNTRY_CODES_BR, query)
                    .header(USER_AGENT, TAMARCADO_IA_BR)
                    .retrieve()
                    .body(responseType);

            if (results == null || results.isEmpty()) {

                log.warn("Nenhum resultado encontrado no Nominatim para o endereço: {}", query);
                return new CoordinatesResponse(null, null);
            }

            var firstResult = results.get(0);

            log.info("Nominatim encontrou coordenadas para {}: lat={}, lon={}", query, firstResult.lat(), firstResult.lon());
            return new CoordinatesResponse(
                    Double.parseDouble(firstResult.lat()),
                    Double.parseDouble(firstResult.lon()));
        } catch (Exception e) {
            log.error("Erro ao converter endereço para coordenadas no Nominatim: {}, {}", city, state, e);
            throw new BusinessException("Erro ao converter endereço para coordenadas (OSM): " + e.getMessage());
        }
    }
}
