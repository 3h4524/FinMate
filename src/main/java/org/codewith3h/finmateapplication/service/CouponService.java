package org.codewith3h.finmateapplication.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.CouponRequest;
import org.codewith3h.finmateapplication.dto.response.CouponResponse;
import org.codewith3h.finmateapplication.entity.Coupon;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.mapper.CouponMapper;
import org.codewith3h.finmateapplication.repository.CouponRepository;
import org.codewith3h.finmateapplication.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CouponService {
    CouponRepository couponRepository;
    CouponMapper couponMapper;
    UserRepository userRepository;

    public CouponResponse getCoupon(Integer couponId) {
        log.info("Fetching data for coupon {}", couponId);

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_FOUND));

        log.info("Coupon fetched successfully.");
        return couponMapper.toResponseDto(coupon);
    }

    public Page<CouponResponse> getCoupons(int page, int size, String sortBy, String sortDirection) {
        log.info("Fetching all coupons");

        Sort sort = sortDirection.equalsIgnoreCase("ACS")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Coupon> couponPage = couponRepository.findAll(pageable);
        return couponPage.map(couponMapper::toResponseDto);
    }

    public CouponResponse createCoupon(CouponRequest couponRequest) {
        log.info("Creating coupon");
        Coupon coupon = couponMapper.toEntity(couponRequest);

        Coupon couponCreated = couponRepository.save(coupon);
        return couponMapper.toResponseDto(couponCreated);
    }

    public CouponResponse updateCoupon(Integer couponId, CouponRequest couponRequest) {
        log.info("Updating coupon {}", couponId);
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_FOUND));

        couponMapper.updateEntityFromDto(couponRequest, coupon);
        Coupon couponUpdated = couponRepository.save(coupon);
        return couponMapper.toResponseDto(couponUpdated);
    }

    public void deleteCoupon(Integer couponId) {
        log.info("Deleting coupon {}", couponId);
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_FOUND));
        couponRepository.delete(coupon);
        log.info("Coupon deleted successfully.");
    }

    public boolean validateCouponCode(String code) {

        try {
            Coupon coupon = couponRepository.findByCode(code)
                    .orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_FOUND));

            if (coupon.getUsedCount() >= coupon.getMaxUsage()) {
                throw new AppException(ErrorCode.EXCEED_MAX_USAGE_COUPON);
            }

            if (!coupon.getIsActive()) {
                throw new AppException(ErrorCode.UNAVAILABLE_COUPON);
            }

            LocalDate now = LocalDate.now();
            if (coupon.getExpiryDate().toLocalDate().isBefore(now)) {
                throw new AppException(ErrorCode.EXPIRED_COUPON);
            }

            User user = userRepository.findById(
                            Integer.parseInt(SecurityContextHolder
                                    .getContext()
                                    .getAuthentication()
                                    .getPrincipal().toString()))
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            if (coupon.getUsers().contains(user)) {
                throw new AppException(ErrorCode.YOU_ALREADY_USED_THIS_COUPON);
            }

            return true;
        } catch (AppException e) {
            log.error(e.getMessage());
            throw e;
        }
    }
}
