package com.mathfe.finance.dto;

import com.mathfe.finance.entity.CategoryType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponseDTO(
        Long id,
        String description,
        BigDecimal amount,
        CategoryType type,
        LocalDate transactionDate,
        CategoryResponseDTO category
) {
}
