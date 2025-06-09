package com.example.kapt.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;


@Entity
@Table(name = "cryptocurrencies", indexes = {
        @Index(name = "idx_symbol", columnList = "symbol"),
        @Index(name = "idx_name", columnList = "name"),
        @Index(name = "idx_last_updated", columnList = "lastUpdated")
})
public class Cryptocurrency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Symbol cannot be blank")
    private String symbol;

    @Column(nullable = false)
    @NotBlank(message = "Name cannot be blank")
    private String name;

    @Column(name = "current_price", precision = 20, scale = 8)
    @PositiveOrZero(message = "Current price must be positive or zero")
    private BigDecimal currentPrice;

    @Column(name = "market_cap", precision = 25, scale = 2)
    @PositiveOrZero(message = "Market cap must be positive or zero")
    private BigDecimal marketCap;

    @Column(name = "total_volume", precision = 25, scale = 2)
    @PositiveOrZero(message = "Total volume must be positive or zero")
    private BigDecimal totalVolume;

    @Column(name = "price_change_24h", precision = 10, scale = 2)
    private BigDecimal priceChange24h;

    @Column(name = "price_change_percentage_24h", precision = 5, scale = 2)
    private BigDecimal priceChangePercentage24h;

    @Column(name = "market_cap_rank")
    @PositiveOrZero(message = "Market cap rank must be positive or zero")
    private Integer marketCapRank;

    @Column(name = "circulating_supply", precision = 25, scale = 2)
    @PositiveOrZero(message = "Circulating supply must be positive or zero")
    private BigDecimal circulatingSupply;

    @Column(name = "total_supply", precision = 25, scale = 2)
    @PositiveOrZero(message = "Total supply must be positive or zero")
    private BigDecimal totalSupply;

    @Column(name = "max_supply", precision = 25, scale = 2)
    @PositiveOrZero(message = "Max supply must be positive or zero")
    private BigDecimal maxSupply;

    @Column(name = "last_updated", nullable = false)
    @NotNull(message = "Last updated time cannot be null")
    private LocalDateTime lastUpdated;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Cryptocurrency() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Cryptocurrency(String symbol, String name) {
        this();
        this.symbol = symbol;
        this.name = name;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(BigDecimal marketCap) {
        this.marketCap = marketCap;
    }

    public BigDecimal getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(BigDecimal totalVolume) {
        this.totalVolume = totalVolume;
    }

    public BigDecimal getPriceChange24h() {
        return priceChange24h;
    }

    public void setPriceChange24h(BigDecimal priceChange24h) {
        this.priceChange24h = priceChange24h;
    }

    public BigDecimal getPriceChangePercentage24h() {
        return priceChangePercentage24h;
    }

    public void setPriceChangePercentage24h(BigDecimal priceChangePercentage24h) {
        this.priceChangePercentage24h = priceChangePercentage24h;
    }

    public Integer getMarketCapRank() {
        return marketCapRank;
    }

    public void setMarketCapRank(Integer marketCapRank) {
        this.marketCapRank = marketCapRank;
    }

    public BigDecimal getCirculatingSupply() {
        return circulatingSupply;
    }

    public void setCirculatingSupply(BigDecimal circulatingSupply) {
        this.circulatingSupply = circulatingSupply;
    }

    public BigDecimal getTotalSupply() {
        return totalSupply;
    }

    public void setTotalSupply(BigDecimal totalSupply) {
        this.totalSupply = totalSupply;
    }

    public BigDecimal getMaxSupply() {
        return maxSupply;
    }

    public void setMaxSupply(BigDecimal maxSupply) {
        this.maxSupply = maxSupply;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cryptocurrency that = (Cryptocurrency) o;
        return Objects.equals(symbol, that.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol);
    }

    @Override
    public String toString() {
        return "Cryptocurrency{" +
                "id=" + id +
                ", symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", currentPrice=" + currentPrice +
                ", marketCap=" + marketCap +
                ", marketCapRank=" + marketCapRank +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
