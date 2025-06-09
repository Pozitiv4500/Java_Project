package com.example.kapt.service;

import com.example.kapt.dto.CoinGeckoResponseDto;
import com.example.kapt.model.Cryptocurrency;
import com.example.kapt.repository.CryptocurrencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CryptocurrencyService Tests")
class CryptocurrencyServiceTest {

    @Mock
    private CryptocurrencyRepository cryptocurrencyRepository;

    @Mock
    private CoinGeckoService coinGeckoService;

    @InjectMocks
    private CryptocurrencyService cryptocurrencyService;

    private Cryptocurrency testCryptocurrency;
    private CoinGeckoResponseDto testDto;

    @BeforeEach
    void setUp() {
        testCryptocurrency = createTestCryptocurrency();
        testDto = createTestDto();
    }

    @Test
    @DisplayName("Should find cryptocurrency by symbol successfully")
    void shouldFindCryptocurrencyBySymbol() {
        // Given
        String symbol = "BTC";
        when(cryptocurrencyRepository.findBySymbolIgnoreCase(symbol))
                .thenReturn(Optional.of(testCryptocurrency));

        // When
        Optional<Cryptocurrency> result = cryptocurrencyService.findBySymbol(symbol);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getSymbol()).isEqualTo("BTC");
        assertThat(result.get().getName()).isEqualTo("Bitcoin");
        verify(cryptocurrencyRepository).findBySymbolIgnoreCase(symbol);
    }

    @Test
    @DisplayName("Should return empty when cryptocurrency not found by symbol")
    void shouldReturnEmptyWhenCryptocurrencyNotFoundBySymbol() {
        // Given
        String symbol = "NONEXISTENT";
        when(cryptocurrencyRepository.findBySymbolIgnoreCase(symbol))
                .thenReturn(Optional.empty());

        // When
        Optional<Cryptocurrency> result = cryptocurrencyService.findBySymbol(symbol);

        // Then
        assertThat(result).isEmpty();
        verify(cryptocurrencyRepository).findBySymbolIgnoreCase(symbol);
    }

    @Test
    @DisplayName("Should search cryptocurrencies by name or symbol")
    void shouldSearchCryptocurrencies() {
        // Given
        String searchTerm = "bit";
        List<Cryptocurrency> expectedResults = Collections.singletonList(testCryptocurrency);
        when(cryptocurrencyRepository.searchByNameOrSymbol(searchTerm))
                .thenReturn(expectedResults);

        // When
        List<Cryptocurrency> results = cryptocurrencyService.searchCryptocurrencies(searchTerm);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getSymbol()).isEqualTo("BTC");
        verify(cryptocurrencyRepository).searchByNameOrSymbol(searchTerm);
    }

    @Test
    @DisplayName("Should get all cryptocurrencies with pagination")
    void shouldGetAllCryptocurrenciesWithPagination() {
        // Given
        int page = 0;
        int size = 10;
        String sortBy = "marketCapRank";
        String sortDir = "asc";

        List<Cryptocurrency> cryptoList = Collections.singletonList(testCryptocurrency);
        Page<Cryptocurrency> expectedPage = new PageImpl<>(cryptoList);

        when(cryptocurrencyRepository.findAll(any(Pageable.class)))
                .thenReturn(expectedPage);

        // When
        Page<Cryptocurrency> result = cryptocurrencyService.getAllCryptocurrencies(page, size, sortBy, sortDir);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getSymbol()).isEqualTo("BTC");
        verify(cryptocurrencyRepository).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Should get top cryptocurrencies by market cap")
    void shouldGetTopCryptocurrenciesByMarketCap() {
        // Given
        int limit = 5;
        List<Cryptocurrency> expectedResults = Collections.singletonList(testCryptocurrency);
        when(cryptocurrencyRepository.findByMarketCapRankIsNotNullOrderByMarketCapRankAsc(any(Pageable.class)))
                .thenReturn(expectedResults);

        // When
        List<Cryptocurrency> results = cryptocurrencyService.getTopByMarketCap(limit);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getMarketCapRank()).isEqualTo(1);
        verify(cryptocurrencyRepository).findByMarketCapRankIsNotNullOrderByMarketCapRankAsc(any(Pageable.class));
    }

    @Test
    @DisplayName("Should get top gainers")
    void shouldGetTopGainers() {
        // Given
        int limit = 5;
        List<Cryptocurrency> expectedResults = Collections.singletonList(testCryptocurrency);
        when(cryptocurrencyRepository.findTopGainers(any(Pageable.class)))
                .thenReturn(expectedResults);

        // When
        List<Cryptocurrency> results = cryptocurrencyService.getTopGainers(limit);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getPriceChangePercentage24h()).isPositive();
        verify(cryptocurrencyRepository).findTopGainers(any(Pageable.class));
    }

    @Test
    @DisplayName("Should get top losers")
    void shouldGetTopLosers() {
        // Given
        int limit = 5;
        Cryptocurrency loser = createTestCryptocurrency();
        loser.setPriceChangePercentage24h(new BigDecimal("-5.25"));
        List<Cryptocurrency> expectedResults = List.of(loser);

        when(cryptocurrencyRepository.findTopLosers(any(Pageable.class)))
                .thenReturn(expectedResults);

        // When
        List<Cryptocurrency> results = cryptocurrencyService.getTopLosers(limit);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getPriceChangePercentage24h()).isNegative();
        verify(cryptocurrencyRepository).findTopLosers(any(Pageable.class));
    }

    @Test
    @DisplayName("Should get cryptocurrencies in price change range")
    void shouldGetCryptocurrenciesInPriceChangeRange() {
        // Given
        BigDecimal minChange = new BigDecimal("-10");
        BigDecimal maxChange = new BigDecimal("10");
        List<Cryptocurrency> expectedResults = Collections.singletonList(testCryptocurrency);

        when(cryptocurrencyRepository.findByPriceChangePercentageRange(minChange, maxChange))
                .thenReturn(expectedResults);

        // When
        List<Cryptocurrency> results = cryptocurrencyService
                .getCryptocurrenciesInPriceChangeRange(minChange, maxChange);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getPriceChangePercentage24h())
                .isBetween(minChange, maxChange);
        verify(cryptocurrencyRepository).findByPriceChangePercentageRange(minChange, maxChange);
    }

    @Test
    @DisplayName("Should get market statistics")
    void shouldGetMarketStatistics() {
        // Given
        Object[] statsArray = {
                5L,                                    // totalCount
                new BigDecimal("1000000000"),         // totalMarketCap
                new BigDecimal("2.5"),                // avgPriceChange
                new BigDecimal("15.5"),               // maxPriceChange
                new BigDecimal("-8.2")                // minPriceChange
        };
        when(cryptocurrencyRepository.getMarketStatistics()).thenReturn(statsArray);

        // When
        CryptocurrencyService.MarketStatistics stats = cryptocurrencyService.getMarketStatistics();

        // Then
        assertThat(stats.getTotalCount()).isEqualTo(5L);
        assertThat(stats.getTotalMarketCap()).isEqualTo(new BigDecimal("1000000000"));
        assertThat(stats.getAvgPriceChange()).isEqualTo(new BigDecimal("2.5"));
        assertThat(stats.getMaxPriceChange()).isEqualTo(new BigDecimal("15.5"));
        assertThat(stats.getMinPriceChange()).isEqualTo(new BigDecimal("-8.2"));
        verify(cryptocurrencyRepository).getMarketStatistics();
    }

    @Test
    @DisplayName("Should return empty statistics when no data available")
    void shouldReturnEmptyStatisticsWhenNoDataAvailable() {
        // Given
        Object[] emptyStatsArray = {};
        when(cryptocurrencyRepository.getMarketStatistics()).thenReturn(emptyStatsArray);

        // When
        CryptocurrencyService.MarketStatistics stats = cryptocurrencyService.getMarketStatistics();

        // Then
        assertThat(stats.getTotalCount()).isEqualTo(0L);
        assertThat(stats.getTotalMarketCap()).isEqualTo(BigDecimal.ZERO);
        assertThat(stats.getAvgPriceChange()).isEqualTo(BigDecimal.ZERO);
        assertThat(stats.getMaxPriceChange()).isEqualTo(BigDecimal.ZERO);
        assertThat(stats.getMinPriceChange()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should get cryptocurrency count")
    void shouldGetCryptocurrencyCount() {
        // Given
        long expectedCount = 100L;
        when(cryptocurrencyRepository.count()).thenReturn(expectedCount);

        // When
        long count = cryptocurrencyService.getCryptocurrencyCount();

        // Then
        assertThat(count).isEqualTo(expectedCount);
        verify(cryptocurrencyRepository).count();
    }

    @Test
    @DisplayName("Should get recently updated cryptocurrencies")
    void shouldGetRecentlyUpdatedCryptocurrencies() {
        // Given
        int hours = 1;
        List<Cryptocurrency> expectedResults = Collections.singletonList(testCryptocurrency);
        when(cryptocurrencyRepository.findByLastUpdatedAfter(any(LocalDateTime.class)))
                .thenReturn(expectedResults);

        // When
        List<Cryptocurrency> results = cryptocurrencyService.getRecentlyUpdated(hours);

        // Then
        assertThat(results).hasSize(1);
        verify(cryptocurrencyRepository).findByLastUpdatedAfter(any(LocalDateTime.class));
    }

    private Cryptocurrency createTestCryptocurrency() {
        Cryptocurrency crypto = new Cryptocurrency();
        crypto.setId(1L);
        crypto.setSymbol("BTC");
        crypto.setName("Bitcoin");
        crypto.setCurrentPrice(new BigDecimal("45000.00"));
        crypto.setMarketCap(new BigDecimal("850000000000"));
        crypto.setMarketCapRank(1);
        crypto.setTotalVolume(new BigDecimal("25000000000"));
        crypto.setPriceChange24h(new BigDecimal("1250.50"));
        crypto.setPriceChangePercentage24h(new BigDecimal("2.85"));
        crypto.setCirculatingSupply(new BigDecimal("19000000"));
        crypto.setTotalSupply(new BigDecimal("21000000"));
        crypto.setMaxSupply(new BigDecimal("21000000"));
        crypto.setLastUpdated(LocalDateTime.now());
        return crypto;
    }

    private CoinGeckoResponseDto createTestDto() {
        CoinGeckoResponseDto dto = new CoinGeckoResponseDto();
        dto.setId("bitcoin");
        dto.setSymbol("btc");
        dto.setName("Bitcoin");
        dto.setCurrentPrice(new BigDecimal("45000.00"));
        dto.setMarketCap(new BigDecimal("850000000000"));
        dto.setMarketCapRank(1);
        dto.setTotalVolume(new BigDecimal("25000000000"));
        dto.setPriceChange24h(new BigDecimal("1250.50"));
        dto.setPriceChangePercentage24h(new BigDecimal("2.85"));
        dto.setCirculatingSupply(new BigDecimal("19000000"));
        dto.setTotalSupply(new BigDecimal("21000000"));
        dto.setMaxSupply(new BigDecimal("21000000"));
        dto.setLastUpdated("2023-12-07T10:30:00.000Z");
        return dto;
    }
}
