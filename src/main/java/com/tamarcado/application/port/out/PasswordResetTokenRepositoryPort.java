package com.tamarcado.application.port.out;

import com.tamarcado.domain.model.user.PasswordResetToken;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepositoryPort {

    PasswordResetToken save(PasswordResetToken token);

    Optional<PasswordResetToken> findByCode(String code);

    void deleteByUserId(UUID userId);
}
