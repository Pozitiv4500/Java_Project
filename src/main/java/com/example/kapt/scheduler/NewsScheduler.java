package com.example.kapt.scheduler;

import com.example.kapt.model.News;
import com.example.kapt.service.NewsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.news.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class NewsScheduler {

    private static final Logger logger = LoggerFactory.getLogger(NewsScheduler.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final NewsService newsService;

    @Value("${app.news.scheduler.timezone:UTC}")
    private String timezone;

    public NewsScheduler(NewsService newsService) {
        this.newsService = newsService;
        logger.info("NewsScheduler initialized with timezone: {}", timezone);
    }

    @Scheduled(initialDelayString = "${app.news.scheduler.initial-delay:60000}", fixedDelayString = "${app.news.scheduler.fixed-delay:3600000}")
    public void updateNews() {
        LocalDateTime startTime = LocalDateTime.now();
        logger.info("Starting scheduled news update at {}", startTime.format(FORMATTER));
        try {

            newsService.fetchAndSaveLatestNews(10);

            List<News> recentArticles = newsService.getRecentNews(1);

            LocalDateTime endTime = LocalDateTime.now();
            long duration = java.time.Duration.between(startTime, endTime).toMillis();

            logger.info("Scheduled news update completed successfully. " + "Recent articles found: {}, Duration: {}ms, Time: {}", recentArticles.size(), duration, endTime.format(FORMATTER));

            if (!recentArticles.isEmpty()) {
                logNewArticlesSummary(recentArticles);
            } else {
                logger.info("No new articles found during this update cycle");
            }

        } catch (Exception e) {
            LocalDateTime errorTime = LocalDateTime.now();
            long duration = java.time.Duration.between(startTime, errorTime).toMillis();

            logger.error("Scheduled news update failed after {}ms at {}: {}", duration, errorTime.format(FORMATTER), e.getMessage(), e);


        }
    }

    public int triggerManualUpdate() {
        logger.info("Manual news update triggered at {}", LocalDateTime.now().format(FORMATTER));

        try {
            newsService.fetchAndSaveLatestNews(50);
            List<News> recentArticles = newsService.getRecentNews(1);
            logger.info("Manual news update completed. Recent articles found: {}", recentArticles.size());
            return recentArticles.size();
        } catch (Exception e) {
            logger.error("Manual news update failed: {}", e.getMessage(), e);
            throw new RuntimeException("Manual news update failed", e);
        }
    }

    public boolean isHealthy() {
        try {

            newsService.getNewsStatistics();
            return true;
        } catch (Exception e) {
            logger.warn("News scheduler health check failed: {}", e.getMessage());
            return false;
        }
    }

    public java.util.Map<String, Object> getStatus() {
        java.util.Map<String, Object> status = new java.util.HashMap<>();
        status.put("enabled", true);
        status.put("timezone", timezone);
        status.put("lastCheck", LocalDateTime.now().format(FORMATTER));
        status.put("healthy", isHealthy());

        try {
            var stats = newsService.getNewsStatistics();
            status.put("totalNews", stats.getTotalCount());
            status.put("sourceStats", stats.getSourceStats().size());
        } catch (Exception e) {
            logger.debug("Could not retrieve news statistics for status: {}", e.getMessage());
            status.put("totalNews", "unavailable");
            status.put("sourceStats", "unavailable");
        }

        return status;
    }

    private void logNewArticlesSummary(List<News> articles) {
        logger.info("=== Articles Summary ===");


        java.util.Map<String, Long> categoryCount = new java.util.HashMap<>();
        java.util.Map<String, Long> sourceCount = new java.util.HashMap<>();
        java.util.Set<String> coinsSet = new java.util.HashSet<>();

        for (News article : articles) {

            if (article.getCategory() != null) {
                for (String category : article.getCategory()) {
                    categoryCount.merge(category, 1L, Long::sum);
                }
            }


            if (article.getSourceName() != null) {
                sourceCount.merge(article.getSourceName(), 1L, Long::sum);
            }


            if (article.getCoinMentioned() != null) {
                coinsSet.addAll(java.util.Arrays.asList(article.getCoinMentioned()));
            }
        }

        logger.info("Categories: {}", categoryCount);
        logger.info("Sources: {}", sourceCount);
        logger.info("Coins mentioned: {}", coinsSet);


        int titlesToLog = Math.min(5, articles.size());
        logger.info("Sample titles:");
        for (int i = 0; i < titlesToLog; i++) {
            logger.info("  - {}", articles.get(i).getTitle());
        }

        if (articles.size() > titlesToLog) {
            logger.info("  ... and {} more articles", articles.size() - titlesToLog);
        }

        logger.info("========================");
    }

    private String generateUpdateSummary(List<News> articles) {
        if (articles.isEmpty()) {
            return "No articles processed";
        }

        java.util.Set<String> sources = new java.util.HashSet<>();
        java.util.Set<String> coins = new java.util.HashSet<>();

        for (News article : articles) {
            if (article.getSourceName() != null) {
                sources.add(article.getSourceName());
            }
            if (article.getCoinMentioned() != null) {
                Collections.addAll(coins, article.getCoinMentioned());
            }
        }

        return String.format("%d articles from %d sources mentioning %d coins", articles.size(), sources.size(), coins.size());
    }
}
