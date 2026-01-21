package com.tamarcado.application.service;

import com.tamarcado.application.port.out.ServiceOfferingRepositoryPort;
import com.tamarcado.domain.model.service.Category;
import com.tamarcado.domain.model.service.ServiceOffering;
import com.tamarcado.domain.model.service.ServiceType;
import com.tamarcado.shared.dto.response.ServiceSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
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
