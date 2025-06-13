package org.codewith3h.finmateapplication.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.UpdateWalletRequest;
import org.codewith3h.finmateapplication.dto.response.WalletResponse;
import org.codewith3h.finmateapplication.entity.Category;
import org.codewith3h.finmateapplication.entity.Transaction;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.entity.Wallet;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.mapper.WalletMapper;
import org.codewith3h.finmateapplication.repository.CategoryRepository;
import org.codewith3h.finmateapplication.repository.TransactionRepository;
import org.codewith3h.finmateapplication.repository.UserRepository;
import org.codewith3h.finmateapplication.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@Transactional
@RequiredArgsConstructor
@Builder
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final WalletMapper walletMapper;

    public WalletResponse updateBalance(UpdateWalletRequest request){
        log.info("Updating balance for user Id: {}, new balance:{}", request.getUserId(), request.getBalance());

        Wallet wallet = walletRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.NO_WALLET_FOR_USER_EXCEPTION));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));

        BigDecimal currentBalance = wallet.getBalance();
        BigDecimal newBalance = request.getBalance().setScale(2, RoundingMode.HALF_UP);

        if(currentBalance.compareTo(newBalance) == 0){
            log.info("No balance change for userId: {}", request.getUserId());
            return walletMapper.walletToWalletResponse(wallet);
        }

        String transactionType = newBalance.compareTo(currentBalance) < 0 ? "EXPENSE" : "INCOME";
        BigDecimal transactionAmount = newBalance.subtract(currentBalance).abs();

        Category category = categoryRepository.findByNameAndType("khác", transactionType)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND_EXCEPTION));

        Transaction transaction = Transaction.builder()
                .user(user)
                .amount(transactionAmount)
                .category(category)
                .transactionDate(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                .note("Cập nhập tài khoản")
                .build();
        transactionRepository.save(transaction);

        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        return walletMapper.walletToWalletResponse(wallet);
    }

    public WalletResponse getWalletForUser(Integer userId){
        log.info("Get ting wallet for user id: {}", userId);
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NO_WALLET_FOR_USER_EXCEPTION));
        return walletMapper.walletToWalletResponse(wallet);
    }
}
