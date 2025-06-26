package org.codewith3h.finmateapplication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.CouponRequest;
import org.codewith3h.finmateapplication.dto.response.CouponResponse;
import org.codewith3h.finmateapplication.entity.Coupon;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.mapper.CouponMapper;
import org.codewith3h.finmateapplication.repository.CouponRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {
    private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;

    public CouponResponse getCoupon(Integer couponId){
        log.info("Fetching data for coupon {}", couponId);

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_FOUND));

        log.info("Coupon fetched successfully.");
        return couponMapper.toResponseDto(coupon);
    }

    public Page<CouponResponse> getCoupons(int page, int size, String sortBy, String sortDirection){
        log.info("Fetching all coupons");

        Sort sort = sortDirection.equalsIgnoreCase("ACS")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Coupon> couponPage = couponRepository.findAll(pageable);
        return couponPage.map(couponMapper :: toResponseDto);
    }

    public CouponResponse createCoupon(CouponRequest couponRequest){
        log.info("Creating coupon");
        Coupon coupon = couponMapper.toEntity(couponRequest);

        Coupon couponCreated = couponRepository.save(coupon);
        return couponMapper.toResponseDto(couponCreated);
    }

    public CouponResponse updateCoupon(Integer couponId,  CouponRequest couponRequest){
        log.info("Updating coupon {}", couponId);
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_FOUND));

        couponMapper.updateEntityFromDto(couponRequest, coupon);
        Coupon couponUpdated = couponRepository.save(coupon);
        return couponMapper.toResponseDto(couponUpdated);
    }

    public void deleteCoupon(Integer couponId){
        log.info("Deleting coupon {}", couponId);
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_FOUND));
        couponRepository.delete(coupon);
        log.info("Coupon deleted successfully.");
    }
}
