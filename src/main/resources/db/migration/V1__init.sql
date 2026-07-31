CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       email VARCHAR(150) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT now(),
                       updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE categories (
                            id BIGSERIAL PRIMARY KEY,
                            user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            name VARCHAR(100) NOT NULL,
                            type VARCHAR(20) NOT NULL,       -- INCOME or EXPENSE
                            color VARCHAR(7),                -- hex color for charts, e.g. #FF5733
                            created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE transactions (
                              id BIGSERIAL PRIMARY KEY,
                              user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                              category_id BIGINT NOT NULL REFERENCES categories(id),
                              description VARCHAR(255) NOT NULL,
                              amount NUMERIC(12, 2) NOT NULL,
                              type VARCHAR(20) NOT NULL,       -- INCOME or EXPENSE
                              transaction_date DATE NOT NULL,
                              created_at TIMESTAMP NOT NULL DEFAULT now(),
                              updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE budgets (
                         id BIGSERIAL PRIMARY KEY,
                         user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                         name VARCHAR(100) NOT NULL,
                         month INT NOT NULL,              -- 1-12
                         year INT NOT NULL,
                         created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE budget_items (
                              id BIGSERIAL PRIMARY KEY,
                              budget_id BIGINT NOT NULL REFERENCES budgets(id) ON DELETE CASCADE,
                              category_id BIGINT NOT NULL REFERENCES categories(id),
                              planned_amount NUMERIC(12, 2) NOT NULL
);

CREATE TABLE refresh_tokens (
                                id BIGSERIAL PRIMARY KEY,
                                user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                token VARCHAR(255) NOT NULL UNIQUE,
                                expiry_date TIMESTAMP NOT NULL,
                                created_at TIMESTAMP NOT NULL DEFAULT now()
);