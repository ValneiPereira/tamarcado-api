package com.tamarcado.adapter.in.rest;

import com.tamarcado.adapter.in.rest.contract.DashboardControllerApi;
import com.tamarcado.application.service.DashboardService;
import com.tamarcado.shared.dto.response.ApiResponse;
import com.tamarcado.shared.dto.response.ClientDashboardResponse;
import com.tamarcado.shared.dto.response.ProfessionalDashboardResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DashboardController implements DashboardControllerApi {

    private final DashboardService dashboardService;

    @Override
    public ResponseEntity<ApiResponse<ProfessionalDashboardResponse>> getProfessionalDashboard() {

        log.debug("Buscando dashboard do profissional autenticado");
        var dashboard = dashboardService.getMyProfessionalDashboard();

        return ResponseEntity.ok(
                ApiResponse.success(dashboard, "Dashboard do profissional obtido com sucesso"));
    }

    @Override
    public ResponseEntity<ApiResponse<ClientDashboardResponse>> getClientDashboard() {

        log.debug("Buscando dashboard do cliente autenticado");
        var dashboard = dashboardService.getMyClientDashboard();

        return ResponseEntity.ok(
                ApiResponse.success(dashboard, "Dashboard do cliente obtido com sucesso"));
    }
}
