package org.codewith3h.finmateapplication.service;

import jakarta.mail.MessagingException;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.TransactionCreationRequest;
import org.codewith3h.finmateapplication.dto.request.TransactionSearchRequest;
import org.codewith3h.finmateapplication.dto.request.TransactionUpdateRequest;
import org.codewith3h.finmateapplication.dto.response.TransactionResponse;
import org.codewith3h.finmateapplication.entity.*;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.mapper.TransactionMapper;
import org.codewith3h.finmateapplication.repository.*;
import org.codewith3h.finmateapplication.scheduler.RecurringTransactionScheduler;
import org.codewith3h.finmateapplication.specification.TransactionSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final UserCategoryRepository  userCategoryRepository;
    private final WalletRepository walletRepository;
    private final TransactionReminderRepository transactionReminderRepository;
    private final EmailService emailService;


    //create transaction
    @Transactional
    public TransactionResponse createTransaction(TransactionCreationRequest transactionCreationRequest) {
        log.info("Creating new transaction for user: {}", transactionCreationRequest.getUserId());
        log.info("Creating transaction for userId: {}, categoryId: {}, userCategoryId: {}",
                transactionCreationRequest.getUserId(),
                transactionCreationRequest.getCategoryId(),
                transactionCreationRequest.getUserCategoryId());

        User user = userRepository.findById(transactionCreationRequest.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Wallet wallet = walletRepository.findByUserId(transactionCreationRequest.getUserId())
                        .orElseThrow(() -> new AppException(ErrorCode.NO_WALLET_FOR_USER_EXCEPTION));

        Category category = null;
        UserCategory userCategory = null;
        String transactionType = null;

        if(transactionCreationRequest.getCategoryId() != null){
             category = categoryRepository.findById(transactionCreationRequest.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND_EXCEPTION));
             transactionType = category.getType();
        } else if (transactionCreationRequest.getUserCategoryId() != null){
            userCategory = userCategoryRepository.findById(transactionCreationRequest.getUserCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND_EXCEPTION));
            transactionType = userCategory.getType();
        } else {
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND_EXCEPTION);
        }

        BigDecimal amount = transactionCreationRequest.getAmount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal currentBalance = wallet.getBalance();
        BigDecimal newBalance = "INCOME".equalsIgnoreCase(transactionType)
                ? currentBalance.add(amount)
                : currentBalance.subtract(amount);

        log.info("Current balance is: {}, new balance: {}, amount: {}, type: {}", currentBalance, newBalance, amount, transactionType);

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            log.error("Negative balance not allowed for userId: {}. Current balance: {}," +
                    "transaction amount: {}, type: {}", transactionCreationRequest.getUserId(), newBalance ,amount, transactionType);
            throw new AppException(ErrorCode.NEGATIVE_BALANCE_NOT_ALLOWED);
        }

        Transaction transaction = transactionMapper.toEntity(transactionCreationRequest);
        transaction.setUser(user);
        transaction.setCategory(category);
        transaction.setUserCategory(userCategory);

        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        Transaction savedTransaction = transactionRepository.save(transaction);

        log.info("Transaction created with ID: {}", savedTransaction.getId());
        return transactionMapper.toResponseDto(savedTransaction);
    }

    //receive transaction
    @PreAuthorize("hasRole('USER')")
    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(Integer transactionId, Integer userId){
        log.info("Fetching transaction {} for user {}", transactionId, userId);

        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND_EXCEPTION));
        return transactionMapper.toResponseDto(transaction);
    }

    //delete transaction
    @Transactional
    @PreAuthorize("hasRole('USER')")
    public void deleteTransaction(Integer transactionId, Integer userId) {
        log.info ("Deleting Transaction {} for user {}", transactionId, userId);

        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND_EXCEPTION));

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NO_WALLET_FOR_USER_EXCEPTION));

        String transactionType = transaction.getCategory() != null
                ? transaction.getCategory().getType()
                : transaction.getUserCategory().getType();

        BigDecimal amount = transaction.getAmount().setScale(2, RoundingMode.HALF_UP);

        BigDecimal currentBalance = wallet.getBalance();

        currentBalance = "INCOME".equalsIgnoreCase(transactionType)
                ? currentBalance.subtract(amount)
                : currentBalance.add(amount);

        if(currentBalance.compareTo(BigDecimal.ZERO) < 0) {
            log.error("Negative balance not allowed for userId: {}. Current balance: {}," +
                    "transaction amount: {}, type: {}", userId, wallet.getBalance() ,amount, transactionType);
            throw new AppException(ErrorCode.NEGATIVE_BALANCE_NOT_ALLOWED);
        }

        wallet.setBalance(currentBalance);
        walletRepository.save(wallet);
        transactionRepository.delete(transaction);
        log.info("Transaction deleted with ID: {}. New Balance", transactionId);
    }

    @Transactional
    @PreAuthorize("hasRole('USER')")
    public TransactionResponse updateTransaction(Integer transactionId, Integer userId, TransactionUpdateRequest transactionUpdateRequest) {

        log.info("Updating transaction {} for user {}", transactionId, userId);

        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND_EXCEPTION));

        Wallet wallet = walletRepository.findByUserId(userId)
                        .orElseThrow(() -> new AppException(ErrorCode.NO_WALLET_FOR_USER_EXCEPTION));

        String currentType = (transaction.getCategory() != null)
                ? transaction.getCategory().getType()
                : transaction.getUserCategory().getType();
        BigDecimal currentAmount = transaction.getAmount().setScale(2, RoundingMode.HALF_UP);

        BigDecimal balance = wallet.getBalance();

        balance = ("INCOME".equalsIgnoreCase(currentType))
                ? balance.subtract(currentAmount)
                : balance.add(currentAmount);

        transactionMapper.updateEntityFromDto(transactionUpdateRequest, transaction, categoryRepository);

        String newType = transaction.getCategory() != null
                ? transaction.getCategory().getType()
                : transaction.getUserCategory().getType();
        BigDecimal newAmount = transaction.getAmount().setScale(2, RoundingMode.HALF_UP);

        balance = ("INCOME".equalsIgnoreCase(newType))
                ? balance.add(newAmount)
                : balance.subtract(newAmount);

        if(balance.compareTo(BigDecimal.ZERO) < 0) {
            log.error("Negative balance not allowed for userId: {}. Current balance: {}, Transaction amount: {}, Type: {}",
                    userId, balance, newAmount, newType);
            throw new AppException(ErrorCode.NEGATIVE_BALANCE_NOT_ALLOWED);
        }

        wallet.setBalance(balance);
        walletRepository.save(wallet);


        Transaction updatedTransaction = transactionRepository.save(transaction);
        log.info("Transaction updated with ID: {}", transactionId);
        return transactionMapper.toResponseDto(updatedTransaction);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('USER')")
    public Page<TransactionResponse> getUserTransactions(Integer userId, int page, int size, String sortBy, String sortDirection) {
        log.info("Fetching transactions for user {} with pagination", userId);
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("UserName: {}", authentication.getName());
        log.info("User role: {}", authentication.getAuthorities());

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Transaction> transactions = transactionRepository.findByUserId(userId, pageable);
        return transactions.map(transactionMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('USER')")
    public Page<TransactionResponse> searchTransaction(TransactionSearchRequest transactionSearchRequest){
        log.info("dto: {}", transactionSearchRequest);
        log.info("Searching transactions with criteria for user: {}", transactionSearchRequest.getUserId());

        Specification<Transaction> spec = (root, query, cb) -> cb.conjunction();

        if (transactionSearchRequest.getUserId() != null) {
            spec = spec.and(TransactionSpecification.hasUserId(transactionSearchRequest.getUserId()));
        }

        if (transactionSearchRequest.getCategoryId() != null) {
            spec = spec.and(TransactionSpecification.hasCategoryId(transactionSearchRequest.getCategoryId()));
        }

        if (transactionSearchRequest.getUserCategoryId() != null) {
            spec = spec.and(TransactionSpecification.hasUserCategoryId(transactionSearchRequest.getUserCategoryId()));
        }

        if (transactionSearchRequest.getMinAmount() != null && transactionSearchRequest.getMaxAmount() != null) {
            spec = spec.and(TransactionSpecification.hasAmountBetween(transactionSearchRequest.getMinAmount(), transactionSearchRequest.getMaxAmount()));
        } else if (transactionSearchRequest.getMinAmount() != null) {
            spec = spec.and(TransactionSpecification.hasMinAmount(transactionSearchRequest.getMinAmount()));
        } else if (transactionSearchRequest.getMaxAmount() != null) {
            spec = spec.and(TransactionSpecification.hasMaxAmount(transactionSearchRequest.getMaxAmount()));
        }


        if (transactionSearchRequest.getStartDate() != null && transactionSearchRequest.getEndDate() != null) {
            spec = spec.and(TransactionSpecification.hasDateBetween(transactionSearchRequest.getStartDate(), transactionSearchRequest.getEndDate()));
        } else if (transactionSearchRequest.getStartDate() != null) {
            spec = spec.and(TransactionSpecification.hasTransactionDateAfter(transactionSearchRequest.getStartDate()));
        } else if (transactionSearchRequest.getEndDate() != null) {
            spec = spec.and(TransactionSpecification.hasTransactionDateBefore(transactionSearchRequest.getEndDate()));
        }


        if (transactionSearchRequest.getIsRecurring() != null) {
            spec = spec.and(TransactionSpecification.isRecurring(transactionSearchRequest.getIsRecurring()));
        }

        if(transactionSearchRequest.getCategoryId() != null) {
            spec = spec.and(TransactionSpecification.hasCategoryId(transactionSearchRequest.getCategoryId()));
        }

        if(transactionSearchRequest.getUserCategoryId() != null) {
            spec = spec.and(TransactionSpecification.hasUserCategoryId(transactionSearchRequest.getUserCategoryId()));
        }



        Sort sort = (transactionSearchRequest.getSortDirection().equalsIgnoreCase("ASC"))
                ? Sort.by(transactionSearchRequest.getSortBy()).ascending()
                : Sort.by(transactionSearchRequest.getSortBy()).descending();

        Pageable pageable = PageRequest.of(transactionSearchRequest.getPage(), transactionSearchRequest.getSize(), sort);
        Page<Transaction> transactions = transactionRepository.findAll(spec, pageable);

        return transactions.map(transactionMapper::toResponseDto);
    }

    public void sendReminderEmail(User user, RecurringTransactionScheduler.TransactionKey key){
        try {
            String token = UUID.randomUUID().toString();
            TransactionReminder reminder = TransactionReminder.builder()
                    .token(token)
                    .createdAt(LocalDateTime.now())
                    .expiryTime(LocalDateTime.now().minusHours(24))
                    .email(user.getEmail())
                    .user(user)
                    .build();

            Specification<Transaction> spec = TransactionSpecification.hasUserId(user.getId())
                    .and(key.categoryId() != null ? TransactionSpecification.hasCategoryId(key.categoryId())
                            : TransactionSpecification.hasUserCategoryId(key.userCategoryId()));

            Transaction transaction = transactionRepository.findOne(spec)
                    .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND_EXCEPTION));

            reminder.setTransaction(transaction);

            transactionReminderRepository.save(reminder);

            String categoryName = transaction.getCategory() != null
                        ? transaction.getCategory().getName()
                        : transaction.getUserCategory().getName();

            String categoryType = transaction.getCategory() != null
                        ? transaction.getCategory().getType()
                        : transaction.getUserCategory().getType();

            String subject = "Recurring Transaction Reminder - FinMate";

            String confirmSingleUrl = "http://127.0.0.1:8080/api/v1/transactions/confirm-reminder?token=" + token;
            String confirmRecurringUrl = "http://127.0.0.1:8080/api/v1/recurringTransactions/confirm-recurring?token=" + token;

            // HTML content for email with two buttons
            String content = String.format(
                    "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>"
                            + "<h2>Recurring Transaction Reminder</h2>"
                            + "<p>Hello %s,</p>"
                            + "<p>We noticed you have a recurring transaction:</p>"
                            + "<ul>"
                            + "<li><strong>Category:</strong> %s</li>"
                            + "<li><strong>Amount:</strong> %s</li>"
                            + "<li><strong>Type:</strong> %s</li>"
                            + "</ul>"
                            + "<p>Would you like to create this transaction again?</p>"
                            + "<a href='%s' style='display: inline-block; padding: 10px 20px; color: white; background-color: #28a745; text-decoration: none; border-radius: 5px; margin-right: 10px;'>"
                            + "Create Single Transaction</a>"
                            + "<a href='%s' style='display: inline-block; padding: 10px 20px; color: white; background-color: #007bff; text-decoration: none; border-radius: 5px;'>"
                            + "Create Recurring Transaction</a>"
                            + "<p>The links will expire in 24 hours.</p>"
                            + "<p>If you do not wish to create this transaction, please ignore this email.</p>"
                            + "<hr>"
                            + "<p style='font-size: 12px; color: #777;'>Best regards,<br>FinMate Team</p>"
                            + "</div>",
                    user.getName(),
                    categoryName,
                    key.amount(),
                    categoryType,
                    confirmSingleUrl,
                    confirmRecurringUrl
            );

            emailService.sendCustomEmail(user.getEmail(), subject, content, true);

        } catch (MessagingException e) {
            log.error("Failed to send reminder email to {}: {}", user.getEmail(), e.getMessage());
        }
    }


    @Transactional
    public TransactionResponse confirmTransactionReminder(String token){
        TransactionReminder reminder = transactionReminderRepository.findByTokenAndIsUsedFalse(token)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

        if(reminder.getExpiryTime().isBefore(LocalDateTime.now())){
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        User user = reminder.getUser();
        if(!user.getIsPremium()){
            throw new AppException(ErrorCode.PREMIUM_REQUIRED);
        }

        Transaction originalTransaction = reminder.getTransaction();

        TransactionCreationRequest request = TransactionCreationRequest.builder()
                .userId(user.getId())
                .categoryId(originalTransaction.getCategory() != null
                        ? originalTransaction.getCategory().getId()
                        : null)
                .userCategoryId(originalTransaction.getUserCategory() != null
                        ? originalTransaction.getUserCategory().getId()
                        : null)
                .amount(originalTransaction.getAmount())
                .transactionDate(originalTransaction.getTransactionDate())
                .note(originalTransaction.getNote())
                .build();

        TransactionResponse response = createTransaction(request);

        reminder.setIsUsed(true);
        transactionReminderRepository.save(reminder);
        log.info("Single transaction created from reminder for user {}", reminder.getUser().getId());
        return response;
    }
}
