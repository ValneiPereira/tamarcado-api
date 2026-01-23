package com.tamarcado.shared.dto.response;

import java.math.BigDecimal;

public record ProfessionalDashboardResponse(
        Integer todayAppointments,
        Integer pendingAppointments,
        Double averageRating,
        Long totalRatings,
        BigDecimal monthRevenue,
        Integer completedThisMonth
) {}
