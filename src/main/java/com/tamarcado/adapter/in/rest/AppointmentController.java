package com.tamarcado.adapter.in.rest;

import com.tamarcado.application.service.AppointmentService;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(ApiConstants.APPOINTMENTS_PATH)
@RequiredArgsConstructor
@Tag(name = "Agendamentos", description = "Endpoints para gerenciamento de agendamentos")
@SecurityRequirement(name = "bearerAuth")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @Operation(summary = "Criar novo agendamento",
               description = "Cria um novo agendamento para o cliente autenticado")
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request
    ) {
        log.debug("Criando novo agendamento");

        AppointmentResponse appointment = appointmentService.createAppointment(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(appointment, "Agendamento criado com sucesso"));
    }

    @GetMapping("/client")
    @Operation(summary = "Listar agendamentos do cliente",
               description = "Lista todos os agendamentos do cliente autenticado, opcionalmente filtrado por status")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getAppointmentsByClient(
            @Parameter(description = "Status do agendamento (PENDING, ACCEPTED, REJECTED, COMPLETED, CANCELLED)")
            @RequestParam(required = false) AppointmentStatus status
    ) {
        log.debug("Listando agendamentos do cliente com status: {}", status);

        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByClient(status);

        return ResponseEntity.ok(
                ApiResponse.success(appointments, "Agendamentos encontrados com sucesso")
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar agendamento por ID",
               description = "Retorna os detalhes de um agendamento específico do cliente autenticado")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointmentById(
            @Parameter(description = "ID do agendamento")
            @PathVariable UUID id
    ) {
        log.debug("Buscando agendamento: {}", id);

        AppointmentResponse appointment = appointmentService.getAppointmentById(id);

        return ResponseEntity.ok(
                ApiResponse.success(appointment, "Agendamento encontrado com sucesso")
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar agendamento",
               description = "Cancela um agendamento do cliente autenticado (apenas se status for PENDING)")
    public ResponseEntity<ApiResponse<String>> cancelAppointment(
            @Parameter(description = "ID do agendamento")
            @PathVariable UUID id
    ) {
        log.debug("Cancelando agendamento: {}", id);

        appointmentService.cancelAppointment(id);

        return ResponseEntity.ok(
                ApiResponse.success("Agendamento cancelado com sucesso", "Agendamento cancelado com sucesso")
        );
    }

    @GetMapping("/professional")
    @Operation(summary = "Listar agendamentos do profissional",
               description = "Lista todos os agendamentos do profissional autenticado, opcionalmente filtrado por status. Inclui distância até o cliente.")
    public ResponseEntity<ApiResponse<List<AppointmentProfessionalResponse>>> getAppointmentsByProfessional(
            @Parameter(description = "Status do agendamento (PENDING, ACCEPTED, REJECTED, COMPLETED, CANCELLED)")
            @RequestParam(required = false) AppointmentStatus status
    ) {
        log.debug("Listando agendamentos do profissional com status: {}", status);

        List<AppointmentProfessionalResponse> appointments = appointmentService.getAppointmentsByProfessional(status);

        return ResponseEntity.ok(
                ApiResponse.success(appointments, "Agendamentos encontrados com sucesso")
        );
    }

    @PutMapping("/{id}/accept")
    @Operation(summary = "Aceitar agendamento",
               description = "Aceita um agendamento pendente do profissional autenticado")
    public ResponseEntity<ApiResponse<String>> acceptAppointment(
            @Parameter(description = "ID do agendamento")
            @PathVariable UUID id
    ) {
        log.debug("Aceitando agendamento: {}", id);

        appointmentService.acceptAppointment(id);

        return ResponseEntity.ok(
                ApiResponse.success("Agendamento aceito com sucesso", "Agendamento aceito com sucesso")
        );
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Rejeitar agendamento",
               description = "Rejeita um agendamento pendente do profissional autenticado")
    public ResponseEntity<ApiResponse<String>> rejectAppointment(
            @Parameter(description = "ID do agendamento")
            @PathVariable UUID id
    ) {
        log.debug("Rejeitando agendamento: {}", id);

        appointmentService.rejectAppointment(id);

        return ResponseEntity.ok(
                ApiResponse.success("Agendamento rejeitado com sucesso", "Agendamento rejeitado com sucesso")
        );
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "Completar agendamento",
               description = "Marca um agendamento aceito como completado pelo profissional autenticado")
    public ResponseEntity<ApiResponse<String>> completeAppointment(
            @Parameter(description = "ID do agendamento")
            @PathVariable UUID id
    ) {
        log.debug("Completando agendamento: {}", id);

        appointmentService.completeAppointment(id);

        return ResponseEntity.ok(
                ApiResponse.success("Agendamento completado com sucesso", "Agendamento completado com sucesso")
        );
    }
}
