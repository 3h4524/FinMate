package org.codewith3h.finmateapplication.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.TransactionCreationRequest;
import org.codewith3h.finmateapplication.entity.RecurringTransaction;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.mapper.RecurringTransactionMapper;
import org.codewith3h.finmateapplication.repository.RecurringTransactionRepository;
import org.codewith3h.finmateapplication.service.RecurringTransactionService;
import org.codewith3h.finmateapplication.service.TransactionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecurringTransactionScheduler {
    private final RecurringTransactionMapper recurringTransactionMapper;
    private final TransactionService transactionService;
    private final RecurringTransactionRepository  recurringTransactionRepository;
    private final RecurringTransactionService recurringTransactionService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void scheduleRecurringTransaction() {
        LocalDate today = LocalDate.now();
        log.info("Processing recurring transactions for date: {}", today);

        List<RecurringTransaction> recurringTransactions = recurringTransactionRepository
                .findByIsActiveTrueAndNextDateLessThanEqualAndEndDateGreaterThanEqual(today, today);

        for(RecurringTransaction recurringTransaction : recurringTransactions){
                TransactionCreationRequest request = recurringTransactionMapper
                        .mapRecurringTransactionToTransactionRequestDto(recurringTransaction);

                request.setIsRecurring(true);
                request.setTransactionDate(today);
                transactionService.createTransaction(request);

                LocalDate newNextDate = calculateNextDate(today, recurringTransaction.getFrequency());
                recurringTransaction.setNextDate(newNextDate);
                if(newNextDate.isAfter(recurringTransaction.getEndDate())){
                    recurringTransaction.setIsActive(false);
                }

                recurringTransactionRepository.save(recurringTransaction);

                log.info("Created transaction for recurring transaction Id: {}, next date set to: {}"
                        , recurringTransaction.getId(), recurringTransaction.getNextDate());
        }
    }

    private LocalDate calculateNextDate(LocalDate currentDate, String frequency){
        return switch (frequency.toUpperCase()) {
            case "DAILY" -> currentDate.plusDays(1);
            case "WEEKLY" -> currentDate.plusWeeks(7);
            case "MONTHLY" -> currentDate.plusMonths(1);
            default -> throw new AppException(ErrorCode.INVALID_FREQUENCY_EXCEPTION);
        };
    }
}
