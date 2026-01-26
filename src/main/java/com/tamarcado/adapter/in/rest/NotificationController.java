package com.tamarcado.adapter.in.rest;

import com.tamarcado.adapter.in.rest.contract.NotificationControllerApi;
import com.tamarcado.application.service.NotificationService;
import com.tamarcado.shared.dto.request.RegisterDeviceRequest;
import com.tamarcado.shared.dto.response.ApiResponse;
import com.tamarcado.shared.dto.response.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class NotificationController implements NotificationControllerApi {

    private final NotificationService notificationService;

    @Override
    public ResponseEntity<ApiResponse<String>> registerDevice(RegisterDeviceRequest request) {

        log.debug("Registrando device token");
        notificationService.registerDevice(request);

        return ResponseEntity.ok(ApiResponse.success(
                "Device token registrado com sucesso", "Device token registrado com sucesso"));
    }

    @Override
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(Boolean unreadOnly) {

        log.debug("Listando notificações com unreadOnly: {}", unreadOnly);
        List<NotificationResponse> notifications = notificationService.getNotifications(unreadOnly);

        return ResponseEntity.ok(ApiResponse.success(notifications, "Notificações encontradas com sucesso"));
    }

    @Override
    public ResponseEntity<ApiResponse<String>> markAsRead(UUID id) {

        log.debug("Marcando notificação {} como lida", id);
        notificationService.markAsRead(id);

        return ResponseEntity.ok(
                ApiResponse.success("Notificação marcada como lida", "Notificação marcada como lida"));
    }

    @Override
    public ResponseEntity<ApiResponse<String>> deleteDeviceToken(String deviceToken) {

        log.debug("Removendo device token: {}", deviceToken);
        notificationService.deleteDeviceToken(deviceToken);

        return ResponseEntity.ok(ApiResponse.success(
                "Device token removido com sucesso", "Device token removido com sucesso"));
    }
}
