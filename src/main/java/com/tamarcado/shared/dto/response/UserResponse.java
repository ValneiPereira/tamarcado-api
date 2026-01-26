package com.tamarcado.shared.dto.response;

import com.tamarcado.domain.model.user.UserType;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        String phone,
        String photo,
        UserType userType,
        AddressResponse address
) {}