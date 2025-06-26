package org.codewith3h.finmateapplication.scheduler;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.entity.Subscription;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.repository.SubscriptionRepository;
import org.codewith3h.finmateapplication.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PremiumSubscriptionExpiryScheduler {

    SubscriptionRepository subscriptionRepository;
    UserRepository userRepository;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
//    @Scheduled(cron = "0/10 * * * * *")
    public void scanExpiredSubscriptions() {
        // Đặt thời gian log về đầu ngày (00:00:00) theo múi giờ Asia/Ho_Chi_Minh
        LocalDateTime startOfDay = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        Instant startOfDayInstant = startOfDay.toInstant(ZoneOffset.ofHours(7));
        log.info("Starting scan for expired subscriptions at {}", startOfDayInstant);
        try {
            Pageable pageable = PageRequest.of(0, 100);
            Page<Subscription> page;

            do {
                page = subscriptionRepository.findSubscriptionsByStatusAndEndDateBefore(
                        "ACTIVE", startOfDayInstant, pageable);
                List<Subscription> subscriptions = page.getContent();
                updateExpiredSubscriptions(subscriptions);
                updateNonPremiumUsers(subscriptions);
                pageable = page.nextPageable();
            } while (page.hasNext());
        } catch (Exception e) {
            log.error("Error while processing expired subscriptions", e);
            throw new AppException(ErrorCode.CAN_NOT_PROCESS_EXPIRED_SUBSCRIPTION);
        }
    }

    private void updateExpiredSubscriptions(List<Subscription> subscriptions) {
        subscriptions.forEach(subscription -> subscription.setStatus("EXPIRED"));
        subscriptionRepository.saveAll(subscriptions);
        log.info("Updated {} subscriptions to EXPIRED", subscriptions.size());
    }

    private void updateNonPremiumUsers(List<Subscription> subscriptionsToExpire) {
        List<User> usersToUpdate = subscriptionsToExpire.stream()
                .map(Subscription::getUser)
                .filter(Objects::nonNull)
                .distinct()
                .filter(user -> !subscriptionRepository.existsByUserIdAndStatus(user.getId(), "ACTIVE"))
                .peek(user -> user.setIsPremium(false))
                .toList();

        userRepository.saveAll(usersToUpdate);
        log.info("Updated {} users to non-premium status", usersToUpdate.size());
    }
}