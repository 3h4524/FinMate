package org.codewith3h.finmateapplication.service;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.codewith3h.finmateapplication.EntityResolver;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Data
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ROLE_USER')")
public class BudgetService {

    BudgetRepository budgetRepository;
    CategoryRepository categoryRepository;
    UserCategoryRepository userCategoryRepository;
    TransactionRepository transactionRepository;
    BudgetMapper budgetMapper;
    EntityResolver entityResolver;

    public BudgetResponse getBudgetById(Integer budgetId) {
        Budget budget = budgetRepository.findById(budgetId).orElseThrow(() -> new AppException(ErrorCode.BUDGET_NOT_FOUND));
        return budgetMapper.toBudgetResponse(budget, transactionRepository);
    }

    public BudgetResponse createBudget(CreateBudgetRequest request) {
        Integer currentUserId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LocalDate today = LocalDate.now();
        if (request.getStartDate().isBefore(today)) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }

        if (request.getCategoryId() == null && request.getUserCategoryId() == null) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }

        Optional<Budget> existingBudget = budgetRepository.findByUserIdAndPeriodTypeAndStartDateAndCategoryId(
                currentUserId, request.getPeriodType(), request.getStartDate(), request.getCategoryId());
        if (existingBudget.isPresent()) {
            throw new AppException(ErrorCode.BUDGET_EXISTS);
        }
        Budget budget = budgetRepository.save(budgetMapper.toBudget(request, entityResolver));
        return budgetMapper.toBudgetResponse(budget, transactionRepository);
    }

    public BudgetResponse updateBudget(Integer budgetId, UpdateBudgetRequest request) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new AppException(ErrorCode.BUDGET_NOT_FOUND));
        if (request.getStartDate() != null) {
            LocalDate today = LocalDate.now();
            if (request.getStartDate().isBefore(today)) {
                throw new AppException(ErrorCode.INVALID_INPUT);
            }
        }

        if (request.getCategoryId() == null && request.getUserCategoryId() == null) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }
        budgetMapper.updateBudget(budget, request, entityResolver);
        return budgetMapper.toBudgetResponse(budgetRepository.save(budget), transactionRepository);
    }

    public void deleteBudget(Integer budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new AppException(ErrorCode.BUDGET_NOT_FOUND));
        Integer currentUserId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!budget.getUser().getId().equals(currentUserId) && !hasRole("ROLE_ADMIN")) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        budgetRepository.delete(budget);
    }

    public Page<BudgetResponse> getBudgets(String periodType, LocalDate startDate, Pageable pageable) {
        Integer currentUserId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Budget> budgets = budgetRepository.findBudgetsByUser_Id(currentUserId);

        List<Budget> filteredBudgets = budgets.stream()
                .filter(budget -> periodType == null || budget.getPeriodType().equalsIgnoreCase(periodType))
                .toList();

        List<BudgetResponse> responseList = filteredBudgets.stream()
                .map(budget -> budgetMapper.toBudgetResponse(budget, transactionRepository))
                .collect(Collectors.toList());

        responseList.forEach(budgetResponse -> {
            System.err.println(budgetResponse.toString());
        });
        return new PageImpl<>(responseList, pageable, filteredBudgets.size());
    }

    public Page<BudgetAnalysisResponse> getBudgetAnalysis(String periodType, LocalDate startDate, Pageable pageable) {
        Integer currentUserId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<BudgetResponse> budgets = budgetRepository.findBudgetsByUser_Id(currentUserId)
                .stream()
                .map(budget -> budgetMapper.toBudgetResponse(budget, transactionRepository))
                .toList();
        if (budgets.isEmpty()) {
            throw new AppException(ErrorCode.BUDGET_NOT_FOUND);
        }

        List<BudgetResponse> filteredBudgets = budgets.stream()
                .filter(budget -> periodType == null || budget.getPeriodType().equalsIgnoreCase(periodType))
                .filter(budget -> startDate == null || budget.getStartDate().equals(startDate))
                .collect(Collectors.toList());

        Page<BudgetResponse> filteredPage = new PageImpl<>(filteredBudgets, pageable, filteredBudgets.size());
        System.out.println("filteredBudgets: " + filteredBudgets);
        return filteredPage.map(budgetMapper::budgetResponseToAnalysisResponse);
    }

    private boolean hasRole(String role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }
}