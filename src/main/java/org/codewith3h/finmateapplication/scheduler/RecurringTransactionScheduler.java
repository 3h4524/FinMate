package org.codewith3h.finmateapplication.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.TransactionCreationRequest;
import org.codewith3h.finmateapplication.entity.RecurringTransaction;
import org.codewith3h.finmateapplication.entity.Transaction;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.entity.Wallet;
import org.codewith3h.finmateapplication.enums.FeatureCode;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.mapper.RecurringTransactionMapper;
import org.codewith3h.finmateapplication.repository.RecurringTransactionRepository;
import org.codewith3h.finmateapplication.repository.TransactionRepository;
import org.codewith3h.finmateapplication.repository.UserRepository;
import org.codewith3h.finmateapplication.repository.WalletRepository;
import org.codewith3h.finmateapplication.service.*;
import org.codewith3h.finmateapplication.specification.TransactionSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecurringTransactionScheduler {
    private final TransactionService transactionService;
    private final RecurringTransactionRepository  recurringTransactionRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final WalletService walletService;
    private final RecurringTransactionService recurringTransactionService;
    private final FeatureService featureService;

    //Hàm tạo transaction dựa trên recurring transaction
    @Transactional
    @Scheduled(cron = "0 0 6 * * ?")
    public void scheduleRecurringTransaction() {
        LocalDate today = LocalDate.now();
        log.info("Processing recurring transactions for date: {}", today);

        List<RecurringTransaction> recurringTransactions = recurringTransactionRepository
                .findByIsActiveTrueAndNextDateLessThanEqualAndEndDateGreaterThanEqual(today, today);

        for(RecurringTransaction recurringTransaction : recurringTransactions){

            boolean exists = transactionRepository.existsByRecurringTransactionsAndTransactionDate(recurringTransaction, recurringTransaction.getNextDate());
            if (exists) continue;

            Transaction createdTransaction = Transaction.builder()
                        .user(recurringTransaction.getUser())
                        .recurringTransactions(recurringTransaction)
                        .category(recurringTransaction.getCategory())
                        .userCategory(recurringTransaction.getUserCategory())
                        .amount(recurringTransaction.getAmount())
                        .note(recurringTransaction.getNote())
                        .transactionDate(recurringTransaction.getNextDate())
                        .build();

            transactionRepository.save(createdTransaction);

            walletService.updateBalanceForCreateNewTransaction(createdTransaction);

            LocalDate newNextDate = recurringTransactionService.calculateNextDate(recurringTransaction.getNextDate(), recurringTransaction.getFrequency());
            recurringTransaction.setNextDate(newNextDate);

            if(newNextDate.isAfter(recurringTransaction.getEndDate())){
                recurringTransaction.setIsActive(false);
            }

            recurringTransactionRepository.save(recurringTransaction);

            log.info("Created transaction for recurring transaction Id: {}, next date set to: {}"
                        , recurringTransaction.getId(), recurringTransaction.getNextDate());
        }
    }


    // Hàm scan những transaction nào lặp lại trong vòng 1 tuần
    @Scheduled(cron = "0 0 6 * * 7")
    @Transactional
    public void scanForRecurringTransactions(){
        log.info("Schedule is running!");
        List<User> premiumUsers = userRepository.findAllByIsPremium(true);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneWeekAgo = now.minusDays(7);

        for(User user : premiumUsers){
            log.info("user {}", user.getEmail());
            boolean isReceiveReminder = featureService.userHasFeature(user.getId(), FeatureCode.SMART_REMINDER.name());

            if(!isReceiveReminder){
                continue;
            }
            log.info("use smart reminder");
            Specification<Transaction> spec = TransactionSpecification.hasUserId(user.getId())
                    .and(TransactionSpecification.hasTransactionDateBetween(oneWeekAgo,now));

            List<Transaction> transactions = transactionRepository.findAll(spec);

            if(transactions.isEmpty())
                continue;

            Map<TransactionKey, Set<LocalDate>> grouped = groupTransactionsByTypeAndDate(transactions);
            Optional<Map.Entry<TransactionKey, Set<LocalDate>>> mostFrequent = findMostFrequentTransaction(grouped);

            mostFrequent.ifPresent(entry -> {
                int repeatDays = entry.getValue().size();
                if(repeatDays >= 3){
                    log.info("User {} most frequent transactionKey: {}, repeated in {} day(s)", user.getId(), entry.getKey(), repeatDays);
                    transactionService.createRecurringTransactionForReminder(user, entry.getKey());
                }
            });
        }
    }
    public record TransactionKey(Integer categoryId, Integer userCategoryId, BigDecimal amount){}

    private Map<TransactionKey, Set<LocalDate>> groupTransactionsByTypeAndDate(List<Transaction> transactions) {
        return transactions.stream()
                .filter(transaction -> transaction.getRecurringTransactions() != null)
                .collect(Collectors.groupingBy(
                        transaction -> new TransactionKey(
                                transaction.getCategory() != null ? transaction.getCategory().getId() : null,
                                transaction.getUserCategory() != null ? transaction.getUserCategory().getId() : null,
                                transaction.getAmount()
                        ),
                        Collectors.mapping(Transaction :: getTransactionDate,
                                Collectors.toSet()
                        )
                ));
    }

    private Optional<Map.Entry<TransactionKey, Set<LocalDate>>> findMostFrequentTransaction(Map<TransactionKey, Set<LocalDate>> grouped) {
        return grouped.entrySet().stream()
                .max(Comparator.comparingInt(entry -> entry.getValue().size()));
    }


}
