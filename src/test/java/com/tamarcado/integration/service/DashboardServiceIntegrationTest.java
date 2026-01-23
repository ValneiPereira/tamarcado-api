package com.tamarcado.integration.service;

import com.tamarcado.AbstractIntegrationTest;
import com.tamarcado.TestUtils;
import com.tamarcado.application.port.out.AppointmentRepositoryPort;
import com.tamarcado.application.port.out.ProfessionalRepositoryPort;
import com.tamarcado.application.port.out.ServiceOfferingRepositoryPort;
import com.tamarcado.application.service.DashboardService;
import com.tamarcado.domain.model.appointment.Appointment;
import com.tamarcado.domain.model.appointment.AppointmentStatus;
import com.tamarcado.domain.model.service.ServiceOffering;
import com.tamarcado.domain.model.user.Professional;
import com.tamarcado.domain.model.user.User;
import com.tamarcado.shared.dto.response.ClientDashboardResponse;
import com.tamarcado.shared.dto.response.ProfessionalDashboardResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class DashboardServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private AppointmentRepositoryPort appointmentRepository;

    @Autowired
    private ProfessionalRepositoryPort professionalRepository;

    @Autowired
    private ServiceOfferingRepositoryPort serviceOfferingRepository;

    private User client;
    private User professional;

    @BeforeEach
    void setUp() {
        client = testUtils.createTestClient("client@dashboard.com", "Cliente Dashboard");
        professional = testUtils.createTestProfessional("prof@dashboard.com", "Prof Dashboard");
    }

    @Test
    void shouldGetProfessionalDashboard() {
        ProfessionalDashboardResponse dashboard = dashboardService.getProfessionalDashboard(professional.getId());

        assertThat(dashboard).isNotNull();
        assertThat(dashboard.todayAppointments()).isNotNull();
        assertThat(dashboard.pendingAppointments()).isNotNull();
        assertThat(dashboard.averageRating()).isNotNull();
        assertThat(dashboard.totalRatings()).isNotNull();
        assertThat(dashboard.monthRevenue()).isNotNull();
        assertThat(dashboard.completedThisMonth()).isNotNull();
    }

    @Test
    void shouldGetClientDashboard() {
        ClientDashboardResponse dashboard = dashboardService.getClientDashboard(client.getId());

        assertThat(dashboard).isNotNull();
        assertThat(dashboard.upcomingAppointments()).isNotNull();
        assertThat(dashboard.completedAppointments()).isNotNull();
    }

    @Test
    void shouldCalculateCorrectStatistics() {
        // Criar alguns agendamentos
        Professional prof = professionalRepository.findById(professional.getId())
                .orElseThrow();

        ServiceOffering service = ServiceOffering.builder()
                .professional(prof)
                .name("Serviço Teste")
                .price(BigDecimal.valueOf(100.00))
                .active(true)
                .build();
        service = serviceOfferingRepository.save(service);

        // Agendamento de hoje
        Appointment todayAppointment = Appointment.builder()
                .client(client)
                .professional(prof)
                .serviceOffering(service)
                .date(LocalDate.now())
                .time(LocalTime.of(14, 0))
                .status(AppointmentStatus.PENDING)
                .build();
        appointmentRepository.save(todayAppointment);

        // Agendamento completado este mês
        Appointment completedAppointment = Appointment.builder()
                .client(client)
                .professional(prof)
                .serviceOffering(service)
                .date(LocalDate.now().minusDays(5))
                .time(LocalTime.of(14, 0))
                .status(AppointmentStatus.COMPLETED)
                .build();
        appointmentRepository.save(completedAppointment);

        ProfessionalDashboardResponse dashboard = dashboardService.getProfessionalDashboard(professional.getId());

        assertThat(dashboard.todayAppointments()).isGreaterThanOrEqualTo(1);
        assertThat(dashboard.pendingAppointments()).isGreaterThanOrEqualTo(1);
        assertThat(dashboard.completedThisMonth()).isGreaterThanOrEqualTo(1);
        assertThat(dashboard.monthRevenue()).isGreaterThanOrEqualTo(BigDecimal.valueOf(100.00));
    }
}
