package com.example.kapt.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AlphaVantageNewsResponseDto {

    @JsonProperty("items")
    private String items;

    @JsonProperty("sentiment_score_definition")
    private String sentimentScoreDefinition;

    @JsonProperty("relevance_score_definition")
    private String relevanceScoreDefinition;

    @JsonProperty("feed")
    private List<AlphaVantageNewsArticleDto> feed;

    public AlphaVantageNewsResponseDto() {
    }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }

    public String getSentimentScoreDefinition() {
        return sentimentScoreDefinition;
    }

    public void setSentimentScoreDefinition(String sentimentScoreDefinition) {
        this.sentimentScoreDefinition = sentimentScoreDefinition;
    }

    public String getRelevanceScoreDefinition() {
        return relevanceScoreDefinition;
    }

    public void setRelevanceScoreDefinition(String relevanceScoreDefinition) {
        this.relevanceScoreDefinition = relevanceScoreDefinition;
    }

    public List<AlphaVantageNewsArticleDto> getFeed() {
        return feed;
    }

    public void setFeed(List<AlphaVantageNewsArticleDto> feed) {
        this.feed = feed;
    }
}
