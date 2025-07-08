package org.codewith3h.finmateapplication.service;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.EntityResolver;
import org.codewith3h.finmateapplication.dto.request.CreateBudgetRequest;
import org.codewith3h.finmateapplication.dto.request.UpdateBudgetRequest;
import org.codewith3h.finmateapplication.dto.response.BudgetAnalysisResponse;
import org.codewith3h.finmateapplication.dto.response.BudgetResponse;
import org.codewith3h.finmateapplication.entity.*;
import org.codewith3h.finmateapplication.enums.FeatureCode;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.mapper.BudgetMapper;
import org.codewith3h.finmateapplication.repository.BudgetRepository;
import org.codewith3h.finmateapplication.repository.CategoryRepository;
import org.codewith3h.finmateapplication.repository.TransactionRepository;
import org.codewith3h.finmateapplication.repository.UserCategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@Data
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ROLE_USER')")
public class BudgetService {

    BudgetRepository budgetRepository;
    CategoryRepository categoryRepository;
    UserCategoryRepository userCategoryRepository;
    TransactionRepository transactionRepository;
    BudgetMapper budgetMapper;
    EntityResolver entityResolver;
    FeatureService featureService;

    public BudgetResponse createBudget(CreateBudgetRequest request) {
        Integer currentUserId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Creating budget for userId: {}, periodType: {}, startDate: {}, categoryId: {}, userCategoryId: {}",
                currentUserId, request.getPeriodType(), request.getStartDate(), request.getCategoryId(), request.getUserCategoryId());

        LocalDate today = LocalDate.now();
        if (request.getStartDate().isBefore(today)) {
            log.error("Invalid start date for budget creation: {} is before today: {}", request.getStartDate(), today);
            throw new AppException(ErrorCode.INVALID_INPUT);
        }

        if (request.getCategoryId() == null && request.getUserCategoryId() == null) {
            log.error("Both categoryId and userCategoryId are null for budget creation by userId: {}", currentUserId);
            throw new AppException(ErrorCode.INVALID_INPUT);
        }

        Optional<Budget> existingBudget = budgetRepository.findByUserIdAndPeriodTypeAndStartDateAndCategoryOrUserCategory(
                currentUserId, request.getPeriodType(), request.getStartDate(),
                request.getCategoryId(), request.getUserCategoryId());
        if (existingBudget.isPresent()) {
            log.warn("Budget already exists for userId: {}, periodType: {}, startDate: {}, categoryId: {}, userCategoryId: {}",
                    currentUserId, request.getPeriodType(), request.getStartDate(), request.getCategoryId(), request.getUserCategoryId());
            throw new AppException(ErrorCode.BUDGET_EXISTS);
        }
        if (!featureService.userHasFeature(currentUserId, FeatureCode.UNLIMITED_BUDGET.getDisplayName())) {
            long currentBudgetCount = budgetRepository.countByUserId(currentUserId);
            if (currentBudgetCount >= 3) {
                throw new AppException(ErrorCode.BUDGET_LIMIT_EXCEEDED);
            }
        }
        Budget budget = budgetMapper.toBudget(request, entityResolver);
        log.info("Saving new budget for userId: {}, budgetId: {}", currentUserId, budget.getId());
        budget = budgetRepository.save(budget);
        log.info("Budget created successfully for userId: {}, budgetId: {}", currentUserId, budget.getId());
        return budgetMapper.toBudgetResponse(budget, transactionRepository);
    }

    public BudgetResponse updateBudget(Integer budgetId, UpdateBudgetRequest request) {
        log.info("Updating budget with budgetId: {} for userId: {}", budgetId,
                (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> {
                    log.error("Budget not found for budgetId: {}", budgetId);
                    return new AppException(ErrorCode.BUDGET_NOT_FOUND);
                });

        if (request.getStartDate() != null) {
            LocalDate today = LocalDate.now();
            if (request.getStartDate().isBefore(today)) {
                log.error("Invalid start date for budget update: {} is before today: {}", request.getStartDate(), today);
                throw new AppException(ErrorCode.INVALID_INPUT);
            }
        }

        if (request.getCategoryId() == null && request.getUserCategoryId() == null) {
            log.error("Both categoryId and userCategoryId are null for budget update, budgetId: {}", budgetId);
            throw new AppException(ErrorCode.INVALID_INPUT);
        }

        budgetMapper.updateBudget(budget, request, entityResolver);
        log.info("Saving updated budget for budgetId: {}", budgetId);
        budget = budgetRepository.save(budget);
        log.info("Budget updated successfully for budgetId: {}", budgetId);
        return budgetMapper.toBudgetResponse(budget, transactionRepository);
    }

    public void deleteBudget(Integer budgetId) {
        Integer currentUserId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Deleting budget with budgetId: {} for userId: {}", budgetId, currentUserId);

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> {
                    log.error("Budget not found for budgetId: {}", budgetId);
                    return new AppException(ErrorCode.BUDGET_NOT_FOUND);
                });

        if (!budget.getUser().getId().equals(currentUserId) && !hasRole("ROLE_ADMIN")) {
            log.error("Unauthorized attempt to delete budgetId: {} by userId: {}", budgetId, currentUserId);
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        budgetRepository.delete(budget);
        log.info("Budget deleted successfully for budgetId: {}", budgetId);
    }

    public Page<BudgetResponse> getBudgets(String periodType, Pageable pageable) {
        Integer currentUserId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Fetching budgets for userId: {}, periodType: {}", currentUserId, periodType);

        Page<Budget> budgets = periodType == null ?
                budgetRepository.findByUser_Id(currentUserId, pageable) :
                budgetRepository.findByUser_IdAndPeriodTypeIgnoreCase(currentUserId, periodType, pageable);

        log.info("Retrieved {} budgets for userId: {}", budgets.getTotalElements(), currentUserId);
        return budgets.map(budget -> budgetMapper.toBudgetResponse(budget, transactionRepository));
    }

    public Page<BudgetAnalysisResponse> getBudgetAnalysis(String periodType, Pageable pageable) {
        Integer currentUserId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Fetching budget analysis for userId: {}, periodType: {}", currentUserId, periodType);

        Page<Budget> budgets = periodType == null ?
                budgetRepository.findByUser_Id(currentUserId, pageable) :
                budgetRepository.findByUser_IdAndPeriodTypeIgnoreCase(currentUserId, periodType, pageable);

        if (budgets.isEmpty()) {
            log.warn("No budgets found for userId: {}, periodType: {}", currentUserId, periodType);
            throw new AppException(ErrorCode.BUDGET_NOT_FOUND);
        }

        log.info("Retrieved {} budgets for analysis for userId: {}", budgets.getTotalElements(), currentUserId);
        return budgets.map(budget -> {
            BudgetResponse budgetResponse = budgetMapper.toBudgetResponse(budget, transactionRepository);
            return budgetMapper.budgetResponseToAnalysisResponse(budgetResponse);
        });
    }

    private boolean hasRole(String role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }
}