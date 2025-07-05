package org.codewith3h.finmateapplication.mapper;

import org.codewith3h.finmateapplication.dto.request.PromotionalOfferCreateRequest;
import org.codewith3h.finmateapplication.dto.response.PromotionalOfferResponse;
import org.codewith3h.finmateapplication.entity.PremiumPackage;
import org.codewith3h.finmateapplication.entity.PromotionalOffer;
import org.codewith3h.finmateapplication.repository.PremiumPackageRepository;
import org.mapstruct.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PromotionalOfferMapper {
    @Mapping(target = "premiumPackages", expression = "java(mapPackages(request.getPackageIds(), premiumPackageRepository))")
    PromotionalOffer toPromotionalOffer(PromotionalOfferCreateRequest request, @Context PremiumPackageRepository premiumPackageRepository);

    default Set<PremiumPackage> mapPackages(Set<Integer> packageIds, PremiumPackageRepository premiumPackageRepository) {
        if (packageIds == null) {
            return new HashSet<>();
        }
        return packageIds.stream()
                .map(premiumPackageRepository::findPremiumPackageById)
                .collect(Collectors.toSet());
    }

    @Mapping(target = "packageIds", expression = "java(mapSetPackageIds(promotionalOffer.getPremiumPackages()))")
    PromotionalOfferResponse toPromotionalOfferResponse(PromotionalOffer promotionalOffer);

    default Set<Integer> mapSetPackageIds(Set<PremiumPackage> premiumPackages) {
        if (premiumPackages == null) {
            return new HashSet<>();
        }
        return premiumPackages.stream()
                .map(PremiumPackage::getId)
                .collect(Collectors.toSet());
    }

}
