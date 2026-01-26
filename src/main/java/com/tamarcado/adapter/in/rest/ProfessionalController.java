package com.tamarcado.adapter.in.rest;

import com.tamarcado.adapter.in.rest.contract.ProfessionalControllerApi;
import com.tamarcado.application.service.ProfessionalService;
import com.tamarcado.shared.dto.request.CreateServiceRequest;
import com.tamarcado.shared.dto.request.UpdateServiceRequest;
import com.tamarcado.shared.dto.response.ApiResponse;
import com.tamarcado.shared.dto.response.ProfessionalDetailResponse;
import com.tamarcado.shared.dto.response.ServiceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ProfessionalController implements ProfessionalControllerApi {

    private final ProfessionalService professionalService;

    @Override
    public ResponseEntity<ApiResponse<ProfessionalDetailResponse>> getProfessional(UUID id, Double latitude,
        Double longitude) {

        log.debug("Buscando profissional: {}", id);
        var professional = professionalService.getProfessionalById(id, latitude, longitude);
        return ResponseEntity.ok(ApiResponse.success(professional, "Profissional encontrado com sucesso"));
    }

    @Override
    public ResponseEntity<ApiResponse<List<ServiceResponse>>> getMyServices() {

        log.debug("Buscando serviços do profissional autenticado");
        var services = professionalService.getMyServices();
        return ResponseEntity.ok(ApiResponse.success(services, "Serviços encontrados com sucesso"));
    }

    @Override
    public ResponseEntity<ApiResponse<ServiceResponse>> createService(CreateServiceRequest request) {

        log.debug("Criando novo serviço para profissional autenticado");
        var service = professionalService.createService(request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(service, "Serviço criado com sucesso"));
    }

    @Override
    public ResponseEntity<ApiResponse<ServiceResponse>> updateService(UUID serviceId,
        UpdateServiceRequest request) {

        log.debug("Atualizando serviço {} do profissional autenticado", serviceId);
        var service = professionalService.updateService(serviceId, request);
        return ResponseEntity.ok(ApiResponse.success(service, "Serviço atualizado com sucesso"));
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> deleteService(UUID serviceId) {

        log.debug("Deletando serviço {} do profissional autenticado", serviceId);
        professionalService.deleteService(serviceId);
        return ResponseEntity.ok(ApiResponse.success(null, "Serviço deletado com sucesso"));
    }
}
