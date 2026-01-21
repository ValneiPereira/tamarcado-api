package com.tamarcado.shared.mapper;

import com.tamarcado.domain.model.review.Review;
import com.tamarcado.shared.dto.response.ReviewResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserDtoMapper.class})
public interface ReviewDtoMapper {

    @Mapping(target = "clientId", source = "appointment.client.id")
    @Mapping(target = "clientName", source = "appointment.client.name")
    ReviewResponse toResponse(Review review);

    List<ReviewResponse> toResponseList(List<Review> reviews);
}
