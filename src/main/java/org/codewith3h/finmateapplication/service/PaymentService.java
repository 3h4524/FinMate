package org.codewith3h.finmateapplication.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.EntityResolver;
import org.codewith3h.finmateapplication.dto.request.PremiumPaymentRequest;
import org.codewith3h.finmateapplication.dto.response.PaymentResponse;
import org.codewith3h.finmateapplication.entity.PremiumPackage;
import org.codewith3h.finmateapplication.entity.Subscription;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.mapper.SubscriptionMapper;
import org.codewith3h.finmateapplication.repository.PremiumPackageRepository;
import org.codewith3h.finmateapplication.repository.SubcriptionRepository;
import org.codewith3h.finmateapplication.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;
import vn.payos.type.PaymentLinkData;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ROLE_USER')")
public class PaymentService {

    SubcriptionRepository subcriptionRepository;
    SubscriptionMapper subscriptionMapper;
    PremiumPackageRepository premiumPackageRepository;
    EntityResolver entityResolver;
    PayOS payOS;
    private final UserRepository userRepository;


    public String createPaymentLink(PremiumPaymentRequest request) {


        PremiumPackage premiumPackage = premiumPackageRepository.findPremiumPackageById(request.getPackageId());

        log.info("User [{}] is initiating payment for package [{}], price: [{}]",
                request.getUserId(), request.getPackageId(), premiumPackage.getPrice());

        String packageName = premiumPackage.getName();
        Integer price = premiumPackage.getPrice();

        Subscription subscription = subscriptionMapper.toSubscription(request, premiumPackageRepository, entityResolver);

        subcriptionRepository.save(subscription);

        // description maximum 25 characters
        String description = "Purchasing " + packageName;
        System.out.println("Description: " + description);
        String returnUrl = "http://127.0.0.1:5500/pages/confirmationPayment.html";
        String cancelUrl = "http://127.0.0.1:5500/pages/confirmationPayment.html";

        System.err.println("Vao tao link");
//        // Generate order code (Do trung` duoc.)
//        String currentTimeString = String.valueOf(new Date().getTime());
//        long orderCode = Long.parseLong(currentTimeString.substring(currentTimeString.length() - 6));

        // tui su dung orderCode = subscription.getId

        ItemData item = ItemData.builder()
                .name(packageName)
                .quantity(1)
                .price(price)
                .build();

        PaymentData paymentData = PaymentData.builder()
                .orderCode(subscription.getId().longValue())
                .amount(price)
                .description(description)
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .item(item)
                .build();


        try {
            CheckoutResponseData data = payOS.createPaymentLink(paymentData);
            String checkoutUrl = data.getCheckoutUrl();
            System.err.println("link: " + checkoutUrl);
            return checkoutUrl;

        } catch (Exception e) {
            System.err.println(e.getMessage());
            throw new AppException(ErrorCode.CANNOT_CREATE_PAYMENT_EXCEPTION);
        }


    }

    public boolean handlePaymentReturn(PaymentResponse response) {
        try {
            PaymentLinkData order = payOS.getPaymentLinkInformation(response.getOrderCode());
            Long subscriptionId = order.getOrderCode();
            String status = order.getStatus();

            log.info("Processing payment return for orderId: {}, status: {}", order.getOrderCode(), status);
            Subscription subscription = subcriptionRepository.findSubscriptionById(subscriptionId.intValue());

            if (status.equals("PAID")) {
                // xu ly success
                User user = subscription.getUser();

                subscription.setStatus("ACTIVE");
                user.setIsPremium(true);
                subcriptionRepository.save(subscription);

                log.info("User premium package [{}] is active for user [{}]. ExpiryDate: [{}]",
                        subscription.getPremiumPackage().getName(),
                        subscription.getUser().getName(),
                        subscription.getEndDate());
                return true;
            } else if (status.equals("CANCELLED")) {
                log.info("Payment Cancelled by user [{}]", subscription.getUser().getName());
            } else {
                log.info("Payment is still pending for user [{}]", subscription.getUser().getName());
            }
            subscription.setStatus(status);
            subcriptionRepository.save(subscription);
            return false;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            throw new AppException(ErrorCode.CANNOT_CREATE_PAYMENT_EXCEPTION);
        }
    }
}
