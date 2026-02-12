package com.tamarcado.shared.dto.response;

import java.util.UUID;

public record BusinessHoursResponse(
        UUID id,
        Integer dayOfWeek,
        String startTime,
        String endTime,
        Boolean active
) {}
