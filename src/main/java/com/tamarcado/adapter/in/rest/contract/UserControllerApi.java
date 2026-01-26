package com.tamarcado.adapter.in.rest.contract;

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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Users", description = "Endpoints para gerenciamento de perfil do usuário")
@RequestMapping(ApiConstants.USERS_PATH)
@SecurityRequirement(name = "bearerAuth")
public interface UserControllerApi {

    @GetMapping("/me")
    @Operation(summary = "Obter perfil do usuário autenticado", description = "Retorna os dados do perfil do usuário atualmente autenticado")
    ResponseEntity<ApiResponse<UserResponse>> getCurrentUser();

    @PutMapping("/me")
    @Operation(summary = "Atualizar perfil do usuário autenticado", description = "Atualiza os dados do perfil do usuário atualmente autenticado")
    ResponseEntity<ApiResponse<UserResponse>> updateCurrentUser(
            @Valid @RequestBody UpdateUserRequest request);

    @PutMapping("/me/password")
    @Operation(summary = "Alterar senha do usuário autenticado", description = "Altera a senha do usuário atualmente autenticado")
    ResponseEntity<ApiResponse<String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request);

    @PutMapping("/me/photo")
    @Operation(summary = "Atualizar foto de perfil", description = "Atualiza a foto de perfil do usuário autenticado")
    ResponseEntity<ApiResponse<UserResponse>> updatePhoto(
            @Valid @RequestBody UpdatePhotoRequest request);

    @DeleteMapping("/me")
    @Operation(summary = "Desativar conta do usuário autenticado", description = "Desativa a conta do usuário atualmente autenticado (soft delete)")
    ResponseEntity<ApiResponse<String>> deleteCurrentUser();
}
