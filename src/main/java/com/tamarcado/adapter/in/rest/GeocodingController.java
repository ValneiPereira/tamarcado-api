package com.tamarcado.adapter.in.rest;

import com.tamarcado.application.service.GeocodingService;
import com.tamarcado.shared.constant.ApiConstants;
import com.tamarcado.shared.dto.request.AddressToCoordsRequest;
import com.tamarcado.shared.dto.request.CepRequest;
import com.tamarcado.shared.dto.response.AddressResponse;
import com.tamarcado.shared.dto.response.ApiResponse;
import com.tamarcado.shared.dto.response.CoordinatesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(ApiConstants.GEOCODING_PATH)
@RequiredArgsConstructor
@Tag(name = "Geocoding", description = "Endpoints de geocoding e busca de endereços")
public class GeocodingController {

    private final GeocodingService geocodingService;

    @PostMapping("/address-to-coords")
    @Operation(summary = "Converter endereço para coordenadas", description = "Converte um endereço completo em coordenadas (latitude, longitude)")
    public ResponseEntity<ApiResponse<CoordinatesResponse>> addressToCoordinates(
            @Valid @RequestBody AddressToCoordsRequest request
    ) {
        log.info("Recebida requisição de conversão de endereço para coordenadas: {}", request.cep());

        CoordinatesResponse response = geocodingService.addressToCoordinates(request);

        return ResponseEntity.ok(ApiResponse.success(response, "Endereço convertido com sucesso"));
    }

    @PostMapping("/cep")
    @Operation(summary = "Buscar endereço por CEP", description = "Busca informações completas de endereço através do CEP")
    public ResponseEntity<ApiResponse<AddressResponse>> cepToAddress(
            @Valid @RequestBody CepRequest request
    ) {
        log.info("Recebida requisição de busca de endereço por CEP: {}", request.cep());

        AddressResponse response = geocodingService.cepToAddress(request);

        return ResponseEntity.ok(ApiResponse.success(response, "Endereço encontrado com sucesso"));
    }
}
