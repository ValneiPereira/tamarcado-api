package com.tamarcado.shared.mapper;

import com.tamarcado.domain.model.user.User;
import com.tamarcado.shared.dto.response.UserResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);
}
