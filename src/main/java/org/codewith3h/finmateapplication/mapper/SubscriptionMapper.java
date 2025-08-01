package org.codewith3h.finmateapplication.mapper;


import org.codewith3h.finmateapplication.EntityResolver;
import org.codewith3h.finmateapplication.dto.response.SubscriptionResponse;
import org.codewith3h.finmateapplication.entity.PremiumPackage;
import org.codewith3h.finmateapplication.entity.Subscription;
import org.codewith3h.finmateapplication.enums.DurationType;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.repository.PremiumPackageRepository;
import org.mapstruct.*;

import java.time.*;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubscriptionMapper {

    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "premiumPackage.id", source = "packageId")
    @Mapping(target = "amount", source = "price")
    Subscription toSubscription(Integer userId, Integer packageId, int price, @Context PremiumPackageRepository premiumPackageRepository, @Context EntityResolver entityResolver);


    @AfterMapping
    default void resolveRelations(@MappingTarget Subscription subscription,
                                  Integer userId, Integer packageId,
                                  @Context EntityResolver entityResolver) {
        subscription.setPremiumPackage(entityResolver.resolverPremiumPackage(packageId));
        subscription.setUser(entityResolver.resolverUser(userId));

    }

    @AfterMapping
    default void calculateEndDate(@MappingTarget Subscription subscription, @Context PremiumPackageRepository premiumPackageRepository) {
        LocalDate purchaseDate = subscription.getStartDate() != null
                ? subscription.getStartDate()
                : LocalDate.now();

        PremiumPackage premiumPackage = subscription.getPremiumPackage();

        DurationType durationType = premiumPackage.getDurationType();
        int durationDays;
        if (durationType == DurationType.MONTH) {
            durationDays = 30;
        } else if (durationType == DurationType.YEAR) {
            durationDays = 365;
        } else {
            throw new AppException(ErrorCode.DURATION_DATE_NOT_FOUND);
        }

        long totalDays = (long) durationDays * premiumPackage.getDurationValue();

        LocalDate endDate = purchaseDate.plusDays(totalDays);

        subscription.setEndDate(endDate);
    }

    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "premiumPackage.id", target = "packageId")
    @Mapping(source = "premiumPackage.name", target = "packageName")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "amount", target = "amount")
    SubscriptionResponse toResponseDto(Subscription entity);

}
