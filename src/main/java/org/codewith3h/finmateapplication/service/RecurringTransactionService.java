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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RecurringTransactionService{

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final RecurringTransactionMapper  recurringTransactionMapper;
    private final TransactionService transactionService;
    private final EntityResolver entityResolver;
    private final TransactionReminderRepository transactionReminderRepository;

        @Transactional
        public RecurringTransactionResponse createRecurringTransaction(RecurringTransactionRequest dto) {

            RecurringTransaction recurringTransaction = recurringTransactionMapper.toEntity(dto, entityResolver);

            switch (dto.getFrequency().toUpperCase()) {
                case "DAILY" -> recurringTransaction.setNextDate(dto.getStartDate().plusDays(1));
                case "WEEKLY" -> recurringTransaction.setNextDate(dto.getStartDate().plusWeeks(1));
                case "MONTHLY" -> recurringTransaction.setNextDate(dto.getStartDate().plusMonths(1));
                default -> throw new AppException(ErrorCode.INVALID_FREQUENCY_EXCEPTION);
            }


            RecurringTransaction saved = recurringTransactionRepository.save(recurringTransaction);

            if(!dto.getStartDate().isAfter(LocalDate.now())){
                TransactionCreationRequest transactionCreationRequest = recurringTransactionMapper.mapRecurringTransactionRequestToTransactionRequestDto(dto);
                transactionCreationRequest.setIsRecurring(true);
                transactionService.createTransaction(transactionCreationRequest);
                log.info("Recurring transaction created: {}, created first transaction immediately.", saved.getId());
            } else {
                log.info("Recurring transaction created: {}, first transaction will start at {}", saved.getId(), dto.getStartDate());
            }
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
}
