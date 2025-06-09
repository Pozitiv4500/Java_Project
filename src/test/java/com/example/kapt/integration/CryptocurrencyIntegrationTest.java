package com.example.kapt.integration;

import com.example.kapt.model.Cryptocurrency;
import com.example.kapt.repository.CryptocurrencyRepository;
import com.example.kapt.service.CryptocurrencyService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@Transactional
@DisplayName("Cryptocurrency Integration Tests")
class CryptocurrencyIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgresql = new PostgreSQLContainer<>("postgres:15-alpine").withDatabaseName("test_crypto_db").withUsername("test_user").withPassword("test_password");
    @Autowired
    private CryptocurrencyService cryptocurrencyService;

    @Autowired
    private CryptocurrencyRepository cryptocurrencyRepository;

    @Autowired
    private EntityManager entityManager;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresql::getJdbcUrl);
        registry.add("spring.datasource.username", postgresql::getUsername);
        registry.add("spring.datasource.password", postgresql::getPassword);
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("app.scheduler.enabled", () -> "false");
        registry.add("app.telegram.bot.enabled", () -> "false");
    }

    @Test
    @DisplayName("Should save and retrieve cryptocurrency")
    void shouldSaveAndRetrieveCryptocurrency() {

        Cryptocurrency bitcoin = createTestBitcoin();


        cryptocurrencyRepository.save(bitcoin);
        Optional<Cryptocurrency> found = cryptocurrencyService.findBySymbol("BTC");


        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Bitcoin");
        assertThat(found.get().getCurrentPrice()).isEqualTo(new BigDecimal("45000.00"));
    }

    @Test
    @DisplayName("Should search cryptocurrencies by name and symbol")
    void shouldSearchCryptocurrenciesByNameAndSymbol() {

        Cryptocurrency bitcoin = createTestBitcoin();
        Cryptocurrency ethereum = createTestEthereum();
        cryptocurrencyRepository.save(bitcoin);
        cryptocurrencyRepository.save(ethereum);


        List<Cryptocurrency> bitcoinResults = cryptocurrencyService.searchCryptocurrencies("bit");
        List<Cryptocurrency> ethereumResults = cryptocurrencyService.searchCryptocurrencies("ETH");


        assertThat(bitcoinResults).hasSize(1);
        assertThat(bitcoinResults.get(0).getSymbol()).isEqualTo("BTC");

        assertThat(ethereumResults).hasSize(1);
        assertThat(ethereumResults.get(0).getSymbol()).isEqualTo("ETH");
    }

    @Test
    @DisplayName("Should get top cryptocurrencies by market cap")
    void shouldGetTopCryptocurrenciesByMarketCap() {

        Cryptocurrency bitcoin = createTestBitcoin();
        bitcoin.setMarketCapRank(1);

        Cryptocurrency ethereum = createTestEthereum();
        ethereum.setMarketCapRank(2);

        cryptocurrencyRepository.save(bitcoin);
        cryptocurrencyRepository.save(ethereum);


        List<Cryptocurrency> topCryptos = cryptocurrencyService.getTopByMarketCap(2);


        assertThat(topCryptos).hasSize(2);
        assertThat(topCryptos.get(0).getMarketCapRank()).isEqualTo(1);
        assertThat(topCryptos.get(1).getMarketCapRank()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should get top gainers and losers")
    void shouldGetTopGainersAndLosers() {

        Cryptocurrency gainer = createTestBitcoin();
        gainer.setPriceChangePercentage24h(new BigDecimal("15.50"));

        Cryptocurrency loser = createTestEthereum();
        loser.setPriceChangePercentage24h(new BigDecimal("-8.25"));

        cryptocurrencyRepository.save(gainer);
        cryptocurrencyRepository.save(loser);


        List<Cryptocurrency> gainers = cryptocurrencyService.getTopGainers(1);
        List<Cryptocurrency> losers = cryptocurrencyService.getTopLosers(1);


        assertThat(gainers).hasSize(1);
        assertThat(gainers.get(0).getPriceChangePercentage24h()).isPositive();

        assertThat(losers).hasSize(1);
        assertThat(losers.get(0).getPriceChangePercentage24h()).isNegative();
    }

    @Test
    @DisplayName("Should get market statistics")
    void shouldGetMarketStatistics() {

        Cryptocurrency bitcoin = createTestBitcoin();
        bitcoin.setPriceChangePercentage24h(new BigDecimal("5.0"));
        bitcoin.setMarketCap(new BigDecimal("850000000000"));

        Cryptocurrency ethereum = createTestEthereum();
        ethereum.setPriceChangePercentage24h(new BigDecimal("-2.5"));
        ethereum.setMarketCap(new BigDecimal("400000000000"));

        System.out.println("Before save - Bitcoin: " + bitcoin);
        System.out.println("Before save - Ethereum: " + ethereum);

        Cryptocurrency savedBitcoin = cryptocurrencyRepository.save(bitcoin);
        Cryptocurrency savedEthereum = cryptocurrencyRepository.save(ethereum);

        System.out.println("After save - Bitcoin: " + savedBitcoin);
        System.out.println("After save - Ethereum: " + savedEthereum);


        entityManager.flush();
        entityManager.clear();


        long count = cryptocurrencyRepository.count();
        System.out.println("Total count in repository: " + count);

        List<Cryptocurrency> allCryptos = cryptocurrencyRepository.findAll();
        System.out.println("All cryptocurrencies: " + allCryptos);


        CryptocurrencyService.MarketStatistics stats = cryptocurrencyService.getMarketStatistics();


        assertThat(stats.getTotalCount()).isEqualTo(2L);
        assertThat(stats.getTotalMarketCap()).isEqualTo(new BigDecimal("1250000000000"));
        assertThat(stats.getAvgPriceChange()).isEqualTo(new BigDecimal("1.25"));
        assertThat(stats.getMaxPriceChange()).isEqualTo(new BigDecimal("5.0"));
        assertThat(stats.getMinPriceChange()).isEqualTo(new BigDecimal("-2.5"));
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void shouldHandlePaginationCorrectly() {

        for (int i = 1; i <= 25; i++) {
            Cryptocurrency crypto = new Cryptocurrency();
            crypto.setSymbol("CRYPTO" + i);
            crypto.setName("Test Crypto " + i);
            crypto.setMarketCapRank(i);
            crypto.setLastUpdated(LocalDateTime.now());
            cryptocurrencyRepository.save(crypto);
        }


        Page<Cryptocurrency> firstPage = cryptocurrencyService.getAllCryptocurrencies(0, 10, "marketCapRank", "asc");
        Page<Cryptocurrency> secondPage = cryptocurrencyService.getAllCryptocurrencies(1, 10, "marketCapRank", "asc");


        assertThat(firstPage.getContent()).hasSize(10);
        assertThat(firstPage.getTotalElements()).isEqualTo(25);
        assertThat(firstPage.getTotalPages()).isEqualTo(3);

        assertThat(secondPage.getContent()).hasSize(10);
        assertThat(secondPage.getNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should get recently updated cryptocurrencies")
    void shouldGetRecentlyUpdatedCryptocurrencies() {

        Cryptocurrency recent = createTestBitcoin();
        recent.setLastUpdated(LocalDateTime.now().minusMinutes(30));

        Cryptocurrency old = createTestEthereum();
        old.setLastUpdated(LocalDateTime.now().minusHours(5));

        cryptocurrencyRepository.save(recent);
        cryptocurrencyRepository.save(old);


        List<Cryptocurrency> recentlyUpdated = cryptocurrencyService.getRecentlyUpdated(1);


        assertThat(recentlyUpdated).hasSize(1);
        assertThat(recentlyUpdated.get(0).getSymbol()).isEqualTo("BTC");
    }

    @Test
    @DisplayName("Should get cryptocurrencies in price change range")
    void shouldGetCryptocurrenciesInPriceChangeRange() {

        Cryptocurrency crypto1 = createTestBitcoin();
        crypto1.setPriceChangePercentage24h(new BigDecimal("5.0"));

        Cryptocurrency crypto2 = createTestEthereum();
        crypto2.setPriceChangePercentage24h(new BigDecimal("15.0"));

        cryptocurrencyRepository.save(crypto1);
        cryptocurrencyRepository.save(crypto2);


        List<Cryptocurrency> inRange = cryptocurrencyService.getCryptocurrenciesInPriceChangeRange(new BigDecimal("0"), new BigDecimal("10"));


        assertThat(inRange).hasSize(1);
        assertThat(inRange.get(0).getPriceChangePercentage24h()).isEqualTo(new BigDecimal("5.0"));
    }

    private Cryptocurrency createTestBitcoin() {
        Cryptocurrency bitcoin = new Cryptocurrency();
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

    private Cryptocurrency createTestEthereum() {
        Cryptocurrency ethereum = new Cryptocurrency();
        ethereum.setSymbol("ETH");
        ethereum.setName("Ethereum");
        ethereum.setCurrentPrice(new BigDecimal("2500.00"));
        ethereum.setMarketCap(new BigDecimal("300000000000"));
        ethereum.setMarketCapRank(2);
        ethereum.setTotalVolume(new BigDecimal("15000000000"));
        ethereum.setPriceChange24h(new BigDecimal("-125.50"));
        ethereum.setPriceChangePercentage24h(new BigDecimal("-4.85"));
        ethereum.setCirculatingSupply(new BigDecimal("120000000"));
        ethereum.setTotalSupply(new BigDecimal("120000000"));
        ethereum.setLastUpdated(LocalDateTime.now());
        return ethereum;
    }
}
