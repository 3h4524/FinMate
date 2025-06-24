package org.codewith3h.finmateapplication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.response.FeatureResponse;
import org.codewith3h.finmateapplication.entity.Feature;
import org.codewith3h.finmateapplication.entity.PremiumPackage;
import org.codewith3h.finmateapplication.entity.Subscription;
import org.codewith3h.finmateapplication.mapper.FeatureMapper;
import org.codewith3h.finmateapplication.repository.FeatureRepository;
import org.codewith3h.finmateapplication.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

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

    public List<FeatureResponse> getFeaturesByIsActive() {

        log.info("Fetching features");
        List<Feature> features = featureRepository.findByIsActiveTrue();
        return features.stream().map(featureMapper::toResponseDto).collect(Collectors.toList());
    }

    public boolean userHasFeature(int userId, String featureCode) {
        List<Subscription> subscriptions = subscriptionRepository
                .findSubscriptionsByUser_IdAndStatus(userId, "ACTIVE");

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
}
