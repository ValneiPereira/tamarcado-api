package com.tamarcado.adapter.in.rest;

import com.tamarcado.adapter.in.rest.contract.UserControllerApi;

import com.tamarcado.application.service.UserService;
import com.tamarcado.shared.dto.request.ChangePasswordRequest;
import com.tamarcado.shared.dto.request.UpdatePhotoRequest;
import com.tamarcado.shared.dto.request.UpdateUserRequest;
import com.tamarcado.shared.dto.response.ApiResponse;
import com.tamarcado.shared.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController implements UserControllerApi {

    private final UserService userService;

    @Override
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {

        log.debug("Buscando perfil do usuário autenticado");
        UserResponse user = userService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(user, "Perfil recuperado com sucesso"));
    }

    @Override
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUser(UpdateUserRequest request) {

        log.debug("Atualizando perfil do usuário autenticado");
        UserResponse user = userService.updateCurrentUser(request);
        return ResponseEntity.ok(ApiResponse.success(user, "Perfil atualizado com sucesso"));
    }

    @Override
    public ResponseEntity<ApiResponse<String>> changePassword(ChangePasswordRequest request) {

        log.debug("Alterando senha do usuário autenticado");
        userService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success("Senha alterada com sucesso", "Senha alterada com sucesso"));
    }

    @Override
    public ResponseEntity<ApiResponse<UserResponse>> updatePhoto(UpdatePhotoRequest request) {

        log.debug("Atualizando foto de perfil. URL recebida: {}", request.photoUrl());
        var user = userService.updatePhoto(request.photoUrl());

        log.debug("Foto atualizada. Photo no UserResponse: {}", user.photo());
        return ResponseEntity.ok(ApiResponse.success(user, "Foto atualizada com sucesso"));
    }

    @Override
    public ResponseEntity<ApiResponse<String>> deleteCurrentUser() {

        log.debug("Desativando conta do usuário autenticado");
        userService.deleteCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("Conta desativada com sucesso", "Conta desativada com sucesso"));
    }
}
