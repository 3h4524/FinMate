package org.codewith3h.finmateapplication.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.PremiumPackageCreationDto;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.PremiumPackageFetchResponse;
import org.codewith3h.finmateapplication.dto.response.PremiumPackageResponse;
import org.codewith3h.finmateapplication.service.PremiumPackageService;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/premium-package")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PremiumPackageController {

    private final PremiumPackageService premiumPackageService;

    @PostMapping
    public ResponseEntity<ApiResponse<PremiumPackageResponse>> createPremiumPackage(
            @Valid @RequestBody PremiumPackageCreationDto request
    ) {
        PremiumPackageResponse premiumPackageResponse = premiumPackageService.createPremiumPackage(request);
        ApiResponse<PremiumPackageResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("premium package created successfully");
        apiResponse.setResult(premiumPackageResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/fetchAll")
    public ResponseEntity<ApiResponse<List<PremiumPackageFetchResponse>>> fetchAllPremiumPackages() {
        log.info("Fetching all premium packages.");
        List<PremiumPackageFetchResponse> packageResponses = premiumPackageService.getAllPremiumPackages();
        ApiResponse<List<PremiumPackageFetchResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Premium packages fetched successfully.");
        apiResponse.setResult(packageResponses);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PremiumPackageResponse>>> getPremiumPackage(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "5") @Min(1) int size,
            @RequestParam(defaultValue = "price") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("Getting premium package");
        Page<PremiumPackageResponse> packageResponses = premiumPackageService
                .getPremiumPackages(page, size, sortBy, sortDirection);

        ApiResponse<Page<PremiumPackageResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("premium package list retrieved successfully");
        apiResponse.setResult(packageResponses);

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/purchasedList")
    public ResponseEntity<ApiResponse<List<PremiumPackageResponse>>> getPremiumPackagePurchased() {
        log.info("Getting premium package purchased");
        List<PremiumPackageResponse> packageResponses = premiumPackageService.getPremiumPackagesPurchased();

        ApiResponse<List<PremiumPackageResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("premium package purchased list retrieved successfully");
        apiResponse.setResult(packageResponses);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{packageId}")
    public ResponseEntity<ApiResponse<PremiumPackageResponse>> updatePremiumPackage(
            @PathVariable @Positive Integer packageId,
            @Valid @RequestBody PremiumPackageCreationDto request
    ) {
        PremiumPackageResponse packageResponse = premiumPackageService.updatePremiumPackage(packageId, request);

        ApiResponse<PremiumPackageResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("premium package updated successfully");
        apiResponse.setResult(packageResponse);
        return ResponseEntity.ok(apiResponse);
    }


    @DeleteMapping("/{packageId}")
    public ResponseEntity<Void> deletePremiumPackage(@PathVariable @Positive Integer packageId) {
        log.info("deleting premium package");
        premiumPackageService.deletePremiumPackage(packageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{packageId}")
    public ResponseEntity<ApiResponse<PremiumPackageResponse>> getPremiumPackage(
            @PathVariable @Positive Integer packageId
    ) {
        PremiumPackageResponse packageResponse = premiumPackageService.getPremiumPackageDetail(packageId);
        ApiResponse<PremiumPackageResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("premium package retrieved successfully");
        apiResponse.setResult(packageResponse);
        return ResponseEntity.ok(apiResponse);
    }

}
