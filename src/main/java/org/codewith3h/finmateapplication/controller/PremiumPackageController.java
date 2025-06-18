package org.codewith3h.finmateapplication.controller;

import lombok.RequiredArgsConstructor;
import org.codewith3h.finmateapplication.dto.request.PremiumPackageCreationDto;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.PremiumPackageResponse;
import org.codewith3h.finmateapplication.service.PremiumPackageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/premium-package")
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

}
