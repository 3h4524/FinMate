package org.codewith3h.finmateapplication.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.FeatureRequestDto;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.FeatureResponse;
import org.codewith3h.finmateapplication.entity.Feature;
import org.codewith3h.finmateapplication.repository.FeatureRepository;
import org.codewith3h.finmateapplication.service.FeatureService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/features")
@Slf4j
@RequiredArgsConstructor
@Validated
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

    @GetMapping("/{featureId}")
    public ResponseEntity<ApiResponse<FeatureResponse>> getFeature(
            @PathVariable Integer featureId) {
        log.info("fetching feature {}", featureId);
        FeatureResponse featureResponse = featureService.getFeatureById(featureId);

        ApiResponse<FeatureResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Feature fetched successfully");
        apiResponse.setResult(featureResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FeatureResponse>> createFeature(
            @Valid @RequestBody FeatureRequestDto featureRequestDto) {
        log.info("creating feature {}", featureRequestDto);
        FeatureResponse featureResponse = featureService.createFeature(featureRequestDto);
        ApiResponse<FeatureResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Feature created successfully");
        apiResponse.setResult(featureResponse);
        log.info("Feature created successfully.");
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{featureId}")
    public ResponseEntity<ApiResponse<FeatureResponse>> updateFeature(
            @PathVariable Integer featureId,
            @Valid @RequestBody FeatureRequestDto featureRequestDto) {
        log.info("Updating feature");
        FeatureResponse featureResponse = featureService.updateFeature(featureId, featureRequestDto);

        ApiResponse<FeatureResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Feature updated successfully");
        apiResponse.setResult(featureResponse);
        log.info("Feature updated successfully.");
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{featureId}")
    public ResponseEntity<Void> deleteFeature(
            @PathVariable Integer featureId
    ) {
        log.info("deleting feature {}", featureId);
        featureService.deleteFeature(featureId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<Page<FeatureResponse>>> getFeatureByStatus(
            @RequestParam boolean status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "code") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        log.info("Fetching feature with status is {}", status);
        Page<FeatureResponse> featureResponsePage = featureService
                .getFeatureByStatus(status, page, size, sortBy, sortDirection);

        ApiResponse<Page<FeatureResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Feature fetched successfully.");
        apiResponse.setResult(featureResponsePage);
        log.info("feature fetched successfully.");
        return ResponseEntity.ok(apiResponse);
    }
}
