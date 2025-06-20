package org.codewith3h.finmateapplication.mapper;

import org.codewith3h.finmateapplication.EntityResolver;
import org.codewith3h.finmateapplication.dto.request.CreateBudgetRequest;
import org.codewith3h.finmateapplication.dto.request.UpdateBudgetRequest;
import org.codewith3h.finmateapplication.dto.response.BudgetAnalysisResponse;
import org.codewith3h.finmateapplication.dto.response.BudgetResponse;
import org.codewith3h.finmateapplication.entity.Budget;
import org.codewith3h.finmateapplication.entity.Transaction;
import org.codewith3h.finmateapplication.repository.TransactionRepository;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Mapper(componentModel = "spring")
public interface BudgetMapper {

    @AfterMapping
    default void resolveRelations(@MappingTarget Budget budget,
                                  CreateBudgetRequest createBudgetRequest,
                                  @Context EntityResolver entityResolver) {
        budget.setUser(entityResolver.resolverUser(createBudgetRequest.getUserId()));
        if (createBudgetRequest.getCategoryId() != null) {
            budget.setCategory(entityResolver.resolverCategory(createBudgetRequest.getCategoryId()));
        } else if (createBudgetRequest.getUserCategoryId() != null) {
            budget.setUserCategory(entityResolver.resolverUserCategory(createBudgetRequest.getUserCategoryId()));
        }
    }

    default LocalDate calculateEndDate(LocalDate startDate, String periodType) {
        if (periodType.equalsIgnoreCase("WEEKLY")) {
            return startDate.plusDays(7);
        }else if (periodType.equalsIgnoreCase("MONTHLY")) {
            return startDate.plusMonths(1);
        }
        return startDate;
    }

    @Mapping(target = "endDate", expression = "java(calculateEndDate(request.getStartDate(), request.getPeriodType()))")
    Budget toBudget(CreateBudgetRequest request, @Context EntityResolver entityResolver);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "userCategoryId", source = "userCategory.id")
    @Mapping(target = "userCategoryName", source = "userCategory.name")
    @Mapping(target = "currentSpending", expression = "java(calculateCurrentSpending(budget, transactionRepository))")
    BudgetResponse toBudgetResponse(Budget budget, @Context TransactionRepository transactionRepository);

    @AfterMapping
    default void enhanceBudgetResponse(@MappingTarget BudgetResponse response, Budget budget) {
        response.setPercentageUsed(calculatePercentageUsed(budget.getAmount(), response.getCurrentSpending()));
        response.setStatus(determineStatus(response.getPercentageUsed(), budget.getNotificationThreshold()));
    }


    @Mapping(target = "endDate", expression = "java(calculateEndDate(request.getStartDate(), request.getPeriodType()))")
    void updateBudget(@MappingTarget Budget budget, UpdateBudgetRequest request, @Context EntityResolver entityResolver);

    @Mapping(target = "budgetId", source = "id")
    @Mapping(target = "plannedAmount", source = "amount")
    @Mapping(target = "actualSpending", source = "currentSpending")
    BudgetAnalysisResponse budgetResponseToAnalysisResponse(BudgetResponse budgetResponse);

    default BigDecimal calculateCurrentSpending(Budget budget, @Context TransactionRepository transactionRepository) {
        List<Transaction> transactions = transactionRepository.findByUserIdAndCategoryIdAndDateRange(
                budget.getUser().getId(),
                budget.getCategory() != null ? budget.getCategory().getId() : budget.getUserCategory() != null ? budget.getUserCategory().getId() : null,
                budget.getStartDate(),
                budget.getEndDate() != null ? budget.getEndDate() : LocalDate.now()
        );

        transactions.forEach(transaction -> {
            System.out.println("TransactionId: " + transaction.getId() + " Amount: " + transaction.getAmount());
        });
        var a = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        System.out.println("Spending: " + a);
        return a;
    }

    default BigDecimal calculatePercentageUsed(BigDecimal budgetAmount, BigDecimal currentSpending) {
        if (budgetAmount == null || budgetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return currentSpending.divide(budgetAmount, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    default String determineStatus(BigDecimal percentageUsed, Integer notificationThreshold) {
        if (percentageUsed == null || notificationThreshold == null) {
            return "UNKNOWN";
        }
        if (percentageUsed.compareTo(BigDecimal.valueOf(notificationThreshold)) >= 0) {
            return "OVER_THRESHOLD";
        }
        return "WITHIN_LIMIT";
    }
}