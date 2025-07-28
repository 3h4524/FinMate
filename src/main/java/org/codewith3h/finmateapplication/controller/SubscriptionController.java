package org.codewith3h.finmateapplication.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.RevenueAndSubscribers;
import org.codewith3h.finmateapplication.dto.response.SubscriptionResponse;
import org.codewith3h.finmateapplication.service.SubscriptionService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Slf4j
@Validated
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/revenue-subscribers")
    public ResponseEntity<ApiResponse<RevenueAndSubscribers>> getTotalRevenueAndSubscribers(){
        log.info("Fetching total revenue and subscribers.");

        RevenueAndSubscribers revenueAndSubscribers = subscriptionService.getTotalRevenueAndSubscriber();

        ApiResponse<RevenueAndSubscribers> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Revenue and subscribers fetched successfully");
        apiResponse.setResult(revenueAndSubscribers);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<Page<SubscriptionResponse>>> getRecentRevenueAndSubscribers(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "5") @Min(1) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ){
        log.info("fetching recent subscriptions");
        Page<SubscriptionResponse> subscriptionResponses = subscriptionService.getSubscriptions(page, size, sortBy, sortDirection);

        ApiResponse<Page<SubscriptionResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Recent subscriptions fetched successfully");
        apiResponse.setResult(subscriptionResponses);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/purchasedList")
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> getSubscriptionsPurchasedIsNotExpired() {
        log.info("Getting premium package purchased");
        List<SubscriptionResponse> packageResponses = subscriptionService.getSubscriptionsPurchasedIsNotExpired();

        ApiResponse<List<SubscriptionResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("purchased list retrieved successfully");
        apiResponse.setResult(packageResponses);
        return ResponseEntity.ok(apiResponse);
    }
}
