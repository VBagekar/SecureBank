package com.securebank.repository;

import com.securebank.model.Account;
import com.securebank.model.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByFromAccountOrToAccountOrderByTimestampDesc(
            Account fromAccount,
            Account toAccount,
            Pageable pageable);
}
