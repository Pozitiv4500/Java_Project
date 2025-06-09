package com.example.kapt.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AlphaVantageTopicDto {

    @JsonProperty("topic")
    private String topic;

    @JsonProperty("relevance_score")
    private String relevanceScore;

    public AlphaVantageTopicDto() {
    }

    public AlphaVantageTopicDto(String topic, String relevanceScore) {
        this.topic = topic;
        this.relevanceScore = relevanceScore;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getRelevanceScore() {
        return relevanceScore;
    }

    public void setRelevanceScore(String relevanceScore) {
        this.relevanceScore = relevanceScore;
    }

    @Override
    public String toString() {
        return "AlphaVantageTopicDto{" +
                "topic='" + topic + '\'' +
                ", relevanceScore='" + relevanceScore + '\'' +
                '}';
    }
}
