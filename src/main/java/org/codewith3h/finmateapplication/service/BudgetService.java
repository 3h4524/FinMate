package org.codewith3h.finmateapplication.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.codewith3h.finmateapplication.dto.request.CreateBudgetRequest;
import org.codewith3h.finmateapplication.dto.request.UpdateBudgetRequest;
import org.codewith3h.finmateapplication.dto.response.BudgetAnalysisResponse;
import org.codewith3h.finmateapplication.dto.response.BudgetResponse;
import org.codewith3h.finmateapplication.entity.*;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.mapper.BudgetMapper;
import org.codewith3h.finmateapplication.repository.BudgetRepository;
import org.codewith3h.finmateapplication.repository.CategoryRepository;
import org.codewith3h.finmateapplication.repository.TransactionRepository;
import org.codewith3h.finmateapplication.repository.UserCategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BudgetService {

    BudgetRepository budgetRepository;
    CategoryRepository categoryRepository;
    UserCategoryRepository userCategoryRepository;
    TransactionRepository transactionRepository;
    BudgetMapper budgetMapper;

    public BudgetService(BudgetRepository budgetRepository, CategoryRepository categoryRepository, UserCategoryRepository userCategoryRepository, TransactionRepository transactionRepository, BudgetMapper budgetMapper) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.userCategoryRepository = userCategoryRepository;
        this.transactionRepository = transactionRepository;
        this.budgetMapper = budgetMapper;
    }

    public BudgetResponse createBudget(CreateBudgetRequest request) {
        System.out.println("Vao createBudgetService: userId " + request.getUserId());
        if (request.getUserId() == null || request.getAmount() == null || request.getPeriodType() == null ||
                request.getStartDate() == null || (request.getCategoryId() == null)) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.AMOUNT_MUST_BE_POSITIVE);
        }

        if (!"DAILY".equalsIgnoreCase(request.getPeriodType()) &&
            !"WEEKLY".equalsIgnoreCase(request.getPeriodType()) &&
            !"MONTHLY".equalsIgnoreCase(request.getPeriodType())) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }

        Integer notificationThreshold = request.getNotificationThreshold() != null ? request.getNotificationThreshold() : 80;
        if (notificationThreshold < 0 || notificationThreshold > 100) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }

        LocalDate today = LocalDate.now();
        if (request.getStartDate().isBefore(today)) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }

        if (request.getEndDate() != null && !request.getEndDate().isAfter(request.getStartDate())) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }

        LocalDate endDate = request.getEndDate();
        if (endDate == null) {
            if ("DAILY".equalsIgnoreCase(request.getPeriodType())) {
                endDate = request.getStartDate();
            } else if ("WEEKLY".equalsIgnoreCase(request.getPeriodType())) {
                endDate = request.getStartDate().plusWeeks(1).minusDays(1);
            } else if ("MONTHLY".equalsIgnoreCase(request.getPeriodType())) {
                endDate = request.getStartDate().plusMonths(1).minusDays(1);
            }
        }

        Category category = null;
        UserCategory userCategory = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND_EXCEPTION));
        }

        Optional<Budget> existingBudget = budgetRepository.findByUserIdAndPeriodTypeAndStartDateAndCategoryId(
                request.getUserId(), request.getPeriodType(), request.getStartDate(), request.getCategoryId());
        if (existingBudget.isPresent()) {
            throw new AppException(ErrorCode.BUDGET_EXISTS);
        }

        Budget budget = budgetMapper.toBudget(request);
        budget.setCreatedAt(Instant.now());
        budget.setUpdatedAt(Instant.now());
        budget.setNotificationThreshold(notificationThreshold);
        budget.setEndDate(endDate);
        budget.setCategory(category);
        budget.setUserCategory(userCategory);
        try {
            budgetRepository.save(budget);
            BudgetResponse response = budgetMapper.toBudgetResponse(budget);
            BigDecimal currentSpending = calculateCurrentSpending(budget);
            response.setCurrentSpending(currentSpending);
            response.setPercentageUsed(calculatePercentageUsed(budget.getAmount(), currentSpending));
            response.setStatus(determineStatus(response.getPercentageUsed(), budget.getNotificationThreshold()));
            return response;
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    public BudgetResponse updateBudget(Integer budgetId, UpdateBudgetRequest request) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new AppException(ErrorCode.BUDGET_NOT_FOUND));

        if (!budget.getUser().getId().equals(request.getUserId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (request.getAmount() != null) {
            if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new AppException(ErrorCode.AMOUNT_MUST_BE_POSITIVE);
            }
            budget.setAmount(request.getAmount());
        }

        if (request.getPeriodType() != null) {
            if (!"DAILY".equalsIgnoreCase(request.getPeriodType()) &&
                    !"WEEKLY".equalsIgnoreCase(request.getPeriodType()) &&
                    !"MONTHLY".equalsIgnoreCase(request.getPeriodType())) {
                throw new AppException(ErrorCode.INVALID_INPUT);
            }
            budget.setPeriodType(request.getPeriodType());
        }

        if (request.getNotificationThreshold() != null) {
            if (request.getNotificationThreshold() < 0 || request.getNotificationThreshold() > 100) {
                throw new AppException(ErrorCode.INVALID_INPUT);
            }
            budget.setNotificationThreshold(request.getNotificationThreshold());
        }

        if (request.getStartDate() != null) {
            LocalDate today = LocalDate.now();
            if (request.getStartDate().isBefore(today)) {
                throw new AppException(ErrorCode.INVALID_INPUT);
            }
            budget.setStartDate(request.getStartDate());
        }

        if (request.getEndDate() != null) {
            if (!request.getEndDate().isAfter(budget.getStartDate())) {
                throw new AppException(ErrorCode.INVALID_INPUT);
            }
            budget.setEndDate(request.getEndDate());
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND_EXCEPTION));
            budget.setCategory(category);
        }

        if (request.getUserCategoryId() != null) {
            UserCategory userCategory = userCategoryRepository.findById(request.getUserCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND_EXCEPTION));
            budget.setUserCategory(userCategory);
        }

        budget.setUpdatedAt(Instant.now());
        budgetRepository.save(budget);

        BudgetResponse response = budgetMapper.toBudgetResponse(budget);
        BigDecimal currentSpending = calculateCurrentSpending(budget);
        response.setCurrentSpending(currentSpending);
        response.setPercentageUsed(calculatePercentageUsed(budget.getAmount(), currentSpending));
        response.setStatus(determineStatus(response.getPercentageUsed(), budget.getNotificationThreshold()));
        return response;
    }

    public void deleteBudget(Integer budgetId, Integer userId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new AppException(ErrorCode.BUDGET_NOT_FOUND));

        if (!budget.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        budgetRepository.delete(budget);
    }

    public Page<BudgetResponse> getBudgets(Integer userId, String periodType, LocalDate startDate, Pageable pageable) {
        Page<Budget> budgets = budgetRepository.findByUser_Id(userId, pageable);
        budgets.forEach(budget -> System.out.println(budget.toString()));
        if (budgets.isEmpty()) {
            throw new AppException(ErrorCode.BUDGET_NOT_FOUND);
        }

        List<Budget> filteredBudgets = budgets.getContent().stream()
                .filter(budget -> periodType == null || budget.getPeriodType().equalsIgnoreCase(periodType))
                .filter(budget -> startDate == null || budget.getStartDate().equals(startDate))
                .collect(Collectors.toList());

        Page<Budget> filteredPage = new PageImpl<>(filteredBudgets, pageable, filteredBudgets.size());

        return filteredPage.map(budget -> {
            BudgetResponse response = budgetMapper.toBudgetResponse(budget);
            BigDecimal currentSpending = calculateCurrentSpending(budget);
            response.setCurrentSpending(currentSpending);
            response.setPercentageUsed(calculatePercentageUsed(budget.getAmount(), currentSpending));
            response.setStatus(determineStatus(response.getPercentageUsed(), budget.getNotificationThreshold()));
            return response;
        });
    }

    public Page<BudgetAnalysisResponse> getBudgetAnalysis(Integer userId, String periodType, LocalDate startDate, Pageable pageable) {
        Page<Budget> budgets = budgetRepository.findByUser_Id(userId, pageable);
        System.out.println("list budgetS: ");
        budgets.forEach(budget -> System.out.println(budget.toString()));
        if (budgets.isEmpty()) {
            throw new AppException(ErrorCode.BUDGET_NOT_FOUND);
        }

        List<Budget> filteredBudgets = budgets.getContent().stream()
                .filter(budget -> periodType == null || budget.getPeriodType().equalsIgnoreCase(periodType))
                .filter(budget -> startDate == null || budget.getStartDate().equals(startDate))
                .collect(Collectors.toList());

        Page<Budget> filteredPage = new PageImpl<>(filteredBudgets, pageable, filteredBudgets.size());

        return filteredPage.map(budget -> {
            BudgetAnalysisResponse response = new BudgetAnalysisResponse();
            response.setBudgetId(budget.getId());
            response.setCategoryName(budget.getCategory() != null ? budget.getCategory().getName() : budget.getUserCategory() != null ? budget.getUserCategory().getName() : "Khác");
            response.setPlannedAmount(budget.getAmount());
            BigDecimal actualSpending = calculateCurrentSpending(budget);
            response.setActualSpending(actualSpending);
            response.setVariance(budget.getAmount().subtract(actualSpending));
            response.setPeriodType(budget.getPeriodType());
            response.setStatus(determineStatus(calculatePercentageUsed(budget.getAmount(), actualSpending), budget.getNotificationThreshold()));
            return response;
        });
    }

    private BigDecimal calculateCurrentSpending(Budget budget) {
        List<Transaction> transactions = transactionRepository.findByUserIdAndCategoryIdAndDateRange(
                budget.getUser().getId(),
                budget.getCategory() != null ? budget.getCategory().getId() : budget.getUserCategory() != null ? budget.getUserCategory().getId() : null,
                budget.getStartDate(),
                budget.getEndDate() != null ? budget.getEndDate() : LocalDate.now()
        );

        System.out.println("cal: transactions: " + transactions.toString());
        return transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculatePercentageUsed(BigDecimal plannedAmount, BigDecimal currentSpending) {
        if (plannedAmount == null || plannedAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return currentSpending
                .divide(plannedAmount, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP);
    }

    private String determineStatus(BigDecimal percentageUsed, Integer threshold) {
        if (percentageUsed.compareTo(BigDecimal.valueOf(threshold)) >= 0) {
            return "Over Budget";
        } else if (percentageUsed.compareTo(BigDecimal.valueOf(threshold * 0.9)) >= 0) {
            return "Approaching Limit";
        } else {
            return "On Track";
        }
    }
}