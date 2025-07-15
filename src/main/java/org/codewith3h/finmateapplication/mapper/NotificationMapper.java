package org.codewith3h.finmateapplication.mapper;

import org.codewith3h.finmateapplication.dto.request.NotificationRequest;
import org.codewith3h.finmateapplication.dto.response.NotificationResponse;
import org.codewith3h.finmateapplication.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface NotificationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    public Notification toEntity(NotificationRequest dto);

    public NotificationResponse toDto(Notification entity);
}
