package org.codewith3h.finmateapplication.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

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


    @PreAuthorize("hasRole('USER')")
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
    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(Integer transactionId, Integer userId){
        log.info("Fetching transaction {} for user {}", transactionId, userId);

        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND_EXCEPTION));
        return transactionMapper.toResponseDto(transaction);
    }

    //delete transaction
    @Transactional
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
    public Page<TransactionResponse> getUserTransactions(Integer userId, int page, int size, String sortBy, String sortDirection) {
        log.info("Fetching transactions for user {} with pagination", userId);

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Transaction> transactions = transactionRepository.findByUserId(userId, pageable);
        return transactions.map(transactionMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
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

}
