package com.securebank.service;

import com.securebank.dto.request.CreateAccountRequest;
import com.securebank.dto.response.AccountResponse;
import com.securebank.exception.ResourceNotFoundException;
import com.securebank.exception.UnauthorizedException;
import com.securebank.model.Account;
import com.securebank.model.User;
import com.securebank.repository.AccountRepository;
import com.securebank.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountService(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public AccountResponse createAccount(CreateAccountRequest req, String userEmail) {
        User owner = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Account account = new Account();
        account.setOwner(owner);
        account.setAccountType(req.getAccountType());
        account.setBalance(BigDecimal.ZERO);
        account.setAccountNumber("ACC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        Account saved = accountRepository.save(account);
        return mapToResponse(saved);
    }

    public List<AccountResponse> getMyAccounts(String userEmail) {
        User owner = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return accountRepository.findByOwner(owner).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AccountResponse getAccountById(Long id, String userEmail) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (!account.getOwner().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("Not authorized to access this account");
        }

        return mapToResponse(account);
    }

    public BigDecimal getBalance(Long id, String userEmail) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (!account.getOwner().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("Not authorized to access account balance");
        }

        return account.getBalance();
    }

    public AccountResponse mapToResponse(Account account) {
        String ownerName = account.getOwner().getFirstName() + " " + account.getOwner().getLastName();
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance(),
                ownerName,
                account.getCreatedAt());
    }
}
