package org.codewith3h.finmateapplication.mapper;


import org.codewith3h.finmateapplication.EntityResolver;
import org.codewith3h.finmateapplication.dto.request.PremiumPaymentRequest;
import org.codewith3h.finmateapplication.entity.Subscription;
import org.codewith3h.finmateapplication.repository.PremiumPackageRepository;
import org.mapstruct.*;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "premiumPackage.id", source = "packageId")
    Subscription toSubscription(PremiumPaymentRequest premiumPaymentRequest, @Context PremiumPackageRepository premiumPackageRepository, @Context EntityResolver entityResolver);

    @AfterMapping
    default void calculateEndDate(@MappingTarget Subscription subscription, @Context PremiumPackageRepository premiumPackageRepository) {
        Instant purchaseDate = subscription.getStartDate() != null
                ? subscription.getStartDate()
                : Instant.now();
        Integer packageId = subscription.getPremiumPackage().getId();
        Integer durationDays = premiumPackageRepository.findPremiumPackageById(packageId).getDurationDays();
        subscription.setEndDate(purchaseDate.plusSeconds(durationDays * 24L * 60 * 60));
    }

    @AfterMapping
    default void resolveRelations(@MappingTarget Subscription subscription,
                                  PremiumPaymentRequest premiumPaymentRequest,
                                  @Context EntityResolver entityResolver) {
        subscription.setPremiumPackage(entityResolver.resolverPremiumPackage(premiumPaymentRequest.getPackageId()));
        subscription.setUser(entityResolver.resolverUser(premiumPaymentRequest.getUserId()));

    }
}
