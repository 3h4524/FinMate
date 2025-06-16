package org.codewith3h.finmateapplication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.EntityResolver;
import org.codewith3h.finmateapplication.dto.request.RecurringTransactionRequest;
import org.codewith3h.finmateapplication.dto.request.TransactionCreationRequest;
import org.codewith3h.finmateapplication.dto.response.RecurringTransactionResponse;
import org.codewith3h.finmateapplication.entity.RecurringTransaction;
import org.codewith3h.finmateapplication.entity.TransactionReminder;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.mapper.RecurringTransactionMapper;
import org.codewith3h.finmateapplication.repository.RecurringTransactionRepository;
import org.codewith3h.finmateapplication.repository.TransactionReminderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

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

//
}
