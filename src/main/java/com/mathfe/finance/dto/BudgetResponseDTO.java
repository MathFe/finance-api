package com.mathfe.finance.dto;

import java.time.LocalDateTime;
import java.util.List;

public record BudgetResponseDTO(
        Long id,
        String name,
        Integer month,
        Integer year,
        LocalDateTime createdAt,
        List<BudgetItemResponseDTO> items
) {
}
