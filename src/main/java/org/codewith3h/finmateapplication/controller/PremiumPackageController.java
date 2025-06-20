package org.codewith3h.finmateapplication.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.codewith3h.finmateapplication.dto.request.PremiumPackageCreationDto;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.PremiumPackageResponse;
import org.codewith3h.finmateapplication.service.PremiumPackageService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/premium-package")
@Validated
@RequiredArgsConstructor
public class PremiumPackageController {

    private final PremiumPackageService premiumPackageService;

    @PostMapping
    public ResponseEntity<ApiResponse<PremiumPackageResponse>> createPremiumPackage(
            @RequestBody PremiumPackageCreationDto request
    ){
        PremiumPackageResponse premiumPackageResponse = premiumPackageService.createPremiumPackage(request);
        ApiResponse<PremiumPackageResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("premium package created successfully");
        apiResponse.setResult(premiumPackageResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PremiumPackageResponse>>> getListPremiumPackage(
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) int size,
            @RequestParam(name = "sortBy", defaultValue = "price") String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "DESC") String sortDirection) {
        System.out.println("getListPremiumPackage");
        Page<PremiumPackageResponse> responses = premiumPackageService.getListPremiumPackage(page, size, sortBy, sortDirection);
        ApiResponse<Page<PremiumPackageResponse>> apiResponse = new  ApiResponse<>();
        apiResponse.setMessage("List premium packages fetched successfully");
        apiResponse.setResult(responses);
        return ResponseEntity.ok(apiResponse);
    }

}
