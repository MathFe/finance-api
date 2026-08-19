package com.mathfe.finance.repository;

import com.mathfe.finance.entity.Budget;
import com.mathfe.finance.entity.BudgetItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetItemRepository extends JpaRepository<BudgetItem, Long> {

    List<BudgetItem> findByBudget (Budget budget);
}
