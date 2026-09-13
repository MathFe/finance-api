package com.mathfe.finance.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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
    private static final String COINGECKO_SIMPLE_PRICE_URL = "https://api.coingecko.com/api/v3/simple/price?ids={ids}&vs_currencies=usd,brl&include_24hr_change=true";

    // Without a key, CoinGecko rate-limits by IP, which on Render is shared across many
    // unrelated tenants and stays saturated regardless of how little this app calls it.
    // A demo API key gets its own rate-limit bucket instead.
    @Value("${COINGECKO_API_KEY:}")
    private String coinGeckoApiKey;

    private final Map<String, CoinData> cache = new ConcurrentHashMap<>();
    private volatile long lastFetchTime = 0;
    private volatile long lastAttemptTime = 0;
    private static final long CACHE_DURATION_MS = 60 * 1000; // 1 minute
    // CoinGecko's free tier rate-limits (429) after a handful of requests. Without this,
    // a failed fetch retries on the very next request too, which keeps re-triggering the
    // rate limit and leaves the price stuck at zero indefinitely. Back off between attempts
    // so a temporary 429 gets a chance to clear.
    private static final long RETRY_BACKOFF_MS = 30 * 1000; // 30 seconds

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

        long now = System.currentTimeMillis();
        boolean cacheCoversRequest = now - lastFetchTime < CACHE_DURATION_MS && cache.keySet().containsAll(coinIds);
        boolean recentAttempt = now - lastAttemptTime < RETRY_BACKOFF_MS;
        if (cacheCoversRequest || recentAttempt) {
            return cache;
        }

        lastAttemptTime = now;

        try {
            String idsParam = String.join(",", coinIds);
            HttpEntity<Void> requestEntity = new HttpEntity<>(buildHeaders());

            // 1. Fetch detailed market data in USD
            String marketsUrl = COINGECKO_API_URL.replace("{ids}", idsParam);
            ResponseEntity<List<Map<String, Object>>> marketResponse = restTemplate.exchange(
                    marketsUrl,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            // 2. Fetch BRL prices
            String priceUrl = COINGECKO_SIMPLE_PRICE_URL.replace("{ids}", idsParam);
            ResponseEntity<Map<String, Map<String, Object>>> priceResponse = restTemplate.exchange(
                    priceUrl,
                    HttpMethod.GET,
                    requestEntity,
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

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        if (coinGeckoApiKey != null && !coinGeckoApiKey.isBlank()) {
            headers.set("x-cg-demo-api-key", coinGeckoApiKey);
        }
        return headers;
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
