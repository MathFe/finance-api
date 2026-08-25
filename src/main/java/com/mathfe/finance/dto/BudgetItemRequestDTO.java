package com.mathfe.finance.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record BudgetItemRequestDTO(
        @NotNull(message = "Category ID is required")
        Long categoryId,

        @NotNull(message = "Planned Amount is required")
        @Positive
        BigDecimal plannedAmount

) {
}
