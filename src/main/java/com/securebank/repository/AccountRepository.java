package com.securebank.repository;

import com.securebank.model.Account;
import com.securebank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByOwner(User owner);

    @Override
    Optional<Account> findById(Long id);
}
