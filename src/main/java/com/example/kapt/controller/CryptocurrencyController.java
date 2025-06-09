package com.example.kapt.controller;

import com.example.kapt.model.Cryptocurrency;
import com.example.kapt.service.CryptocurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/cryptocurrencies")
@Tag(name = "Cryptocurrency", description = "Cryptocurrency management API")
public class CryptocurrencyController {

    private static final Logger logger = LoggerFactory.getLogger(CryptocurrencyController.class);

    private final CryptocurrencyService cryptocurrencyService;

    public CryptocurrencyController(CryptocurrencyService cryptocurrencyService) {
        this.cryptocurrencyService = cryptocurrencyService;
    }

    @GetMapping
    @Operation(summary = "Get all cryptocurrencies", description = "Retrieve all cryptocurrencies with pagination and sorting")
    public ResponseEntity<Page<Cryptocurrency>> getAllCryptocurrencies(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "marketCapRank") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)")
            @RequestParam(defaultValue = "asc") String sortDir) {

        logger.info("Getting cryptocurrencies: page={}, size={}, sortBy={}, sortDir={}",
                page, size, sortBy, sortDir);

        Page<Cryptocurrency> cryptocurrencies = cryptocurrencyService.getAllCryptocurrencies(page, size, sortBy, sortDir);
        return ResponseEntity.ok(cryptocurrencies);
    }

    @GetMapping("/{symbol}")
    @Operation(summary = "Get cryptocurrency by symbol", description = "Retrieve a specific cryptocurrency by its symbol")
    public ResponseEntity<Cryptocurrency> getCryptocurrencyBySymbol(
            @Parameter(description = "Cryptocurrency symbol (e.g., BTC, ETH)")
            @PathVariable String symbol) {

        logger.info("Getting cryptocurrency by symbol: {}", symbol);

        Optional<Cryptocurrency> cryptocurrency = cryptocurrencyService.findBySymbol(symbol);
        return cryptocurrency.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @Operation(summary = "Search cryptocurrencies", description = "Search cryptocurrencies by name or symbol")
    public ResponseEntity<List<Cryptocurrency>> searchCryptocurrencies(
            @Parameter(description = "Search term")
            @RequestParam String q) {

        logger.info("Searching cryptocurrencies with term: {}", q);

        List<Cryptocurrency> results = cryptocurrencyService.searchCryptocurrencies(q);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/top")
    @Operation(summary = "Get top cryptocurrencies", description = "Get top cryptocurrencies by market capitalization")
    public ResponseEntity<List<Cryptocurrency>> getTopCryptocurrencies(
            @Parameter(description = "Number of cryptocurrencies to return")
            @RequestParam(defaultValue = "10") int limit) {

        logger.info("Getting top {} cryptocurrencies by market cap", limit);

        List<Cryptocurrency> topCryptocurrencies = cryptocurrencyService.getTopByMarketCap(limit);
        return ResponseEntity.ok(topCryptocurrencies);
    }

    @GetMapping("/gainers")
    @Operation(summary = "Get top gainers", description = "Get cryptocurrencies with highest price increase in last 24 hours")
    public ResponseEntity<List<Cryptocurrency>> getTopGainers(
            @Parameter(description = "Number of gainers to return")
            @RequestParam(defaultValue = "10") int limit) {

        logger.info("Getting top {} gainers", limit);

        List<Cryptocurrency> gainers = cryptocurrencyService.getTopGainers(limit);
        return ResponseEntity.ok(gainers);
    }

    @GetMapping("/losers")
    @Operation(summary = "Get top losers", description = "Get cryptocurrencies with highest price decrease in last 24 hours")
    public ResponseEntity<List<Cryptocurrency>> getTopLosers(
            @Parameter(description = "Number of losers to return")
            @RequestParam(defaultValue = "10") int limit) {

        logger.info("Getting top {} losers", limit);

        List<Cryptocurrency> losers = cryptocurrencyService.getTopLosers(limit);
        return ResponseEntity.ok(losers);
    }

    @GetMapping("/price-change")
    @Operation(summary = "Get cryptocurrencies by price change range",
            description = "Get cryptocurrencies with 24h price change percentage in specified range")
    public ResponseEntity<List<Cryptocurrency>> getCryptocurrenciesByPriceChangeRange(
            @Parameter(description = "Minimum price change percentage")
            @RequestParam BigDecimal minChange,
            @Parameter(description = "Maximum price change percentage")
            @RequestParam BigDecimal maxChange) {

        logger.info("Getting cryptocurrencies with price change between {}% and {}%", minChange, maxChange);

        List<Cryptocurrency> cryptocurrencies = cryptocurrencyService
                .getCryptocurrenciesInPriceChangeRange(minChange, maxChange);
        return ResponseEntity.ok(cryptocurrencies);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get market statistics", description = "Get overall cryptocurrency market statistics")
    public ResponseEntity<CryptocurrencyService.MarketStatistics> getMarketStatistics() {
        logger.info("Getting market statistics");

        CryptocurrencyService.MarketStatistics stats = cryptocurrencyService.getMarketStatistics();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/update")
    @Operation(summary = "Update cryptocurrency data", description = "Manually trigger cryptocurrency data update from external API")
    public ResponseEntity<String> updateCryptocurrencyData() {
        logger.info("Manual cryptocurrency data update triggered");

        try {
            cryptocurrencyService.fetchAndSaveCryptocurrencies();
            return ResponseEntity.ok("Cryptocurrency data update started successfully");
        } catch (Exception e) {
            logger.error("Error triggering cryptocurrency data update", e);
            return ResponseEntity.internalServerError()
                    .body("Error triggering cryptocurrency data update: " + e.getMessage());
        }
    }
}
