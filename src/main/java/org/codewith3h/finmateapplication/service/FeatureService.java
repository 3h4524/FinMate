package org.codewith3h.finmateapplication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.FeatureRequestDto;
import org.codewith3h.finmateapplication.dto.response.FeatureResponse;
import org.codewith3h.finmateapplication.entity.Feature;
import org.codewith3h.finmateapplication.entity.PremiumPackage;
import org.codewith3h.finmateapplication.entity.Subscription;
import org.codewith3h.finmateapplication.enums.Status;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.mapper.FeatureMapper;
import org.codewith3h.finmateapplication.repository.FeatureRepository;
import org.codewith3h.finmateapplication.repository.SubscriptionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureService {

    private final FeatureRepository featureRepository;
    private final FeatureMapper featureMapper;
    private final SubscriptionRepository subscriptionRepository;

    public List<FeatureResponse> getFeaturesByIsActive() {

        log.info("Fetching features");
        List<Feature> features = featureRepository.findByIsActiveTrue();
        return features.stream().map(featureMapper::toResponseDto).toList();
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

    public FeatureResponse createFeature(FeatureRequestDto dto){
        log.info("Creating new feature");
        Feature feature = featureMapper.toEntity(dto);
        log.info("Feature created successfully.");
        Feature savedFeature = featureRepository.save(feature);

        return featureMapper.toResponseDto(savedFeature);
    }

    public FeatureResponse updateFeature(Integer featureId,FeatureRequestDto dto){
        log.info("Updating feature");
        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() -> new AppException(ErrorCode.FEATURE_NOT_FOUND));

        featureMapper.updateEntityFromDto(dto, feature);

        Feature updatedFeature = featureRepository.save(feature);
        log.info("Feature updated successfully.");
        return featureMapper.toResponseDto(updatedFeature);
    }

    public void deleteFeature(Integer featureId){
        log.info("Deleting feature");
        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() -> new AppException(ErrorCode.FEATURE_NOT_FOUND));

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


}
