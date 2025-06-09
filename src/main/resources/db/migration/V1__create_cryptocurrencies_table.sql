-- Create cryptocurrencies table
CREATE TABLE cryptocurrencies
(
    id                          BIGSERIAL PRIMARY KEY,
    symbol                      VARCHAR(20)  NOT NULL UNIQUE,
    name                        VARCHAR(255) NOT NULL,
    current_price               DECIMAL(20, 8),
    market_cap                  DECIMAL(25, 2),
    total_volume                DECIMAL(25, 2),
    price_change_24h            DECIMAL(10, 2),
    price_change_percentage_24h DECIMAL(5, 2),
    market_cap_rank             INTEGER,
    circulating_supply          DECIMAL(25, 2),
    total_supply                DECIMAL(25, 2),
    max_supply                  DECIMAL(25, 2),
    last_updated                TIMESTAMP    NOT NULL,
    created_at                  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_cryptocurrencies_symbol ON cryptocurrencies (symbol);
CREATE INDEX idx_cryptocurrencies_name ON cryptocurrencies (name);
CREATE INDEX idx_cryptocurrencies_last_updated ON cryptocurrencies (last_updated);
CREATE INDEX idx_cryptocurrencies_market_cap_rank ON cryptocurrencies (market_cap_rank);
CREATE INDEX idx_cryptocurrencies_price_change_percentage_24h ON cryptocurrencies (price_change_percentage_24h);

-- Add comments for documentation
COMMENT
ON TABLE cryptocurrencies IS 'Stores cryptocurrency market data';
COMMENT
ON COLUMN cryptocurrencies.symbol IS 'Cryptocurrency symbol (e.g., BTC, ETH)';
COMMENT
ON COLUMN cryptocurrencies.name IS 'Full name of the cryptocurrency';
COMMENT
ON COLUMN cryptocurrencies.current_price IS 'Current price in USD';
COMMENT
ON COLUMN cryptocurrencies.market_cap IS 'Market capitalization in USD';
COMMENT
ON COLUMN cryptocurrencies.total_volume IS '24h trading volume in USD';
COMMENT
ON COLUMN cryptocurrencies.price_change_24h IS 'Price change in USD over 24h';
COMMENT
ON COLUMN cryptocurrencies.price_change_percentage_24h IS 'Price change percentage over 24h';
COMMENT
ON COLUMN cryptocurrencies.market_cap_rank IS 'Rank by market capitalization';
COMMENT
ON COLUMN cryptocurrencies.circulating_supply IS 'Circulating supply amount';
COMMENT
ON COLUMN cryptocurrencies.total_supply IS 'Total supply amount';
COMMENT
ON COLUMN cryptocurrencies.max_supply IS 'Maximum supply amount';
COMMENT
ON COLUMN cryptocurrencies.last_updated IS 'Last update timestamp from API';
