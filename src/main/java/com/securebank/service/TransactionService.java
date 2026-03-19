package com.securebank.service;

import com.securebank.dto.request.TransferRequest;
import com.securebank.dto.response.TransactionResponse;
import com.securebank.exception.InsufficientFundsException;
import com.securebank.exception.ResourceNotFoundException;
import com.securebank.exception.UnauthorizedException;
import com.securebank.model.Account;
import com.securebank.model.Transaction;
import com.securebank.model.TransactionType;
import com.securebank.model.User;
import com.securebank.repository.AccountRepository;
import com.securebank.repository.TransactionRepository;
import com.securebank.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              AccountRepository accountRepository,
                              UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TransactionResponse transfer(TransferRequest req, String userEmail) {
        Account fromAccount = accountRepository.findById(req.getFromAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("From account not found"));

        if (!fromAccount.getOwner().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("Not authorized to transfer from this account");
        }

        Account toAccount = accountRepository.findById(req.getToAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("To account not found"));

        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        if (fromAccount.getBalance().compareTo(req.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds for transfer");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(req.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(req.getAmount()));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        Transaction transaction = new Transaction();
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(req.getAmount());
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setDescription(req.getDescription());

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    public Page<TransactionResponse> getHistory(Long accountId, String userEmail, Pageable pageable) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (!account.getOwner().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("Not authorized to view transaction history");
        }

        Page<Transaction> page = transactionRepository
                .findByFromAccountOrToAccountOrderByTimestampDesc(account, account, pageable);

        return page.map(this::mapToResponse);
    }

    public Page<TransactionResponse> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable).map(this::mapToResponse);
    }

    public TransactionResponse mapToResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getTransactionType(),
                transaction.getDescription(),
                transaction.getTimestamp(),
                transaction.getFromAccount().getAccountNumber(),
                transaction.getToAccount().getAccountNumber());
    }
}
