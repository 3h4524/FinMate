package org.codewith3h.finmateapplication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.response.FeatureResponse;
import org.codewith3h.finmateapplication.entity.Feature;
import org.codewith3h.finmateapplication.mapper.FeatureMapper;
import org.codewith3h.finmateapplication.repository.FeatureRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureService {

    private final FeatureRepository featureRepository;
    private final FeatureMapper featureMapper;

    public List<FeatureResponse> getFeaturesByIsActive() {

        log.info("Fetching features");
        List<Feature> features = featureRepository.findByIsActiveTrue();
        return features.stream().map(featureMapper::toResponseDto).collect(Collectors.toList());
    }
}
