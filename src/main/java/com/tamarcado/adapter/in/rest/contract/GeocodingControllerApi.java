package com.tamarcado.adapter.in.rest.contract;

import com.tamarcado.shared.constant.ApiConstants;
import com.tamarcado.shared.dto.request.AddressToCoordsRequest;
import com.tamarcado.shared.dto.request.CepRequest;
import com.tamarcado.shared.dto.response.AddressResponse;
import com.tamarcado.shared.dto.response.ApiResponse;
import com.tamarcado.shared.dto.response.CoordinatesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Geocoding", description = "Endpoints de geocoding e busca de endereços")
@RequestMapping(ApiConstants.GEOCODING_PATH)
public interface GeocodingControllerApi {

    @PostMapping("/address-to-coords")
    @Operation(summary = "Converter endereço para coordenadas", description = "Converte um endereço completo em coordenadas (latitude, longitude)")
    ResponseEntity<ApiResponse<CoordinatesResponse>> addressToCoordinates(
            @Valid @RequestBody AddressToCoordsRequest request);

    @PostMapping("/cep")
    @Operation(summary = "Buscar endereço por CEP", description = "Busca informações completas de endereço através do CEP")
    ResponseEntity<ApiResponse<AddressResponse>> cepToAddress(@Valid @RequestBody CepRequest request);
}
