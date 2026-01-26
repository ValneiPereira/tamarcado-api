package com.tamarcado.adapter.in.rest;

import com.tamarcado.adapter.in.rest.contract.SearchControllerApi;
import com.tamarcado.application.service.SearchService;
import com.tamarcado.domain.model.service.Category;
import com.tamarcado.domain.model.service.ServiceType;
import com.tamarcado.shared.dto.response.ApiResponse;
import com.tamarcado.shared.dto.response.ProfessionalSearchResponse;
import com.tamarcado.shared.dto.response.ServiceSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SearchController implements SearchControllerApi {

        private final SearchService searchService;

        @Override
        public ResponseEntity<ApiResponse<List<ServiceSearchResponse>>> searchServices(Category category,
                        ServiceType serviceType) {

                log.debug("Buscando serviços - Categoria: {}, Tipo: {}", category, serviceType);
                var services = searchService.searchServices(category, serviceType);

                return ResponseEntity.ok(
                                ApiResponse.success(services, "Serviços encontrados com sucesso"));
        }

        @Override
        public ResponseEntity<ApiResponse<Page<ProfessionalSearchResponse>>> searchProfessionals(
                        UUID serviceId, Category category, ServiceType serviceType, Double latitude, Double longitude,
                        Double maxDistanceKm, String sortBy, int page, int size) {

                log.debug("Buscando profissionais - ServiceId: {}, Category: {}, ServiceType: {}, Lat: {}, Lng: {}",
                                serviceId, category, serviceType, latitude, longitude);

                Pageable pageable = PageRequest.of(page, size);
                Page<ProfessionalSearchResponse> professionals = searchService.searchProfessionals(
                                serviceId, category, serviceType, latitude, longitude, maxDistanceKm, sortBy, pageable);

                return ResponseEntity.ok(
                                ApiResponse.success(professionals, "Profissionais encontrados com sucesso"));
        }
}
