package com.tamarcado.shared.mapper;

import com.tamarcado.domain.model.user.Address;
import com.tamarcado.shared.dto.request.AddressRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AddressRequestMapper {

    @Mapping(target = "latitude", ignore = true)
    @Mapping(target = "longitude", ignore = true)
    Address toDomain(AddressRequest addressRequest);
}
