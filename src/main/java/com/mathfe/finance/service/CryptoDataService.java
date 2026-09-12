package com.mathfe.finance.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CryptoDataService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String COINGECKO_API_URL = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&ids={ids}&price_change_percentage=24h,7d,30d";

    // Fallback BRL conversion if we don't fetch directly (CoinGecko only allows one vs_currency in markets endpoint easily,
    // but we can actually fetch simple/price with multiple vs_currencies or just use an exchange rate.
    // For simplicity and completeness per the requirements, we will fetch both vs USD and BRL or do the math if needed.
    // Wait, the easiest is to just use simple/price endpoint:
    private static final String COINGECKO_SIMPLE_PRICE_URL = "https://api.coingecko.com/api/v3/simple/price?ids={ids}&vs_currencies=usd,brl&include_24hr_change=true";

    // Better yet, use markets for USD and just fetch BRL conversion rate.
    // Let's use the markets endpoint for USD which gives all the 7d/30d changes, and simple price for BRL.

    private final Map<String, CoinData> cache = new ConcurrentHashMap<>();
    private long lastFetchTime = 0;
    private static final long CACHE_DURATION_MS = 60 * 1000; // 1 minute

    public static class CoinData {
        public String id;
        public String symbol;
        public String name;
        public BigDecimal currentPriceUsd;
        public BigDecimal currentPriceBrl;
        public BigDecimal change24h;
        public BigDecimal change7d;
        public BigDecimal change30d;
    }

    public Map<String, CoinData> getCryptoData(List<String> coinIds) {
        if (coinIds == null || coinIds.isEmpty()) {
            return new HashMap<>();
        }

        if (System.currentTimeMillis() - lastFetchTime < CACHE_DURATION_MS && cache.keySet().containsAll(coinIds)) {
            return cache;
        }

        try {
            String idsParam = String.join(",", coinIds);

            // 1. Fetch detailed market data in USD
            String marketsUrl = COINGECKO_API_URL.replace("{ids}", idsParam);
            ResponseEntity<List<Map<String, Object>>> marketResponse = restTemplate.exchange(
                    marketsUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            // 2. Fetch BRL prices
            String priceUrl = COINGECKO_SIMPLE_PRICE_URL.replace("{ids}", idsParam);
            ResponseEntity<Map<String, Map<String, Object>>> priceResponse = restTemplate.exchange(
                    priceUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Map<String, Object>>>() {}
            );

            List<Map<String, Object>> marketData = marketResponse.getBody();
            Map<String, Map<String, Object>> priceData = priceResponse.getBody();

            if (marketData != null && priceData != null) {
                for (Map<String, Object> coin : marketData) {
                    CoinData data = new CoinData();
                    data.id = (String) coin.get("id");
                    data.symbol = (String) coin.get("symbol");
                    data.name = (String) coin.get("name");

                    data.currentPriceUsd = getBigDecimal(coin.get("current_price"));
                    data.change24h = getBigDecimal(coin.get("price_change_percentage_24h_in_currency"));
                    if (data.change24h == null) data.change24h = getBigDecimal(coin.get("price_change_percentage_24h")); // fallback
                    data.change7d = getBigDecimal(coin.get("price_change_percentage_7d_in_currency"));
                    data.change30d = getBigDecimal(coin.get("price_change_percentage_30d_in_currency"));

                    Map<String, Object> prices = priceData.get(data.id);
                    if (prices != null) {
                        data.currentPriceBrl = getBigDecimal(prices.get("brl"));
                    } else {
                        data.currentPriceBrl = BigDecimal.ZERO;
                    }

                    cache.put(data.id, data);
                }
                lastFetchTime = System.currentTimeMillis();
            }

        } catch (Exception e) {
            System.err.println("Failed to fetch crypto data: " + e.getMessage());
            // If API fails, we return whatever is in cache, even if stale
        }

        return cache;
    }

    private BigDecimal getBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof Number) {
            return new BigDecimal(value.toString());
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
