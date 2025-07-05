package org.codewith3h.finmateapplication.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.PromotionalOfferCreateRequest;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.PromotionalOfferResponse;
import org.codewith3h.finmateapplication.service.PromotionalOfferService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/promotional-offer")
@RequiredArgsConstructor
@Slf4j
@Validated
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PromotionalOfferController {
    PromotionalOfferService promotionalOfferService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createPromotionalOffer(@Valid @RequestBody PromotionalOfferCreateRequest request) {
        Set<String> result = promotionalOfferService.createOffer(request);
        ApiResponse<Void> response = new ApiResponse<>();
        response.setCode(result.isEmpty() ? 1000 : 0);
        response.setMessage(result.isEmpty() ? "Create an offer successfully" :
                "The following packages are either currently in a promotion or have a promotion that hasn't started yet: " + String.join(", ", result));
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Set<PromotionalOfferResponse>>> getPromotionalOffers() {
        // is Active and in range (start date to expiry date, đã và đang bắt đầu nhé)
        Set<PromotionalOfferResponse> promotionalOfferResponses = promotionalOfferService.getCurrentApplicableOffers();

        ApiResponse<Set<PromotionalOfferResponse>> response = new ApiResponse<>();
        response.setResult(promotionalOfferResponses);
        return ResponseEntity.ok(response);
    }
}
