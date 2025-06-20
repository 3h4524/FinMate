package org.codewith3h.finmateapplication.mapper;

import org.codewith3h.finmateapplication.dto.response.FeatureResponse;
import org.codewith3h.finmateapplication.entity.Feature;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface FeatureMapper {

    @Mapping(source = "code", target = "featureCode")
    @Mapping(source = "name", target = "featureName")
    FeatureResponse toResponseDto(Feature entity);
}
