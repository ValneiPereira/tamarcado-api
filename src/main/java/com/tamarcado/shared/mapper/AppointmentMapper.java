package com.tamarcado.shared.mapper;

import com.tamarcado.domain.model.appointment.Appointment;
import com.tamarcado.shared.dto.response.AppointmentProfessionalResponse;
import com.tamarcado.shared.dto.response.AppointmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ServiceMapper.class})
public interface AppointmentMapper {

    @Mapping(target = "professionalId", source = "professional.id")
    @Mapping(target = "professionalName", source = "professional.user.name")
    @Mapping(target = "professionalPhone", source = "professional.user.phone")
    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientName", source = "client.name")
    @Mapping(target = "service", source = "serviceOffering")
    AppointmentResponse toResponse(Appointment appointment);

    List<AppointmentResponse> toResponseList(List<Appointment> appointments);

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientName", source = "client.name")
    @Mapping(target = "clientPhone", source = "client.phone")
    @Mapping(target = "distance", ignore = true)
    @Mapping(target = "service", source = "serviceOffering")
    AppointmentProfessionalResponse toProfessionalResponse(Appointment appointment);

    List<AppointmentProfessionalResponse> toProfessionalResponseList(List<Appointment> appointments);
}
