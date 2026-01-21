package com.tamarcado.application.service;

import com.tamarcado.application.port.out.ProfessionalRepositoryPort;
import com.tamarcado.application.port.out.ServiceOfferingRepositoryPort;
import com.tamarcado.domain.model.service.Category;
import com.tamarcado.domain.model.service.ServiceOffering;
import com.tamarcado.domain.model.service.ServiceType;
import com.tamarcado.domain.model.user.Address;
import com.tamarcado.domain.model.user.Professional;
import com.tamarcado.shared.dto.response.ProfessionalSearchResponse;
import com.tamarcado.shared.dto.response.ServiceSearchResponse;
import com.tamarcado.shared.mapper.ProfessionalDtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final ServiceOfferingRepositoryPort serviceOfferingRepository;
    private final ProfessionalRepositoryPort professionalRepository;
    private final ProfessionalDtoMapper professionalDtoMapper;

    /**
     * Busca serviços agrupados por nome, categoria e tipo
     * Calcula preço mínimo, máximo e conta profissionais
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "serviceSearch", key = "#category + '_' + #serviceType")
    public List<ServiceSearchResponse> searchServices(Category category, ServiceType serviceType) {
        log.debug("Buscando serviços - Categoria: {}, Tipo: {}", category, serviceType);

        // Buscar todos os serviços ativos que correspondem aos filtros
        List<ServiceOffering> services = serviceOfferingRepository
                .findActiveServicesByCategoryAndType(category, serviceType);

        // Agrupar por nome do serviço, categoria e tipo
        // Usar Map para armazenar informações de agrupamento
        Map<String, ServiceGroupInfo> serviceGroups = new LinkedHashMap<>();

        for (ServiceOffering service : services) {
            String key = buildServiceKey(service.getName(), 
                    service.getProfessional().getCategory(), 
                    service.getProfessional().getServiceType());

            serviceGroups.compute(key, (k, existing) -> {
                if (existing == null) {
                    // Primeira ocorrência deste serviço
                    Set<UUID> professionals = new HashSet<>();
                    professionals.add(service.getProfessional().getId());
                    
                    return new ServiceGroupInfo(
                            service.getName(),
                            service.getProfessional().getCategory(),
                            service.getProfessional().getServiceType(),
                            service.getPrice(),
                            service.getPrice(),
                            professionals
                    );
                } else {
                    // Já existe, atualizar min/max e adicionar profissional
                    BigDecimal minPrice = existing.minPrice.min(service.getPrice());
                    BigDecimal maxPrice = existing.maxPrice.max(service.getPrice());
                    existing.professionals.add(service.getProfessional().getId());
                    
                    return new ServiceGroupInfo(
                            existing.serviceName,
                            existing.category,
                            existing.serviceType,
                            minPrice,
                            maxPrice,
                            existing.professionals
                    );
                }
            });
        }

        // Converter para lista de respostas
        List<ServiceSearchResponse> result = serviceGroups.values().stream()
                .map(group -> new ServiceSearchResponse(
                        group.serviceName,
                        group.category,
                        group.serviceType,
                        group.minPrice,
                        group.maxPrice,
                        (long) group.professionals.size()
                ))
                .sorted(Comparator.comparing(ServiceSearchResponse::serviceName))
                .collect(Collectors.toList());

        log.debug("Encontrados {} serviços únicos", result.size());
        return result;
    }

    /**
     * Busca profissionais por serviço, categoria/tipo ou ambos
     * Calcula distância geográfica se coordenadas forem fornecidas
     * Ordena por distância ou rating
     */
    @Transactional(readOnly = true)
    public Page<ProfessionalSearchResponse> searchProfessionals(
            UUID serviceId,
            Category category,
            ServiceType serviceType,
            Double latitude,
            Double longitude,
            Double maxDistanceKm,
            String sortBy,
            Pageable pageable
    ) {
        log.debug("Buscando profissionais - ServiceId: {}, Category: {}, ServiceType: {}, Lat: {}, Lng: {}", 
                serviceId, category, serviceType, latitude, longitude);

        List<Professional> professionals;

        // Se serviceId fornecido e coordenadas disponíveis, usar busca geográfica otimizada
        if (serviceId != null && latitude != null && longitude != null && maxDistanceKm != null) {
            professionals = professionalRepository.findNearbyProfessionals(
                    serviceId, latitude, longitude, maxDistanceKm, 1000 // Limite alto para paginação
            );
        } else {
            // Busca simples sem distância
            if (serviceId != null) {
                // Buscar profissionais pelo serviço
                Optional<ServiceOffering> serviceOpt = serviceOfferingRepository.findById(serviceId);
                if (serviceOpt.isEmpty() || !serviceOpt.get().getActive()) {
                    return new PageImpl<>(Collections.emptyList(), pageable, 0);
                }
                professionals = List.of(serviceOpt.get().getProfessional());
            } else if (category != null || serviceType != null) {
                // Buscar por categoria/tipo
                professionals = professionalRepository.findByCategoryAndServiceType(category, serviceType);
            } else {
                // Buscar todos os profissionais ativos
                professionals = professionalRepository.findByActiveTrue();
            }

            // Filtrar apenas ativos
            professionals = professionals.stream()
                    .filter(p -> p.getActive() != null && p.getActive())
                    .filter(p -> p.getUser() != null && p.getUser().getActive() != null && p.getUser().getActive())
                    .collect(Collectors.toList());
        }

        // Converter para DTOs e calcular distância se necessário
        List<ProfessionalSearchResponse> responses = professionals.stream()
                .map(professional -> {
                    Double distance = null;
                    if (latitude != null && longitude != null && professional.getUser() != null 
                            && professional.getUser().getAddress() != null) {
                        Address address = professional.getUser().getAddress();
                        if (address.getLatitude() != null && address.getLongitude() != null) {
                            distance = calculateDistance(
                                    latitude, longitude,
                                    address.getLatitude(), address.getLongitude()
                            );
                            
                            // Filtrar por distância máxima se especificada
                            if (maxDistanceKm != null && distance > maxDistanceKm) {
                                return null;
                            }
                        }
                    }

                    ProfessionalSearchResponse response = professionalDtoMapper.toSearchResponse(professional);
                    return new ProfessionalSearchResponse(
                            response.id(),
                            response.name(),
                            response.email(),
                            response.phone(),
                            response.photo(),
                            response.category(),
                            response.serviceType(),
                            response.averageRating(),
                            response.totalRatings(),
                            distance
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Ordenar
        if ("distance".equalsIgnoreCase(sortBy) && latitude != null && longitude != null) {
            responses.sort(Comparator.comparing(ProfessionalSearchResponse::distanceKm, 
                    Comparator.nullsLast(Comparator.naturalOrder())));
        } else if ("rating".equalsIgnoreCase(sortBy)) {
            responses.sort(Comparator.comparing(ProfessionalSearchResponse::averageRating,
                    Comparator.nullsLast(Comparator.reverseOrder())));
        }

        // Paginação manual
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), responses.size());
        List<ProfessionalSearchResponse> pagedResults = start < responses.size() 
                ? responses.subList(start, end) 
                : Collections.emptyList();

        return new PageImpl<>(pagedResults, pageable, responses.size());
    }

    /**
     * Calcula distância entre dois pontos usando fórmula de Haversine
     * Retorna distância em quilômetros
     */
    public Double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;

        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Cria uma chave única para agrupar serviços
     */
    private String buildServiceKey(String name, Category category, ServiceType serviceType) {
        return String.format("%s_%s_%s", name.toUpperCase(), category, serviceType);
    }

    /**
     * Classe auxiliar para agrupar informações de serviços
     */
    private static class ServiceGroupInfo {
        final String serviceName;
        final Category category;
        final ServiceType serviceType;
        BigDecimal minPrice;
        BigDecimal maxPrice;
        final Set<UUID> professionals;

        ServiceGroupInfo(String serviceName, Category category, ServiceType serviceType,
                        BigDecimal minPrice, BigDecimal maxPrice, Set<UUID> professionals) {
            this.serviceName = serviceName;
            this.category = category;
            this.serviceType = serviceType;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
            this.professionals = professionals;
        }
    }
}
