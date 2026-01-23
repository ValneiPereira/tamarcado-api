package com.tamarcado.application.service;

import com.tamarcado.application.port.out.AppointmentRepositoryPort;
import com.tamarcado.application.port.out.ProfessionalRepositoryPort;
import com.tamarcado.application.port.out.ServiceOfferingRepositoryPort;
import com.tamarcado.application.port.out.UserRepositoryPort;
import com.tamarcado.domain.model.appointment.Appointment;
import com.tamarcado.domain.model.appointment.AppointmentStatus;
import com.tamarcado.domain.model.service.ServiceOffering;
import com.tamarcado.domain.model.user.Address;
import com.tamarcado.domain.model.user.Professional;
import com.tamarcado.domain.model.user.User;
import com.tamarcado.domain.model.user.UserType;
import com.tamarcado.infrastructure.security.SecurityUtils;
import com.tamarcado.shared.dto.request.CreateAppointmentRequest;
import com.tamarcado.shared.dto.response.AppointmentProfessionalResponse;
import com.tamarcado.shared.dto.response.AppointmentResponse;
import com.tamarcado.domain.model.notification.NotificationType;
import com.tamarcado.shared.exception.BusinessException;
import com.tamarcado.shared.exception.ResourceNotFoundException;
import com.tamarcado.shared.mapper.AppointmentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepositoryPort appointmentRepository;
    private final UserRepositoryPort userRepository;
    private final ProfessionalRepositoryPort professionalRepository;
    private final ServiceOfferingRepositoryPort serviceOfferingRepository;
    private final AppointmentMapper appointmentMapper;
    private final SearchService searchService;
    private final NotificationService notificationService;

    /**
     * Cria um novo agendamento
     */
    @Transactional
    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {
        log.debug("Criando agendamento para profissional {} e serviço {}",
                request.professionalId(), request.serviceId());

        // Obter cliente autenticado
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) {
            throw new BusinessException("Usuário não autenticado");
        }

        User client = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // Validar que é um cliente
        if (client.getUserType() != UserType.CLIENT) {
            throw new BusinessException("Apenas clientes podem criar agendamentos");
        }

        // Validar que o cliente está ativo
        if (!client.getActive()) {
            throw new BusinessException("Conta do cliente está desativada");
        }

        // Buscar profissional
        Professional professional = professionalRepository.findById(request.professionalId())
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado"));

        // Validar que o profissional está ativo
        if (!professional.getActive() || professional.getUser() == null
                || !professional.getUser().getActive()) {
            throw new BusinessException("Profissional não encontrado ou inativo");
        }

        // Buscar serviço
        ServiceOffering service = serviceOfferingRepository.findById(request.serviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado"));

        // Validar que o serviço está ativo
        if (!service.getActive()) {
            throw new BusinessException("Serviço não está disponível");
        }

        // Validar que o serviço pertence ao profissional
        if (!service.getProfessional().getId().equals(professional.getId())) {
            throw new BusinessException("Serviço não pertence ao profissional informado");
        }

        // Validar que a data não é passada (já validado no DTO com @Future, mas validar também se é hoje)
        LocalDate today = LocalDate.now();
        if (request.date().isBefore(today)) {
            throw new BusinessException("Data não pode ser passada");
        }

        Appointment appointment = appointmentMapper.toEntity(request, client, professional, service);

        Appointment savedAppointment = appointmentRepository.save(appointment);
        log.info("Agendamento {} criado com sucesso para cliente {} e profissional {}",
                savedAppointment.getId(), client.getId(), professional.getId());

        // Enviar notificação para o profissional
        try {
            String dateFormatted = formatDate(savedAppointment.getDate());
            String timeFormatted = formatTime(savedAppointment.getTime());
            Map<String, Object> notificationData = createNotificationData(savedAppointment, client.getName());

            notificationService.sendNotification(
                    professional.getId(),
                    NotificationType.APPOINTMENT_CREATED,
                    "Novo Agendamento",
                    String.format("%s solicitou um agendamento para %s às %s",
                            client.getName(), dateFormatted, timeFormatted),
                    notificationData
            );
        } catch (Exception e) {
            log.error("Erro ao enviar notificação de novo agendamento", e);
            // Não falhar o agendamento se a notificação falhar
        }

        return appointmentMapper.toResponse(savedAppointment);
    }

    /**
     * Lista agendamentos do cliente autenticado, opcionalmente filtrado por status
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByClient(AppointmentStatus status) {
        String email = SecurityUtils.getCurrentUsername();

        if (email == null) {
            throw new BusinessException("Usuário não autenticado");
        }

        User client = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // Validar que é um cliente
        if (client.getUserType() != UserType.CLIENT) {
            throw new BusinessException("Apenas clientes podem listar agendamentos");
        }

        List<Appointment> appointments;
        if (status != null) {
            appointments = appointmentRepository.findByClientIdAndStatus(client.getId(), status);
        } else {
            appointments = appointmentRepository.findByClientId(client.getId());
        }

        return appointmentMapper.toResponseList(appointments);
    }

    /**
     * Busca um agendamento por ID
     */
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(UUID appointmentId) {
        log.debug("Buscando agendamento: {}", appointmentId);

        Appointment appointment = findAppointmentWithDetails(appointmentId);
        User currentUser = getCurrentAuthenticatedUser();

        validateClientOwnership(appointment, currentUser, "visualizar");

        return appointmentMapper.toResponse(appointment);
    }

    /**
     * Cancela um agendamento (apenas se status for PENDING)
     */
    @Transactional
    public void cancelAppointment(UUID appointmentId) {
        log.debug("Cancelando agendamento: {}", appointmentId);

        Appointment appointment = findAppointmentWithDetails(appointmentId);
        User currentUser = getCurrentAuthenticatedUser();

        validateClientOwnership(appointment, currentUser, "cancelar");

        // Validar que o status permite cancelamento
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new BusinessException(
                    String.format("Agendamento não pode ser cancelado. Status atual: %s",
                            appointment.getStatus()));
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

        log.info("Agendamento {} cancelado pelo cliente {}", appointmentId, currentUser.getId());
    }

    /**
     * Lista agendamentos do profissional autenticado, opcionalmente filtrado por status
     * Calcula distância até o cliente se coordenadas disponíveis
     */
    @Transactional(readOnly = true)
    public List<AppointmentProfessionalResponse> getAppointmentsByProfessional(AppointmentStatus status) {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) {
            throw new BusinessException("Usuário não autenticado");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // Validar que é um profissional
        if (user.getUserType() != UserType.PROFESSIONAL) {
            throw new BusinessException("Apenas profissionais podem listar seus agendamentos");
        }

        Professional professional = professionalRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado"));

        List<Appointment> appointments;
        if (status != null) {
            appointments = appointmentRepository.findByProfessionalIdAndStatus(professional.getId(), status);
        } else {
            appointments = appointmentRepository.findByProfessionalId(professional.getId());
        }

        // Converter para DTOs e calcular distância
        return appointments.stream()
                .map(appointment -> enrichResponseWithDistance(
                        appointmentMapper.toProfessionalResponse(appointment),
                        professional,
                        appointment
                ))
                .collect(Collectors.toList());
    }

    /**
     * Aceita um agendamento (apenas se status for PENDING)
     */
    @Transactional
    public void acceptAppointment(UUID appointmentId) {
        log.debug("Aceitando agendamento: {}", appointmentId);

        Appointment appointment = findAppointmentWithDetails(appointmentId);
        User currentUser = getCurrentAuthenticatedUser();

        validateProfessionalOwnership(appointment, currentUser);

        // Validar que o status permite aceitação
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new BusinessException(
                    String.format("Agendamento não pode ser aceito. Status atual: %s",
                            appointment.getStatus()));
        }

        appointment.setStatus(AppointmentStatus.ACCEPTED);
        appointmentRepository.save(appointment);

        log.info("Agendamento {} aceito pelo profissional {}", appointmentId, currentUser.getId());

        // Enviar notificação para o cliente
        try {
            String dateFormatted = formatDate(appointment.getDate());
            String timeFormatted = formatTime(appointment.getTime());
            Map<String, Object> notificationData = createNotificationData(appointment, null);

            notificationService.sendNotification(
                    appointment.getClient().getId(),
                    NotificationType.APPOINTMENT_ACCEPTED,
                    "Agendamento Confirmado!",
                    String.format("%s aceitou seu agendamento para %s às %s",
                            appointment.getProfessional().getUser().getName(), dateFormatted, timeFormatted),
                    notificationData
            );
        } catch (Exception e) {
            log.error("Erro ao enviar notificação de agendamento aceito", e);
            // Não falhar a operação se a notificação falhar
        }
    }

    /**
     * Rejeita um agendamento (apenas se status for PENDING)
     */
    @Transactional
    public void rejectAppointment(UUID appointmentId) {
        log.debug("Rejeitando agendamento: {}", appointmentId);

        Appointment appointment = findAppointmentWithDetails(appointmentId);
        User currentUser = getCurrentAuthenticatedUser();

        validateProfessionalOwnership(appointment, currentUser);

        // Validar que o status permite rejeição
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new BusinessException(
                    String.format("Agendamento não pode ser rejeitado. Status atual: %s",
                            appointment.getStatus()));
        }

        appointment.setStatus(AppointmentStatus.REJECTED);
        appointmentRepository.save(appointment);

        log.info("Agendamento {} rejeitado pelo profissional {}", appointmentId, currentUser.getId());

        // Enviar notificação para o cliente
        try {
            String dateFormatted = formatDate(appointment.getDate());
            String timeFormatted = formatTime(appointment.getTime());
            Map<String, Object> notificationData = createNotificationData(appointment, null);

            notificationService.sendNotification(
                    appointment.getClient().getId(),
                    NotificationType.APPOINTMENT_REJECTED,
                    "Agendamento Recusado",
                    String.format("%s recusou seu agendamento para %s às %s",
                            appointment.getProfessional().getUser().getName(), dateFormatted, timeFormatted),
                    notificationData
            );
        } catch (Exception e) {
            log.error("Erro ao enviar notificação de agendamento rejeitado", e);
            // Não falhar a operação se a notificação falhar
        }
    }

    /**
     * Completa um agendamento (apenas se status for ACCEPTED)
     */
    @Transactional
    public void completeAppointment(UUID appointmentId) {
        log.debug("Completando agendamento: {}", appointmentId);

        Appointment appointment = findAppointmentWithDetails(appointmentId);
        User currentUser = getCurrentAuthenticatedUser();

        validateProfessionalOwnership(appointment, currentUser);

        // Validar que o status permite conclusão
        if (appointment.getStatus() != AppointmentStatus.ACCEPTED) {
            throw new BusinessException(
                    String.format("Agendamento não pode ser completado. Status atual: %s",
                            appointment.getStatus()));
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);

        log.info("Agendamento {} completado pelo profissional {}", appointmentId, currentUser.getId());

        // Enviar notificação para o cliente
        try {
            Map<String, Object> notificationData = createNotificationData(appointment, null);

            notificationService.sendNotification(
                    appointment.getClient().getId(),
                    NotificationType.APPOINTMENT_COMPLETED,
                    "Agendamento Concluído",
                    String.format("O agendamento com %s foi concluído. Que tal avaliar o serviço?",
                            appointment.getProfessional().getUser().getName()),
                    notificationData
            );
        } catch (Exception e) {
            log.error("Erro ao enviar notificação de agendamento completado", e);
            // Não falhar a operação se a notificação falhar
        }
    }

    // ========== Métodos auxiliares privados ==========

    /**
     * Busca o usuário autenticado atual
     */
    private User getCurrentAuthenticatedUser() {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) {
            throw new BusinessException("Usuário não autenticado");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    /**
     * Busca um agendamento com todos os detalhes (com relacionamentos carregados)
     */
    private Appointment findAppointmentWithDetails(UUID appointmentId) {
        return appointmentRepository.findByIdWithDetails(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado"));
    }

    /**
     * Valida se o usuário autenticado é o cliente do agendamento
     */
    private void validateClientOwnership(Appointment appointment, User currentUser, String action) {
        if (!appointment.getClient().getId().equals(currentUser.getId())) {
            throw new BusinessException(
                    String.format("Acesso negado. Você só pode %s seus próprios agendamentos.", action));
        }
    }

    /**
     * Valida se o usuário autenticado é o profissional do agendamento
     */
    private void validateProfessionalOwnership(Appointment appointment, User currentUser) {
        if (!appointment.getProfessional().getId().equals(currentUser.getId())) {
            throw new BusinessException("Acesso negado. Você só pode gerenciar seus próprios agendamentos.");
        }
    }

    /**
     * Formata a data do agendamento
     */
    private String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /**
     * Formata o horário do agendamento
     */
    private String formatTime(java.time.LocalTime time) {
        return time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    /**
     * Cria o Map de dados para notificação
     * @param appointment Agendamento
     * @param clientName Nome do cliente (opcional, usado apenas quando notificando profissional)
     * @return Map com dados da notificação
     */
    private Map<String, Object> createNotificationData(Appointment appointment, String clientName) {
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("appointmentId", appointment.getId().toString());

        if (clientName != null) {
            // Quando notificando profissional sobre novo agendamento
            notificationData.put("clientName", clientName);
        } else {
            // Quando notificando cliente sobre mudanças no agendamento
            notificationData.put("professionalName", appointment.getProfessional().getUser().getName());
        }

        return notificationData;
    }

    /**
     * Enriquece o response com a distância calculada entre profissional e cliente
     */
    private AppointmentProfessionalResponse enrichResponseWithDistance(
            AppointmentProfessionalResponse response,
            Professional professional,
            Appointment appointment) {
        Double distance = calculateDistanceToClient(professional, appointment);

        return new AppointmentProfessionalResponse(
                response.id(),
                response.clientId(),
                response.clientName(),
                response.clientPhone(),
                distance,
                response.service(),
                response.date(),
                response.time(),
                response.notes(),
                response.status(),
                response.createdAt(),
                response.updatedAt()
        );
    }

    /**
     * Calcula a distância entre o profissional e o cliente do agendamento
     * @return Distância em quilômetros ou null se não for possível calcular
     */
    private Double calculateDistanceToClient(Professional professional, Appointment appointment) {
        if (professional.getUser() == null || professional.getUser().getAddress() == null
                || appointment.getClient() == null || appointment.getClient().getAddress() == null) {
            return null;
        }

        Address professionalAddress = professional.getUser().getAddress();
        Address clientAddress = appointment.getClient().getAddress();

        if (professionalAddress.getLatitude() == null || professionalAddress.getLongitude() == null
                || clientAddress.getLatitude() == null || clientAddress.getLongitude() == null) {
            return null;
        }

        return searchService.calculateDistance(
                professionalAddress.getLatitude(),
                professionalAddress.getLongitude(),
                clientAddress.getLatitude(),
                clientAddress.getLongitude()
        );
    }
}
