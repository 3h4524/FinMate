package org.codewith3h.finmateapplication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.entity.PremiumPackage;
import org.codewith3h.finmateapplication.entity.Subscription;
import org.codewith3h.finmateapplication.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public List<Object> getRevenueAndSubscriptionForPremiumPackage(PremiumPackage premiumPackage) {
        List<Subscription> subscriptions = subscriptionRepository.findByPremiumPackage(premiumPackage);
        int revenue = subscriptions.stream()
                .mapToInt(Subscription::getAmount)
                .sum();

        Long subscription = (long) subscriptions.size();
        return List.of(revenue, subscription);
    }

}
