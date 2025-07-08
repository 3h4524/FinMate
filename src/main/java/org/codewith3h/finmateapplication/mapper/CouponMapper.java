package org.codewith3h.finmateapplication.mapper;

import org.codewith3h.finmateapplication.dto.request.CouponRequest;
import org.codewith3h.finmateapplication.dto.response.CouponResponse;
import org.codewith3h.finmateapplication.dto.response.PremiumPackageFetchResponse;
import org.codewith3h.finmateapplication.entity.Coupon;
import org.codewith3h.finmateapplication.entity.PremiumPackage;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.repository.PremiumPackageRepository;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CouponMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "expiryDate", expression = "java(toEndOfDay(dto.getExpiryDate()))")
    @Mapping(target = "premiumPackages", expression = "java(mapPremiumId(dto.getPremiumId(), premiumPackageRepository))")
    Coupon toEntity(CouponRequest dto, @Context PremiumPackageRepository  premiumPackageRepository);

    default List<PremiumPackage> mapPremiumId(List<Integer> premiumId, @Context PremiumPackageRepository premiumPackageRepository) {
        if(premiumId==null){
            return Collections.emptyList();
        }
        return premiumId.stream()
                .map(id -> premiumPackageRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.PREMIUM_PACKAGE_NOT_FOUND)))
                .collect(Collectors.toList());
    }

    @Mapping(target = "premiumPackages", expression = "java(mapPremiumPackageFetch(entity.getPremiumPackages()))")
    CouponResponse toResponseDto(Coupon entity);

    default List<PremiumPackageFetchResponse> mapPremiumPackageFetch(List<PremiumPackage> premiumPackages) {
        return premiumPackages.stream()
                .map(premium -> PremiumPackageFetchResponse.builder()
                        .id(premium.getId())
                        .name(premium.getName())
                        .build())
                .collect(Collectors.toList());
    }


    default LocalDateTime toEndOfDay(LocalDate date) {
        if (date == null) return null;
        return date.atTime(23, 59, 59, 997_000_000);
    }

    @Mapping(target = "premiumPackages", expression = "java(mapPremiumId(dto.getPremiumId(), premiumPackageRepository))")
    void updateEntityFromDto(CouponRequest dto, @MappingTarget Coupon entity, @Context PremiumPackageRepository premiumPackageRepository);


}
