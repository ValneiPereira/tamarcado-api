package com.tamarcado;

import com.tamarcado.application.port.out.ProfessionalRepositoryPort;
import com.tamarcado.application.port.out.UserRepositoryPort;
import com.tamarcado.domain.model.service.Category;
import com.tamarcado.domain.model.service.ServiceType;
import com.tamarcado.domain.model.user.Address;
import com.tamarcado.domain.model.user.Professional;
import com.tamarcado.domain.model.user.User;
import com.tamarcado.domain.model.user.UserType;
import com.tamarcado.infrastructure.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class TestUtils {

    @Autowired
    private UserRepositoryPort userRepository;

    @Autowired
    private ProfessionalRepositoryPort professionalRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Cria um usuário cliente de teste
     */
    public User createTestClient(String email, String name) {
        Address address = Address.builder()
                .cep("01310-100")
                .street("Av. Paulista")
                .number("1000")
                .neighborhood("Bela Vista")
                .city("São Paulo")
                .state("SP")
                .build();

        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode("senha123"))
                .phone("11999999999")
                .userType(UserType.CLIENT)
                .address(address)
                .active(true)
                .build();

        return userRepository.save(user);
    }

    /**
     * Cria um usuário profissional de teste
     */
    public User createTestProfessional(String email, String name) {
        Address address = Address.builder()
                .cep("01310-100")
                .street("Av. Paulista")
                .number("2000")
                .neighborhood("Bela Vista")
                .city("São Paulo")
                .state("SP")
                .build();

        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode("senha123"))
                .phone("11988888888")
                .userType(UserType.PROFESSIONAL)
                .address(address)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        // Criar Professional associado
        Professional professional = Professional.builder()
                .user(savedUser)
                .category(Category.BELEZA)
                .serviceType(ServiceType.CABELEIREIRO)
                .active(true)
                .build();

        professionalRepository.save(professional);

        return savedUser;
    }

    /**
     * Gera um token JWT para um usuário
     */
    public String generateToken(User user) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getUserType().name()))
        );
        return jwtTokenProvider.generateToken(authentication);
    }

    /**
     * Cria um header de autorização com Bearer token
     */
    public String getAuthorizationHeader(String token) {
        return "Bearer " + token;
    }
}
