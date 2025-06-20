package org.codewith3h.finmateapplication.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.FeatureResponse;
import org.codewith3h.finmateapplication.repository.FeatureRepository;
import org.codewith3h.finmateapplication.service.FeatureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/features")
@Slf4j
@RequiredArgsConstructor
public class FeatureController {

    private final FeatureService featureService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FeatureResponse>>> getFeatures() {
        log.info("fetching features");

        List<FeatureResponse> featureResponses = featureService.getFeaturesByIsActive();

        ApiResponse<List<FeatureResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Features fetched successfully");
        apiResponse.setResult(featureResponses);
        return  ResponseEntity.ok(apiResponse);
    }
}
