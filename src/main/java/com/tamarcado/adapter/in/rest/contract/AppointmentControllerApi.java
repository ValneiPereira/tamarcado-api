package com.tamarcado.adapter.in.rest.contract;

import com.tamarcado.domain.model.appointment.AppointmentStatus;
import com.tamarcado.shared.constant.ApiConstants;
import com.tamarcado.shared.dto.request.CreateAppointmentRequest;
import com.tamarcado.shared.dto.response.ApiResponse;
import com.tamarcado.shared.dto.response.AppointmentProfessionalResponse;
import com.tamarcado.shared.dto.response.AppointmentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Agendamentos", description = "Endpoints para gerenciamento de agendamentos")
@RequestMapping(ApiConstants.APPOINTMENTS_PATH)
@SecurityRequirement(name = "bearerAuth")
public interface AppointmentControllerApi {

    @PostMapping
    @Operation(summary = "Criar novo agendamento", description = "Cria um novo agendamento para o cliente autenticado")
    ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(@Valid @RequestBody CreateAppointmentRequest request);

    @GetMapping("/client")
    @Operation(
        summary = "Listar agendamentos do cliente",
        description = "Lista todos os agendamentos do cliente autenticado, opcionalmente filtrado por status")
    ResponseEntity<ApiResponse<List<AppointmentResponse>>> getAppointmentsByClient(
            @Parameter(description = "Status do agendamento (PENDING, ACCEPTED, REJECTED, COMPLETED, CANCELLED)")
            @RequestParam(required = false) AppointmentStatus status);

    @GetMapping("/{id}")
    @Operation(summary = "Buscar agendamento por ID", description = "Retorna os detalhes de um agendamento específico do cliente autenticado")
    ResponseEntity<ApiResponse<AppointmentResponse>> getAppointmentById(@Parameter(description = "ID do agendamento") @PathVariable UUID id);

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar agendamento", description = "Cancela um agendamento do cliente autenticado (apenas se status for PENDING)")
    ResponseEntity<ApiResponse<String>> cancelAppointment(@Parameter(description = "ID do agendamento") @PathVariable UUID id);

    @GetMapping("/professional")
    @Operation(summary = "Listar agendamentos do profissional", description = "Lista todos os agendamentos do profissional autenticado, opcionalmente filtrado por status. Inclui distância até o cliente.")
    ResponseEntity<ApiResponse<List<AppointmentProfessionalResponse>>> getAppointmentsByProfessional(
            @Parameter(description = "Status do agendamento (PENDING, ACCEPTED, REJECTED, COMPLETED, CANCELLED)")
            @RequestParam(required = false) AppointmentStatus status);

    @PutMapping("/{id}/accept")
    @Operation(summary = "Aceitar agendamento", description = "Aceita um agendamento pendente do profissional autenticado")
    ResponseEntity<ApiResponse<String>> acceptAppointment(
            @Parameter(description = "ID do agendamento") @PathVariable UUID id);

    @PutMapping("/{id}/reject")
    @Operation(summary = "Rejeitar agendamento", description = "Rejeita um agendamento pendente do profissional autenticado")
    ResponseEntity<ApiResponse<String>> rejectAppointment(
            @Parameter(description = "ID do agendamento") @PathVariable UUID id);

    @PutMapping("/{id}/complete")
    @Operation(summary = "Completar agendamento", description = "Marca um agendamento aceito como completado pelo profissional autenticado")
    ResponseEntity<ApiResponse<String>> completeAppointment(
            @Parameter(description = "ID do agendamento") @PathVariable UUID id);
}
