package com.tamarcado.adapter.in.rest;

import com.tamarcado.adapter.in.rest.contract.AuthControllerApi;
import com.tamarcado.application.service.AuthService;
import com.tamarcado.shared.dto.request.LoginRequest;
import com.tamarcado.shared.dto.request.RefreshTokenRequest;
import com.tamarcado.shared.dto.request.RegisterClientRequest;
import com.tamarcado.shared.dto.request.RegisterProfessionalRequest;
import com.tamarcado.shared.dto.response.ApiResponse;
import com.tamarcado.shared.dto.response.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController implements AuthControllerApi {

    private final AuthService authService;

    @Override
    public ResponseEntity<ApiResponse<AuthResponse>> registerClient(RegisterClientRequest request) {

        log.info("Recebida requisição de registro de cliente: {}", request.email());
        var response = authService.registerClient(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Cliente registrado com sucesso"));
    }

    @Override
    public ResponseEntity<ApiResponse<AuthResponse>> registerProfessional(RegisterProfessionalRequest request) {

        log.info("Recebida requisição de registro de profissional: {}", request.email());
        var response = authService.registerProfessional(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Profissional registrado com sucesso"));
    }

    @Override
    public ResponseEntity<ApiResponse<AuthResponse>> login(LoginRequest request) {

        log.info("Recebida requisição de login: {}", request.email());
        var response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login realizado com sucesso"));
    }

    @Override
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(RefreshTokenRequest request) {

        log.debug("Recebida requisição de refresh token");
        var response = authService.refreshToken(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success(response, "Token atualizado com sucesso"));
    }
}
