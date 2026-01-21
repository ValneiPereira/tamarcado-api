package com.tamarcado.shared.mapper;

import com.tamarcado.domain.model.service.ServiceOffering;
import com.tamarcado.shared.dto.response.ServiceResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ServiceDtoMapper {

    ServiceResponse toResponse(ServiceOffering serviceOffering);

    List<ServiceResponse> toResponseList(List<ServiceOffering> serviceOfferings);
}
