package org.codewith3h.finmateapplication.mapper;

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

    @Mapping(target = "notificationId", source = "id")
    public NotificationResponse toDto(Notification entity);
}
