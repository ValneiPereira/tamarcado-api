package com.tamarcado.application.service;

import com.tamarcado.application.port.out.AppointmentRepositoryPort;
import com.tamarcado.application.port.out.ProfessionalRepositoryPort;
import com.tamarcado.application.port.out.ReviewRepositoryPort;
import com.tamarcado.application.port.out.UserRepositoryPort;
import com.tamarcado.domain.model.appointment.Appointment;
import com.tamarcado.domain.model.appointment.AppointmentStatus;
import com.tamarcado.domain.model.review.Review;
import com.tamarcado.domain.model.user.Professional;
import com.tamarcado.domain.model.user.User;
import com.tamarcado.infrastructure.security.SecurityUtils;
import com.tamarcado.shared.dto.request.CreateReviewRequest;
import com.tamarcado.shared.dto.response.PaginationResponse;
import com.tamarcado.shared.dto.response.ReviewListResponse;
import com.tamarcado.shared.dto.response.ReviewResponse;
import com.tamarcado.shared.exception.BusinessException;
import com.tamarcado.shared.exception.ResourceNotFoundException;
import com.tamarcado.shared.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepositoryPort reviewRepository;
    private final AppointmentRepositoryPort appointmentRepository;
    private final ProfessionalRepositoryPort professionalRepository;
    private final UserRepositoryPort userRepository;
    private final ReviewMapper reviewMapper;

    /**
     * Cria uma nova avaliação
     */
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request) {
        log.debug("Criando avaliação para agendamento {}", request.appointmentId());

        // Obter cliente autenticado
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) {
            throw new BusinessException("Usuário não autenticado");
        }

        User client = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // Buscar agendamento com detalhes
        Appointment appointment = appointmentRepository.findByIdWithDetails(request.appointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado"));

        // Validar que o agendamento pertence ao cliente autenticado
        if (!appointment.getClient().getId().equals(client.getId())) {
            throw new BusinessException("Acesso negado. Você só pode avaliar seus próprios agendamentos.");
        }

        // Validar que o agendamento está COMPLETED
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new BusinessException(
                    String.format("Apenas agendamentos completados podem ser avaliados. Status atual: %s",
                            appointment.getStatus()));
        }

        // Validar que o agendamento ainda não foi avaliado
        if (reviewRepository.existsByAppointmentId(request.appointmentId())) {
            throw new BusinessException("Este agendamento já foi avaliado.");
        }

        // Buscar profissional
        Professional professional = appointment.getProfessional();

        // Criar avaliação usando mapper
        Review review = reviewMapper.toEntity(request, appointment, professional);
        Review savedReview = reviewRepository.save(review);

        log.info("Avaliação {} criada para agendamento {} e profissional {}",
                savedReview.getId(), request.appointmentId(), professional.getId());

        // Atualizar média de avaliação do profissional
        updateProfessionalAverageRating(professional.getId());

        return reviewMapper.toResponse(savedReview);
    }

    /**
     * Lista avaliações de um profissional com paginação
     */
    @Transactional(readOnly = true)
    public ReviewListResponse getReviewsByProfessional(UUID professionalId, Integer page, Integer pageSize) {
        log.debug("Listando avaliações do profissional {} com paginação", professionalId);

        // Validar que o profissional existe
        Professional professional = professionalRepository.findById(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado"));

        // Configurar paginação
        int pageNumber = (page != null && page > 0) ? page - 1 : 0;
        int size = (pageSize != null && pageSize > 0) ? pageSize : 10;
        Pageable pageable = PageRequest.of(pageNumber, size);

        // Buscar avaliações paginadas
        Page<Review> reviewPage = reviewRepository.findByProfessionalId(professionalId, pageable);

        // Calcular média de avaliações
        Double averageRating = reviewRepository.calculateAverageRatingByProfessionalId(professionalId);
        Long totalReviews = reviewRepository.countByProfessionalId(professionalId);

        // Converter para DTOs
        List<ReviewResponse> reviews = reviewMapper.toResponseList(reviewPage.getContent());

        // Criar resposta de paginação
        PaginationResponse pagination = new PaginationResponse(
                pageNumber + 1,
                size,
                reviewPage.getTotalPages(),
                reviewPage.getTotalElements()
        );

        return new ReviewListResponse(
                averageRating != null ? averageRating : 0.0,
                totalReviews,
                reviews,
                pagination
        );
    }

    /**
     * Lista avaliações feitas pelo cliente autenticado
     */
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByClient() {
        log.debug("Listando avaliações do cliente autenticado");

        // Obter cliente autenticado
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) {
            throw new BusinessException("Usuário não autenticado");
        }

        User client = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // Buscar agendamentos do cliente que foram completados
        List<Appointment> completedAppointments = appointmentRepository
                .findByClientIdAndStatus(client.getId(), AppointmentStatus.COMPLETED);

        // Buscar avaliações para esses agendamentos
        List<Review> reviews = completedAppointments.stream()
                .map(appointment -> reviewRepository.findByAppointmentId(appointment.getId()))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .toList();

        return reviewMapper.toResponseList(reviews);
    }

    /**
     * Atualiza a média de avaliação do profissional
     */
    @Transactional
    public void updateProfessionalAverageRating(UUID professionalId) {
        log.debug("Atualizando média de avaliação do profissional {}", professionalId);

        Double averageRating = reviewRepository.calculateAverageRatingByProfessionalId(professionalId);
        Long totalReviews = reviewRepository.countByProfessionalId(professionalId);

        Professional professional = professionalRepository.findById(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado"));

        // Atualizar média (se o profissional tiver campo para isso)
        // Por enquanto, apenas logamos - pode ser implementado no futuro se necessário
        log.info("Média de avaliação do profissional {}: {} (total: {} avaliações)",
                professionalId, averageRating != null ? averageRating : 0.0, totalReviews);
    }
}
