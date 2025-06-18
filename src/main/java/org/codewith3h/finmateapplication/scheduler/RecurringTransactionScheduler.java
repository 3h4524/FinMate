package org.codewith3h.finmateapplication.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.TransactionCreationRequest;
import org.codewith3h.finmateapplication.entity.RecurringTransaction;
import org.codewith3h.finmateapplication.entity.Transaction;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.mapper.RecurringTransactionMapper;
import org.codewith3h.finmateapplication.repository.RecurringTransactionRepository;
import org.codewith3h.finmateapplication.repository.TransactionRepository;
import org.codewith3h.finmateapplication.repository.UserRepository;
import org.codewith3h.finmateapplication.service.EmailService;
import org.codewith3h.finmateapplication.service.RecurringTransactionService;
import org.codewith3h.finmateapplication.service.TransactionService;
import org.codewith3h.finmateapplication.specification.TransactionSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecurringTransactionScheduler {
    private final RecurringTransactionMapper recurringTransactionMapper;
    private final TransactionService transactionService;
    private final RecurringTransactionRepository  recurringTransactionRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final EmailService emailService;

    //Hàm tạo transaction dựa trên recurring transaction
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

//    // Hàm scan những transaction nào lặp lại trong vòng 1 tuần
//    @Scheduled(cron = "0 * * * * ?")
//    @Transactional
//    public void scanForRecurringTransactions(){
//        log.info("Schedule is running!");
//        List<User> premiumUsers = userRepository.findAllByIsPremium(true);
//
//        LocalDateTime now = LocalDateTime.now();
//        LocalDateTime oneWeekAgo = now.minusDays(7);
//        int currentHour = now.getHour();
//
//        log.info("now: {}", now);
//        if(currentHour < 6 ||  currentHour > 22){
//            return;
//        }
//
//        for(User user : premiumUsers){
//            Specification<Transaction> spec = TransactionSpecification.hasUserId(user.getId())
//                    .and(TransactionSpecification.hasTransactionDateBetween(oneWeekAgo,now));
//
//                List<Transaction> transactions = transactionRepository.findAll(spec);
//            log.info("transaction: {}", transactions);
//            transactions.stream()
//                    .filter(t -> {
//                        LocalDateTime createdAt = t.getCreatedAt();
//                        Duration duration = Duration.between(createdAt, now);
//                        return createdAt.isBefore(now.plusHours(1)) && !createdAt.isBefore(now);
//                    })
//                    .collect(Collectors.groupingBy(
//                            t -> {
//                                TransactionKey key = new TransactionKey(
//                                        t.getCategory() != null ? t.getCategory().getId() : null,
//                                        t.getUserCategory() != null ? t.getUserCategory().getId() : null,
//                                        t.getAmount()
//                                );
//                                log.info("transaction: {}", t);
//                                return key;
//                            }
//                            , Collectors.counting()
//                    ))
//                    .forEach((key, value) -> {
//
//                        if (value >= 1 &&
//                                !transactionRepository.existsByUserIdAndAmountAndCreatedAtBetweenAndCategoryOrUserCategory(
//                                        user.getId(),
//                                        key.amount(),
//                                        now.minusHours(1),
//                                        now,
//                                        key.categoryId(),
//                                        key.userCategoryId()
//                                )
//                        ) {
//                            log.info("executing schedule");
//                            transactionService.sendReminderEmail(user, key);
//                        }
//                    });
//        }
//    }
    public record TransactionKey(Integer categoryId, Integer userCategoryId, BigDecimal amount){}

}
