package com.example.kapt.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AlphaVantageTickerSentimentDto {

    @JsonProperty("ticker")
    private String ticker;

    @JsonProperty("relevance_score")
    private String relevanceScore;

    @JsonProperty("ticker_sentiment_score")
    private String tickerSentimentScore;

    @JsonProperty("ticker_sentiment_label")
    private String tickerSentimentLabel;

    public AlphaVantageTickerSentimentDto() {
    }

    public AlphaVantageTickerSentimentDto(String ticker, String relevanceScore, String tickerSentimentScore, String tickerSentimentLabel) {
        this.ticker = ticker;
        this.relevanceScore = relevanceScore;
        this.tickerSentimentScore = tickerSentimentScore;
        this.tickerSentimentLabel = tickerSentimentLabel;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getRelevanceScore() {
        return relevanceScore;
    }

    public void setRelevanceScore(String relevanceScore) {
        this.relevanceScore = relevanceScore;
    }

    public String getTickerSentimentScore() {
        return tickerSentimentScore;
    }

    public void setTickerSentimentScore(String tickerSentimentScore) {
        this.tickerSentimentScore = tickerSentimentScore;
    }

    public String getTickerSentimentLabel() {
        return tickerSentimentLabel;
    }

    public void setTickerSentimentLabel(String tickerSentimentLabel) {
        this.tickerSentimentLabel = tickerSentimentLabel;
    }

    @Override
    public String toString() {
        return "AlphaVantageTickerSentimentDto{" +
                "ticker='" + ticker + '\'' +
                ", relevanceScore='" + relevanceScore + '\'' +
                ", tickerSentimentScore='" + tickerSentimentScore + '\'' +
                ", tickerSentimentLabel='" + tickerSentimentLabel + '\'' +
                '}';
    }
}
