package org.codewith3h.finmateapplication.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.CouponRequest;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.CouponResponse;
import org.codewith3h.finmateapplication.service.CouponService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupon")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CouponController {
    private final CouponService couponService;

    @GetMapping("/{couponId}")
    public ResponseEntity<ApiResponse<CouponResponse>> getCoupon(@PathVariable Integer couponId) {
        log.info("Fetching coupon {}", couponId);
        CouponResponse couponResponse = couponService.getCoupon(couponId);

        ApiResponse<CouponResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Coupon fetched successfully.");
        apiResponse.setResult(couponResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CouponResponse>>> getCoupons(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "6") @Min(1) int size,
            @RequestParam(defaultValue = "usedCount") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        log.info("Fetching coupons");
        Page<CouponResponse> couponResponses = couponService.getCoupons(page, size, sortBy, sortDirection);
        ApiResponse<Page<CouponResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Coupons fetched successfully.");
        apiResponse.setResult(couponResponses);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(
            @Valid @RequestBody CouponRequest dto
    ) {
        log.info("Creating coupon {}", dto);
        CouponResponse couponResponse = couponService.createCoupon(dto);
        ApiResponse<CouponResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Coupon created successfully.");
        apiResponse.setResult(couponResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{couponId}")
    public ResponseEntity<ApiResponse<CouponResponse>> updateCoupon(
            @Valid @RequestBody CouponRequest dto,
            @PathVariable Integer couponId
    ) {
        log.info("Updating coupon {}", dto);
        CouponResponse couponResponse = couponService.updateCoupon(couponId, dto);
        ApiResponse<CouponResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Coupon updated successfully.");
        apiResponse.setResult(couponResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{couponId}")
    public ResponseEntity<Void> deleteCoupon(
            @PathVariable Integer couponId
    ) {
        log.info("Deleting coupon {}", couponId);
        couponService.deleteCoupon(couponId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/validate/{code}")
    public ResponseEntity<ApiResponse<Boolean>> validateCoupon(@PathVariable String code ) {
        log.info("Validating coupon code {}", code);
        Boolean result = couponService.validateCouponCode(code);
        ApiResponse<Boolean> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Coupon code applied");
        apiResponse.setResult(result);
        log.info("Validated coupon code {}", code);
        return ResponseEntity.ok(apiResponse);
    }
}
