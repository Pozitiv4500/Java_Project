package com.example.kapt.service;

import com.example.kapt.dto.AlphaVantageNewsArticleDto;
import com.example.kapt.dto.AlphaVantageNewsResponseDto;
import com.example.kapt.model.News;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AlphaVantageNewsService {

    private static final Logger logger = LoggerFactory.getLogger(AlphaVantageNewsService.class);

    private final WebClient webClient;
    @Value("${app.alphavantage.api.key:demo}")
    private String apiKey;

    @Value("${app.alphavantage.api.url:https://www.alphavantage.co/query}")
    private String baseUrl;

    @Value("${app.alphavantage.api.rate-limit-delay:12000}")
    private long requestDelay;

    public AlphaVantageNewsService(WebClient webClient) {
        this.webClient = webClient;
    }

    public List<AlphaVantageNewsArticleDto> fetchLatestCryptoNews(int size) {
        return fetchCryptoNewsByTickers("CRYPTO:BTC,CRYPTO:ETH", size);
    }

    public List<AlphaVantageNewsArticleDto> fetchCryptoNewsByTickers(String tickers, int size) {
        try {
            logger.info("Fetching crypto news for tickers: {} with size: {}", tickers, size);

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .queryParam("function", "NEWS_SENTIMENT")
                    .queryParam("tickers", tickers)
                    .queryParam("limit", Math.min(size, 1000))
                    .queryParam("apikey", apiKey);

            String url = builder.toUriString();
            logger.debug("Making request to: {}", url);


            Thread.sleep(requestDelay);

            AlphaVantageNewsResponseDto response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(AlphaVantageNewsResponseDto.class)
                    .block();

            if (response == null || response.getFeed() == null) {
                logger.warn("No data received from Alpha Vantage API");
                return new ArrayList<>();
            }

            logger.info("Successfully fetched {} crypto news articles", response.getFeed().size());
            return response.getFeed();

        } catch (WebClientResponseException e) {
            logger.error("Error fetching crypto news from Alpha Vantage API: HTTP {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            return new ArrayList<>();
        } catch (InterruptedException e) {
            logger.error("Request delay interrupted", e);
            Thread.currentThread().interrupt();
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("Unexpected error fetching crypto news from Alpha Vantage", e);
            return new ArrayList<>();
        }
    }

    public List<AlphaVantageNewsArticleDto> fetchCryptoNewsByTopics(String topics, int size) {
        try {
            logger.info("Fetching crypto news for topics: {} with size: {}", topics, size);

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .queryParam("function", "NEWS_SENTIMENT")
                    .queryParam("topics", topics)
                    .queryParam("limit", Math.min(size, 1000))
                    .queryParam("apikey", apiKey);

            String url = builder.toUriString();
            logger.debug("Making request to: {}", url);

            Thread.sleep(requestDelay);

            AlphaVantageNewsResponseDto response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(AlphaVantageNewsResponseDto.class)
                    .block();

            if (response == null || response.getFeed() == null) {
                logger.warn("No data received from Alpha Vantage API");
                return new ArrayList<>();
            }

            logger.info("Successfully fetched {} news articles for topics", response.getFeed().size());
            return response.getFeed();

        } catch (Exception e) {
            logger.error("Error fetching news by topics from Alpha Vantage", e);
            return new ArrayList<>();
        }
    }

    public News convertToEntity(AlphaVantageNewsArticleDto dto) {
        if (dto == null || dto.getTitle() == null || dto.getUrl() == null) {
            logger.warn("Invalid Alpha Vantage DTO data, skipping conversion: {}", dto);
            return null;
        }

        try {
            News news = new News();


            news.setArticleId(generateArticleId(dto.getUrl()));
            news.setTitle(dto.getTitle());
            news.setLink(dto.getUrl());
            news.setDescription(dto.getSummary());
            news.setContent(dto.getSummary());
            news.setSourceName(dto.getSource());
            news.setSourceUrl(dto.getSourceDomain());
            news.setLanguage("en");
            news.setSentiment(dto.getOverallSentimentLabel());
            news.setDuplicate(false);


            if (dto.getAuthors() != null && !dto.getAuthors().isEmpty()) {
                news.setCreator(dto.getAuthors().toArray(new String[0]));
            }


            if (dto.getTimePublished() != null) {
                try {
                    LocalDateTime pubDate = parseAlphaVantageDate(dto.getTimePublished());
                    news.setPubDate(pubDate);
                } catch (DateTimeParseException e) {
                    logger.warn("Could not parse publication date: {}", dto.getTimePublished());
                    news.setPubDate(LocalDateTime.now());
                }
            } else {
                news.setPubDate(LocalDateTime.now());
            }


            String[] extractedCoins = extractCryptocurrencyMentions(dto.getTitle(), dto.getSummary(), dto.getTickerSentiment());
            news.setCoinMentioned(extractedCoins);

            return news;

        } catch (Exception e) {
            logger.error("Error converting Alpha Vantage DTO to entity: {}", dto, e);
            return null;
        }
    }

    private String generateArticleId(String url) {
        return "av_" + Math.abs(url.hashCode());
    }

    private LocalDateTime parseAlphaVantageDate(String dateString) {
        try {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
            return LocalDateTime.parse(dateString, formatter);
        } catch (DateTimeParseException e) {

            return LocalDateTime.parse(dateString);
        }
    }

    private String[] extractCryptocurrencyMentions(String title, String summary, List<Object> tickerSentiment) {
        List<String> mentions = new ArrayList<>();


        if (tickerSentiment != null) {
            for (Object ticker : tickerSentiment) {

                String tickerStr = ticker.toString().toLowerCase();
                if (tickerStr.contains("crypto:")) {
                    String crypto = tickerStr.substring(tickerStr.indexOf("crypto:") + 7);
                    if (crypto.length() >= 3) {
                        mentions.add(crypto.substring(0, 3).toLowerCase());
                    }
                }
            }
        }


        String content = (title + " " + (summary != null ? summary : "")).toLowerCase();

        String[] cryptoTerms = {
                "bitcoin", "btc", "ethereum", "eth", "binance", "bnb", "cardano", "ada",
                "solana", "sol", "dogecoin", "doge", "polygon", "matic", "chainlink", "link",
                "avalanche", "avax", "polkadot", "dot", "uniswap", "uni", "litecoin", "ltc",
                "ripple", "xrp", "stellar", "xlm", "tron", "trx", "cosmos", "atom"
        };

        for (String term : cryptoTerms) {
            if (content.contains(term) && !mentions.contains(term)) {
                mentions.add(term);
            }
        }

        return mentions.toArray(new String[0]);
    }

    public String[] getSupportedCryptoTickers() {
        return new String[]{
                "CRYPTO:BTC", "CRYPTO:ETH", "CRYPTO:BNB", "CRYPTO:ADA", "CRYPTO:SOL",
                "CRYPTO:DOGE", "CRYPTO:MATIC", "CRYPTO:LINK", "CRYPTO:AVAX", "CRYPTO:DOT",
                "CRYPTO:UNI", "CRYPTO:LTC", "CRYPTO:XRP", "CRYPTO:XLM", "CRYPTO:TRX", "CRYPTO:ATOM"
        };
    }

    public String[] getSupportedTopics() {
        return new String[]{
                "blockchain", "technology", "financial_markets", "economy_macro",
                "economy_monetary", "finance", "earnings", "ipo", "mergers_and_acquisitions"
        };
    }
}
