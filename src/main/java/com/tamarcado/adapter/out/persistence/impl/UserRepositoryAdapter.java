package com.tamarcado.adapter.out.persistence.impl;

import com.tamarcado.adapter.out.persistence.jpa.UserJpaRepository;
import com.tamarcado.application.port.out.UserRepositoryPort;
import com.tamarcado.domain.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository jpaRepository;

    @Override
    public User save(User user) {
        return jpaRepository.save(user);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public Optional<User> findClientById(UUID id) {
        return jpaRepository.findClientById(id);
    }

    @Override
    public Optional<User> findProfessionalById(UUID id) {
        return jpaRepository.findProfessionalById(id);
    }
}