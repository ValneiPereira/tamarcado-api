package com.tamarcado.shared.mapper;

import com.tamarcado.domain.model.user.Address;
import com.tamarcado.shared.dto.response.AddressResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    AddressResponse toResponse(Address address);
}
