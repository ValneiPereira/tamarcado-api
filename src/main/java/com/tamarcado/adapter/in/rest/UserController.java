package com.tamarcado.adapter.in.rest;

import com.tamarcado.application.service.UserService;
import com.tamarcado.shared.constant.ApiConstants;
import com.tamarcado.shared.dto.request.ChangePasswordRequest;
import com.tamarcado.shared.dto.request.UpdatePhotoRequest;
import com.tamarcado.shared.dto.request.UpdateUserRequest;
import com.tamarcado.shared.dto.response.ApiResponse;
import com.tamarcado.shared.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(ApiConstants.USERS_PATH)
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints para gerenciamento de perfil do usuário")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Obter perfil do usuário autenticado", description = "Retorna os dados do perfil do usuário atualmente autenticado")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        log.debug("Buscando perfil do usuário autenticado");

        UserResponse user = userService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(user, "Perfil recuperado com sucesso"));
    }

    @PutMapping("/me")
    @Operation(summary = "Atualizar perfil do usuário autenticado", description = "Atualiza os dados do perfil do usuário atualmente autenticado")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUser(
        @Valid @RequestBody UpdateUserRequest request) {
        log.debug("Atualizando perfil do usuário autenticado");

        UserResponse user = userService.updateCurrentUser(request);

        return ResponseEntity.ok(ApiResponse.success(user, "Perfil atualizado com sucesso"));
    }

    @PutMapping("/me/password")
    @Operation(summary = "Alterar senha do usuário autenticado", description = "Altera a senha do usuário atualmente autenticado")
    public ResponseEntity<ApiResponse<String>> changePassword(
        @Valid @RequestBody ChangePasswordRequest request) {
        log.debug("Alterando senha do usuário autenticado");

        userService.changePassword(request);

        return ResponseEntity.ok(ApiResponse.success("Senha alterada com sucesso", "Senha alterada com sucesso"));
    }

    @PutMapping("/me/photo")
    @Operation(summary = "Atualizar foto de perfil", description = "Atualiza a foto de perfil do usuário autenticado")
    public ResponseEntity<ApiResponse<UserResponse>> updatePhoto(
        @Valid @RequestBody UpdatePhotoRequest request) {
        log.debug("Atualizando foto de perfil do usuário autenticado");

        UserResponse user = userService.updatePhoto(request.photoUrl());

        return ResponseEntity.ok(ApiResponse.success(user, "Foto atualizada com sucesso"));
    }

    @DeleteMapping("/me")
    @Operation(summary = "Desativar conta do usuário autenticado", description = "Desativa a conta do usuário atualmente autenticado (soft delete)")
    public ResponseEntity<ApiResponse<String>> deleteCurrentUser() {
        log.debug("Desativando conta do usuário autenticado");

        userService.deleteCurrentUser();

        return ResponseEntity.ok(ApiResponse.success("Conta desativada com sucesso", "Conta desativada com sucesso"));
    }
}
