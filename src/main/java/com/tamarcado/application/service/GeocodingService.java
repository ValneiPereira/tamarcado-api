package com.tamarcado.application.service;

import com.tamarcado.application.port.out.GeocodingPort;
import com.tamarcado.shared.dto.request.AddressToCoordsRequest;
import com.tamarcado.shared.dto.request.CepRequest;
import com.tamarcado.shared.dto.response.AddressResponse;
import com.tamarcado.shared.dto.response.CoordinatesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeocodingService {

    private final GeocodingPort geocodingPort;

    @Cacheable(value = "geocoding:coordinates", key = "#request.street() + '|' + #request.number() + '|' + #request.city() + '|' + #request.state() + '|' + #request.cep()")
    public CoordinatesResponse addressToCoordinates(AddressToCoordsRequest request) {
        log.info("Convertendo endereço para coordenadas: {}, {}, {}, {}, {}", 
                request.street(), request.number(), request.city(), request.state(), request.cep());

        return geocodingPort.addressToCoordinates(
                request.street(),
                request.number(),
                request.city(),
                request.state(),
                request.cep()
        );
    }

    @Cacheable(value = "geocoding:address", key = "#request.cep()")
    public AddressResponse cepToAddress(CepRequest request) {
        log.info("Buscando endereço por CEP: {}", request.cep());

        return geocodingPort.cepToAddress(request.cep());
    }
}
