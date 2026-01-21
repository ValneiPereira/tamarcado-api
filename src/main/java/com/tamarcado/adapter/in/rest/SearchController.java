package com.tamarcado.adapter.in.rest;

import com.tamarcado.application.service.SearchService;
import com.tamarcado.domain.model.service.Category;
import com.tamarcado.domain.model.service.ServiceType;
import com.tamarcado.shared.constant.ApiConstants;
import com.tamarcado.shared.dto.response.ApiResponse;
import com.tamarcado.shared.dto.response.ServiceSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(ApiConstants.SEARCH_PATH)
@RequiredArgsConstructor
@Tag(name = "Busca", description = "Endpoints para busca de serviços e profissionais")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/services")
    @Operation(summary = "Buscar serviços", 
               description = "Busca serviços agrupados por nome, categoria e tipo. Retorna preço mínimo, máximo e contagem de profissionais.")
    public ResponseEntity<ApiResponse<List<ServiceSearchResponse>>> searchServices(
            @Parameter(description = "Categoria do serviço (opcional)")
            @RequestParam(required = false) Category category,
            
            @Parameter(description = "Tipo do serviço (opcional)")
            @RequestParam(required = false) ServiceType serviceType
    ) {
        log.debug("Buscando serviços - Categoria: {}, Tipo: {}", category, serviceType);
        
        List<ServiceSearchResponse> services = searchService.searchServices(category, serviceType);
        
        return ResponseEntity.ok(
                ApiResponse.success(services, "Serviços encontrados com sucesso")
        );
    }
}
