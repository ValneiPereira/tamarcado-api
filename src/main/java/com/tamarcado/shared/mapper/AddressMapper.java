package com.tamarcado.shared.mapper;

import com.tamarcado.domain.model.user.Address;
import com.tamarcado.shared.dto.response.AddressResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    @org.mapstruct.Named("addressToResponse")
    @Mapping(source = "latitude", target = "latitude")
    @Mapping(source = "longitude", target = "longitude")
    AddressResponse toResponse(Address address);
}
