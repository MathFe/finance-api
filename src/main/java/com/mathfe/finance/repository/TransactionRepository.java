package com.mathfe.finance.repository;

import com.mathfe.finance.entity.Category;
import com.mathfe.finance.entity.Transaction;
import com.mathfe.finance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUser (User user);

    List<Transaction> findByUserAndCategory (User user, Category category);
}
