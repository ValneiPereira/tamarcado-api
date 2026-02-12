package com.tamarcado.shared.mapper;

import com.tamarcado.domain.model.professional.BusinessHours;
import com.tamarcado.shared.dto.response.BusinessHoursResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring")
public interface BusinessHoursMapper {

    @Mapping(target = "startTime", expression = "java(formatTime(businessHours.getStartTime()))")
    @Mapping(target = "endTime", expression = "java(formatTime(businessHours.getEndTime()))")
    BusinessHoursResponse toResponse(BusinessHours businessHours);

    List<BusinessHoursResponse> toResponseList(List<BusinessHours> businessHours);

    default String formatTime(LocalTime time) {
        if (time == null) return null;
        return time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}
