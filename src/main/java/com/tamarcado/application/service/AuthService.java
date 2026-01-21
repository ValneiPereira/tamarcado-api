package com.tamarcado.application.service;

import com.tamarcado.application.port.out.ProfessionalRepositoryPort;
import com.tamarcado.application.port.out.ServiceOfferingRepositoryPort;
import com.tamarcado.application.port.out.UserRepositoryPort;
import com.tamarcado.domain.model.service.ServiceOffering;
import com.tamarcado.domain.model.user.Address;
import com.tamarcado.domain.model.user.Professional;
import com.tamarcado.domain.model.user.User;
import com.tamarcado.domain.model.user.UserType;
import com.tamarcado.infrastructure.security.JwtTokenProvider;
import com.tamarcado.shared.constant.ErrorMessages;
import com.tamarcado.shared.dto.request.LoginRequest;
import com.tamarcado.shared.dto.request.RegisterClientRequest;
import com.tamarcado.shared.dto.request.RegisterProfessionalRequest;
import com.tamarcado.shared.dto.response.AuthResponse;
import com.tamarcado.shared.dto.response.UserResponse;
import com.tamarcado.shared.exception.BusinessException;
import com.tamarcado.shared.exception.ResourceNotFoundException;
import com.tamarcado.shared.mapper.AddressRequestMapper;
import com.tamarcado.shared.mapper.UserDtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepositoryPort userRepository;
    private final ProfessionalRepositoryPort professionalRepository;
    private final ServiceOfferingRepositoryPort serviceOfferingRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserDtoMapper userDtoMapper;
    private final AddressRequestMapper addressRequestMapper;

    @Transactional
    public AuthResponse registerClient(RegisterClientRequest request) {
        log.info("Registrando cliente: {}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorMessages.USER_ALREADY_EXISTS);
        }

        // Converter AddressRequest para Address usando mapper
        Address address = addressRequestMapper.toDomain(request.address());

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .userType(UserType.CLIENT)
                .address(address)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        log.info("Cliente registrado com sucesso: {}", savedUser.getId());

        // Criar autenticação antes de gerar tokens
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                savedUser.getEmail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENT"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return createAuthResponse(savedUser);
    }

    @Transactional
    public AuthResponse registerProfessional(RegisterProfessionalRequest request) {
        log.info("Registrando profissional: {}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorMessages.USER_ALREADY_EXISTS);
        }


        Address address = addressRequestMapper.toDomain(request.address());

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .userType(UserType.PROFESSIONAL)
                .address(address)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        Professional professional = Professional.builder()
                .id(savedUser.getId())
                .user(savedUser)
                .category(request.category())
                .serviceType(request.serviceType())
                .active(true)
                .totalRatings(0)
                .build();

        Professional savedProfessional = professionalRepository.save(professional);


        for (var serviceRequest : request.services()) {
            ServiceOffering serviceOffering = ServiceOffering.builder()
                    .professional(savedProfessional)
                    .name(serviceRequest.name())
                    .price(serviceRequest.price())
                    .active(true)
                    .build();

            serviceOfferingRepository.save(serviceOffering);
        }

        log.info("Profissional registrado com sucesso: {}", savedUser.getId());


        Authentication authentication = new UsernamePasswordAuthenticationToken(
                savedUser.getEmail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PROFESSIONAL"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return createAuthResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Tentativa de login: {}", request.email());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userRepository.findByEmail(request.email())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

            log.info("Login realizado com sucesso: {}", user.getId());

            return createAuthResponse(user);
        } catch (org.springframework.security.core.AuthenticationException e) {
            log.warn("Falha na autenticação: {}", request.email());
            throw new BusinessException(ErrorMessages.INVALID_CREDENTIALS);
        }
    }

    public AuthResponse refreshToken(String refreshToken) {
        log.debug("Refreshing token");

        if (!tokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorMessages.INVALID_TOKEN);
        }

        String username = tokenProvider.getUsernameFromToken(refreshToken);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        if (!user.getActive()) {
            throw new BusinessException(ErrorMessages.UNAUTHORIZED);
        }


        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user.getEmail(), null, null);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return createAuthResponse(user);
    }

    private AuthResponse createAuthResponse(User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        UserResponse userResponse = userDtoMapper.toResponse(user);

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                3600L, // 1 hora em segundos
                userResponse
        );
    }
}
