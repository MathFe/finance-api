CREATE TABLE crypto_holdings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    coin_id VARCHAR(100) NOT NULL, -- The coingecko id e.g. "bitcoin", "solana"
    amount NUMERIC(24, 8) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE(user_id, coin_id)
);