package com.example.kapt.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "news", indexes = {@Index(name = "idx_news_article_id", columnList = "articleId"), @Index(name = "idx_news_pub_date", columnList = "pubDate"), @Index(name = "idx_news_source_name", columnList = "sourceName"), @Index(name = "idx_news_language", columnList = "language"), @Index(name = "idx_news_sentiment", columnList = "sentiment"), @Index(name = "idx_news_duplicate", columnList = "duplicate"), @Index(name = "idx_news_created_at", columnList = "createdAt")})
public class NewsH2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "article_id", unique = true, nullable = false)
    @NotBlank(message = "Article ID cannot be blank")
    @Size(max = 255, message = "Article ID cannot exceed 255 characters")
    private String articleId;

    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Title cannot be blank")
    private String title;

    @Column(name = "link", nullable = false, length = 2048)
    @NotBlank(message = "Link cannot be blank")
    @Size(max = 2048, message = "Link cannot exceed 2048 characters")
    private String link;


    @Column(name = "keywords", columnDefinition = "TEXT")
    private String keywords;

    @Column(name = "creator", columnDefinition = "TEXT")
    private String creator;

    @Column(name = "video_url", length = 2048)
    @Size(max = 2048, message = "Video URL cannot exceed 2048 characters")
    private String videoUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "pub_date")
    private LocalDateTime pubDate;

    @Column(name = "source_icon", length = 2048)
    @Size(max = 2048, message = "Source icon URL cannot exceed 2048 characters")
    private String sourceIcon;

    @Column(name = "source_name")
    @Size(max = 255, message = "Source name cannot exceed 255 characters")
    private String sourceName;

    @Column(name = "source_url", length = 2048)
    @Size(max = 2048, message = "Source URL cannot exceed 2048 characters")
    private String sourceUrl;

    @Column(name = "source_priority")
    private Integer sourcePriority;


    @Column(name = "country", columnDefinition = "TEXT")
    private String country;

    @Column(name = "category", columnDefinition = "TEXT")
    private String category;

    @Column(name = "language", length = 10)
    @Size(max = 10, message = "Language code cannot exceed 10 characters")
    private String language;

    @Column(name = "coin_mentioned", columnDefinition = "TEXT")
    private String coinMentioned;

    @Column(name = "sentiment", length = 20)
    @Size(max = 20, message = "Sentiment cannot exceed 20 characters")
    private String sentiment;

    @Column(name = "ai_tag", columnDefinition = "TEXT")
    private String aiTag;

    @Column(name = "duplicate", nullable = false)
    @NotNull
    private Boolean duplicate = false;

    @Column(name = "created_at", nullable = false)
    @NotNull
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @NotNull
    private LocalDateTime updatedAt;


    public NewsH2() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.duplicate = false;
    }

    public NewsH2(String articleId, String title, String link) {
        this();
        this.articleId = articleId;
        this.title = title;
        this.link = link;
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

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getPubDate() {
        return pubDate;
    }

    public void setPubDate(LocalDateTime pubDate) {
        this.pubDate = pubDate;
    }

    public String getSourceIcon() {
        return sourceIcon;
    }

    public void setSourceIcon(String sourceIcon) {
        this.sourceIcon = sourceIcon;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public Integer getSourcePriority() {
        return sourcePriority;
    }

    public void setSourcePriority(Integer sourcePriority) {
        this.sourcePriority = sourcePriority;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCoinMentioned() {
        return coinMentioned;
    }

    public void setCoinMentioned(String coinMentioned) {
        this.coinMentioned = coinMentioned;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public String getAiTag() {
        return aiTag;
    }

    public void setAiTag(String aiTag) {
        this.aiTag = aiTag;
    }

    public Boolean getDuplicate() {
        return duplicate;
    }

    public void setDuplicate(Boolean duplicate) {
        this.duplicate = duplicate;
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


    public boolean hasCoinMention(String coin) {
        if (coinMentioned == null || coin == null) {
            return false;
        }
        return coinMentioned.toLowerCase().contains(coin.toLowerCase());
    }

    public boolean hasCategory(String categoryName) {
        if (category == null || categoryName == null) {
            return false;
        }
        return category.contains(categoryName);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewsH2 news = (NewsH2) o;
        return Objects.equals(articleId, news.articleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(articleId);
    }

    @Override
    public String toString() {
        return "NewsH2{" + "id=" + id + ", articleId='" + articleId + '\'' + ", title='" + title + '\'' + ", sourceName='" + sourceName + '\'' + ", pubDate=" + pubDate + ", duplicate=" + duplicate + '}';
    }
}
