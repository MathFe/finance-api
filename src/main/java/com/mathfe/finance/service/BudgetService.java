package com.mathfe.finance.service;

import com.mathfe.finance.dto.BudgetItemResponseDTO;
import com.mathfe.finance.dto.BudgetRequestDTO;
import com.mathfe.finance.dto.BudgetResponseDTO;
import com.mathfe.finance.dto.CategoryResponseDTO;
import com.mathfe.finance.entity.Budget;
import com.mathfe.finance.entity.BudgetItem;
import com.mathfe.finance.entity.User;
import com.mathfe.finance.repository.BudgetItemRepository;
import com.mathfe.finance.repository.BudgetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetItemRepository budgetItemRepository;

    public BudgetService(BudgetRepository budgetRepository, BudgetItemRepository budgetItemRepository) {
        this.budgetRepository = budgetRepository;
        this.budgetItemRepository = budgetItemRepository;
    }

    @Transactional
    public BudgetResponseDTO create(BudgetRequestDTO dto, User user) {
        if (budgetRepository.findByMonthAndUserAndYear(dto.month(), user, dto.year()).isPresent()) {
            throw new IllegalArgumentException("Budget for this month and year already exists");
        }

        Budget budget = Budget.builder()
                .user(user)
                .name(dto.name())
                .month(dto.month())
                .year(dto.year())
                .build();
        // Since the JPA lifecycle hook @PrePersist on Budget doesn't have @PrePersist annotation in the entity:
        if (budget.getCreatedAt() == null) {
            budget.setCreatedAt(LocalDateTime.now());
        }

        Budget savedBudget = budgetRepository.save(budget);

        return new BudgetResponseDTO(
                savedBudget.getId(),
                savedBudget.getName(),
                savedBudget.getMonth(),
                savedBudget.getYear(),
                savedBudget.getCreatedAt(),
                List.of()
        );
    }

    public List<BudgetResponseDTO> listByUser(User user) {
        List<Budget> budgets = budgetRepository.findByUser(user);

        return budgets.stream().map(budget -> {
            List<BudgetItem> items = budgetItemRepository.findByBudget(budget);
            List<BudgetItemResponseDTO> itemDTOs = items.stream().map(item ->
                    new BudgetItemResponseDTO(
                            item.getId(),
                            item.getPlannedAmount(),
                            new CategoryResponseDTO(
                                    item.getCategory().getId(),
                                    item.getCategory().getName(),
                                    item.getCategory().getType(),
                                    item.getCategory().getColor(),
                                    item.getCategory().getCreatedAt()
                            )
                    )
            ).toList();

            return new BudgetResponseDTO(
                    budget.getId(),
                    budget.getName(),
                    budget.getMonth(),
                    budget.getYear(),
                    budget.getCreatedAt(),
                    itemDTOs
            );
        }).toList();
    }
}
