package org.codewith3h.finmateapplication.service;

import org.codewith3h.finmateapplication.entity.Transaction;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserService userService;

    @Transactional(readOnly = true)
    public Map<String, Object> getTransactions(Integer userId) {
        logger.info("Getting transactions for user ID: {}", userId);
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                logger.warn("User not found for ID: {}", userId);
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }

            List<Transaction> transactions = transactionRepository.findByUser(user);
            response.put("success", true);
            response.put("transactions", transactions);
            return response;
        } catch (Exception e) {
            logger.error("Error getting transactions: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to get transactions");
            return response;
        }
    }

    @Transactional
    public Map<String, Object> createTransaction(Transaction transaction) {
        logger.info("Creating transaction for user ID: {}", transaction.getUser().getId());
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userService.getUserById(transaction.getUser().getId());
            if (user == null) {
                logger.warn("User not found for ID: {}", transaction.getUser().getId());
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }

            transaction.setUser(user);
            Transaction savedTransaction = transactionRepository.save(transaction);
            response.put("success", true);
            response.put("transaction", savedTransaction);
            return response;
        } catch (Exception e) {
            logger.error("Error creating transaction: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to create transaction");
            return response;
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getTransactionStatistics(Integer userId) {
        logger.info("Getting transaction statistics for user ID: {}", userId);
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                logger.warn("User not found for ID: {}", userId);
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("income", calculateIncome(userId));
            statistics.put("expenses", calculateExpenses(userId));
            statistics.put("balance", calculateIncome(userId).subtract(calculateExpenses(userId)));
            
            response.put("success", true);
            response.put("statistics", statistics);
            return response;
        } catch (Exception e) {
            logger.error("Error getting transaction statistics: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to get transaction statistics");
            return response;
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getIncome(Integer userId) {
        logger.info("Calculating income for user ID: {}", userId);
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                logger.warn("User not found for ID: {}", userId);
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }

            BigDecimal income = calculateIncome(userId);
            response.put("success", true);
            response.put("total", income);
            return response;
        } catch (Exception e) {
            logger.error("Error calculating income: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to calculate income");
            return response;
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getExpenses(Integer userId) {
        logger.info("Calculating expenses for user ID: {}", userId);
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                logger.warn("User not found for ID: {}", userId);
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }

            BigDecimal expenses = calculateExpenses(userId);
            response.put("success", true);
            response.put("total", expenses);
            return response;
        } catch (Exception e) {
            logger.error("Error calculating expenses: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to calculate expenses");
            return response;
        }
    }

    private BigDecimal calculateIncome(Integer userId) {
        return transactionRepository.calculateIncome(userId);
    }

    private BigDecimal calculateExpenses(Integer userId) {
        return transactionRepository.calculateExpenses(userId);
    }
} 