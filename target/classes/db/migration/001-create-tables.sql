CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS refresh_token (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    owner_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expiry_date TIMESTAMP NOT NULL,
    ip_address VARCHAR(255) NOT NULL,
    user_agent VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    session_id VARCHAR(255) NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    token_salt VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS blacklisted_access_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS blacklisted_session (
    session_id VARCHAR(255) PRIMARY KEY,
    expiry_date TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS cards (
    id BIGSERIAL PRIMARY KEY,
    encrypted_number VARCHAR(255) NOT NULL,
    expiration_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    balance NUMERIC(19,2) NOT NULL DEFAULT 0,
    owner_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    card_hash VARCHAR(255) NOT NULL UNIQUE,
    card_salt VARCHAR(255) NOT NULL,
    card_number_search_hash VARCHAR(255) UNIQUE,
    last_four_digits VARCHAR(4) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    from_card_id INTEGER REFERENCES cards(id) ON DELETE SET NULL,
    to_card_id INTEGER REFERENCES cards(id) ON DELETE SET NULL,
    amount NUMERIC(19,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS processed_events (
    id BIGSERIAL PRIMARY KEY,
    message_id VARCHAR(255) NOT NULL UNIQUE,
    event_id VARCHAR(255) NOT NULL
);
