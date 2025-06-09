package com.example.kapt.controller;

import com.example.kapt.model.Cryptocurrency;
import com.example.kapt.service.CryptocurrencyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CryptocurrencyController.class)
@DisplayName("CryptocurrencyController Tests")
class CryptocurrencyControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CryptocurrencyService cryptocurrencyService;

    @Test
    @DisplayName("Should get all cryptocurrencies with default pagination")
    void shouldGetAllCryptocurrenciesWithDefaultPagination() throws Exception {

        List<Cryptocurrency> cryptocurrencies = List.of(createTestBitcoin());
        Page<Cryptocurrency> page = new PageImpl<>(cryptocurrencies);
        when(cryptocurrencyService.getAllCryptocurrencies(anyInt(), anyInt(), anyString(), anyString())).thenReturn(page);
        mockMvc.perform(get("/api/v1/cryptocurrencies")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.content", hasSize(1))).andExpect(jsonPath("$.content[0].symbol", is("BTC"))).andExpect(jsonPath("$.content[0].name", is("Bitcoin"))).andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    @DisplayName("Should get all cryptocurrencies with custom pagination")
    void shouldGetAllCryptocurrenciesWithCustomPagination() throws Exception {

        List<Cryptocurrency> cryptocurrencies = List.of(createTestBitcoin());
        Page<Cryptocurrency> page = new PageImpl<>(cryptocurrencies);
        when(cryptocurrencyService.getAllCryptocurrencies(1, 5, "currentPrice", "desc")).thenReturn(page);


        mockMvc.perform(get("/api/v1/cryptocurrencies").param("page", "1").param("size", "5").param("sortBy", "currentPrice").param("sortDir", "desc")).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @DisplayName("Should get cryptocurrency by symbol")
    void shouldGetCryptocurrencyBySymbol() throws Exception {

        Cryptocurrency bitcoin = createTestBitcoin();
        when(cryptocurrencyService.findBySymbol("BTC")).thenReturn(Optional.of(bitcoin));


        mockMvc.perform(get("/api/v1/cryptocurrencies/BTC")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.symbol", is("BTC"))).andExpect(jsonPath("$.name", is("Bitcoin"))).andExpect(jsonPath("$.currentPrice", is(45000.00))).andExpect(jsonPath("$.marketCapRank", is(1)));
    }

    @Test
    @DisplayName("Should return 404 when cryptocurrency not found by symbol")
    void shouldReturn404WhenCryptocurrencyNotFoundBySymbol() throws Exception {

        when(cryptocurrencyService.findBySymbol("NONEXISTENT")).thenReturn(Optional.empty());


        mockMvc.perform(get("/api/v1/cryptocurrencies/NONEXISTENT")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should search cryptocurrencies")
    void shouldSearchCryptocurrencies() throws Exception {

        List<Cryptocurrency> results = List.of(createTestBitcoin());
        when(cryptocurrencyService.searchCryptocurrencies("bit")).thenReturn(results);


        mockMvc.perform(get("/api/v1/cryptocurrencies/search").param("q", "bit")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].symbol", is("BTC")));
    }

    @Test
    @DisplayName("Should get top cryptocurrencies")
    void shouldGetTopCryptocurrencies() throws Exception {

        List<Cryptocurrency> topCryptos = List.of(createTestBitcoin());
        when(cryptocurrencyService.getTopByMarketCap(10)).thenReturn(topCryptos);


        mockMvc.perform(get("/api/v1/cryptocurrencies/top").param("limit", "10")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].marketCapRank", is(1)));
    }

    @Test
    @DisplayName("Should get top gainers")
    void shouldGetTopGainers() throws Exception {

        Cryptocurrency gainer = createTestBitcoin();
        gainer.setPriceChangePercentage24h(new BigDecimal("15.50"));
        List<Cryptocurrency> gainers = List.of(gainer);
        when(cryptocurrencyService.getTopGainers(5)).thenReturn(gainers);


        mockMvc.perform(get("/api/v1/cryptocurrencies/gainers").param("limit", "5")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].priceChangePercentage24h", is(15.50)));
    }

    @Test
    @DisplayName("Should get top losers")
    void shouldGetTopLosers() throws Exception {

        Cryptocurrency loser = createTestBitcoin();
        loser.setPriceChangePercentage24h(new BigDecimal("-8.25"));
        List<Cryptocurrency> losers = List.of(loser);
        when(cryptocurrencyService.getTopLosers(5)).thenReturn(losers);


        mockMvc.perform(get("/api/v1/cryptocurrencies/losers").param("limit", "5")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].priceChangePercentage24h", is(-8.25)));
    }

    @Test
    @DisplayName("Should get cryptocurrencies by price change range")
    void shouldGetCryptocurrenciesByPriceChangeRange() throws Exception {

        List<Cryptocurrency> cryptos = List.of(createTestBitcoin());
        when(cryptocurrencyService.getCryptocurrenciesInPriceChangeRange(new BigDecimal("0"), new BigDecimal("10"))).thenReturn(cryptos);


        mockMvc.perform(get("/api/v1/cryptocurrencies/price-change").param("minChange", "0").param("maxChange", "10")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("Should get market statistics")
    void shouldGetMarketStatistics() throws Exception {

        CryptocurrencyService.MarketStatistics stats = new CryptocurrencyService.MarketStatistics(100L, new BigDecimal("2000000000000"), new BigDecimal("2.5"), new BigDecimal("15.0"), new BigDecimal("-10.0"));
        when(cryptocurrencyService.getMarketStatistics()).thenReturn(stats);
        mockMvc.perform(get("/api/v1/cryptocurrencies/statistics")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.totalCount").value(100)).andExpect(jsonPath("$.totalMarketCap").value(2.0E12)).andExpect(jsonPath("$.avgPriceChange").value(2.5)).andExpect(jsonPath("$.maxPriceChange").value(15.0)).andExpect(jsonPath("$.minPriceChange").value(-10.0));
    }

    @Test
    @DisplayName("Should trigger manual data update")
    void shouldTriggerManualDataUpdate() throws Exception {

        mockMvc.perform(post("/api/v1/cryptocurrencies/update")).andExpect(status().isOk()).andExpect(content().string(containsString("update started successfully")));
    }

    @Test
    @DisplayName("Should handle service exception during manual update")
    void shouldHandleServiceExceptionDuringManualUpdate() throws Exception {

        doThrow(new RuntimeException("Service error")).when(cryptocurrencyService).fetchAndSaveCryptocurrencies();
        mockMvc.perform(post("/api/v1/cryptocurrencies/update")).andExpect(status().isInternalServerError()).andExpect(content().string(containsString("Error triggering cryptocurrency data update")));
    }

    private Cryptocurrency createTestBitcoin() {
        Cryptocurrency bitcoin = new Cryptocurrency();
        bitcoin.setId(1L);
        bitcoin.setSymbol("BTC");
        bitcoin.setName("Bitcoin");
        bitcoin.setCurrentPrice(new BigDecimal("45000.00"));
        bitcoin.setMarketCap(new BigDecimal("850000000000"));
        bitcoin.setMarketCapRank(1);
        bitcoin.setTotalVolume(new BigDecimal("25000000000"));
        bitcoin.setPriceChange24h(new BigDecimal("1250.50"));
        bitcoin.setPriceChangePercentage24h(new BigDecimal("2.85"));
        bitcoin.setCirculatingSupply(new BigDecimal("19000000"));
        bitcoin.setTotalSupply(new BigDecimal("21000000"));
        bitcoin.setMaxSupply(new BigDecimal("21000000"));
        bitcoin.setLastUpdated(LocalDateTime.now());
        return bitcoin;
    }
}
