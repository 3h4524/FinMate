package org.codewith3h.finmateapplication.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.CouponRequest;
import org.codewith3h.finmateapplication.dto.request.CouponSearchRequest;
import org.codewith3h.finmateapplication.dto.response.CouponResponse;
import org.codewith3h.finmateapplication.entity.Coupon;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.entity.PremiumPackage;
import org.codewith3h.finmateapplication.entity.PremiumPackageCoupon;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.mapper.CouponMapper;
import org.codewith3h.finmateapplication.repository.CouponRepository;
import org.codewith3h.finmateapplication.repository.UserRepository;
import org.codewith3h.finmateapplication.repository.PremiumPackageCouponRepository;
import org.codewith3h.finmateapplication.repository.PremiumPackageRepository;
import org.codewith3h.finmateapplication.specification.CouponSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.codewith3h.finmateapplication.util.AdminLogUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CouponService {
    CouponRepository couponRepository;
    CouponMapper couponMapper;
    UserRepository userRepository;
    PremiumPackageRepository premiumPackageRepository;
    AdminLogUtil adminLogUtil;

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

    @Transactional
    public CouponResponse createCoupon(CouponRequest couponRequest) {
        log.info("Creating coupon");
        Coupon coupon = couponMapper.toEntity(couponRequest, premiumPackageRepository);

        adminLogUtil.logCouponAction("CREATE", null, coupon.getCode());

        Coupon couponCreated = couponRepository.save(coupon);
        return couponMapper.toResponseDto(couponCreated);
    }

    @Transactional
    public CouponResponse updateCoupon(Integer couponId, CouponRequest couponRequest) {
        log.info("Updating coupon {}", couponId);
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_FOUND));

        adminLogUtil.logCouponAction("UPDATE", couponId, coupon.getCode());

        couponMapper.updateEntityFromDto(couponRequest, coupon, premiumPackageRepository);
        Coupon couponUpdated = couponRepository.save(coupon);
        return couponMapper.toResponseDto(couponUpdated);
    }

    @Transactional
    public void deleteCoupon(Integer couponId) {
        log.info("Deleting coupon {}", couponId);
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_FOUND));

        adminLogUtil.logCouponAction("DELETE", couponId, coupon.getCode());

        couponRepository.delete(coupon);
        log.info("Coupon deleted successfully.");
    }

    public Page<CouponResponse> searchCoupon(CouponSearchRequest dto){
        log.info("Searching for dto {}", dto);

        Specification<Coupon> spec = (root, query, cb) -> cb.conjunction();

        if(dto.getIsActive() != null) {
            spec = spec.and(CouponSpecification.hasIsActive(dto.getIsActive()));
        }

        if(dto.getCode() != null){
            spec = spec.and(CouponSpecification.hasCode(dto.getCode()));
        }

        if(dto.getStartDate() != null || dto.getEndDate() != null){
            spec = spec.and(CouponSpecification.hasExpiryDateBetween(dto.getStartDate(), dto.getEndDate()));
        }

        Sort sort = dto.getSortDirection().equalsIgnoreCase("DESC")
                ? Sort.by(dto.getSortBy()).descending()
                : Sort.by(dto.getSortBy()).ascending();

        Pageable pageable = PageRequest.of(dto.getPage(), dto.getSize(), sort);

        Page<Coupon> couponPage = couponRepository.findAll(spec, pageable);
        return couponPage.map(couponMapper :: toResponseDto);
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
