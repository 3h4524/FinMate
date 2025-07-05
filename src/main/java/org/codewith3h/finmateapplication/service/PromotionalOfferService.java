package org.codewith3h.finmateapplication.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.PromotionalOfferCreateRequest;
import org.codewith3h.finmateapplication.dto.response.PromotionalOfferResponse;
import org.codewith3h.finmateapplication.entity.PromotionalOffer;
import org.codewith3h.finmateapplication.entity.PromotionalOfferPremiumPackage;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.mapper.PromotionalOfferMapper;
import org.codewith3h.finmateapplication.repository.PremiumPackageRepository;
import org.codewith3h.finmateapplication.repository.PromotionalOfferPremiumPackageRepository;
import org.codewith3h.finmateapplication.repository.PromotionalOfferRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PromotionalOfferService {

    PromotionalOfferRepository promotionalOfferRepository;
    PromotionalOfferPremiumPackageRepository promotionalOfferPremiumPackageRepository;
    PromotionalOfferMapper promotionalOfferMapper;
    PremiumPackageRepository premiumPackageRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Set<String> createOffer(PromotionalOfferCreateRequest request) {
        Set<String> packageNameUnderPromotionSet = setPackagesUnderPromotion(request.getPackageIds());

        if (packageNameUnderPromotionSet.isEmpty()) {
            log.info("Creating promotional offer.");
            PromotionalOffer promotionalOffer = promotionalOfferMapper.toPromotionalOffer(request, premiumPackageRepository);

            promotionalOfferRepository.save(promotionalOffer);
            log.info("Successfully created promotional offer.");
        }

        return packageNameUnderPromotionSet;
    }

    private Set<String> setPackagesUnderPromotion(Set<Integer> setPackage) {
        Set<String> packageNameUnderPromotionSet = new HashSet<>();
        setPackage.forEach(pkgId -> {
            Set<PromotionalOfferPremiumPackage> promotionalOfferPremiumPackageSet =
                    promotionalOfferPremiumPackageRepository.findPromotionalOfferPremiumPackagesByPackageFieldId(pkgId);

            for (PromotionalOfferPremiumPackage promotionalOfferPremiumPackage : promotionalOfferPremiumPackageSet) {
                if (validateInPromotionalOffer(promotionalOfferPremiumPackage.getOffer())) {
                    packageNameUnderPromotionSet.add(promotionalOfferPremiumPackage.getPackageField().getName());
                    break;
                }
            }

        });
        return packageNameUnderPromotionSet;
    }

    private boolean validateInPromotionalOffer(PromotionalOffer promotionalOffer) {
        LocalDate now = LocalDate.now();
        if (promotionalOffer.getExpiryDate().isBefore(now)) return false;

        return promotionalOffer.getIsActive();
    }


    public Set<PromotionalOfferResponse> getCurrentApplicableOffers() {
        Set<PromotionalOffer> promotionalOfferSet = promotionalOfferRepository
                .findByIsActiveAndStartDateLessThanEqualAndExpiryDateGreaterThanEqual(true, LocalDate.now(), LocalDate.now());

        return promotionalOfferSet
                .stream()
                .map(promotionalOfferMapper::toPromotionalOfferResponse)
                .collect(Collectors.toSet());
    }
}
