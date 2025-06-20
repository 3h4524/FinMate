package org.codewith3h.finmateapplication.mapper;

import org.codewith3h.finmateapplication.dto.response.SubscriptionResponse;
import org.codewith3h.finmateapplication.entity.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface SubscriptionMapper {

    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "premiumPackage.name", target = "packageName")
    @Mapping(source = "status", target = "status")
    @Mapping(source ="amount", target = "amount")
    SubscriptionResponse toResponseDto(Subscription entity);
}
