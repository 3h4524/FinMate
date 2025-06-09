package org.codewith3h.finmateapplication.service;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.TransactionCreationRequest;
import org.codewith3h.finmateapplication.dto.request.TransactionSearchRequest;
import org.codewith3h.finmateapplication.dto.request.TransactionUpdateRequest;
import org.codewith3h.finmateapplication.dto.response.TransactionResponse;
import org.codewith3h.finmateapplication.entity.Category;
import org.codewith3h.finmateapplication.entity.Transaction;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.entity.UserCategory;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.mapper.TransactionMapper;
import org.codewith3h.finmateapplication.repository.CategoryRepository;
import org.codewith3h.finmateapplication.repository.TransactionRepository;
import org.codewith3h.finmateapplication.repository.UserCategoryRepository;
import org.codewith3h.finmateapplication.repository.UserRepository;
import org.codewith3h.finmateapplication.specification.TransactionSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


    //create transaction
    public TransactionResponse createTransaction(TransactionCreationRequest transactionCreationRequest) {
        log.info("Creating new transaction for user: {}", transactionCreationRequest.getUserId());
        log.info("Creating transaction for userId: {}, categoryId: {}, userCategoryId: {}",
                transactionCreationRequest.getUserId(),
                transactionCreationRequest.getCategoryId(),
                transactionCreationRequest.getUserCategoryId());

        Transaction transaction = transactionMapper.toEntity(transactionCreationRequest);
        User user = userRepository.findById(transactionCreationRequest.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND_EXCEPTION));
        log.info("User: {}", user.toString());
        log.info("User: {}", user);
        transaction.setUser(user);
        if(transactionCreationRequest.getCategoryId() != null){
            Category category = categoryRepository.findById(transactionCreationRequest.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND_EXCEPTION));
            transaction.setCategory(category);
        } else if (transactionCreationRequest.getUserCategoryId() != null){
            UserCategory userCategory = userCategoryRepository.findById(transactionCreationRequest.getUserCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND_EXCEPTION));
            transaction.setUserCategory(userCategory);
        }

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
    public void deleteTransaction(Integer transactionId, Integer userId) {
        log.info ("Deleting Transaction {} for user {}", transactionId, userId);

        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND_EXCEPTION));

        transactionRepository.delete(transaction);
        log.info("Transaction deleted with ID: {}", transaction.getId());
    }

    //update transaction
    public TransactionResponse updateTransaction(Integer transactionId, Integer userId, TransactionUpdateRequest transactionUpdateRequest) {
        log.info("Updating transaction {} for user {}", transactionId, userId);
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND_EXCEPTION));
        transactionMapper.updateEntityFromDto(transactionUpdateRequest, transaction, categoryRepository);
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
