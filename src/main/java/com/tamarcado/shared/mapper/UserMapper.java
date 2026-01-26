package com.tamarcado.shared.mapper;

import com.tamarcado.domain.model.user.User;
import com.tamarcado.shared.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {AddressMapper.class})
public interface UserMapper {

    @Mapping(source = "address", target = "address")
    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);
}
