package com.mathfe.finance.dto;

import java.math.BigDecimal;
import java.util.List;

public record CryptoDashboardResponseDTO(
        BigDecimal totalValueUsd,
        BigDecimal totalValueBrl,
        List<CryptoAssetDTO> assets
) {
}
