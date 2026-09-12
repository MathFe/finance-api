package com.mathfe.finance.dto;

import java.math.BigDecimal;

public record CryptoAssetDTO(
        Long holdingId,
        String coinId,
        String name,
        String symbol,
        BigDecimal amount,
        BigDecimal currentPriceUsd,
        BigDecimal currentPriceBrl,
        BigDecimal totalValueUsd,
        BigDecimal totalValueBrl,
        BigDecimal change24h,
        BigDecimal change7d,
        BigDecimal change30d
) {
}
