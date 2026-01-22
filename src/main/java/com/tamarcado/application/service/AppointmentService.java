package com.tamarcado.application.service;

import com.tamarcado.application.port.out.AppointmentRepositoryPort;
import com.tamarcado.application.port.out.ProfessionalRepositoryPort;
import com.tamarcado.application.port.out.ServiceOfferingRepositoryPort;
import com.tamarcado.application.port.out.UserRepositoryPort;
import com.tamarcado.domain.model.appointment.Appointment;
import com.tamarcado.domain.model.appointment.AppointmentStatus;
import com.tamarcado.domain.model.service.ServiceOffering;
import com.tamarcado.domain.model.user.Professional;
import com.tamarcado.domain.model.user.User;
import com.tamarcado.domain.model.user.UserType;
import com.tamarcado.infrastructure.security.SecurityUtils;
import com.tamarcado.shared.dto.request.CreateAppointmentRequest;
import com.tamarcado.shared.dto.response.AppointmentResponse;
import com.tamarcado.shared.exception.BusinessException;
import com.tamarcado.shared.exception.ResourceNotFoundException;
import com.tamarcado.shared.mapper.AppointmentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepositoryPort appointmentRepository;
    private final UserRepositoryPort userRepository;
    private final ProfessionalRepositoryPort professionalRepository;
    private final ServiceOfferingRepositoryPort serviceOfferingRepository;
    private final AppointmentMapper appointmentDtoMapper;

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

        // Criar agendamento
        Appointment appointment = Appointment.builder()
                .client(client)
                .professional(professional)
                .serviceOffering(service)
                .date(request.date())
                .time(request.time())
                .notes(request.notes())
                .status(AppointmentStatus.PENDING)
                .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);
        log.info("Agendamento {} criado com sucesso para cliente {} e profissional {}",
                savedAppointment.getId(), client.getId(), professional.getId());

        return appointmentDtoMapper.toResponse(savedAppointment);
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

        return appointmentDtoMapper.toResponseList(appointments);
    }

    /**
     * Busca um agendamento por ID
     */
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(UUID appointmentId) {
        log.debug("Buscando agendamento: {}", appointmentId);

        Appointment appointment = appointmentRepository.findByIdWithDetails(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado"));

        // Validar que o usuário autenticado é o cliente do agendamento
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) {
            throw new BusinessException("Usuário não autenticado");
        }

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (!appointment.getClient().getId().equals(currentUser.getId())) {
            throw new BusinessException("Acesso negado. Você só pode visualizar seus próprios agendamentos.");
        }

        return appointmentDtoMapper.toResponse(appointment);
    }

    /**
     * Cancela um agendamento (apenas se status for PENDING)
     */
    @Transactional
    public void cancelAppointment(UUID appointmentId) {
        log.debug("Cancelando agendamento: {}", appointmentId);

        Appointment appointment = appointmentRepository.findByIdWithDetails(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado"));

        // Validar que o usuário autenticado é o cliente do agendamento
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) {
            throw new BusinessException("Usuário não autenticado");
        }

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (!appointment.getClient().getId().equals(currentUser.getId())) {
            throw new BusinessException("Acesso negado. Você só pode cancelar seus próprios agendamentos.");
        }

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
}
