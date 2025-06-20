package org.codewith3h.finmateapplication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.response.RevenueAndSubscribers;
import org.codewith3h.finmateapplication.dto.response.SubscriptionResponse;
import org.codewith3h.finmateapplication.entity.PremiumPackage;
import org.codewith3h.finmateapplication.entity.Subscription;
import org.codewith3h.finmateapplication.mapper.SubscriptionMapper;
import org.codewith3h.finmateapplication.repository.SubscriptionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;

    public RevenueAndSubscribers getRevenueAndSubscriptionForPremiumPackage(PremiumPackage premiumPackage){
        List<Subscription> subscriptions =  subscriptionRepository.findByPremiumPackage(premiumPackage);
        BigDecimal revenue = subscriptions.stream()
                .map(Subscription::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Integer subscription = subscriptions.size();
        return RevenueAndSubscribers.builder()
                .subscribers(subscription)
                .revenue(revenue)
                .build();
    }


    public RevenueAndSubscribers getTotalRevenueAndSubscriber(){
        List<Subscription> subscriptions = subscriptionRepository.findAll();
        BigDecimal revenue = subscriptions.stream()
                .map(re -> re.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer subscription = subscriptions.size();
        return RevenueAndSubscribers.builder()
                .revenue(revenue)
                .subscribers(subscription)
                .build();
    }

    public Page<SubscriptionResponse> getSubscriptions(int page, int size, String sortBy, String sortDirection){
        log.info("Fetching subscription");
        Sort sort = sortDirection.equalsIgnoreCase("ACS")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Subscription> subscriptions = subscriptionRepository.findAll(pageable);

        return subscriptions.map(subscriptionMapper :: toResponseDto);
    }

}
