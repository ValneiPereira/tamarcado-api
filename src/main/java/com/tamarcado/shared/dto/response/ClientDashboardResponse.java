package com.tamarcado.shared.dto.response;

import com.tamarcado.domain.model.service.Category;

public record ClientDashboardResponse(
        Integer upcomingAppointments,
        Integer completedAppointments,
        Category favoriteCategory
) {}
