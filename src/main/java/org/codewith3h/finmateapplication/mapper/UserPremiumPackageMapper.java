package org.codewith3h.finmateapplication.mapper;


import org.codewith3h.finmateapplication.EntityResolver;
import org.codewith3h.finmateapplication.dto.request.PremiumPaymentRequest;
import org.codewith3h.finmateapplication.dto.request.RecurringTransactionRequest;
import org.codewith3h.finmateapplication.entity.RecurringTransaction;
import org.codewith3h.finmateapplication.entity.UserPremiumPackage;
import org.codewith3h.finmateapplication.repository.PremiumPackageRepository;
import org.mapstruct.*;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface UserPremiumPackageMapper {

    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "premiumPackage.id", source = "packageId")
    @Mapping(target = "expiryDate", ignore = true)
    UserPremiumPackage toUserPremiumPackage(PremiumPaymentRequest premiumPaymentRequest, @Context PremiumPackageRepository premiumPackageRepository, @Context EntityResolver entityResolver);

    @AfterMapping
    default void calculateExpiryDate(@MappingTarget UserPremiumPackage userPremiumPackage, @Context PremiumPackageRepository premiumPackageRepository) {
        Instant purchaseDate = userPremiumPackage.getPurchaseDate() != null
                ? userPremiumPackage.getPurchaseDate()
                : Instant.now();
        Integer packageId = userPremiumPackage.getPremiumPackage().getId();
        Integer durationDays = premiumPackageRepository.findPremiumPackageById(packageId).getDurationDays();
        userPremiumPackage.setExpiryDate(purchaseDate.plusSeconds(durationDays * 24L * 60 * 60));
    }

    @AfterMapping
    default void resolveRelations(@MappingTarget UserPremiumPackage userPremiumPackage,
                                  PremiumPaymentRequest premiumPaymentRequest,
                                  @Context EntityResolver entityResolver) {
        userPremiumPackage.setPremiumPackage(entityResolver.resolverPremiumPackage(premiumPaymentRequest.getPackageId()));
        userPremiumPackage.setUser(entityResolver.resolverUser(premiumPaymentRequest.getUserId()));

    }
}
