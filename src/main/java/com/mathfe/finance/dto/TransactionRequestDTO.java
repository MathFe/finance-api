package com.mathfe.finance.dto;

import com.mathfe.finance.entity.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequestDTO(

        @NotNull(message = "Category is required")
        Long categoryId,

        @NotBlank(message = "Description is required")
        String description,

        @Positive
        @NotNull(message = "Amount is required")
        BigDecimal amount,

        @NotNull(message = "Type is required")
        CategoryType type,

        @NotNull(message = "Date is required")
        LocalDate transactionDate
) {
}
