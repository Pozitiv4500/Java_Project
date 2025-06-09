-- Create news table for cryptocurrency news aggregation
CREATE TABLE news
(
    id              BIGSERIAL PRIMARY KEY,
    article_id      VARCHAR(255) UNIQUE NOT NULL,
    title           TEXT                NOT NULL,
    link            VARCHAR(2048)       NOT NULL,
    keywords        TEXT[],
    creator         VARCHAR(255)[],
    video_url       VARCHAR(2048),
    description     TEXT,
    content         TEXT,
    pub_date        TIMESTAMP,
    source_icon     VARCHAR(2048),
    source_name     VARCHAR(255),
    source_url      VARCHAR(2048),
    source_priority INTEGER,
    country         VARCHAR(10)[],
    category        VARCHAR(100)[],
    language        VARCHAR(10),
    coin_mentioned  VARCHAR(50)[],
    sentiment       VARCHAR(20),
    ai_tag          VARCHAR(100)[],
    duplicate       BOOLEAN                      DEFAULT FALSE,
    created_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_news_article_id ON news (article_id);
CREATE INDEX idx_news_pub_date ON news (pub_date);
CREATE INDEX idx_news_source_name ON news (source_name);
CREATE INDEX idx_news_language ON news (language);
CREATE INDEX idx_news_sentiment ON news (sentiment);
CREATE INDEX idx_news_duplicate ON news (duplicate);
CREATE INDEX idx_news_coin_mentioned ON news USING GIN(coin_mentioned);
CREATE INDEX idx_news_category ON news USING GIN(category);
CREATE INDEX idx_news_ai_tag ON news USING GIN(ai_tag);
CREATE INDEX idx_news_keywords ON news USING GIN(keywords);
CREATE INDEX idx_news_created_at ON news (created_at);

-- Add comments for documentation
COMMENT
ON TABLE news IS 'Stores cryptocurrency news articles from NewsData.io API';
COMMENT
ON COLUMN news.article_id IS 'Unique article identifier from NewsData.io';
COMMENT
ON COLUMN news.title IS 'Article title';
COMMENT
ON COLUMN news.link IS 'URL to the full article';
COMMENT
ON COLUMN news.keywords IS 'Array of keywords extracted from the article';
COMMENT
ON COLUMN news.creator IS 'Array of article authors/creators';
COMMENT
ON COLUMN news.video_url IS 'URL to associated video content';
COMMENT
ON COLUMN news.description IS 'Article summary/description';
COMMENT
ON COLUMN news.content IS 'Full article content (if available)';
COMMENT
ON COLUMN news.pub_date IS 'Publication date from the source';
COMMENT
ON COLUMN news.source_icon IS 'URL to source favicon';
COMMENT
ON COLUMN news.source_name IS 'Name of the news source';
COMMENT
ON COLUMN news.source_url IS 'URL of the news source';
COMMENT
ON COLUMN news.source_priority IS 'Priority ranking of the source';
COMMENT
ON COLUMN news.country IS 'Array of countries related to the news';
COMMENT
ON COLUMN news.category IS 'Array of news categories';
COMMENT
ON COLUMN news.language IS 'Language code of the article';
COMMENT
ON COLUMN news.coin_mentioned IS 'Array of cryptocurrency coins mentioned';
COMMENT
ON COLUMN news.sentiment IS 'Sentiment analysis result (positive/negative/neutral)';
COMMENT
ON COLUMN news.ai_tag IS 'Array of AI-classified tags';
COMMENT
ON COLUMN news.duplicate IS 'Flag indicating if this is a duplicate article';
