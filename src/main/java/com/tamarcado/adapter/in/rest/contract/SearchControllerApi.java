package com.tamarcado.adapter.in.rest.contract;

import com.tamarcado.domain.model.service.Category;
import com.tamarcado.domain.model.service.ServiceType;
import com.tamarcado.shared.constant.ApiConstants;
import com.tamarcado.shared.dto.response.ApiResponse;
import com.tamarcado.shared.dto.response.ProfessionalSearchResponse;
import com.tamarcado.shared.dto.response.ServiceSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Tag(name = "Busca", description = "Endpoints para busca de serviços e profissionais")
@RequestMapping(ApiConstants.SEARCH_PATH)
public interface SearchControllerApi {

    @GetMapping("/services")
    @Operation(summary = "Buscar serviços", description = "Busca serviços agrupados por nome, categoria e tipo. Retorna preço mínimo, máximo e contagem de profissionais.")
    ResponseEntity<ApiResponse<List<ServiceSearchResponse>>> searchServices(
            @Parameter(description = "Categoria do serviço (opcional)") @RequestParam(required = false) Category category,
            @Parameter(description = "Tipo do serviço (opcional)") @RequestParam(required = false) ServiceType serviceType);

    @GetMapping("/professionals")
    @Operation(summary = "Buscar profissionais", description = "Busca profissionais por serviço, categoria/tipo ou ambos. Calcula distância geográfica se coordenadas forem fornecidas.")
    ResponseEntity<ApiResponse<Page<ProfessionalSearchResponse>>> searchProfessionals(
            @Parameter(description = "ID do serviço (opcional)") @RequestParam(required = false) UUID serviceId,
            @Parameter(description = "Categoria do profissional (opcional)") @RequestParam(required = false) Category category,
            @Parameter(description = "Tipo de serviço do profissional (opcional)") @RequestParam(required = false) ServiceType serviceType,
            @Parameter(description = "Latitude para cálculo de distância (opcional)") @RequestParam(required = false) Double latitude,
            @Parameter(description = "Longitude para cálculo de distância (opcional)") @RequestParam(required = false) Double longitude,
            @Parameter(description = "Distância máxima em km (opcional, requer coordenadas)") @RequestParam(required = false) Double maxDistanceKm,
            @Parameter(description = "Ordenação: 'distance' ou 'rating' (padrão: distance se coordenadas fornecidas)") @RequestParam(required = false, defaultValue = "distance") String sortBy,
            @Parameter(description = "Número da página (padrão: 0)") @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página (padrão: 20)") @RequestParam(required = false, defaultValue = "20") int size);
}
