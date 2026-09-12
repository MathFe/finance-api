package com.mathfe.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CryptoHoldingRequestDTO(
        @NotBlank(message = "Coin ID is required")
        String coinId,

        @NotNull(message = "Amount is required")
        @Positive
        BigDecimal amount
) {
}
