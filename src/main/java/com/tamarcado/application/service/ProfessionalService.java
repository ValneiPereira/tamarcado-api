package com.tamarcado.application.service;

import com.tamarcado.application.port.out.AppointmentRepositoryPort;
import com.tamarcado.application.port.out.ProfessionalRepositoryPort;
import com.tamarcado.application.port.out.ReviewRepositoryPort;
import com.tamarcado.application.port.out.ServiceOfferingRepositoryPort;
import com.tamarcado.application.port.out.UserRepositoryPort;
import com.tamarcado.domain.model.appointment.AppointmentStatus;
import com.tamarcado.domain.model.service.ServiceOffering;
import com.tamarcado.domain.model.user.Address;
import com.tamarcado.domain.model.user.Professional;
import com.tamarcado.infrastructure.security.SecurityUtils;
import com.tamarcado.shared.dto.request.CreateServiceRequest;
import com.tamarcado.shared.dto.request.UpdateServiceRequest;
import com.tamarcado.shared.dto.response.ProfessionalDetailResponse;
import com.tamarcado.shared.dto.response.ReviewResponse;
import com.tamarcado.shared.dto.response.ServiceResponse;
import com.tamarcado.shared.exception.BusinessException;
import com.tamarcado.shared.exception.ResourceNotFoundException;
import com.tamarcado.shared.mapper.AddressMapper;
import com.tamarcado.shared.mapper.ProfessionalMapper;
import com.tamarcado.shared.mapper.ReviewMapper;
import com.tamarcado.shared.mapper.ServiceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfessionalService {

    private final ProfessionalRepositoryPort professionalRepository;
    private final ServiceOfferingRepositoryPort serviceOfferingRepository;
    private final ReviewRepositoryPort reviewRepository;
    private final AppointmentRepositoryPort appointmentRepository;
    private final SearchService searchService;
    private final UserRepositoryPort userRepository;
    private final ProfessionalMapper professionalDtoMapper;
    private final ServiceMapper serviceDtoMapper;
    private final ReviewMapper reviewDtoMapper;
    private final AddressMapper addressDtoMapper;

    /**
     * Busca detalhes completos de um profissional por ID
     * Inclui serviços, avaliações e calcula distância se coordenadas forem fornecidas
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "professionalDetail", key = "#id + '_' + (#latitude != null ? #latitude : 'null') + '_' + (#longitude != null ? #longitude : 'null')")
    public ProfessionalDetailResponse getProfessionalById(UUID id, Double latitude, Double longitude) {
        log.debug("Buscando profissional: {}", id);

        Professional professional = professionalRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado"));

        if (!professional.getActive() || professional.getUser() == null
                || !professional.getUser().getActive()) {
            throw new ResourceNotFoundException("Profissional não encontrado ou inativo");
        }

        // Buscar serviços ativos
        List<ServiceOffering> services = serviceOfferingRepository
                .findByProfessionalIdAndActiveTrue(id);
        List<ServiceResponse> serviceResponses = serviceDtoMapper.toResponseList(services);

        // Buscar últimas 10 avaliações
        Pageable pageable = PageRequest.of(0, 10);
        List<com.tamarcado.domain.model.review.Review> reviewsList = reviewRepository.findByProfessionalId(id, pageable)
                .getContent();
        List<ReviewResponse> reviews = reviewDtoMapper.toResponseList(reviewsList);

        // Calcular distância se coordenadas fornecidas
        Double distance = null;
        if (latitude != null && longitude != null && professional.getUser().getAddress() != null) {
            Address address = professional.getUser().getAddress();
            if (address.getLatitude() != null && address.getLongitude() != null) {
                distance = searchService.calculateDistance(
                        latitude, longitude,
                        address.getLatitude(), address.getLongitude()
                );
            }
        }

        // Construir resposta usando mapper base e adicionar campos extras
        ProfessionalDetailResponse response = professionalDtoMapper.toDetailResponseBase(professional);

        // Criar nova instância com todos os campos (MapStruct não suporta múltiplos parâmetros facilmente)
        return new ProfessionalDetailResponse(
                response.id(),
                response.name(),
                response.email(),
                response.phone(),
                response.photo(),
                response.category(),
                response.serviceType(),
                response.averageRating(),
                response.totalRatings(),
                response.address(),
                distance,
                serviceResponses,
                reviews
        );
    }

    /**
     * Busca serviços do profissional autenticado
     */
    @Transactional(readOnly = true)
    public List<ServiceResponse> getMyServices() {
        String email = SecurityUtils.getCurrentUsername();

        if (email == null) {
            throw new BusinessException("Usuário não autenticado");
        }

        Professional professional = getCurrentProfessional();

        List<ServiceOffering> services = serviceOfferingRepository
                .findByProfessionalIdAndActiveTrue(professional.getId());

        return serviceDtoMapper.toResponseList(services);
    }

    /**
     * Cria um novo serviço para o profissional autenticado
     */
    @Transactional
    public ServiceResponse createService(CreateServiceRequest request) {
        Professional professional = getCurrentProfessional();

        ServiceOffering serviceOffering = ServiceOffering.builder()
                .professional(professional)
                .name(request.name())
                .price(request.price())
                .active(true)
                .build();

        ServiceOffering saved = serviceOfferingRepository.save(serviceOffering);
        log.info("Serviço {} criado para profissional {}", saved.getId(), professional.getId());

        return serviceDtoMapper.toResponse(saved);
    }

    /**
     * Atualiza um serviço do profissional autenticado
     */
    @Transactional
    public ServiceResponse updateService(UUID serviceId, UpdateServiceRequest request) {
        Professional professional = getCurrentProfessional();

        ServiceOffering serviceOffering = serviceOfferingRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado"));

        // Validar que o serviço pertence ao profissional
        if (!serviceOffering.getProfessional().getId().equals(professional.getId())) {
            throw new BusinessException("Você não tem permissão para atualizar este serviço");
        }

        serviceOffering.setName(request.name());
        serviceOffering.setPrice(request.price());

        ServiceOffering updated = serviceOfferingRepository.save(serviceOffering);
        log.info("Serviço {} atualizado pelo profissional {}", updated.getId(), professional.getId());

        return serviceDtoMapper.toResponse(updated);
    }

    /**
     * Deleta um serviço do profissional autenticado
     * Valida que não há agendamentos ativos com este serviço
     */
    @Transactional
    public void deleteService(UUID serviceId) {
        Professional professional = getCurrentProfessional();

        ServiceOffering serviceOffering = serviceOfferingRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado"));

        // Validar que o serviço pertence ao profissional
        if (!serviceOffering.getProfessional().getId().equals(professional.getId())) {
            throw new BusinessException("Você não tem permissão para deletar este serviço");
        }

        // Verificar agendamentos específicos deste serviço que estão ativos
        List<com.tamarcado.domain.model.appointment.Appointment> appointments =
                appointmentRepository.findByProfessionalId(professional.getId());

        boolean hasActiveAppointments = appointments.stream()
                .anyMatch(apt -> apt.getServiceOffering().getId().equals(serviceId)
                        && (apt.getStatus() == AppointmentStatus.PENDING
                            || apt.getStatus() == AppointmentStatus.ACCEPTED));

        if (hasActiveAppointments) {
            throw new BusinessException(
                    "Não é possível excluir o serviço pois existem agendamentos ativos (pendentes ou aceitos)"
            );
        }

        // Soft delete - desativar serviço
        serviceOffering.setActive(false);
        serviceOfferingRepository.save(serviceOffering);

        log.info("Serviço {} desativado pelo profissional {}", serviceId, professional.getId());
    }

    /**
     * Obtém o profissional autenticado atual
     */
    private Professional getCurrentProfessional() {
        String email = SecurityUtils.getCurrentUsername();

        if (email == null) {
            throw new BusinessException("Usuário não autenticado");
        }

        // Buscar usuário pelo email
        com.tamarcado.domain.model.user.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // Buscar profissional pelo ID do usuário
        Professional professional = professionalRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException("Usuário não é um profissional"));

        if (!professional.getActive()) {
            throw new BusinessException("Profissional inativo");
        }

        return professional;
    }

}
