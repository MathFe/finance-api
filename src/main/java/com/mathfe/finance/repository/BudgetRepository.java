package com.mathfe.finance.repository;

import com.mathfe.finance.entity.Budget;
import com.mathfe.finance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    Optional<Budget> findByMonthAndUserAndYear (Integer month, User user, Integer year);
    List<Budget> findByUser(User user);
}
