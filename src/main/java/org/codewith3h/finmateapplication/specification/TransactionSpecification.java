package org.codewith3h.finmateapplication.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.codewith3h.finmateapplication.entity.RecurringTransaction;
import org.codewith3h.finmateapplication.entity.Transaction;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


public class TransactionSpecification {

    public static Specification<Transaction> hasUserId(Integer userId) {
        return ((root, query, criteriaBuilder) ->
                userId == null ? null : criteriaBuilder.equal(root.get("user").get("id"), userId));
    }

    public static Specification<Transaction> hasCategoryId(Integer categoryId) {
        return (root, query, criteriaBuilder) ->
                categoryId == null ? null : criteriaBuilder.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Transaction> hasUserCategoryId(Integer userCategoryId) {
        return (root, query, cb) ->
                userCategoryId == null ? null : cb.equal(root.get("userCategory").get("id"), userCategoryId);
    }

    public static Specification<Transaction> hasMinAmount(BigDecimal minAmount) {
        return (root, query, criteriaBuilder) ->
                minAmount == null ? null : criteriaBuilder.ge(root.get("amount"), minAmount);
    }

    public static Specification<Transaction> hasMaxAmount(BigDecimal maxAmount) {
        return (root, query, criteriaBuilder) ->
                maxAmount == null ? null : criteriaBuilder.equal(root.get("amount"), maxAmount);
    }

    public static Specification<Transaction> hasAmountBetween(BigDecimal minAmount, BigDecimal maxAmount) {
        return (root, query, criteriaBuilder) -> {
            if (minAmount == null && maxAmount == null) {
                return null;
            } else if (minAmount == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("amount"), maxAmount);
            } else if (maxAmount == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), minAmount);
            } else {
                return criteriaBuilder.between(root.get("amount"), minAmount, maxAmount);
            }
        };
    }

    public static Specification<Transaction> hasDateBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null && endDate == null) {
                return criteriaBuilder.conjunction();
            } else if (startDate == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("transactionDate"), endDate);
            } else if (endDate == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("transactionDate"), startDate);
            } else {
                return criteriaBuilder.between(root.get("transactionDate"), startDate, endDate);
            }
        };
    }

    public static Specification<Transaction> hasTransactionDateAfter(LocalDate date) {
        return (root, query, criteriaBuilder) ->
                date == null ? null : criteriaBuilder.greaterThanOrEqualTo(root.get("transactionDate"), date);
    }

    public static Specification<Transaction> hasTransactionDateBefore(LocalDate date) {
        return (root, query, criteriaBuilder) ->
                date == null ? null : criteriaBuilder.lessThanOrEqualTo(root.get("transactionDate"), date);
    }

    public static Specification<Transaction> isRecurring(Boolean isRecurring) {
        return (root, query, criteriaBuilder) ->{
            if(isRecurring == null) {
                return criteriaBuilder.conjunction();
            }
            if(isRecurring){
                return criteriaBuilder.isNotNull(root.get("recurringTransactions"));
            } else {
                return criteriaBuilder.isNull(root.get("recurringTransactions"));
            }
        };
    }

    public static Specification<Transaction> hasRecurringPattern(String recurringPattern) {
        return (root, query, criteriaBuilder) -> {
            if(recurringPattern == null) return criteriaBuilder.conjunction();

            Join<Transaction, RecurringTransaction> recurringJoin = root.join("recurringTransaction", JoinType.INNER);
            return criteriaBuilder.equal(recurringJoin.get("frequency"), recurringPattern);
        };
    }

    public static Specification<Transaction> hasUserAndDateRange(Integer userId, LocalDate startDate, LocalDate endDate) {
        Specification<Transaction> spec = hasUserId(userId);
        spec.and(hasDateBetween(startDate, endDate));
        return spec;
    }

    public static Specification<Transaction> hasUserAndCategory(Integer userId, Integer categoryId, Integer userCategoryId) {
        Specification<Transaction> spec = hasUserId(userId);

        if (categoryId != null) {
            spec = spec.and(hasCategoryId(categoryId));
        } else if (userCategoryId != null) {
            spec = spec.and(hasUserCategoryId(userCategoryId));
        }

        return spec;
    }


    public static Specification<Transaction> hasUserAndAmountRange(Integer userId, BigDecimal minAmount, BigDecimal maxAmount) {
        Specification<Transaction> spec = hasUserId(userId);
        spec.and(hasAmountBetween(minAmount, maxAmount));
        return spec;
    }

    public static Specification<Transaction> hasTransactionDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate != null && endDate != null) {
                return criteriaBuilder.between(root.get("createdAt"), startDate, endDate);
            }
            if (startDate != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate);
            }
            if (endDate != null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate);
            }
            return criteriaBuilder.conjunction();
        };
    }

}