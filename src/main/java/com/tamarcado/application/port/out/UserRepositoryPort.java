package com.tamarcado.application.port.out;

import com.tamarcado.domain.model.user.User;
import com.tamarcado.domain.model.user.UserType;

import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findClientById(UUID id);

    Optional<User> findProfessionalById(UUID id);
}