package com.tamarcado.adapter.out.geocoding;

import com.tamarcado.adapter.out.geocoding.dto.GoogleGeocodeResponse;
import com.tamarcado.adapter.out.geocoding.dto.ViaCepResponse;
import com.tamarcado.application.port.out.GeocodingPort;
import com.tamarcado.shared.dto.response.AddressResponse;
import com.tamarcado.shared.dto.response.CoordinatesResponse;
import com.tamarcado.shared.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class GeocodingAdapter implements GeocodingPort {

    private final RestClient restClient;

    @Value("${geocoding.api.provider}")
    private String provider;

    @Value("${geocoding.api.viacep.base-url}")
    private String viaCepBaseUrl;

    @Value("${geocoding.api.google.base-url}")
    private String googleBaseUrl;

    @Value("${geocoding.api.google.api-key:}")
    private String googleApiKey;

    public GeocodingAdapter(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @Override
    public CoordinatesResponse addressToCoordinates(String street, String number, String city, String state, String cep) {
        log.debug("Convertendo endereço para coordenadas: {}, {}, {}, {}, {}", street, number, city, state, cep);

        if ("google".equalsIgnoreCase(provider) && !googleApiKey.isEmpty()) {
            return addressToCoordinatesGoogle(street, number, city, state, cep);
        } else {
            // ViaCEP não fornece coordenadas, retornar null
            log.warn("ViaCEP não fornece coordenadas. Configure Google Maps API para geocoding.");
            return new CoordinatesResponse(null, null);
        }
    }

    @Override
    public AddressResponse cepToAddress(String cep) {
        log.debug("Buscando endereço por CEP: {}", cep);

        String cleanCep = cep.replaceAll("[^0-9]", "");

        try {

            String baseUrl = viaCepBaseUrl.endsWith("/") ? viaCepBaseUrl.substring(0, viaCepBaseUrl.length() - 1) : viaCepBaseUrl;
            String uri = String.format("%s/%s/json/", baseUrl, cleanCep);

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
                    null  // longitude - ViaCEP não fornece coordenadas
            );
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao buscar endereço por CEP: {}", cep, e);
            throw new BusinessException("Erro ao buscar endereço por CEP: " + e.getMessage());
        }
    }

    private CoordinatesResponse addressToCoordinatesGoogle(String street, String number, String city, String state, String cep) {
        String address = String.format("%s %s, %s, %s, %s", street, number, city, state, cep);

        try {

            validateGoogleBaseUrl(googleBaseUrl);

            String baseUrl = googleBaseUrl.endsWith("/") ? googleBaseUrl.substring(0, googleBaseUrl.length() - 1) : googleBaseUrl;
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String encodedApiKey = URLEncoder.encode(googleApiKey, StandardCharsets.UTF_8);
            String uri = String.format("%s/geocode/json?address=%s&key=%s", baseUrl, encodedAddress, encodedApiKey);

            GoogleGeocodeResponse geocodeResponse = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(GoogleGeocodeResponse.class);

            if (geocodeResponse == null) {
                throw new BusinessException("Erro ao converter endereço para coordenadas");
            }

            if ("OK".equals(geocodeResponse.status()) && !geocodeResponse.results().isEmpty()) {
                GoogleGeocodeResponse.Result firstResult = geocodeResponse.results().getFirst();
                GoogleGeocodeResponse.Location location = firstResult.geometry().location();

                return new CoordinatesResponse(location.lat(), location.lng());
            }

            throw new BusinessException("Erro ao converter endereço para coordenadas");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao converter endereço para coordenadas: {}", address, e);
            throw new BusinessException("Erro ao converter endereço para coordenadas: " + e.getMessage());
        }
    }


    private void validateGoogleBaseUrl(String url) {
        try {

            URI uri = URI.create(url);
            String host = uri.getHost();
            String scheme = uri.getScheme();

            // Permitir apenas domínios do Google Maps
            if (host == null || (!host.equals("maps.googleapis.com") && !host.endsWith(".maps.googleapis.com"))) {
                throw new BusinessException("URL base do Google não é um domínio confiável: " + host);
            }

            // Verificar que é HTTPS
            if (!"https".equalsIgnoreCase(scheme)) {
                throw new BusinessException("URL base do Google deve usar HTTPS: " + url);
            }
        } catch (IllegalArgumentException e) {
            throw new BusinessException("URL base do Google inválida: " + url, e);
        }
    }
}
