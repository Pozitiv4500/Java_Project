package com.example.kapt.service;

import com.example.kapt.dto.CoinGeckoResponseDto;
import com.example.kapt.model.Cryptocurrency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;


@Service
public class CoinGeckoService {

    private static final Logger logger = LoggerFactory.getLogger(CoinGeckoService.class);
    private static final String COINGECKO_BASE_URL = "https://api.coingecko.com/api/v3";

    private final WebClient webClient;

    @Value("${app.coingecko.request-delay:1000}")
    private long requestDelay;

    public CoinGeckoService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl(COINGECKO_BASE_URL)
                .build();
    }


    public List<CoinGeckoResponseDto> fetchTopCryptocurrencies(int page, int perPage) {
        try {

            Thread.sleep(requestDelay);

            String uri = String.format("/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=%d&page=%d&sparkline=false&locale=en",
                    Math.min(perPage, 250), page);

            logger.info("Fetching cryptocurrencies from CoinGecko: page={}, perPage={}", page, perPage);

            List<CoinGeckoResponseDto> response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<CoinGeckoResponseDto>>() {
                    })
                    .block();

            logger.info("Successfully fetched {} cryptocurrencies", response != null ? response.size() : 0);
            return response != null ? response : new ArrayList<>();

        } catch (WebClientResponseException e) {
            logger.error("Error fetching data from CoinGecko API: HTTP {}, Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 429) {
                logger.warn("Rate limit exceeded, increasing delay");
                requestDelay = Math.min(requestDelay * 2, 10000);
            }
            return new ArrayList<>();
        } catch (InterruptedException e) {
            logger.error("Thread interrupted while waiting", e);
            Thread.currentThread().interrupt();
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("Unexpected error fetching data from CoinGecko API", e);
            return new ArrayList<>();
        }
    }


    public CoinGeckoResponseDto fetchCryptocurrencyBySymbol(String symbol) {
        try {
            Thread.sleep(requestDelay);

            String uri = String.format("/coins/%s", symbol.toLowerCase());

            logger.info("Fetching cryptocurrency data for symbol: {}", symbol);


            return fetchTopCryptocurrencies(1, 250).stream()
                    .filter(crypto -> crypto.getSymbol().equalsIgnoreCase(symbol))
                    .findFirst()
                    .orElse(null);

        } catch (InterruptedException e) {
            logger.error("Thread interrupted while waiting", e);
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            logger.error("Error fetching cryptocurrency by symbol: {}", symbol, e);
            return null;
        }
    }


    public Cryptocurrency convertToEntity(CoinGeckoResponseDto dto) {
        if (dto == null) {
            return null;
        }

        Cryptocurrency crypto = new Cryptocurrency();
        crypto.setSymbol(dto.getSymbol().toUpperCase());
        crypto.setName(dto.getName());
        crypto.setCurrentPrice(dto.getCurrentPrice());
        crypto.setMarketCap(dto.getMarketCap());
        crypto.setMarketCapRank(dto.getMarketCapRank());
        crypto.setTotalVolume(dto.getTotalVolume());
        crypto.setPriceChange24h(dto.getPriceChange24h());
        crypto.setPriceChangePercentage24h(dto.getPriceChangePercentage24h());
        crypto.setCirculatingSupply(dto.getCirculatingSupply());
        crypto.setTotalSupply(dto.getTotalSupply());
        crypto.setMaxSupply(dto.getMaxSupply());


        if (dto.getLastUpdated() != null) {
            try {

                LocalDateTime lastUpdated = LocalDateTime.parse(
                        dto.getLastUpdated().replace("Z", ""),
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME
                );
                crypto.setLastUpdated(lastUpdated);
            } catch (DateTimeParseException e) {
                logger.warn("Could not parse last updated timestamp: {}", dto.getLastUpdated());
                crypto.setLastUpdated(LocalDateTime.now());
            }
        } else {
            crypto.setLastUpdated(LocalDateTime.now());
        }

        return crypto;
    }


    public boolean isServiceAvailable() {
        try {
            webClient.get()
                    .uri("/ping")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return true;
        } catch (Exception e) {
            logger.warn("CoinGecko API is not available: {}", e.getMessage());
            return false;
        }
    }
}

