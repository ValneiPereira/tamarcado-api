package com.tamarcado.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Serviço customizado para carregar detalhes do usuário
 * TODO: Implementar consulta ao banco de dados quando as entidades estiverem prontas
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    // TODO: Injeter UserRepository quando estiver pronto
    // private final UserRepositoryPort userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Carregando usuário: {}", username);

        // TODO: Buscar usuário do banco de dados
        // User user = userRepository.findByEmail(username)
        //     .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

        // Por enquanto, retorna um usuário mock para permitir desenvolvimento
        // REMOVER quando implementar consulta ao banco
        if (username.equals("admin@test.com")) {
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            
            return User.builder()
                    .username(username)
                    .password("$2a$10$dummy") // BCrypt hash dummy
                    .authorities(authorities)
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(false)
                    .build();
        }

        throw new UsernameNotFoundException("Usuário não encontrado: " + username);
    }

    /**
     * Carrega usuário por ID
     * TODO: Implementar quando as entidades estiverem prontas
     */
    public UserDetails loadUserById(String userId) throws UsernameNotFoundException {
        log.debug("Carregando usuário por ID: {}", userId);

        // TODO: Buscar usuário do banco de dados por ID
        // User user = userRepository.findById(UUID.fromString(userId))
        //     .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + userId));

        throw new UsernameNotFoundException("Usuário não encontrado com ID: " + userId);
    }
}