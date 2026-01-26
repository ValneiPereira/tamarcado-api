package com.tamarcado.adapter.in.rest.contract;

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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Profissionais", description = "Endpoints para gerenciamento de profissionais e seus serviços")
@RequestMapping(ApiConstants.PROFESSIONALS_PATH)
public interface ProfessionalControllerApi {

    @GetMapping("/{id}")
    @Operation(summary = "Obter detalhes do profissional",
        description = "Retorna informações completas do profissional incluindo serviços, avaliações e distância (se coordenadas fornecidas)")
    ResponseEntity<ApiResponse<ProfessionalDetailResponse>> getProfessional(
            @Parameter(description = "ID do profissional") @PathVariable UUID id,
            @Parameter(description = "Latitude para cálculo de distância (opcional)") @RequestParam(required = false) Double latitude,
            @Parameter(description = "Longitude para cálculo de distância (opcional)") @RequestParam(required = false) Double longitude);

    @GetMapping("/me/services")
    @Operation(summary = "Listar serviços do profissional autenticado", description = "Retorna todos os serviços ativos do profissional autenticado")
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<ApiResponse<List<ServiceResponse>>> getMyServices();

    @PostMapping("/me/services")
    @Operation(summary = "Criar novo serviço", description = "Cria um novo serviço para o profissional autenticado")
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<ApiResponse<ServiceResponse>> createService(
            @Valid @RequestBody CreateServiceRequest request);

    @PutMapping("/me/services/{serviceId}")
    @Operation(summary = "Atualizar serviço", description = "Atualiza um serviço do profissional autenticado")
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<ApiResponse<ServiceResponse>> updateService(
            @Parameter(description = "ID do serviço") @PathVariable UUID serviceId,
            @Valid @RequestBody UpdateServiceRequest request);

    @DeleteMapping("/me/services/{serviceId}")
    @Operation(summary = "Deletar serviço", description = "Deleta (desativa) um serviço do profissional autenticado. Não permite deletar se houver agendamentos ativos.")
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<ApiResponse<Void>> deleteService(
            @Parameter(description = "ID do serviço") @PathVariable UUID serviceId);
}
