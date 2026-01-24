package com.tamarcado.application.service;

import com.tamarcado.application.port.out.DeviceTokenRepositoryPort;
import com.tamarcado.application.port.out.NotificationRepositoryPort;
import com.tamarcado.application.port.out.UserRepositoryPort;
import com.tamarcado.domain.model.notification.DeviceToken;
import com.tamarcado.domain.model.notification.Notification;
import com.tamarcado.domain.model.notification.NotificationType;
import com.tamarcado.domain.model.user.User;
import com.tamarcado.infrastructure.security.SecurityUtils;
import com.tamarcado.shared.dto.request.RegisterDeviceRequest;
import com.tamarcado.shared.dto.response.NotificationResponse;
import com.tamarcado.shared.exception.BusinessException;
import com.tamarcado.shared.exception.ResourceNotFoundException;
import com.tamarcado.shared.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepositoryPort notificationRepository;
    private final DeviceTokenRepositoryPort deviceTokenRepository;
    private final UserRepositoryPort userRepository;
    private final NotificationMapper notificationMapper;

    /**
     * Registra um device token para receber notificações push
     */
    @Transactional
    public void registerDevice(RegisterDeviceRequest request) {
        log.debug("Registrando device token para usuário autenticado");

        String email = SecurityUtils.getCurrentUsername();
        if (email == null) {
            throw new BusinessException("Usuário não autenticado");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // Verificar se já existe o device token para este usuário
        DeviceToken existingToken = deviceTokenRepository
                .findByUserIdAndDeviceToken(user.getId(), request.deviceToken())
                .orElse(null);

        if (existingToken != null) {
            // Atualizar platform se necessário
            if (!existingToken.getPlatform().equals(request.platform())) {
                existingToken.setPlatform(request.platform());
                deviceTokenRepository.save(existingToken);
                log.info("Platform do device token {} atualizado para {}", request.deviceToken(), request.platform());
            } else {
                log.debug("Device token já registrado para o usuário {}", user.getId());
            }
        } else {
            // Criar novo device token
            DeviceToken deviceToken = DeviceToken.builder()
                    .user(user)
                    .deviceToken(request.deviceToken())
                    .platform(request.platform())
                    .build();

            deviceTokenRepository.save(deviceToken);
            log.info("Device token registrado com sucesso para usuário {}", user.getId());
        }
    }

    /**
     * Lista notificações do usuário autenticado
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Boolean unreadOnly) {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) {
            throw new BusinessException("Usuário não autenticado");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        List<Notification> notifications;
        if (unreadOnly != null && unreadOnly) {
            notifications = notificationRepository.findByUserIdAndIsRead(user.getId(), false);
        } else {
            notifications = notificationRepository.findByUserId(user.getId());
        }

        return notificationMapper.toResponseList(notifications);
    }

    /**
     * Marca uma notificação como lida
     */
    @Transactional
    public void markAsRead(UUID notificationId) {
        log.debug("Marcando notificação {} como lida", notificationId);

        // Validar que o usuário autenticado é o dono da notificação
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) {
            throw new BusinessException("Usuário não autenticado");
        }

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // Obter o userId da notificação sem carregar a entidade completa (evita problemas com JSON converter)
        UUID notificationUserId = notificationRepository.findUserIdByNotificationId(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificação não encontrada"));

        if (!notificationUserId.equals(currentUser.getId())) {
            throw new BusinessException("Acesso negado. Você só pode marcar suas próprias notificações como lidas.");
        }

        // Atualizar diretamente no banco sem carregar a entidade completa (evita problemas com JSON converter)
        notificationRepository.markAsReadById(notificationId);

        log.info("Notificação {} marcada como lida pelo usuário {}", notificationId, currentUser.getId());
    }

    /**
     * Remove um device token
     */
    @Transactional
    public void deleteDeviceToken(String deviceToken) {
        log.debug("Removendo device token: {}", deviceToken);

        String email = SecurityUtils.getCurrentUsername();
        if (email == null) {
            throw new BusinessException("Usuário não autenticado");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        DeviceToken token = deviceTokenRepository
                .findByUserIdAndDeviceToken(user.getId(), deviceToken)
                .orElseThrow(() -> new ResourceNotFoundException("Device token não encontrado"));

        deviceTokenRepository.delete(token);
        log.info("Device token {} removido para usuário {}", deviceToken, user.getId());
    }

    /**
     * Envia uma notificação para um usuário (salva no banco)
     * TODO: Integrar com Firebase Cloud Messaging para envio push
     */
    @Transactional
    public Notification sendNotification(
            UUID userId,
            NotificationType type,
            String title,
            String message,
            Map<String, Object> data) {
        log.debug("Enviando notificação do tipo {} para usuário {}", type, userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        Notification notification = notificationMapper.toEntity(user, type, title, message, data);
        Notification savedNotification = notificationRepository.save(notification);

        log.info("Notificação {} criada para usuário {}", savedNotification.getId(), userId);

        // TODO: Enviar push notification via Firebase Cloud Messaging
        // sendPushNotification(userId, type, title, message, data);

        return savedNotification;
    }

    /**
     * Conta notificações não lidas do usuário autenticado
     */
    @Transactional(readOnly = true)
    public long countUnreadNotifications() {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) {
            throw new BusinessException("Usuário não autenticado");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }
}
