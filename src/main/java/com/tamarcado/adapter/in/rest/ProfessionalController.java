package com.tamarcado.adapter.in.rest;

import com.tamarcado.application.service.ProfessionalService;
import com.tamarcado.shared.constant.ApiConstants;
import com.tamarcado.shared.dto.request.CreateServiceRequest;
import com.tamarcado.shared.dto.request.UpdateServiceRequest;
import com.tamarcado.shared.dto.response.ApiResponse;
import com.tamarcado.shared.dto.response.ProfessionalDetailResponse;
import com.tamarcado.shared.dto.response.ServiceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(ApiConstants.PROFESSIONALS_PATH)
@RequiredArgsConstructor
@Tag(name = "Profissionais", description = "Endpoints para gerenciamento de profissionais e seus serviços")
public class ProfessionalController {

    private final ProfessionalService professionalService;

    @GetMapping("/{id}")
    @Operation(summary = "Obter detalhes do profissional", 
               description = "Retorna informações completas do profissional incluindo serviços, avaliações e distância (se coordenadas fornecidas)")
    public ResponseEntity<ApiResponse<ProfessionalDetailResponse>> getProfessional(
            @Parameter(description = "ID do profissional")
            @PathVariable UUID id,
            
            @Parameter(description = "Latitude para cálculo de distância (opcional)")
            @RequestParam(required = false) Double latitude,
            
            @Parameter(description = "Longitude para cálculo de distância (opcional)")
            @RequestParam(required = false) Double longitude
    ) {
        log.debug("Buscando profissional: {}", id);
        
        ProfessionalDetailResponse professional = professionalService.getProfessionalById(id, latitude, longitude);
        
        return ResponseEntity.ok(
                ApiResponse.success(professional, "Profissional encontrado com sucesso")
        );
    }

    @GetMapping("/me/services")
    @Operation(summary = "Listar serviços do profissional autenticado", 
               description = "Retorna todos os serviços ativos do profissional autenticado")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<ServiceResponse>>> getMyServices() {
        log.debug("Buscando serviços do profissional autenticado");
        
        List<ServiceResponse> services = professionalService.getMyServices();
        
        return ResponseEntity.ok(
                ApiResponse.success(services, "Serviços encontrados com sucesso")
        );
    }

    @PostMapping("/me/services")
    @Operation(summary = "Criar novo serviço", 
               description = "Cria um novo serviço para o profissional autenticado")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<ServiceResponse>> createService(
            @Valid @RequestBody CreateServiceRequest request
    ) {
        log.debug("Criando novo serviço para profissional autenticado");
        
        ServiceResponse service = professionalService.createService(request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(service, "Serviço criado com sucesso"));
    }

    @PutMapping("/me/services/{serviceId}")
    @Operation(summary = "Atualizar serviço", 
               description = "Atualiza um serviço do profissional autenticado")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<ServiceResponse>> updateService(
            @Parameter(description = "ID do serviço")
            @PathVariable UUID serviceId,
            
            @Valid @RequestBody UpdateServiceRequest request
    ) {
        log.debug("Atualizando serviço {} do profissional autenticado", serviceId);
        
        ServiceResponse service = professionalService.updateService(serviceId, request);
        
        return ResponseEntity.ok(
                ApiResponse.success(service, "Serviço atualizado com sucesso")
        );
    }

    @DeleteMapping("/me/services/{serviceId}")
    @Operation(summary = "Deletar serviço", 
               description = "Deleta (desativa) um serviço do profissional autenticado. Não permite deletar se houver agendamentos ativos.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> deleteService(
            @Parameter(description = "ID do serviço")
            @PathVariable UUID serviceId
    ) {
        log.debug("Deletando serviço {} do profissional autenticado", serviceId);
        
        professionalService.deleteService(serviceId);
        
        return ResponseEntity.ok(
                ApiResponse.success(null, "Serviço deletado com sucesso")
        );
    }
}
