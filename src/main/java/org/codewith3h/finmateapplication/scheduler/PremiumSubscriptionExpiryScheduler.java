package org.codewith3h.finmateapplication.scheduler;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PremiumSubscriptionExpiryScheduler {

    @Scheduled(cron = "0 0 0 * * *")
    private void scanExpiredSubscriptions() {

    }

}
