package com.tamarcado.application.service;

import com.tamarcado.application.port.out.AppointmentRepositoryPort;
import com.tamarcado.application.port.out.ProfessionalRepositoryPort;
import com.tamarcado.application.port.out.ReviewRepositoryPort;
import com.tamarcado.application.port.out.UserRepositoryPort;
import com.tamarcado.domain.model.appointment.Appointment;
import com.tamarcado.domain.model.appointment.AppointmentStatus;
import com.tamarcado.domain.model.service.Category;
import com.tamarcado.domain.model.user.Professional;
import com.tamarcado.domain.model.user.User;
import com.tamarcado.domain.model.user.UserType;
import com.tamarcado.infrastructure.security.SecurityUtils;
import com.tamarcado.shared.dto.response.ClientDashboardResponse;
import com.tamarcado.shared.dto.response.ProfessionalDashboardResponse;
import com.tamarcado.shared.exception.BusinessException;
import com.tamarcado.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AppointmentRepositoryPort appointmentRepository;
    private final ReviewRepositoryPort reviewRepository;
    private final ProfessionalRepositoryPort professionalRepository;
    private final UserRepositoryPort userRepository;

    /**
     * Obtém estatísticas do dashboard do profissional
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "professionalDashboard", key = "#professionalId")
    public ProfessionalDashboardResponse getProfessionalDashboard(UUID professionalId) {
        log.debug("Buscando dashboard do profissional {}", professionalId);

        Professional professional = professionalRepository.findById(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado"));

        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        // Agendamentos de hoje
        List<Appointment> todayAppointments = appointmentRepository
                .findByProfessionalIdAndDate(professionalId, today);
        int todayAppointmentsCount = todayAppointments.size();

        // Agendamentos pendentes
        long pendingAppointments = appointmentRepository
                .countByProfessionalIdAndStatus(professionalId, AppointmentStatus.PENDING);

        // Média de avaliações
        Double averageRating = reviewRepository.calculateAverageRatingByProfessionalId(professionalId);
        Long totalRatings = reviewRepository.countByProfessionalId(professionalId);

        // Receita do mês (agendamentos completados)
        List<Appointment> completedThisMonth = appointmentRepository
                .findByProfessionalIdAndDateRangeAndStatus(
                        professionalId,
                        firstDayOfMonth,
                        lastDayOfMonth,
                        AppointmentStatus.COMPLETED
                );

        BigDecimal monthRevenue = completedThisMonth.stream()
                .map(appointment -> appointment.getServiceOffering().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int completedThisMonthCount = completedThisMonth.size();

        return new ProfessionalDashboardResponse(
                todayAppointmentsCount,
                (int) pendingAppointments,
                averageRating != null ? averageRating : 0.0,
                totalRatings,
                monthRevenue,
                completedThisMonthCount
        );
    }

    /**
     * Obtém estatísticas do dashboard do cliente
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "clientDashboard", key = "#clientId")
    public ClientDashboardResponse getClientDashboard(UUID clientId) {
        log.debug("Buscando dashboard do cliente {}", clientId);

        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));

        // Próximos agendamentos (PENDING ou ACCEPTED)
        List<Appointment> upcomingAppointments = appointmentRepository
                .findByClientIdAndStatusIn(
                        clientId,
                        Arrays.asList(AppointmentStatus.PENDING, AppointmentStatus.ACCEPTED)
                );
        int upcomingCount = upcomingAppointments.size();

        // Agendamentos completados
        List<Appointment> completedAppointments = appointmentRepository
                .findByClientIdAndStatus(clientId, AppointmentStatus.COMPLETED);
        int completedCount = completedAppointments.size();

        // Categoria favorita (baseada nos serviços mais utilizados)
        Category favoriteCategory = calculateFavoriteCategory(completedAppointments);

        return new ClientDashboardResponse(
                upcomingCount,
                completedCount,
                favoriteCategory
        );
    }

    /**
     * Calcula a categoria favorita do cliente baseada nos agendamentos completados
     * Nota: ServiceOffering não possui categoria diretamente, então retornamos null por enquanto
     * TODO: Adicionar categoria/tipo ao ServiceOffering ou calcular de outra forma
     */
    private Category calculateFavoriteCategory(List<Appointment> completedAppointments) {
        // Por enquanto, retornamos null pois ServiceOffering não tem categoria
        // Pode ser implementado no futuro quando a estrutura for expandida
        return null;
    }

    /**
     * Obtém dashboard do profissional autenticado
     */
    @Transactional(readOnly = true)
    public ProfessionalDashboardResponse getMyProfessionalDashboard() {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) {
            throw new BusinessException("Usuário não autenticado");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (user.getUserType() != UserType.PROFESSIONAL) {
            throw new BusinessException("Apenas profissionais podem acessar este dashboard");
        }

        Professional professional = professionalRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado"));

        return getProfessionalDashboard(professional.getId());
    }

    /**
     * Obtém dashboard do cliente autenticado
     */
    @Transactional(readOnly = true)
    public ClientDashboardResponse getMyClientDashboard() {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) {
            throw new BusinessException("Usuário não autenticado");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (user.getUserType() != UserType.CLIENT) {
            throw new BusinessException("Apenas clientes podem acessar este dashboard");
        }

        return getClientDashboard(user.getId());
    }
}
