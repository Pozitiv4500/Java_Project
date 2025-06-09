package com.example.kapt.service;

import com.example.kapt.dto.CoinGeckoResponseDto;
import com.example.kapt.model.Cryptocurrency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CoinGeckoService.
 * Tests focus on API integration and data transformation logic.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CoinGeckoService Tests")
class CoinGeckoServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private CoinGeckoService coinGeckoService;

    @BeforeEach
    void setUp() {
        WebClient.Builder webClientBuilder = mock(WebClient.Builder.class);
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        coinGeckoService = new CoinGeckoService(webClientBuilder);
        ReflectionTestUtils.setField(coinGeckoService, "requestDelay", 0L); // Remove delay for tests
    }

    @Test
    @DisplayName("Should convert CoinGecko DTO to Cryptocurrency entity successfully")
    void shouldConvertDtoToEntitySuccessfully() {
        // Given
        CoinGeckoResponseDto dto = createTestDto();

        // When
        Cryptocurrency result = coinGeckoService.convertToEntity(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSymbol()).isEqualTo("BTC");
        assertThat(result.getName()).isEqualTo("Bitcoin");
        assertThat(result.getCurrentPrice()).isEqualTo(new BigDecimal("45000.00"));
        assertThat(result.getMarketCap()).isEqualTo(new BigDecimal("850000000000"));
        assertThat(result.getMarketCapRank()).isEqualTo(1);
        assertThat(result.getTotalVolume()).isEqualTo(new BigDecimal("25000000000"));
        assertThat(result.getPriceChange24h()).isEqualTo(new BigDecimal("1250.50"));
        assertThat(result.getPriceChangePercentage24h()).isEqualTo(new BigDecimal("2.85"));
        assertThat(result.getCirculatingSupply()).isEqualTo(new BigDecimal("19000000"));
        assertThat(result.getTotalSupply()).isEqualTo(new BigDecimal("21000000"));
        assertThat(result.getMaxSupply()).isEqualTo(new BigDecimal("21000000"));
        assertThat(result.getLastUpdated()).isNotNull();
    }

    @Test
    @DisplayName("Should return null when converting null DTO")
    void shouldReturnNullWhenConvertingNullDto() {
        // When
        Cryptocurrency result = coinGeckoService.convertToEntity(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should handle missing last updated timestamp")
    void shouldHandleMissingLastUpdatedTimestamp() {
        // Given
        CoinGeckoResponseDto dto = createTestDto();
        dto.setLastUpdated(null);

        // When
        Cryptocurrency result = coinGeckoService.convertToEntity(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLastUpdated()).isNotNull();
        assertThat(result.getLastUpdated()).isBefore(LocalDateTime.now().plusMinutes(1));
    }

    @Test
    @DisplayName("Should handle invalid last updated timestamp format")
    void shouldHandleInvalidLastUpdatedTimestampFormat() {
        // Given
        CoinGeckoResponseDto dto = createTestDto();
        dto.setLastUpdated("invalid-timestamp");

        // When
        Cryptocurrency result = coinGeckoService.convertToEntity(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLastUpdated()).isNotNull();
        assertThat(result.getLastUpdated()).isBefore(LocalDateTime.now().plusMinutes(1));
    }

    @Test
    @DisplayName("Should fetch top cryptocurrencies successfully")
    void shouldFetchTopCryptocurrenciesSuccessfully() {
        // Given
        int page = 1;
        int perPage = 10;
        List<CoinGeckoResponseDto> expectedResponse = List.of(createTestDto());

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(expectedResponse));

        // When
        List<CoinGeckoResponseDto> result = coinGeckoService.fetchTopCryptocurrencies(page, perPage);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSymbol()).isEqualTo("btc");
        assertThat(result.get(0).getName()).isEqualTo("Bitcoin");
    }

    @Test
    @DisplayName("Should return empty list when API call fails")
    void shouldReturnEmptyListWhenApiCallFails() {
        // Given
        int page = 1;
        int perPage = 10;

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.error(new RuntimeException("API Error")));

        // When
        List<CoinGeckoResponseDto> result = coinGeckoService.fetchTopCryptocurrencies(page, perPage);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should limit per page parameter to maximum of 250")
    void shouldLimitPerPageParameterToMaximum250() {
        // Given
        int page = 1;
        int perPage = 300; // More than allowed maximum
        List<CoinGeckoResponseDto> expectedResponse = List.of(createTestDto());

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(contains("per_page=250"))).thenReturn(requestHeadersSpec); // Should be limited to 250
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(expectedResponse));

        // When
        List<CoinGeckoResponseDto> result = coinGeckoService.fetchTopCryptocurrencies(page, perPage);

        // Then
        assertThat(result).hasSize(1);
        verify(requestHeadersUriSpec).uri(contains("per_page=250"));
    }

    @Test
    @DisplayName("Should convert symbol to uppercase")
    void shouldConvertSymbolToUppercase() {
        // Given
        CoinGeckoResponseDto dto = createTestDto();
        dto.setSymbol("btc"); // lowercase

        // When
        Cryptocurrency result = coinGeckoService.convertToEntity(dto);

        // Then
        assertThat(result.getSymbol()).isEqualTo("BTC"); // Should be uppercase
    }

    @Test
    @DisplayName("Should handle null values in DTO")
    void shouldHandleNullValuesInDto() {
        // Given
        CoinGeckoResponseDto dto = new CoinGeckoResponseDto();
        dto.setSymbol("test");
        dto.setName("Test Coin");
        // All other fields are null

        // When
        Cryptocurrency result = coinGeckoService.convertToEntity(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSymbol()).isEqualTo("TEST");
        assertThat(result.getName()).isEqualTo("Test Coin");
        assertThat(result.getCurrentPrice()).isNull();
        assertThat(result.getMarketCap()).isNull();
        assertThat(result.getLastUpdated()).isNotNull();
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
        dto.setLastUpdated("2023-12-07T10:30:00.000");
        return dto;
    }
}
