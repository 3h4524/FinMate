package org.codewith3h.finmateapplication.service;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.PremiumPackageCreationDto;
import org.codewith3h.finmateapplication.dto.response.PremiumPackageResponse;
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
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final SubscriptionService subscriptionService;

    public PremiumPackageResponse createPremiumPackage(PremiumPackageCreationDto request) {

        log.info("Creating premium package.");
        PremiumPackage premiumPackage = premiumPackageMapper.toEntity(request, featureRepository);

        premiumPackageRepository.save(premiumPackage);
        log.info("Premium package created successfully!");
        return premiumPackageMapper.toResponseDto(premiumPackage);
    }

    public PremiumPackageResponse getPremiumPackageDetail(Integer id) {

        log.info("Fetching premium package's information");
        PremiumPackage premiumPackage = premiumPackageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PREMIUM_PACKAGE_NOT_FOUND));

        PremiumPackageResponse premiumPackageResponse = premiumPackageMapper.toResponseDto(premiumPackage);

        List<Object> revenueAndSubscribers = subscriptionService.getRevenueAndSubscriptionForPremiumPackage(premiumPackage);
        BigDecimal revenue = (BigDecimal) revenueAndSubscribers.get(0);
        Integer subscribers = (Integer) revenueAndSubscribers.get(1);

        premiumPackageResponse.setRevenue(revenue);
        premiumPackageResponse.setSubscribers(subscribers);

        return premiumPackageResponse;
    }

    @PreAuthorize("hasRole('USER')")
    public Page<PremiumPackageResponse> getListPremiumPackage(@Min(0) int page, @Min(1) int size, String sortBy, String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<PremiumPackage> responses = premiumPackageRepository.findAll(pageable);
        responses.getContent().forEach(p ->
                System.out.println(p.toString()));
        return responses.map(premiumPackageMapper::toResponseDto);
    }
}
