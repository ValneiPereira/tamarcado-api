package com.tamarcado.adapter.in.rest.contract;

import com.tamarcado.shared.constant.ApiConstants;
import com.tamarcado.shared.dto.request.RegisterDeviceRequest;
import com.tamarcado.shared.dto.response.ApiResponse;
import com.tamarcado.shared.dto.response.NotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Notificações", description = "Endpoints para gerenciamento de notificações e device tokens")
@RequestMapping(ApiConstants.NOTIFICATIONS_PATH)
@SecurityRequirement(name = "bearerAuth")
public interface NotificationControllerApi {

    @PostMapping("/register-device")
    @Operation(summary = "Registrar device token", description = "Registra um device token para receber notificações push")
    ResponseEntity<ApiResponse<String>> registerDevice(@Valid @RequestBody RegisterDeviceRequest request);

    @GetMapping
    @Operation(summary = "Listar notificações", description = "Lista todas as notificações do usuário autenticado, opcionalmente filtrado por não lidas")
    ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
            @Parameter(description = "Filtrar apenas notificações não lidas") @RequestParam(required = false) Boolean unreadOnly);

    @PutMapping("/{id}/read")
    @Operation(summary = "Marcar notificação como lida", description = "Marca uma notificação específica como lida")
    ResponseEntity<ApiResponse<String>> markAsRead(@Parameter(description = "ID da notificação") @PathVariable UUID id);

    @DeleteMapping("/device/{deviceToken}")
    @Operation(summary = "Remover device token", description = "Remove um device token do usuário autenticado")
    ResponseEntity<ApiResponse<String>> deleteDeviceToken(
            @Parameter(description = "Device token a ser removido") @PathVariable String deviceToken);
}
