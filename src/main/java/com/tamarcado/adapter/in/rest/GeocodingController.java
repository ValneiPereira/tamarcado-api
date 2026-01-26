package com.tamarcado.adapter.in.rest;

import com.tamarcado.adapter.in.rest.contract.GeocodingControllerApi;
import com.tamarcado.application.service.GeocodingService;
import com.tamarcado.shared.dto.request.AddressToCoordsRequest;
import com.tamarcado.shared.dto.request.CepRequest;
import com.tamarcado.shared.dto.response.AddressResponse;
import com.tamarcado.shared.dto.response.ApiResponse;
import com.tamarcado.shared.dto.response.CoordinatesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GeocodingController implements GeocodingControllerApi {

    private final GeocodingService geocodingService;

    @Override
    public ResponseEntity<ApiResponse<CoordinatesResponse>> addressToCoordinates(AddressToCoordsRequest request) {

        log.info("Recebida requisição de conversão de endereço para coordenadas: {}", request.cep());
        var response = geocodingService.addressToCoordinates(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Endereço convertido com sucesso"));
    }

    @Override
    public ResponseEntity<ApiResponse<AddressResponse>> cepToAddress(CepRequest request) {

        log.info("Recebida requisição de busca de endereço por CEP: {}", request.cep());
        var response = geocodingService.cepToAddress(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Endereço encontrado com sucesso"));
    }
}
