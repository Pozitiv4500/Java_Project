package com.example.kapt.repository;

import com.example.kapt.model.Cryptocurrency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CryptocurrencyRepository extends JpaRepository<Cryptocurrency, Long> {

    Optional<Cryptocurrency> findBySymbolIgnoreCase(String symbol);

    List<Cryptocurrency> findByNameContainingIgnoreCase(String name);

    List<Cryptocurrency> findBySymbolContainingIgnoreCase(String symbol);

    List<Cryptocurrency> findByMarketCapRankIsNotNullOrderByMarketCapRankAsc(Pageable pageable);

    @Query("SELECT c FROM Cryptocurrency c WHERE c.priceChangePercentage24h BETWEEN :minChange AND :maxChange ORDER BY c.priceChangePercentage24h DESC")
    List<Cryptocurrency> findByPriceChangePercentageRange(@Param("minChange") BigDecimal minChange,
                                                          @Param("maxChange") BigDecimal maxChange);

    List<Cryptocurrency> findByMarketCapGreaterThanOrderByMarketCapDesc(BigDecimal marketCap);

    List<Cryptocurrency> findByLastUpdatedAfter(LocalDateTime dateTime);

    @Query("SELECT c FROM Cryptocurrency c WHERE c.priceChangePercentage24h > 0 ORDER BY c.priceChangePercentage24h DESC")
    List<Cryptocurrency> findTopGainers(Pageable pageable);

    @Query("SELECT c FROM Cryptocurrency c WHERE c.priceChangePercentage24h < 0 ORDER BY c.priceChangePercentage24h ASC")
    List<Cryptocurrency> findTopLosers(Pageable pageable);

    @Query("SELECT c FROM Cryptocurrency c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(c.symbol) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Cryptocurrency> searchByNameOrSymbol(@Param("searchTerm") String searchTerm);

    @Query("SELECT " +
            "CAST(COUNT(c) as long), " +
            "COALESCE(SUM(c.marketCap), 0), " +
            "COALESCE(AVG(c.priceChangePercentage24h), 0), " +
            "COALESCE(MAX(c.priceChangePercentage24h), 0), " +
            "COALESCE(MIN(c.priceChangePercentage24h), 0) " +
            "FROM Cryptocurrency c")
    Object[] getMarketStatistics();

    Page<Cryptocurrency> findAll(Pageable pageable);
}
