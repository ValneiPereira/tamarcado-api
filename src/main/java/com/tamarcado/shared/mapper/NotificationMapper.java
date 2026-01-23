package com.tamarcado.shared.mapper;

import com.tamarcado.domain.model.notification.Notification;
import com.tamarcado.domain.model.notification.NotificationType;
import com.tamarcado.domain.model.user.User;
import com.tamarcado.shared.dto.response.NotificationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponse toResponse(Notification notification);

    List<NotificationResponse> toResponseList(List<Notification> notifications);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "message", source = "message")
    @Mapping(target = "data", expression = "java(data != null ? data : new java.util.HashMap<>())")
    @Mapping(target = "isRead", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    Notification toEntity(User user, NotificationType type, String title, String message, Map<String, Object> data);
}
