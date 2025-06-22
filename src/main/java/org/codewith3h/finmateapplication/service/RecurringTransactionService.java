package org.codewith3h.finmateapplication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.EntityResolver;
import org.codewith3h.finmateapplication.dto.request.RecurringTransactionRequest;
import org.codewith3h.finmateapplication.dto.request.TransactionCreationRequest;
import org.codewith3h.finmateapplication.dto.response.RecurringTransactionResponse;
import org.codewith3h.finmateapplication.entity.RecurringTransaction;
import org.codewith3h.finmateapplication.entity.Transaction;
import org.codewith3h.finmateapplication.entity.TransactionReminder;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.mapper.RecurringTransactionMapper;
import org.codewith3h.finmateapplication.repository.RecurringTransactionRepository;
import org.codewith3h.finmateapplication.repository.TransactionReminderRepository;
import org.codewith3h.finmateapplication.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RecurringTransactionService{

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final RecurringTransactionMapper  recurringTransactionMapper;
    private final EntityResolver entityResolver;
    private final TransactionReminderRepository transactionReminderRepository;
    private final TransactionRepository transactionRepository;
    private final WalletService walletService;

        @Transactional
        public RecurringTransactionResponse createRecurringTransaction(RecurringTransactionRequest dto) {

            RecurringTransaction recurringTransaction = recurringTransactionMapper.toEntity(dto, entityResolver);

            LocalDate nextDate =  recurringTransaction.getStartDate();

            recurringTransaction.setNextDate(nextDate);
            RecurringTransaction saved = recurringTransactionRepository.save(recurringTransaction);
            log.info("Recurring transaction created: {}, first transaction will start at {}", saved.getId(), dto.getStartDate());
            return recurringTransactionMapper.toResponseDto(saved);
        }

        @Transactional
        public RecurringTransactionResponse confirmRecurringTransactionReminder(String token){

            TransactionReminder reminder = transactionReminderRepository.findByTokenAndIsUsedFalse(token)
                    .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

            if(reminder.getExpiryTime().isBefore(LocalDateTime.now())){
                throw new AppException(ErrorCode.TOKEN_EXPIRED);
            }

            Transaction originalTransaction = reminder.getTransaction();

            RecurringTransactionRequest request = RecurringTransactionRequest.builder()
                                    .userId(reminder.getUser().getId())
                                    .categoryId(originalTransaction.getCategory() != null
                                    ? originalTransaction.getCategory().getId() : null)
                                    .userCategoryId(originalTransaction.getUserCategory() != null
                                    ? originalTransaction.getUserCategory().getId() : null)
                                    .amount(originalTransaction.getAmount())
                                    .startDate(LocalDate.now())
                                    .endDate(LocalDate.now().plusMonths(1))
                                    .isActive(true)
                                    .frequency("MONTHLY")
                                    .note(originalTransaction.getNote())
                                    .build();

            reminder.setIsUsed(true);
            transactionReminderRepository.save(reminder);

            RecurringTransactionResponse response = createRecurringTransaction(request);
            log.info("Recurring transaction created for userId={} via reminder token={}", request.getUserId(), token);

            return response;
        }


        public RecurringTransactionResponse updateRecurringTransaction(Integer transactionId, Integer userId, RecurringTransactionRequest dto){

            log.info("Updating recurring transaction {} for user {}", transactionId, userId);

            RecurringTransaction recurringTransaction = recurringTransactionRepository
                    .findByIdAndUserId(transactionId, userId)
                    .orElseThrow(() -> new AppException(ErrorCode.RECURRING_TRANSACTION_NOT_FOUND));

            recurringTransactionMapper.updateEntityFromDto(dto, recurringTransaction, entityResolver);

            if(!dto.getStartDate().isBefore(LocalDate.now())){
                recurringTransaction.setNextDate(dto.getStartDate());
            } else {
                switch(dto.getFrequency().toUpperCase()){
                    case "DAILY" -> recurringTransaction.setNextDate(dto.getStartDate().plusDays(1));
                    case "WEEKLY" -> recurringTransaction.setNextDate(dto.getStartDate().plusWeeks(1));
                    case "MONTHLY" -> recurringTransaction.setNextDate(dto.getStartDate().plusMonths(1));
                }
            }

            log.info("nextDate: {}", recurringTransaction.getNextDate());

            RecurringTransaction savedTransaction = recurringTransactionRepository.save(recurringTransaction);
            log.info("recurring Transaction updated successfully.");
            return recurringTransactionMapper.toResponseDto(savedTransaction);
        }

        public void deleteRecurringTransaction(Integer transactionId, Integer userId){
            log.info("Deleting recurring transaction {} for user {}", transactionId, userId);

            RecurringTransaction recurringTransaction = recurringTransactionRepository
                    .findByIdAndUserId(transactionId, userId)
                    .orElseThrow(() -> new AppException(ErrorCode.RECURRING_TRANSACTION_NOT_FOUND));

            recurringTransaction.setIsActive(false);
            log.info("Recurring transaction deleted successfully.");
            recurringTransactionRepository.save(recurringTransaction);
        }

        public Page<RecurringTransactionResponse> getRecurringTransactions(
                Integer userId, int page, int size, String sortBy, String sortDirection) {

            log.info("Fetching recurring transactions for user {}", userId);
            Sort sort = sortDirection.equalsIgnoreCase("ACS")
                    ? Sort.by(sortBy).ascending()
                    : Sort.by(sortBy).descending();

            Pageable pageable = PageRequest.of(page, size, sort);

            Page<RecurringTransaction> recurringTransactionPage = recurringTransactionRepository.findByUserIdAndIsActiveTrue(userId, pageable);

            log.info("Recurring transactions successfully.");
            return recurringTransactionPage.map(recurringTransactionMapper :: toResponseDto);
        }

        public RecurringTransactionResponse getRecurringTransactionById(Integer transactionId, Integer userId) {
            log.info("Fetching recurring transaction {} for user {}", transactionId, userId);

            RecurringTransaction recurringTransaction = recurringTransactionRepository
                    .findByIdAndUserId(transactionId, userId)
                    .orElseThrow(() -> new AppException(ErrorCode.RECURRING_TRANSACTION_NOT_FOUND));

            log.info("Recurring transaction fetched successfully.");
            return recurringTransactionMapper.toResponseDto(recurringTransaction);
        }

    public LocalDate calculateNextDate(LocalDate currentDate, String frequency){
        return switch (frequency.toUpperCase()) {
            case "DAILY" -> currentDate.plusDays(1);
            case "WEEKLY" -> currentDate.plusWeeks(1);
            case "MONTHLY" -> currentDate.plusMonths(1);
            default -> throw new AppException(ErrorCode.INVALID_FREQUENCY_EXCEPTION);
        };
    }

    public List<RecurringTransactionResponse> getRecurringTransactionInMonth(Integer userId, int limit) {
        log.info("Fetching up to {} recurring transactions for user {}", limit, userId);

        List<RecurringTransaction> recurringTransactionList =
                recurringTransactionRepository.findRecurringTransactionForUserInThisMonth(userId, limit);

        return recurringTransactionList.stream()
                .map(recurringTransactionMapper::toResponseDto)
                .toList();
    }
}
