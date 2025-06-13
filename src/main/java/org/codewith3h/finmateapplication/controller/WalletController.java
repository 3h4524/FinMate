package org.codewith3h.finmateapplication.controller;

import jakarta.validation.Valid;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.UpdateWalletRequest;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.WalletResponse;
import org.codewith3h.finmateapplication.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@Slf4j
@Builder
public class WalletController {

    private final WalletService  walletService;

    @PutMapping
    public ResponseEntity<ApiResponse<WalletResponse>> updateWallet(
            @Valid @RequestBody UpdateWalletRequest request){

        log.info("updating wallet for user id: {}", request.getUserId());
        WalletResponse updatedWallet = walletService.updateBalance(request);
        ApiResponse<WalletResponse> response = new ApiResponse<>();

        String message = updatedWallet.getBalance().equals(updatedWallet.getBalance())
                ? "Wallet balance updated successfully"
                : "No change to wallet balance";
        response.setMessage(message);
        response.setResult(updatedWallet);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<WalletResponse>> getWallet(
            @RequestParam Integer userId
    ) {
        log.info("getting wallet for user id: {}", userId);
        WalletResponse walletResponse = walletService.getWalletForUser(userId);
        ApiResponse<WalletResponse> response = new ApiResponse<>();
        response.setMessage("success.");
        response.setResult(walletResponse);
        return ResponseEntity.ok(response);
    }
}
