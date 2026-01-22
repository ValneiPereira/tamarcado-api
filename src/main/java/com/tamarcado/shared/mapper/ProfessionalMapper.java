package com.tamarcado.shared.mapper;

import com.tamarcado.domain.model.user.Professional;
import com.tamarcado.shared.dto.response.ProfessionalDetailResponse;
import com.tamarcado.shared.dto.response.ProfessionalSearchResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class, AddressMapper.class, ServiceMapper.class, ReviewMapper.class})
public interface ProfessionalMapper {

    @Mapping(target = "name", source = "user.name")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "phone", source = "user.phone")
    @Mapping(target = "photo", source = "user.photo")
    @Mapping(target = "address", source = "user.address")
    @Mapping(target = "distanceKm", ignore = true)
    @Mapping(target = "services", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    ProfessionalDetailResponse toDetailResponseBase(Professional professional);

    @Mapping(target = "name", source = "user.name")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "phone", source = "user.phone")
    @Mapping(target = "photo", source = "user.photo")
    @Mapping(target = "distanceKm", ignore = true)
    ProfessionalSearchResponse toSearchResponse(Professional professional);
}
