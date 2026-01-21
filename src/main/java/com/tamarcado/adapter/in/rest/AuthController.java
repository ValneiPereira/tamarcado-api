package com.tamarcado.adapter.in.rest;

import com.tamarcado.application.service.AuthService;
import com.tamarcado.shared.constant.ApiConstants;
import com.tamarcado.shared.dto.request.LoginRequest;
import com.tamarcado.shared.dto.request.RefreshTokenRequest;
import com.tamarcado.shared.dto.request.RegisterClientRequest;
import com.tamarcado.shared.dto.request.RegisterProfessionalRequest;
import com.tamarcado.shared.dto.response.ApiResponse;
import com.tamarcado.shared.dto.response.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(ApiConstants.AUTH_PATH)
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints de autenticação e registro")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/client")
    @Operation(summary = "Registrar cliente", description = "Cria uma nova conta de cliente")
    public ResponseEntity<ApiResponse<AuthResponse>> registerClient(
            @Valid @RequestBody RegisterClientRequest request
    ) {
        log.info("Recebida requisição de registro de cliente: {}", request.email());

        AuthResponse response = authService.registerClient(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Cliente registrado com sucesso"));
    }

    @PostMapping("/register/professional")
    @Operation(summary = "Registrar profissional", description = "Cria uma nova conta de profissional")
    public ResponseEntity<ApiResponse<AuthResponse>> registerProfessional(
            @Valid @RequestBody RegisterProfessionalRequest request
    ) {
        log.info("Recebida requisição de registro de profissional: {}", request.email());

        AuthResponse response = authService.registerProfessional(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Profissional registrado com sucesso"));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Autentica um usuário e retorna tokens JWT")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        log.info("Recebida requisição de login: {}", request.email());

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(ApiResponse.success(response, "Login realizado com sucesso"));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh token", description = "Gera novos tokens usando o refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        log.debug("Recebida requisição de refresh token");

        AuthResponse response = authService.refreshToken(request.refreshToken());

        return ResponseEntity.ok(ApiResponse.success(response, "Token atualizado com sucesso"));
    }
}