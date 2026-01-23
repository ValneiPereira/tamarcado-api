package com.tamarcado.integration.repository;

import com.tamarcado.AbstractIntegrationTest;
import com.tamarcado.TestUtils;
import com.tamarcado.application.port.out.AppointmentRepositoryPort;
import com.tamarcado.application.port.out.ReviewRepositoryPort;
import com.tamarcado.domain.model.appointment.Appointment;
import com.tamarcado.domain.model.appointment.AppointmentStatus;
import com.tamarcado.domain.model.review.Review;
import com.tamarcado.domain.model.service.ServiceOffering;
import com.tamarcado.domain.model.user.Professional;
import com.tamarcado.domain.model.user.User;
import com.tamarcado.application.port.out.ProfessionalRepositoryPort;
import com.tamarcado.application.port.out.ServiceOfferingRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class ReviewRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ReviewRepositoryPort reviewRepository;

    @Autowired
    private AppointmentRepositoryPort appointmentRepository;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private ProfessionalRepositoryPort professionalRepository;

    @Autowired
    private ServiceOfferingRepositoryPort serviceOfferingRepository;

    private User client;
    private User professional;
    private Appointment appointment;
    private Review review;

    @BeforeEach
    void setUp() {
        client = testUtils.createTestClient("client@review.com", "Cliente Review");
        professional = testUtils.createTestProfessional("prof@review.com", "Prof Review");

        Professional prof = professionalRepository.findById(professional.getId())
                .orElseThrow();

        ServiceOffering service = ServiceOffering.builder()
                .professional(prof)
                .name("Serviço Teste")
                .price(BigDecimal.valueOf(50.00))
                .active(true)
                .build();
        service = serviceOfferingRepository.save(service);

        appointment = Appointment.builder()
                .client(client)
                .professional(prof)
                .serviceOffering(service)
                .date(LocalDate.now().plusDays(1))
                .time(LocalTime.of(14, 0))
                .status(AppointmentStatus.COMPLETED)
                .build();
        appointment = appointmentRepository.save(appointment);

        review = Review.builder()
                .appointment(appointment)
                .professional(prof)
                .rating(5)
                .comment("Excelente serviço!")
                .build();
        review = reviewRepository.save(review);
    }

    @Test
    void shouldFindReviewByAppointmentId() {
        Optional<Review> found = reviewRepository.findByAppointmentId(appointment.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(review.getId());
        assertThat(found.get().getRating()).isEqualTo(5);
    }

    @Test
    void shouldCheckIfReviewExistsByAppointmentId() {
        boolean exists = reviewRepository.existsByAppointmentId(appointment.getId());
        assertThat(exists).isTrue();

        boolean notExists = reviewRepository.existsByAppointmentId(java.util.UUID.randomUUID());
        assertThat(notExists).isFalse();
    }

    @Test
    void shouldCountReviewsByProfessionalId() {
        long count = reviewRepository.countByProfessionalId(professional.getId());
        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldCalculateAverageRating() {
        Double average = reviewRepository.calculateAverageRatingByProfessionalId(professional.getId());
        assertThat(average).isEqualTo(5.0);
    }
}
