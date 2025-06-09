package com.example.kapt.controller;

import com.example.kapt.model.News;
import com.example.kapt.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/news")
@Tag(name = "Cryptocurrency News", description = "Cryptocurrency news aggregation and management API")
public class NewsController {

    private static final Logger logger = LoggerFactory.getLogger(NewsController.class);

    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }    @GetMapping
    @Operation(summary = "Get all news", description = "Retrieve all cryptocurrency news articles with pagination and sorting")
    public ResponseEntity<Page<News>> getAllNews(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "pubDate") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)")
            @RequestParam(defaultValue = "desc") String sortDir) {

        // Validate pagination parameters
        if (page < 0) {
            return ResponseEntity.badRequest().build();
        }
        if (size <= 0 || size > 100) {
            return ResponseEntity.badRequest().build();
        }

        logger.info("Getting all news: page={}, size={}, sortBy={}, sortDir={}",
                page, size, sortBy, sortDir);

        Page<News> news = newsService.getAllNews(page, size, sortBy, sortDir);
        return ResponseEntity.ok(news);
    }

    @GetMapping("/{articleId}")
    @Operation(summary = "Get news by article ID", description = "Retrieve a specific news article by its unique ID")
    public ResponseEntity<News> getNewsByArticleId(
            @Parameter(description = "Unique article identifier")
            @PathVariable String articleId) {

        logger.info("Getting news by article ID: {}", articleId);

        Optional<News> news = newsService.findByArticleId(articleId);
        return news.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }    @GetMapping("/search")
    @Operation(summary = "Search news", description = "Search news articles by keyword in title or description")
    public ResponseEntity<List<News>> searchNews(
            @Parameter(description = "Search keyword")
            @RequestParam String q) {

        // Validate search keyword
        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        logger.info("Searching news with keyword: {}", q);

        List<News> results = newsService.searchNews(q.trim());
        return ResponseEntity.ok(results);
    }

    @GetMapping("/coin/{coin}")
    @Operation(summary = "Get news by cryptocurrency", description = "Get news articles mentioning specific cryptocurrency")
    public ResponseEntity<List<News>> getNewsByCoin(
            @Parameter(description = "Cryptocurrency symbol (e.g., btc, eth)")
            @PathVariable String coin) {

        logger.info("Getting news for cryptocurrency: {}", coin);

        List<News> results = newsService.getNewsByCoin(coin);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/source/{sourceName}")
    @Operation(summary = "Get news by source", description = "Get news articles from specific source")
    public ResponseEntity<List<News>> getNewsBySource(
            @Parameter(description = "News source name")
            @PathVariable String sourceName) {

        logger.info("Getting news from source: {}", sourceName);

        List<News> results = newsService.getNewsBySource(sourceName);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/sentiment/{sentiment}")
    @Operation(summary = "Get news by sentiment", description = "Get news articles with specific sentiment")
    public ResponseEntity<Page<News>> getNewsBySentiment(
            @Parameter(description = "Sentiment (positive, negative, neutral)")
            @PathVariable String sentiment,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {

        logger.info("Getting news with sentiment: {}", sentiment);

        Page<News> results = newsService.getNewsBySentiment(sentiment, page, size);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/language/{language}")
    @Operation(summary = "Get news by language", description = "Get news articles in specific language")
    public ResponseEntity<Page<News>> getNewsByLanguage(
            @Parameter(description = "Language code (e.g., en, fr, de)")
            @PathVariable String language,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {

        logger.info("Getting news in language: {}", language);

        Page<News> results = newsService.getNewsByLanguage(language, page, size);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get news by category", description = "Get news articles in specific category")
    public ResponseEntity<List<News>> getNewsByCategory(
            @Parameter(description = "News category")
            @PathVariable String category) {

        logger.info("Getting news in category: {}", category);

        List<News> results = newsService.getNewsByCategory(category);
        return ResponseEntity.ok(results);
    }    @GetMapping("/recent")
    @Operation(summary = "Get recent news", description = "Get news articles published in the last N hours")
    public ResponseEntity<List<News>> getRecentNews(
            @Parameter(description = "Number of hours back to search")
            @RequestParam(defaultValue = "24") int hours) {

        if (hours <= 0) {
            return ResponseEntity.badRequest().build();
        }

        logger.info("Getting recent news from last {} hours", hours);

        List<News> results = newsService.getRecentNews(hours);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/between")
    @Operation(summary = "Get news between dates", description = "Get news articles published between two dates")
    public ResponseEntity<List<News>> getNewsBetweenDates(
            @Parameter(description = "Start date (ISO format: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        logger.info("Getting news between {} and {}", startDate, endDate);

        List<News> results = newsService.getNewsBetweenDates(startDate, endDate);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/advanced-search")
    @Operation(summary = "Advanced news search", description = "Search news with multiple criteria")
    public ResponseEntity<Page<News>> advancedSearch(
            @Parameter(description = "Search keyword")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Source name")
            @RequestParam(required = false) String sourceName,
            @Parameter(description = "Language code")
            @RequestParam(required = false) String language,
            @Parameter(description = "Sentiment")
            @RequestParam(required = false) String sentiment,
            @Parameter(description = "Cryptocurrency coin")
            @RequestParam(required = false) String coin,
            @Parameter(description = "Category")
            @RequestParam(required = false) String category,
            @Parameter(description = "From date")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @Parameter(description = "To date")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {

        logger.info("Advanced search - keyword: {}, source: {}, language: {}, sentiment: {}, coin: {}, category: {}",
                keyword, sourceName, language, sentiment, coin, category);

        Page<News> results = newsService.searchWithCriteria(
                keyword, sourceName, language, sentiment, coin, category,
                fromDate, toDate, page, size
        );
        return ResponseEntity.ok(results);
    }

    @GetMapping("/trending/coins")
    @Operation(summary = "Get trending cryptocurrencies", description = "Get most mentioned cryptocurrencies in recent news")
    public ResponseEntity<List<String>> getTrendingCryptocurrencies(
            @Parameter(description = "Number of hours back to analyze")
            @RequestParam(defaultValue = "24") int hours) {

        logger.info("Getting trending cryptocurrencies from last {} hours", hours);

        List<String> trending = newsService.getTrendingCryptocurrencies(hours);
        return ResponseEntity.ok(trending);
    }

    @GetMapping("/sources/top")
    @Operation(summary = "Get top news sources", description = "Get news sources with most articles")
    public ResponseEntity<List<String>> getTopNewsSources(
            @Parameter(description = "Number of top sources to return")
            @RequestParam(defaultValue = "10") int limit) {

        logger.info("Getting top {} news sources", limit);

        List<String> topSources = newsService.getTopNewsSources(limit);
        return ResponseEntity.ok(topSources);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get news statistics", description = "Get comprehensive news statistics")
    public ResponseEntity<NewsService.NewsStatistics> getNewsStatistics() {
        logger.info("Getting news statistics");

        NewsService.NewsStatistics stats = newsService.getNewsStatistics();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/update")
    @Operation(summary = "Update news", description = "Manually trigger news update from external API")
    public ResponseEntity<String> updateNews(
            @Parameter(description = "Batch size for fetching news")
            @RequestParam(defaultValue = "50") int batchSize) {

        logger.info("Manual news update triggered with batch size: {}", batchSize);

        try {
            newsService.fetchAndSaveLatestNews(batchSize);
            return ResponseEntity.ok("News update started successfully");
        } catch (Exception e) {
            logger.error("Error triggering news update", e);
            return ResponseEntity.internalServerError()
                    .body("Error triggering news update: " + e.getMessage());
        }
    }

    @PostMapping("/update/coin/{coin}")
    @Operation(summary = "Update news for coin", description = "Fetch latest news for specific cryptocurrency")
    public ResponseEntity<String> updateNewsForCoin(
            @Parameter(description = "Cryptocurrency symbol")
            @PathVariable String coin,
            @Parameter(description = "Batch size for fetching news")
            @RequestParam(defaultValue = "30") int batchSize) {

        logger.info("Manual news update triggered for coin: {} with batch size: {}", coin, batchSize);

        try {
            newsService.fetchAndSaveNewsByCoin(new String[]{coin}, batchSize);
            return ResponseEntity.ok("News update for " + coin + " started successfully");
        } catch (Exception e) {
            logger.error("Error triggering news update for coin: {}", coin, e);
            return ResponseEntity.internalServerError()
                    .body("Error triggering news update for " + coin + ": " + e.getMessage());
        }
    }

    @PostMapping("/search-and-save")
    @Operation(summary = "Search and save news", description = "Search for news by keyword and save to database")
    public ResponseEntity<String> searchAndSaveNews(
            @Parameter(description = "Search keyword")
            @RequestParam String keyword,
            @Parameter(description = "Batch size for fetching news")
            @RequestParam(defaultValue = "30") int batchSize) {

        logger.info("Search and save news triggered for keyword: {} with batch size: {}", keyword, batchSize);

        try {
            newsService.searchAndSaveNews(keyword, batchSize);
            return ResponseEntity.ok("Search and save for keyword '" + keyword + "' started successfully");
        } catch (Exception e) {
            logger.error("Error in search and save for keyword: {}", keyword, e);
            return ResponseEntity.internalServerError()
                    .body("Error in search and save for keyword '" + keyword + "': " + e.getMessage());
        }
    }

    @GetMapping("/count")
    @Operation(summary = "Get news count", description = "Get total number of news articles")
    public ResponseEntity<Long> getNewsCount() {
        logger.info("Getting total news count");

        long count = newsService.getNewsCount();
        return ResponseEntity.ok(count);
    }

    @DeleteMapping("/cleanup")
    @Operation(summary = "Cleanup old news", description = "Delete news articles older than specified days")
    public ResponseEntity<String> cleanupOldNews(
            @Parameter(description = "Number of days - news older than this will be deleted")
            @RequestParam(defaultValue = "90") int daysOld) {

        logger.info("Cleanup old news triggered for articles older than {} days", daysOld);

        try {
            int deletedCount = newsService.deleteOldNews(daysOld);
            return ResponseEntity.ok("Successfully deleted " + deletedCount + " old news articles");
        } catch (Exception e) {
            logger.error("Error during news cleanup", e);
            return ResponseEntity.internalServerError()
                    .body("Error during news cleanup: " + e.getMessage());
        }
    }
}
