package org.codewith3h.finmateapplication.mapper;

import org.codewith3h.finmateapplication.dto.request.FeatureRequestDto;
import org.codewith3h.finmateapplication.dto.response.FeatureResponse;
import org.codewith3h.finmateapplication.entity.Feature;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface FeatureMapper {

    @Mapping(source = "code", target = "featureCode")
    @Mapping(source = "name", target = "featureName")
    @Mapping(source = "description", target = "featureDescription")
    FeatureResponse toResponseDto(Feature entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "code", target = "code")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    Feature toEntity(FeatureRequestDto featureRequestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "code", target = "code")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "active", target = "isActive")
    void updateEntityFromDto(FeatureRequestDto featureRequestDto, @MappingTarget Feature feature);
}
