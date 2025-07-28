package org.codewith3h.finmateapplication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.FeatureRequestDto;
import org.codewith3h.finmateapplication.dto.request.FeatureSearchRequest;
import org.codewith3h.finmateapplication.dto.response.FeatureResponse;
import org.codewith3h.finmateapplication.dto.response.FeatureStatsResponse;
import org.codewith3h.finmateapplication.entity.Feature;
import org.codewith3h.finmateapplication.entity.PremiumPackage;
import org.codewith3h.finmateapplication.entity.Subscription;
import org.codewith3h.finmateapplication.enums.Status;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.mapper.FeatureMapper;
import org.codewith3h.finmateapplication.repository.FeatureRepository;
import org.codewith3h.finmateapplication.repository.SubscriptionRepository;
import org.codewith3h.finmateapplication.specification.FeatureSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.codewith3h.finmateapplication.util.AdminLogUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureService {

    private final FeatureRepository featureRepository;
    private final FeatureMapper featureMapper;
    private final SubscriptionRepository subscriptionRepository;
    private final AdminLogUtil adminLogUtil;

    public List<FeatureResponse> getFeaturesByIsActive() {

        log.info("Fetching features");
        List<Feature> features = featureRepository.findByIsActiveTrue();
        return features.stream().map(featureMapper::toResponseDto).collect(Collectors.toList());
    }

    public FeatureResponse getFeatureById(Integer featureId){
        log.info("Fetching feature {}", featureId);
        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() -> new AppException(ErrorCode.FEATURE_NOT_FOUND));

        return featureMapper.toResponseDto(feature);
    }

    public boolean userHasFeature(int userId, String featureCode) {
        List<Subscription> subscriptions = subscriptionRepository
                .findSubscriptionsByUser_IdAndStatus(userId, Status.ACTIVE.name());

        return subscriptions.stream()
                .flatMap(sub -> {
                    PremiumPackage pack = sub.getPremiumPackage();
                    if (pack == null || !pack.getIsActive() || pack.getFeatures() == null) {
                        return Stream.empty();
                    }
                    return pack.getFeatures().stream();
                })
                .anyMatch(feature -> feature.getIsActive() && feature.getCode().equals(featureCode));
    }

    @Transactional
    public FeatureResponse createFeature(FeatureRequestDto dto){
        log.info("Creating new feature");
        Feature feature = featureMapper.toEntity(dto);
        
        adminLogUtil.logFeatureAction("CREATE", null, feature.getName());
        
        log.info("Feature created successfully.");
        Feature savedFeature = featureRepository.save(feature);

        return featureMapper.toResponseDto(savedFeature);
    }

    @Transactional
    public FeatureResponse updateFeature(Integer featureId,FeatureRequestDto dto){
        log.info("Updating feature");
        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() -> new AppException(ErrorCode.FEATURE_NOT_FOUND));

        adminLogUtil.logFeatureAction("UPDATE", featureId, feature.getName());

        featureMapper.updateEntityFromDto(dto, feature);

        Feature updatedFeature = featureRepository.save(feature);
        log.info("Feature updated successfully.");
        return featureMapper.toResponseDto(updatedFeature);
    }

    @Transactional
    public void deleteFeature(Integer featureId){
        log.info("Deleting feature");
        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() -> new AppException(ErrorCode.FEATURE_NOT_FOUND));

        adminLogUtil.logFeatureAction("DELETE", featureId, feature.getName());

        featureRepository.delete(feature);
        log.info("Feature deleted successfully");
    }

    public Page<FeatureResponse> getFeatureByStatus(boolean status, int page, int size, String sortBy, String sortDirection){
        Sort sort = sortDirection.equalsIgnoreCase("ACS")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Feature> features = featureRepository.findByIsActive(status, pageable);
        return features.map(featureMapper::toResponseDto);
    }

        public FeatureStatsResponse getFeatureStatistics(){
            log.info("Fetching feature statistics");
            List<Feature> features =  featureRepository.findAll();
            int totalFeatures = features.size();
            int totalIsActive = (int) features.stream().filter(Feature :: getIsActive).count();
            return FeatureStatsResponse.builder()
                    .activeFeatures(totalIsActive)
                    .totalFeatures(totalFeatures)
                    .build();
        }

        public Page<FeatureResponse> searchFeature(FeatureSearchRequest dto){
            log.info("Searching feature");
            Specification<Feature> spec = (root, query, criteriaBuilder)
                    -> criteriaBuilder.conjunction();

            if(dto.getIsActive() != null){
                spec = spec.and(FeatureSpecification.hasActive(dto.getIsActive()));
            }

            if(dto.getFeatureName() != null){
                spec = spec.and(FeatureSpecification.hasName(dto.getFeatureName()));
            }

            if(dto.getFeatureCode() != null){
                spec = spec.and(FeatureSpecification.hasCode(dto.getFeatureCode()));
            }

            Sort sort = dto.getSortDirection().equalsIgnoreCase("DESC")
                    ? Sort.by(dto.getSortBy()).descending()
                    : Sort.by(dto.getSortBy()).ascending();

            Pageable pageable = PageRequest.of(dto.getPage(), dto.getSize(), sort);
            Page<Feature> features = featureRepository.findAll(spec, pageable);
            log.info("page {}", features.getTotalElements());
            return features.map(featureMapper :: toResponseDto);
        }


}
