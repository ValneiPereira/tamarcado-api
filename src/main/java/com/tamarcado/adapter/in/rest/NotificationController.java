package com.tamarcado.adapter.in.rest;

import com.tamarcado.application.service.NotificationService;
import com.tamarcado.shared.constant.ApiConstants;
import com.tamarcado.shared.dto.request.RegisterDeviceRequest;
import com.tamarcado.shared.dto.response.ApiResponse;
import com.tamarcado.shared.dto.response.NotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(ApiConstants.NOTIFICATIONS_PATH)
@RequiredArgsConstructor
@Tag(name = "Notificações", description = "Endpoints para gerenciamento de notificações e device tokens")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/register-device")
    @Operation(summary = "Registrar device token", description = "Registra um device token para receber notificações push")
    public ResponseEntity<ApiResponse<String>> registerDevice(
        @Valid @RequestBody RegisterDeviceRequest request) {
        log.debug("Registrando device token");

        notificationService.registerDevice(request);

        return ResponseEntity.ok(ApiResponse.success(
            "Device token registrado com sucesso", "Device token registrado com sucesso"));
    }

    @GetMapping
    @Operation(summary = "Listar notificações", description = "Lista todas as notificações do usuário autenticado, opcionalmente filtrado por não lidas")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
        @Parameter(description = "Filtrar apenas notificações não lidas")
        @RequestParam(required = false) Boolean unreadOnly) {
        log.debug("Listando notificações com unreadOnly: {}", unreadOnly);

        List<NotificationResponse> notifications = notificationService.getNotifications(unreadOnly);

        return ResponseEntity.ok(ApiResponse.success(notifications, "Notificações encontradas com sucesso"));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Marcar notificação como lida", description = "Marca uma notificação específica como lida")
    public ResponseEntity<ApiResponse<String>> markAsRead(
        @Parameter(description = "ID da notificação")
        @PathVariable UUID id) {
        log.debug("Marcando notificação {} como lida", id);

        notificationService.markAsRead(id);

        return ResponseEntity.ok(
            ApiResponse.success("Notificação marcada como lida", "Notificação marcada como lida")
        );
    }

    @DeleteMapping("/device/{deviceToken}")
    @Operation(summary = "Remover device token", description = "Remove um device token do usuário autenticado")
    public ResponseEntity<ApiResponse<String>> deleteDeviceToken(
        @Parameter(description = "Device token a ser removido")
        @PathVariable String deviceToken) {
        log.debug("Removendo device token: {}", deviceToken);

        notificationService.deleteDeviceToken(deviceToken);

        return ResponseEntity.ok(ApiResponse.success(
            "Device token removido com sucesso", "Device token removido com sucesso"));
    }
}
