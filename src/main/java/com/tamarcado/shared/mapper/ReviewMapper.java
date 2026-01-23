package com.tamarcado.shared.mapper;

import com.tamarcado.domain.model.appointment.Appointment;
import com.tamarcado.domain.model.review.Review;
import com.tamarcado.domain.model.user.Professional;
import com.tamarcado.shared.dto.request.CreateReviewRequest;
import com.tamarcado.shared.dto.response.ReviewResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface ReviewMapper {

    @Mapping(target = "clientId", source = "appointment.client.id")
    @Mapping(target = "clientName", source = "appointment.client.name")
    ReviewResponse toResponse(Review review);

    List<ReviewResponse> toResponseList(List<Review> reviews);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appointment", source = "appointment")
    @Mapping(target = "professional", source = "professional")
    @Mapping(target = "rating", source = "request.rating")
    @Mapping(target = "comment", source = "request.comment")
    @Mapping(target = "createdAt", ignore = true)
    Review toEntity(CreateReviewRequest request, Appointment appointment, Professional professional);
}
