package com.tamarcado.adapter.in.rest;

import com.tamarcado.adapter.in.rest.contract.ReviewControllerApi;
import com.tamarcado.application.service.ReviewService;
import com.tamarcado.shared.dto.request.CreateReviewRequest;
import com.tamarcado.shared.dto.response.ApiResponse;
import com.tamarcado.shared.dto.response.ReviewListResponse;
import com.tamarcado.shared.dto.response.ReviewResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ReviewController implements ReviewControllerApi {

        private final ReviewService reviewService;

        @Override
        public ResponseEntity<ApiResponse<ReviewResponse>> createReview(CreateReviewRequest request) {

                log.debug("Criando avaliação para agendamento {}", request.appointmentId());
                var review = reviewService.createReview(request);

                return ResponseEntity.ok(
                                ApiResponse.success(review, "Avaliação criada com sucesso"));
        }

        @Override
        public ResponseEntity<ApiResponse<ReviewListResponse>> getReviewsByProfessional(UUID professionalId,
                        Integer page, Integer pageSize) {

                log.debug("Listando avaliações do profissional {} com paginação", professionalId);
                var reviews = reviewService.getReviewsByProfessional(professionalId, page, pageSize);

                return ResponseEntity.ok(
                                ApiResponse.success(reviews, "Avaliações encontradas com sucesso"));
        }

        @Override
        public ResponseEntity<ApiResponse<List<ReviewResponse>>> getMyReviews() {

                log.debug("Listando avaliações do cliente autenticado");
                var reviews = reviewService.getReviewsByClient();

                return ResponseEntity.ok(
                                ApiResponse.success(reviews, "Avaliações encontradas com sucesso"));
        }
}
