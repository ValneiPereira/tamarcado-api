package com.tamarcado.adapter.in.rest.contract;

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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Autenticação", description = "Endpoints de autenticação e registro")
@RequestMapping(ApiConstants.AUTH_PATH)
public interface AuthControllerApi {

    @PostMapping("/register/client")
    @Operation(summary = "Registrar cliente", description = "Cria uma nova conta de cliente")
    ResponseEntity<ApiResponse<AuthResponse>> registerClient(@Valid @RequestBody RegisterClientRequest request);

    @PostMapping("/register/professional")
    @Operation(summary = "Registrar profissional", description = "Cria uma nova conta de profissional")
    ResponseEntity<ApiResponse<AuthResponse>> registerProfessional(
            @Valid @RequestBody RegisterProfessionalRequest request);

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Autentica um usuário e retorna tokens JWT")
    ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request);

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh token", description = "Gera novos tokens usando o refresh token")
    ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request);
}
