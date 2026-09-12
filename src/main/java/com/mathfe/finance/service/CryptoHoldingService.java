package com.mathfe.finance.service;

import com.mathfe.finance.dto.CryptoAssetDTO;
import com.mathfe.finance.dto.CryptoDashboardResponseDTO;
import com.mathfe.finance.dto.CryptoHoldingRequestDTO;
import com.mathfe.finance.dto.CryptoHoldingResponseDTO;
import com.mathfe.finance.entity.CryptoHolding;
import com.mathfe.finance.entity.User;
import com.mathfe.finance.repository.CryptoHoldingRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CryptoHoldingService {

    private final CryptoHoldingRepository cryptoHoldingRepository;
    private final CryptoDataService cryptoDataService;

    public CryptoHoldingService(CryptoHoldingRepository cryptoHoldingRepository, CryptoDataService cryptoDataService) {
        this.cryptoHoldingRepository = cryptoHoldingRepository;
        this.cryptoDataService = cryptoDataService;
    }

    public CryptoHoldingResponseDTO addHolding(CryptoHoldingRequestDTO dto, User user) {
        Optional<CryptoHolding> existing = cryptoHoldingRepository.findByUserAndCoinId(user, dto.coinId());
        CryptoHolding holding;

        if (existing.isPresent()) {
            holding = existing.get();
            holding.setAmount(holding.getAmount().add(dto.amount()));
        } else {
            holding = CryptoHolding.builder()
                    .user(user)
                    .coinId(dto.coinId())
                    .amount(dto.amount())
                    .build();
        }

        CryptoHolding saved = cryptoHoldingRepository.save(holding);

        return new CryptoHoldingResponseDTO(
                saved.getId(),
                saved.getCoinId(),
                saved.getAmount()
        );
    }

    public void removeHolding(Long id, User user) {
        CryptoHolding holding = cryptoHoldingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Holding not found"));

        if (!holding.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized");
        }

        cryptoHoldingRepository.delete(holding);
    }

    public CryptoDashboardResponseDTO getDashboard(User user) {
        List<CryptoHolding> holdings = cryptoHoldingRepository.findByUser(user);

        List<String> coinIds = holdings.stream()
                .map(CryptoHolding::getCoinId)
                .distinct()
                .collect(Collectors.toList());

        Map<String, CryptoDataService.CoinData> marketData = cryptoDataService.getCryptoData(coinIds);

        BigDecimal totalUsd = BigDecimal.ZERO;
        BigDecimal totalBrl = BigDecimal.ZERO;
        List<CryptoAssetDTO> assets = new ArrayList<>();

        for (CryptoHolding holding : holdings) {
            CryptoDataService.CoinData data = marketData.get(holding.getCoinId());

            String name = data != null ? data.name : holding.getCoinId();
            String symbol = data != null ? data.symbol : holding.getCoinId();
            BigDecimal priceUsd = data != null ? data.currentPriceUsd : BigDecimal.ZERO;
            BigDecimal priceBrl = data != null ? data.currentPriceBrl : BigDecimal.ZERO;
            BigDecimal valUsd = priceUsd.multiply(holding.getAmount()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal valBrl = priceBrl.multiply(holding.getAmount()).setScale(2, RoundingMode.HALF_UP);

            totalUsd = totalUsd.add(valUsd);
            totalBrl = totalBrl.add(valBrl);

            assets.add(new CryptoAssetDTO(
                    holding.getId(),
                    holding.getCoinId(),
                    name,
                    symbol,
                    holding.getAmount(),
                    priceUsd,
                    priceBrl,
                    valUsd,
                    valBrl,
                    data != null ? data.change24h : BigDecimal.ZERO,
                    data != null ? data.change7d : BigDecimal.ZERO,
                    data != null ? data.change30d : BigDecimal.ZERO
            ));
        }

        return new CryptoDashboardResponseDTO(totalUsd, totalBrl, assets);
    }
}
