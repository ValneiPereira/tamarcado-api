package com.tamarcado.application.service;

import com.tamarcado.application.port.out.UserRepositoryPort;
import com.tamarcado.domain.model.user.User;
import com.tamarcado.infrastructure.security.SecurityUtils;
import com.tamarcado.shared.constant.ErrorMessages;
import com.tamarcado.shared.dto.request.ChangePasswordRequest;
import com.tamarcado.shared.dto.request.UpdateUserRequest;
import com.tamarcado.shared.dto.response.UserResponse;
import com.tamarcado.shared.exception.BusinessException;
import com.tamarcado.shared.exception.ResourceNotFoundException;
import com.tamarcado.shared.mapper.UserDtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDtoMapper userDtoMapper;

    /**
     * Obtém o perfil do usuário autenticado
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        String email = SecurityUtils.getCurrentUsername();
        
        if (email == null) {
            throw new BusinessException("Usuário não autenticado");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        return userDtoMapper.toResponse(user);
    }

    /**
     * Atualiza o perfil do usuário autenticado
     */
    @Transactional
    public UserResponse updateCurrentUser(UpdateUserRequest request) {
        String email = SecurityUtils.getCurrentUsername();
        
        if (email == null) {
            throw new BusinessException("Usuário não autenticado");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // Verificar se o email já está em uso por outro usuário
        if (!user.getEmail().equals(request.email())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new BusinessException(ErrorMessages.USER_ALREADY_EXISTS);
            }
        }

        // Atualizar dados
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPhone(request.phone());

        User updatedUser = userRepository.save(user);
        log.info("Perfil do usuário {} atualizado", updatedUser.getId());

        return userDtoMapper.toResponse(updatedUser);
    }

    /**
     * Altera a senha do usuário autenticado
     */
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        String email = SecurityUtils.getCurrentUsername();
        
        if (email == null) {
            throw new BusinessException("Usuário não autenticado");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // Verificar senha atual
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessException("Senha atual incorreta");
        }

        // Verificar se as novas senhas coincidem
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BusinessException("Nova senha e confirmação não coincidem");
        }

        // Validar que a nova senha é diferente da atual
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BusinessException("A nova senha deve ser diferente da senha atual");
        }

        // Atualizar senha
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        
        log.info("Senha do usuário {} alterada com sucesso", user.getId());
    }

    /**
     * Desativa a conta do usuário autenticado (soft delete)
     */
    @Transactional
    public void deleteCurrentUser() {
        String email = SecurityUtils.getCurrentUsername();
        
        if (email == null) {
            throw new BusinessException("Usuário não autenticado");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // Soft delete - desativar conta
        user.setActive(false);
        userRepository.save(user);
        
        log.info("Conta do usuário {} desativada", user.getId());
    }

    /**
     * Verifica se o usuário tem permissão para acessar um recurso
     */
    public void validateUserAccess(UUID userId) {
        String email = SecurityUtils.getCurrentUsername();
        
        if (email == null) {
            throw new BusinessException("Usuário não autenticado");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (!user.getId().equals(userId)) {
            throw new BusinessException("Acesso negado. Você só pode acessar seus próprios recursos.");
        }
    }

}
