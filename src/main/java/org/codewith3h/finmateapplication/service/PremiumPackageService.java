package org.codewith3h.finmateapplication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.PremiumPackageCreationDto;
import org.codewith3h.finmateapplication.dto.response.PremiumPackageResponse;
import org.codewith3h.finmateapplication.dto.response.RevenueAndSubscribers;
import org.codewith3h.finmateapplication.entity.PremiumPackage;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.mapper.PremiumPackageMapper;
import org.codewith3h.finmateapplication.repository.FeatureRepository;
import org.codewith3h.finmateapplication.repository.PremiumPackageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PremiumPackageService {

    private final PremiumPackageRepository premiumPackageRepository;
    private final PremiumPackageMapper premiumPackageMapper;
    private final FeatureRepository featureRepository;
    private final SubscriptionService  subscriptionService;

    public PremiumPackageResponse createPremiumPackage(PremiumPackageCreationDto request) {

        log.info("Creating premium package.");
        PremiumPackage premiumPackage = premiumPackageMapper.toEntity(request, featureRepository);

        premiumPackageRepository.save(premiumPackage);
        log.info("Premium package created successfully!");
        return premiumPackageMapper.toResponseDto(premiumPackage);
    }

    public PremiumPackageResponse getPremiumPackageDetail(Integer id){

        log.info("Fetching premium package's information");
        PremiumPackage premiumPackage = premiumPackageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PREMIUM_PACKAGE_NOT_FOUND));

        return premiumPackageMapper.toResponseDto(premiumPackage);
    }

    public Page<PremiumPackageResponse> getPremiumPackages(int page, int size, String sortBy, String sortDirection ){
        log.info("Fetching premium packages");
        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PremiumPackage> premiumPackages = premiumPackageRepository.findAll(pageable);

        Page<PremiumPackageResponse> responsePages = premiumPackages.map(premiumPackage -> {
            PremiumPackageResponse response = premiumPackageMapper.toResponseDto(premiumPackage);
            RevenueAndSubscribers revenueAndSubscription = subscriptionService.getRevenueAndSubscriptionForPremiumPackage(premiumPackage);
            response.setRevenue(revenueAndSubscription.getRevenue());
            response.setSubscribers(revenueAndSubscription.getSubscribers());
            return response;
        });
        log.info("Premium packages fetched successfully.");
        return responsePages;
    }

    public PremiumPackageResponse updatePremiumPackage(Integer id, PremiumPackageCreationDto request) {
        log.info("Updating premium package.");

        PremiumPackage premiumPackage = premiumPackageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PREMIUM_PACKAGE_NOT_FOUND));

        premiumPackageMapper.updateEntityFromDto(request, premiumPackage, featureRepository);

        premiumPackageRepository.save(premiumPackage);
        log.info("Premium package updated successfully.");
        return  premiumPackageMapper.toResponseDto(premiumPackage);
    }

    public void deletePremiumPackage(Integer id) {
        log.info("Deleting premium package.");
        PremiumPackage premiumPackage = premiumPackageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PREMIUM_PACKAGE_NOT_FOUND));

        premiumPackageRepository.delete(premiumPackage);
        log.info("Premium package deleted successfully.");
    }

}
