package com.tamarcado.adapter.in.rest;

import com.tamarcado.application.service.DashboardService;
import com.tamarcado.shared.constant.ApiConstants;
import com.tamarcado.shared.dto.response.ApiResponse;
import com.tamarcado.shared.dto.response.ClientDashboardResponse;
import com.tamarcado.shared.dto.response.ProfessionalDashboardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(ApiConstants.DASHBOARD_PATH)
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Endpoints para dashboards de profissionais e clientes")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/professional/stats")
    @Operation(summary = "Dashboard do profissional",
               description = "Retorna estatísticas do profissional autenticado: agendamentos de hoje, pendentes, média de avaliações, receita do mês, etc.")
    public ResponseEntity<ApiResponse<ProfessionalDashboardResponse>> getProfessionalDashboard() {
        log.debug("Buscando dashboard do profissional autenticado");

        ProfessionalDashboardResponse dashboard = dashboardService.getMyProfessionalDashboard();

        return ResponseEntity.ok(
                ApiResponse.success(dashboard, "Dashboard do profissional obtido com sucesso")
        );
    }

    @GetMapping("/client/stats")
    @Operation(summary = "Dashboard do cliente",
               description = "Retorna estatísticas do cliente autenticado: próximos agendamentos, completados, categoria favorita, etc.")
    public ResponseEntity<ApiResponse<ClientDashboardResponse>> getClientDashboard() {
        log.debug("Buscando dashboard do cliente autenticado");

        ClientDashboardResponse dashboard = dashboardService.getMyClientDashboard();

        return ResponseEntity.ok(
                ApiResponse.success(dashboard, "Dashboard do cliente obtido com sucesso")
        );
    }
}
