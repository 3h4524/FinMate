package org.codewith3h.finmateapplication.mapper;

import org.codewith3h.finmateapplication.dto.request.PremiumPackageCreationDto;
import org.codewith3h.finmateapplication.dto.response.PremiumPackageResponse;
import org.codewith3h.finmateapplication.entity.Feature;
import org.codewith3h.finmateapplication.entity.PremiumPackage;
import org.codewith3h.finmateapplication.enums.DurationType;
import org.codewith3h.finmateapplication.enums.FeatureCode;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.repository.FeatureRepository;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PremiumPackageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "features", expression = "java(mapFeatureCodes(premiumPackageCreationDto.getFeatures(), featureRepository))")
    PremiumPackage toEntity(PremiumPackageCreationDto premiumPackageCreationDto, @Context FeatureRepository featureRepository);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "durationType", source = "durationType")
    @Mapping(target = "durationValue", source = "durationValue")
    @Mapping(target = "isActive", source = "isActive")
    @Mapping(target = "features", expression = "java(premiumPackage.getFeatures().stream().map(feature -> feature.getName()).collect(java.util.stream.Collectors.toList()))")
    PremiumPackageResponse toResponseDto(PremiumPackage premiumPackage);

    default List<Feature> mapFeatureCodes(List<FeatureCode> featureCodes, @Context FeatureRepository featureRepository) {
        if(featureCodes==null){
            return Collections.emptyList();
        }
        return featureCodes.stream()
                .map(code -> featureRepository.findByCode(code.name())
                        .orElseThrow(() -> new AppException(ErrorCode.FEATURE_NOT_FOUND)))
                .collect(Collectors.toList());
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "features", expression = "java(mapFeatureCodes(premiumPackageCreationDto.getFeatures(), featureRepository))")
    void updateEntityFromDto(PremiumPackageCreationDto premiumPackageCreationDto, @MappingTarget PremiumPackage entity, @Context FeatureRepository featureRepository);


}
