package org.codewith3h.finmateapplication.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.codewith3h.finmateapplication.dto.request.CreateBudgetRequest;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.BudgetAnalysisResponse;
import org.codewith3h.finmateapplication.dto.response.BudgetResponse;
import org.codewith3h.finmateapplication.entity.*;
import org.codewith3h.finmateapplication.mapper.BudgetMapper;
import org.codewith3h.finmateapplication.repository.BudgetRepository;
import org.codewith3h.finmateapplication.repository.CategoryRepository;
import org.codewith3h.finmateapplication.repository.TransactionRepository;
import org.codewith3h.finmateapplication.repository.UserCategoryRepository;
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
        // Validate required fields (BR-07)
        if (request.getUserId() == null || request.getAmount() == null || request.getPeriodType() == null ||
                request.getStartDate() == null || (request.getCategoryId() == null)) {
            throw new RuntimeException("Tất cả các trường đều bắt buộc");
        }

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Số tiền ngân sách phải lớn hơn 0.");
        }

        // Validate period type
        if (!"DAILY".equalsIgnoreCase(request.getPeriodType()) &&
                !"WEEKLY".equalsIgnoreCase(request.getPeriodType()) &&
                !"MONTHLY".equalsIgnoreCase(request.getPeriodType()) &&
                !"YEARLY".equalsIgnoreCase(request.getPeriodType())) {
            throw new RuntimeException("Chu kỳ phải là DAILY, WEEKLY, MONTHLY hoặc YEARLY.");
        }

        // Validate notification threshold
        Integer notificationThreshold = request.getNotificationThreshold() != null ? request.getNotificationThreshold() : 80;
        if (notificationThreshold < 0 || notificationThreshold > 100) {
            throw new RuntimeException("Ngưỡng cảnh báo phải nằm trong khoảng 0-100.");
        }

        // Validate start date (must be today or future)
        LocalDate today = LocalDate.now();
        if (request.getStartDate().isBefore(today)) {
            throw new RuntimeException("Ngày bắt đầu phải từ hôm nay trở đi.");
        }

        // Validate end date (if provided, must be after start date)
        if (request.getEndDate() != null && !request.getEndDate().isAfter(request.getStartDate())) {
            throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu.");
        }

        // Auto-calculate end date if not provided
        LocalDate endDate = request.getEndDate();
        if (endDate == null) {
            if ("DAILY".equalsIgnoreCase(request.getPeriodType())) {
                endDate = request.getStartDate();
            } else if ("WEEKLY".equalsIgnoreCase(request.getPeriodType())) {
                endDate = request.getStartDate().plusWeeks(1).minusDays(1);
            } else if ("MONTHLY".equalsIgnoreCase(request.getPeriodType())) {
                endDate = request.getStartDate().plusMonths(1).minusDays(1);
            } else if ("YEARLY".equalsIgnoreCase(request.getPeriodType())) {
                endDate = request.getStartDate().plusYears(1).minusDays(1);
            }
        }

        // Validate category existence
        Category category = null;
        UserCategory userCategory = null;
        if (request.getCategoryId() != null) {
            Optional<Category> categoryOpt = categoryRepository.findById(request.getCategoryId());
            if (categoryOpt.isEmpty()) {
                throw new RuntimeException("Danh mục không tồn tại.");
            }
            category = categoryOpt.get();
        }

        // Check for existing budget
        Optional<Budget> existingBudget = budgetRepository.findByUserIdAndPeriodTypeAndStartDate(
                request.getUserId(), request.getPeriodType(), request.getStartDate());
        if (existingBudget.isPresent()) {
            throw new RuntimeException("Ngân sách cho chu kỳ này đã tồn tại. Vui lòng chỉnh sửa hoặc tạo phiên bản mới.");
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
            // Calculate additional fields
            BigDecimal currentSpending = calculateCurrentSpending(budget);
            response.setCurrentSpending(currentSpending);
            response.setPercentageUsed(calculatePercentageUsed(budget.getAmount(), currentSpending));
            response.setStatus(determineStatus(response.getPercentageUsed(), budget.getNotificationThreshold()));
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Tạo ngân sách thất bại. Vui lòng kiểm tra dữ liệu nhập.");
        }
    }

    public List<BudgetResponse> getBudgets(Integer userId, String periodType, LocalDate startDate) {
        List<Budget> budgets = budgetRepository.findByUser_Id(userId);
        budgets.forEach(budget -> {
            System.out.println(budget.toString());
        });
        if (budgets.isEmpty()) {
            throw new RuntimeException("Không tìm thấy ngân sách. Vui lòng tạo kế hoạch ngân sách.");
        }

        List<BudgetResponse> responses = budgets.stream()
                .filter(budget -> periodType == null || budget.getPeriodType().equalsIgnoreCase(periodType))
                .filter(budget -> startDate == null || budget.getStartDate().equals(startDate))
                .map(budget -> {
                    BudgetResponse response = budgetMapper.toBudgetResponse(budget);
                    BigDecimal currentSpending = calculateCurrentSpending(budget);
                    response.setCurrentSpending(currentSpending);
                    response.setPercentageUsed(calculatePercentageUsed(budget.getAmount(), currentSpending));
                    response.setStatus(determineStatus(response.getPercentageUsed(), budget.getNotificationThreshold()));
                    return response;
                })
                .collect(Collectors.toList());

        if (responses.isEmpty()) {
            throw new RuntimeException("Không thể tải dữ liệu sử dụng.");
        }
        responses.forEach(response -> {
            System.out.println(response.toString());
        });
        return responses;
    }

    public List<BudgetAnalysisResponse> getBudgetAnalysis(Integer userId, String periodType, LocalDate startDate) {
        List<Budget> budgets = budgetRepository.findByUser_Id(userId);
        System.out.println("list budgetS: ");
        budgets.forEach(budget -> {
            System.out.println(budget.toString());
        });
        if (budgets.isEmpty()) {
            throw new RuntimeException("Không tìm thấy ngân sách. Vui lòng tạo kế hoạch ngân sách.");
        }

        List<BudgetAnalysisResponse> responses = budgets.stream()
                .filter(budget -> periodType == null || budget.getPeriodType().equalsIgnoreCase(periodType))
                .filter(budget -> startDate == null || budget.getStartDate().equals(startDate))
                .map(budget -> {
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
                })
                .collect(Collectors.toList());

        if (responses.isEmpty()) {
            throw new RuntimeException("Không thể tải dữ liệu sử dụng.");
        }
        responses.forEach(response -> {
            System.out.println(response.toString());
        });
        return responses;
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
