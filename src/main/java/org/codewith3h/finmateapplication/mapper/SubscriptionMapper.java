package org.codewith3h.finmateapplication.mapper;


import org.codewith3h.finmateapplication.EntityResolver;
import org.codewith3h.finmateapplication.dto.request.PremiumPaymentRequest;
import org.codewith3h.finmateapplication.entity.Subscription;
import org.codewith3h.finmateapplication.enums.DurationType;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
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

        DurationType durationType = premiumPackageRepository.findPremiumPackageById(packageId).getDurationType();
        int durationDays;
        if (durationType == DurationType.MONTH) {
            durationDays = 30;
        } else if (durationType == DurationType.YEAR) {
            durationDays = 365;
        } else {
            throw new AppException(ErrorCode.DURATION_DATE_NOT_FOUND);
        }

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
