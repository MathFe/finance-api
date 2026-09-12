package com.mathfe.finance.dto;

import java.math.BigDecimal;

public record CryptoHoldingResponseDTO(
        Long id,
        String coinId,
        BigDecimal amount
) {
}
