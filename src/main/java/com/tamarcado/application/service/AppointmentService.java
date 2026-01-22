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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepositoryPort appointmentRepository;
    private final UserRepositoryPort userRepository;
    private final ProfessionalRepositoryPort professionalRepository;
    private final ServiceOfferingRepositoryPort serviceOfferingRepository;
    private final AppointmentMapper appointmentDtoMapper;
    private final SearchService searchService;

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
                .map(appointment -> {
                    AppointmentProfessionalResponse response = appointmentDtoMapper.toProfessionalResponse(appointment);

                    // Calcular distância se coordenadas disponíveis
                    Double distance = null;
                    if (professional.getUser() != null && professional.getUser().getAddress() != null
                            && appointment.getClient() != null && appointment.getClient().getAddress() != null) {
                        Address professionalAddress = professional.getUser().getAddress();
                        Address clientAddress = appointment.getClient().getAddress();

                        if (professionalAddress.getLatitude() != null && professionalAddress.getLongitude() != null
                                && clientAddress.getLatitude() != null && clientAddress.getLongitude() != null) {
                            distance = searchService.calculateDistance(
                                    professionalAddress.getLatitude(), professionalAddress.getLongitude(),
                                    clientAddress.getLatitude(), clientAddress.getLongitude()
                            );
                        }
                    }

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
                })
                .collect(Collectors.toList());
    }

    /**
     * Aceita um agendamento (apenas se status for PENDING)
     */
    @Transactional
    public void acceptAppointment(UUID appointmentId) {
        log.debug("Aceitando agendamento: {}", appointmentId);

        Appointment appointment = appointmentRepository.findByIdWithDetails(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado"));

        // Validar que o usuário autenticado é o profissional do agendamento
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) {
            throw new BusinessException("Usuário não autenticado");
        }

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (!appointment.getProfessional().getId().equals(currentUser.getId())) {
            throw new BusinessException("Acesso negado. Você só pode gerenciar seus próprios agendamentos.");
        }

        // Validar que o status permite aceitação
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new BusinessException(
                    String.format("Agendamento não pode ser aceito. Status atual: %s",
                            appointment.getStatus()));
        }

        appointment.setStatus(AppointmentStatus.ACCEPTED);
        appointmentRepository.save(appointment);

        log.info("Agendamento {} aceito pelo profissional {}", appointmentId, currentUser.getId());
    }

    /**
     * Rejeita um agendamento (apenas se status for PENDING)
     */
    @Transactional
    public void rejectAppointment(UUID appointmentId) {
        log.debug("Rejeitando agendamento: {}", appointmentId);

        Appointment appointment = appointmentRepository.findByIdWithDetails(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado"));

        // Validar que o usuário autenticado é o profissional do agendamento
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) {
            throw new BusinessException("Usuário não autenticado");
        }

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (!appointment.getProfessional().getId().equals(currentUser.getId())) {
            throw new BusinessException("Acesso negado. Você só pode gerenciar seus próprios agendamentos.");
        }

        // Validar que o status permite rejeição
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new BusinessException(
                    String.format("Agendamento não pode ser rejeitado. Status atual: %s",
                            appointment.getStatus()));
        }

        appointment.setStatus(AppointmentStatus.REJECTED);
        appointmentRepository.save(appointment);

        log.info("Agendamento {} rejeitado pelo profissional {}", appointmentId, currentUser.getId());
    }

    /**
     * Completa um agendamento (apenas se status for ACCEPTED)
     */
    @Transactional
    public void completeAppointment(UUID appointmentId) {
        log.debug("Completando agendamento: {}", appointmentId);

        Appointment appointment = appointmentRepository.findByIdWithDetails(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado"));

        // Validar que o usuário autenticado é o profissional do agendamento
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) {
            throw new BusinessException("Usuário não autenticado");
        }

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (!appointment.getProfessional().getId().equals(currentUser.getId())) {
            throw new BusinessException("Acesso negado. Você só pode gerenciar seus próprios agendamentos.");
        }

        // Validar que o status permite conclusão
        if (appointment.getStatus() != AppointmentStatus.ACCEPTED) {
            throw new BusinessException(
                    String.format("Agendamento não pode ser completado. Status atual: %s",
                            appointment.getStatus()));
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);

        log.info("Agendamento {} completado pelo profissional {}", appointmentId, currentUser.getId());
    }
}
