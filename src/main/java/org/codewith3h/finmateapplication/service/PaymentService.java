package org.codewith3h.finmateapplication.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.EntityResolver;
import org.codewith3h.finmateapplication.dto.request.PaymentRequest;
import org.codewith3h.finmateapplication.dto.response.PaymentResponse;
import org.codewith3h.finmateapplication.entity.Coupon;
import org.codewith3h.finmateapplication.entity.PremiumPackage;
import org.codewith3h.finmateapplication.entity.Subscription;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.enums.Status;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.mapper.SubscriptionMapper;
import org.codewith3h.finmateapplication.repository.CouponRepository;
import org.codewith3h.finmateapplication.repository.PremiumPackageRepository;
import org.codewith3h.finmateapplication.repository.SubscriptionRepository;
import org.codewith3h.finmateapplication.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;
import vn.payos.type.PaymentLinkData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Objects;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ROLE_USER')")
public class PaymentService {

    SubscriptionRepository subscriptionRepository;
    SubscriptionMapper subscriptionMapper;
    PremiumPackageRepository premiumPackageRepository;
    EntityResolver entityResolver;
    PayOS payOS;
    UserRepository userRepository;
    CouponRepository couponRepository;

    static BigDecimal HUNDRED = BigDecimal.valueOf(100);

    @Transactional
    public String createPaymentLink(PaymentRequest request) {

        int userId = (int) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        PremiumPackage premiumPackage = premiumPackageRepository.findPremiumPackageById(request.getPackageId());

        String packageName = premiumPackage.getName();

        Coupon coupon = couponRepository.findByCode(request.getCode()).orElse(null);

        int price = calculateFinalPrice(coupon, request.getAmount());

        log.info("User [{}] is initiating payment for package [{}], coupon code: [{}], price: [{}]",
                userId, request.getPackageId(), request.getCode(), price);

        Subscription subscription = subscriptionMapper.toSubscription(userId, request.getPackageId(), price, premiumPackageRepository, entityResolver);

        subscriptionRepository.save(subscription);

        // description maximum 25 characters
        String truncatedPackageName = packageName.length() > 12 ? packageName.substring(0, 12) + "..." : packageName;
        String description = "Purchasing " + truncatedPackageName;

        log.info("Description: " + description);
        String returnUrl = "http://127.0.0.1:5500/pages/user_premium/";
        String cancelUrl = "http://127.0.0.1:5500/pages/user_premium/";

        if (coupon != null) {
            returnUrl += "?couponId=" + coupon.getId();
        }

        // Generate order code (Do trung` duoc.) (ID + 6 chu so lay tu time)
        String currentTimeString = String.valueOf(new Date().getTime());
        long orderCode = Long.parseLong(subscription.getId() + currentTimeString.substring(currentTimeString.length() - 6));

        ItemData item = ItemData.builder()
                .name(packageName)
                .quantity(1)
                .price(price)
                .build();


        PaymentData paymentData = PaymentData.builder()
                .orderCode(orderCode)
                .amount(price)
                .description(description)
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .item(item)
                .build();


        try {
            CheckoutResponseData data = payOS.createPaymentLink(paymentData);
            String checkoutUrl = data.getCheckoutUrl();
            log.info("link: {}", checkoutUrl);
            return checkoutUrl;

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.CANNOT_CREATE_PAYMENT_EXCEPTION);
        }


    }

    private int calculateFinalPrice(Coupon coupon, Integer paymentPrice) {

        BigDecimal price = BigDecimal.valueOf(paymentPrice);

        if (!Objects.isNull(coupon)) {
            BigDecimal couponDiscount = coupon.getDiscountPercentage();

            price = price.multiply(BigDecimal.valueOf(1).subtract(
                    couponDiscount.divide(HUNDRED, 4, RoundingMode.HALF_UP)));
        }

        return price.setScale(0, RoundingMode.HALF_UP).intValue();
    }


    public boolean handlePaymentReturn(PaymentResponse response) {
        try {
            PaymentLinkData order = payOS.getPaymentLinkInformation(response.getOrderCode());
            String orderCodeStr = String.valueOf(order.getOrderCode());
            Integer subscriptionId = Integer.parseInt(orderCodeStr.substring(0, orderCodeStr.length() - 6));
            String status = order.getStatus();

            log.info("Processing payment return for orderId: {}, status: {}", order.getOrderCode(), status);
            Subscription subscription = subscriptionRepository.findSubscriptionById(subscriptionId);

            if (status.equals("PAID")) {
                // xu ly success
                subscription.setStatus(Status.ACTIVE.name());

                User user = subscription.getUser();
                if (Boolean.FALSE.equals(user.getIsPremium())) {
                    user.setIsPremium(true);
                    userRepository.save(user);
                }

                subscriptionRepository.save(subscription);

                Integer couponId = response.getCouponId();

                if (couponId != null) {
                    Coupon coupon = couponRepository.findById(couponId)
                            .orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_FOUND));

                    coupon.getUsers().add(user);
                    coupon.setUsedCount(coupon.getUsedCount() + 1);


                    log.info("Added user {} to coupon {}", user.getId(), coupon.getCode());

                    couponRepository.save(coupon);
                }

                log.info("User premium package [{}] is active for user [{}]. ExpiryDate: [{}]",
                        subscription.getPremiumPackage().getName(),
                        subscription.getUser().getName(),
                        subscription.getEndDate());
                return true;
            } else if (status.equals(Status.CANCELLED.name())) {
                log.info("Payment Cancelled by user [{}]", subscription.getUser().getName());
            } else {
                log.info("Payment is still pending for user [{}]", subscription.getUser().getName());
            }
            subscription.setStatus(status);
            subscriptionRepository.save(subscription);
            return false;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.CANNOT_CREATE_PAYMENT_EXCEPTION);
        }
    }
}
