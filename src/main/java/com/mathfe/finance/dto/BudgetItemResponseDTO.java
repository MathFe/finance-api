package com.mathfe.finance.dto;

import java.math.BigDecimal;

public record BudgetItemResponseDTO(
        Long id,
        BigDecimal plannedAmount,
        CategoryResponseDTO category
) {
}
