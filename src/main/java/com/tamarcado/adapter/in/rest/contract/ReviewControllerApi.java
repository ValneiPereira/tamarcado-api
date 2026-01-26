package com.tamarcado.adapter.in.rest.contract;

import com.tamarcado.shared.constant.ApiConstants;
import com.tamarcado.shared.dto.request.CreateReviewRequest;
import com.tamarcado.shared.dto.response.ApiResponse;
import com.tamarcado.shared.dto.response.ReviewListResponse;
import com.tamarcado.shared.dto.response.ReviewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Avaliações", description = "Endpoints para gerenciamento de avaliações")
@RequestMapping(ApiConstants.REVIEWS_PATH)
@SecurityRequirement(name = "bearerAuth")
public interface ReviewControllerApi {

    @PostMapping
    @Operation(summary = "Criar avaliação", description = "Cria uma nova avaliação para um agendamento completado")
    ResponseEntity<ApiResponse<ReviewResponse>> createReview(@Valid @RequestBody CreateReviewRequest request);

    @GetMapping("/professionals/{professionalId}")
    @Operation(summary = "Listar avaliações de um profissional", description = "Lista todas as avaliações de um profissional com paginação. Endpoint público.")
    ResponseEntity<ApiResponse<ReviewListResponse>> getReviewsByProfessional(
            @Parameter(description = "ID do profissional") @PathVariable UUID professionalId,
            @Parameter(description = "Número da página (começa em 1)") @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "Tamanho da página") @RequestParam(required = false, defaultValue = "10") Integer pageSize);

    @GetMapping("/client/me")
    @Operation(summary = "Listar minhas avaliações", description = "Lista todas as avaliações feitas pelo cliente autenticado")
    ResponseEntity<ApiResponse<List<ReviewResponse>>> getMyReviews();
}
