package org.codewith3h.finmateapplication.util;

import lombok.RequiredArgsConstructor;
import org.codewith3h.finmateapplication.dto.request.AdminLogCreateRequest;
import org.codewith3h.finmateapplication.service.AdminLogService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminLogUtil {

    private final AdminLogService adminLogService;

    public void logAdminAction(String action, String entityType, Integer entityId, String details) {
        Integer adminId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        AdminLogCreateRequest logRequest = AdminLogCreateRequest.builder()
                .adminId(adminId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .build();
        
        adminLogService.createAdminLog(logRequest);
    }

    public void logUserAction(String action, Integer userId, String userEmail) {
        logAdminAction(action, "USER", userId, String.format("%s user with email: %s", action, userEmail));
    }

    public void logPremiumPackageAction(String action, Integer packageId, String packageName) {
        logAdminAction(action, "PREMIUM_PACKAGE", packageId, String.format("%s premium package: %s", action, packageName));
    }

    public void logFeatureAction(String action, Integer featureId, String featureName) {
        logAdminAction(action, "FEATURE", featureId, String.format("%s feature: %s", action, featureName));
    }

    public void logPromotionalOfferAction(String action, Integer offerId, String offerEvent) {
        logAdminAction(action, "PROMOTIONAL_OFFER", offerId, String.format("%s promotional offer: %s", action, offerEvent));
    }

    public void logCouponAction(String action, Integer couponId, String couponCode) {
        logAdminAction(action, "COUPON", couponId, String.format("%s coupon: %s", action, couponCode));
    }
} 