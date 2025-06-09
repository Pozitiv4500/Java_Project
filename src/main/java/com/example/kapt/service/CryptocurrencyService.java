package com.example.kapt.service;

import com.example.kapt.dto.CoinGeckoResponseDto;
import com.example.kapt.model.Cryptocurrency;
import com.example.kapt.repository.CryptocurrencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CryptocurrencyService {

    private static final Logger logger = LoggerFactory.getLogger(CryptocurrencyService.class);

    private final CryptocurrencyRepository cryptocurrencyRepository;
    private final CoinGeckoService coinGeckoService;

    public CryptocurrencyService(CryptocurrencyRepository cryptocurrencyRepository, CoinGeckoService coinGeckoService) {
        this.cryptocurrencyRepository = cryptocurrencyRepository;
        this.coinGeckoService = coinGeckoService;
    }

    public void fetchAndSaveCryptocurrencies() {
        logger.info("Starting cryptocurrency data fetch and save process");

        try {
            int page = 1;
            int perPage = 100;
            int totalSaved = 0;

            while (page <= 3) {
                List<CoinGeckoResponseDto> dtos = coinGeckoService.fetchTopCryptocurrencies(page, perPage);

                if (dtos.isEmpty()) {
                    logger.warn("No data received for page {}, stopping fetch", page);
                    break;
                }

                for (CoinGeckoResponseDto dto : dtos) {
                    try {
                        saveOrUpdateCryptocurrency(dto);
                        totalSaved++;
                    } catch (Exception e) {
                        logger.error("Error saving cryptocurrency: {}", dto.getSymbol(), e);
                    }
                }

                page++;
            }

            logger.info("Successfully saved/updated {} cryptocurrencies", totalSaved);

        } catch (Exception e) {
            logger.error("Error during cryptocurrency fetch and save process", e);
        }
    }

    private void saveOrUpdateCryptocurrency(CoinGeckoResponseDto dto) {
        Cryptocurrency crypto = coinGeckoService.convertToEntity(dto);
        if (crypto == null) {
            return;
        }

        Optional<Cryptocurrency> existing = cryptocurrencyRepository.findBySymbolIgnoreCase(crypto.getSymbol());

        if (existing.isPresent()) {

            Cryptocurrency existingCrypto = existing.get();
            updateCryptocurrencyData(existingCrypto, crypto);
            cryptocurrencyRepository.save(existingCrypto);
            logger.debug("Updated cryptocurrency: {}", crypto.getSymbol());
        } else {

            cryptocurrencyRepository.save(crypto);
            logger.debug("Saved new cryptocurrency: {}", crypto.getSymbol());
        }
    }

    private void updateCryptocurrencyData(Cryptocurrency existing, Cryptocurrency newData) {
        existing.setName(newData.getName());
        existing.setCurrentPrice(newData.getCurrentPrice());
        existing.setMarketCap(newData.getMarketCap());
        existing.setMarketCapRank(newData.getMarketCapRank());
        existing.setTotalVolume(newData.getTotalVolume());
        existing.setPriceChange24h(newData.getPriceChange24h());
        existing.setPriceChangePercentage24h(newData.getPriceChangePercentage24h());
        existing.setCirculatingSupply(newData.getCirculatingSupply());
        existing.setTotalSupply(newData.getTotalSupply());
        existing.setMaxSupply(newData.getMaxSupply());
        existing.setLastUpdated(newData.getLastUpdated());
    }

    @Transactional(readOnly = true)
    public Page<Cryptocurrency> getAllCryptocurrencies(int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return cryptocurrencyRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Cryptocurrency> findBySymbol(String symbol) {
        return cryptocurrencyRepository.findBySymbolIgnoreCase(symbol);
    }

    @Transactional(readOnly = true)
    public List<Cryptocurrency> searchCryptocurrencies(String searchTerm) {
        return cryptocurrencyRepository.searchByNameOrSymbol(searchTerm);
    }

    @Transactional(readOnly = true)
    public List<Cryptocurrency> getTopByMarketCap(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return cryptocurrencyRepository.findByMarketCapRankIsNotNullOrderByMarketCapRankAsc(pageable);
    }

    @Transactional(readOnly = true)
    public List<Cryptocurrency> getTopGainers(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return cryptocurrencyRepository.findTopGainers(pageable);
    }

    @Transactional(readOnly = true)
    public List<Cryptocurrency> getTopLosers(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return cryptocurrencyRepository.findTopLosers(pageable);
    }

    @Transactional(readOnly = true)
    public List<Cryptocurrency> getCryptocurrenciesInPriceChangeRange(BigDecimal minChange, BigDecimal maxChange) {
        return cryptocurrencyRepository.findByPriceChangePercentageRange(minChange, maxChange);
    }

    public MarketStatistics getMarketStatistics() {
        System.out.println("CryptocurrencyService.getMarketStatistics() called");


        long totalCount = cryptocurrencyRepository.count();
        System.out.println("Total count before query: " + totalCount);

        Object[] stats = cryptocurrencyRepository.getMarketStatistics();
        System.out.println("Raw query result: " + java.util.Arrays.toString(stats));


        Object[] actualStats = null;
        if (stats != null && stats.length > 0) {
            if (stats[0] instanceof Object[]) {

                actualStats = (Object[]) stats[0];
                System.out.println("Extracted inner array: " + java.util.Arrays.toString(actualStats));
            } else if (stats.length >= 5) {

                actualStats = stats;
            }
        }

        if (actualStats != null && actualStats.length >= 5) {
            try {
                logger.debug("Processing stats array: {}", java.util.Arrays.toString(actualStats));
                Long count = actualStats[0] instanceof Number ? ((Number) actualStats[0]).longValue() : 0L;

                Long totalMarketCap = 0L;
                if (actualStats[1] instanceof BigDecimal) {
                    totalMarketCap = ((BigDecimal) actualStats[1]).longValue();
                } else if (actualStats[1] instanceof Number) {
                    totalMarketCap = ((Number) actualStats[1]).longValue();
                }
                BigDecimal avgPriceChange = actualStats[2] instanceof Number ? new BigDecimal(actualStats[2].toString()) : BigDecimal.ZERO;
                BigDecimal maxPriceChange = actualStats[3] instanceof Number ? new BigDecimal(actualStats[3].toString()) : BigDecimal.ZERO;
                BigDecimal minPriceChange = actualStats[4] instanceof Number ? new BigDecimal(actualStats[4].toString()) : BigDecimal.ZERO;
                System.out.println("Processed stats - count: " + count + ", totalMarketCap: " + totalMarketCap + ", avgPriceChange: " + avgPriceChange + ", maxPriceChange: " + maxPriceChange + ", minPriceChange: " + minPriceChange);
                logger.debug("Processed stats - count: {}, totalMarketCap: {}, avgPriceChange: {}, maxPriceChange: {}, minPriceChange: {}", count, totalMarketCap, avgPriceChange, maxPriceChange, minPriceChange);
                return new MarketStatistics(count, new BigDecimal(totalMarketCap), avgPriceChange, maxPriceChange, minPriceChange);
            } catch (Exception e) {
                System.out.println("Error processing market statistics: " + e.getMessage());
                logger.warn("Error processing market statistics: {}", e.getMessage());
                return new MarketStatistics(0L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
            }
        }
        System.out.println("No stats returned from query or insufficient data");
        return new MarketStatistics(0L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public long getCryptocurrencyCount() {
        return cryptocurrencyRepository.count();
    }

    @Transactional(readOnly = true)
    public List<Cryptocurrency> getRecentlyUpdated(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return cryptocurrencyRepository.findByLastUpdatedAfter(since);
    }

    public static class MarketStatistics {
        private final Long totalCount;
        private final BigDecimal totalMarketCap;
        private final BigDecimal avgPriceChange;
        private final BigDecimal maxPriceChange;
        private final BigDecimal minPriceChange;

        public MarketStatistics(Long totalCount, BigDecimal totalMarketCap, BigDecimal avgPriceChange, BigDecimal maxPriceChange, BigDecimal minPriceChange) {
            this.totalCount = totalCount;
            this.totalMarketCap = totalMarketCap;
            this.avgPriceChange = avgPriceChange;
            this.maxPriceChange = maxPriceChange;
            this.minPriceChange = minPriceChange;
        }

        public Long getTotalCount() {
            return totalCount;
        }

        public BigDecimal getTotalMarketCap() {
            return totalMarketCap;
        }

        public BigDecimal getAvgPriceChange() {
            return avgPriceChange;
        }

        public BigDecimal getMaxPriceChange() {
            return maxPriceChange;
        }

        public BigDecimal getMinPriceChange() {
            return minPriceChange;
        }
    }
}
