package com.securebank.controller;

import com.securebank.dto.request.TransferRequest;
import com.securebank.dto.response.TransactionResponse;
import com.securebank.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    private String currentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer funds between accounts", description = "Validates ownership, sufficient balance, and executes atomic transfer")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {
        TransactionResponse response = transactionService.transfer(request, currentUserEmail());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{accountId}")
    @Operation(summary = "Get transaction history for account")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Page<TransactionResponse>> getHistory(@PathVariable Long accountId, Pageable pageable) {
        Page<TransactionResponse> page = transactionService.getHistory(accountId, currentUserEmail(), pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/all")
    @Operation(summary = "Admin: view all transactions")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Page<TransactionResponse>> getAll(Pageable pageable) {
        Page<TransactionResponse> page = transactionService.getAllTransactions(pageable);
        return ResponseEntity.ok(page);
    }
}
