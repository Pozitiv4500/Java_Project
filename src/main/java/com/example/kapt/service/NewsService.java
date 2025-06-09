package com.example.kapt.service;

import com.example.kapt.dto.AlphaVantageNewsArticleDto;
import com.example.kapt.model.News;
import com.example.kapt.repository.NewsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class NewsService {

    private static final Logger logger = LoggerFactory.getLogger(NewsService.class);

    private final NewsRepository newsRepository;
    private final AlphaVantageNewsService alphaVantageNewsService;

    public NewsService(NewsRepository newsRepository, AlphaVantageNewsService alphaVantageNewsService) {
        this.newsRepository = newsRepository;
        this.alphaVantageNewsService = alphaVantageNewsService;
    }


    public void fetchAndSaveLatestNews() {
        fetchAndSaveLatestNews(10);
    }


    public void fetchAndSaveLatestNews(int batchSize) {
        logger.info("Starting news fetch and save process with batch size: {}", batchSize);

        try {

            int adjustedBatchSize = Math.min(batchSize, 50);
            List<AlphaVantageNewsArticleDto> newsDtos = alphaVantageNewsService.fetchLatestCryptoNews(adjustedBatchSize);
            int savedCount = 0;
            int duplicateCount = 0;

            for (AlphaVantageNewsArticleDto dto : newsDtos) {
                try {
                    if (saveOrUpdateNews(dto)) {
                        savedCount++;
                    } else {
                        duplicateCount++;
                    }
                } catch (Exception e) {
                    logger.error("Error saving news article: {}", dto.getUrl(), e);
                }
            }

            logger.info("News fetch completed - saved: {}, duplicates: {}", savedCount, duplicateCount);

        } catch (Exception e) {
            logger.error("Error during news fetch and save process", e);
        }
    }


    public void fetchAndSaveNewsByCoin(String[] coins, int batchSize) {
        logger.info("Fetching news for coins: {} with batch size: {}", String.join(",", coins), batchSize);

        try {

            String tickers = String.join(",", java.util.Arrays.stream(coins).map(coin -> "CRYPTO:" + coin.toUpperCase()).toArray(String[]::new));
            List<AlphaVantageNewsArticleDto> newsDtos = alphaVantageNewsService.fetchCryptoNewsByTickers(tickers, batchSize);
            int savedCount = 0;

            for (AlphaVantageNewsArticleDto dto : newsDtos) {
                try {
                    if (saveOrUpdateNews(dto)) {
                        savedCount++;
                    }
                } catch (Exception e) {
                    logger.error("Error saving news article: {}", dto.getUrl(), e);
                }
            }

            logger.info("Saved {} news articles for coins: {}", savedCount, String.join(",", coins));

        } catch (Exception e) {
            logger.error("Error fetching news for coins", e);
        }
    }


    public void searchAndSaveNews(String keyword, int batchSize) {
        logger.info("Searching and saving news for keyword: {} with batch size: {}", keyword, batchSize);

        try {

            String topics = mapKeywordToTopics(keyword);
            List<AlphaVantageNewsArticleDto> newsDtos = alphaVantageNewsService.fetchCryptoNewsByTopics(topics, batchSize);
            int savedCount = 0;

            for (AlphaVantageNewsArticleDto dto : newsDtos) {
                try {
                    if (saveOrUpdateNews(dto)) {
                        savedCount++;
                    }
                } catch (Exception e) {
                    logger.error("Error saving news article: {}", dto.getUrl(), e);
                }
            }

            logger.info("Saved {} news articles for keyword: {}", savedCount, keyword);

        } catch (Exception e) {
            logger.error("Error searching and saving news for keyword: {}", keyword, e);
        }
    }


    private String mapKeywordToTopics(String keyword) {
        String lowerKeyword = keyword.toLowerCase();

        if (lowerKeyword.contains("bitcoin") || lowerKeyword.contains("btc") || lowerKeyword.contains("ethereum") || lowerKeyword.contains("eth") || lowerKeyword.contains("crypto")) {
            return "blockchain,technology";
        } else if (lowerKeyword.contains("finance") || lowerKeyword.contains("market")) {
            return "financial_markets,finance";
        } else if (lowerKeyword.contains("economy")) {
            return "economy_macro,economy_monetary";
        } else {
            return "blockchain,technology,financial_markets";
        }
    }

    private boolean saveOrUpdateNews(AlphaVantageNewsArticleDto dto) {
        News newsEntity = alphaVantageNewsService.convertToEntity(dto);
        if (newsEntity == null) {
            return false;
        }


        Optional<News> existing = newsRepository.findByArticleId(newsEntity.getArticleId());

        if (existing.isPresent()) {

            News existingNews = existing.get();
            updateNewsData(existingNews, newsEntity);
            newsRepository.save(existingNews);
            logger.debug("Updated existing news article: {}", newsEntity.getArticleId());
            return false;
        } else {

            newsRepository.save(newsEntity);
            logger.debug("Saved new news article: {}", newsEntity.getArticleId());
            return true;
        }
    }

    private void updateNewsData(News existing, News newData) {
        existing.setTitle(newData.getTitle());
        existing.setLink(newData.getLink());
        existing.setKeywords(newData.getKeywords());
        existing.setCreator(newData.getCreator());
        existing.setVideoUrl(newData.getVideoUrl());
        existing.setDescription(newData.getDescription());
        existing.setContent(newData.getContent());
        existing.setPubDate(newData.getPubDate());
        existing.setSourceIcon(newData.getSourceIcon());
        existing.setSourceName(newData.getSourceName());
        existing.setSourceUrl(newData.getSourceUrl());
        existing.setSourcePriority(newData.getSourcePriority());
        existing.setCountry(newData.getCountry());
        existing.setCategory(newData.getCategory());
        existing.setLanguage(newData.getLanguage());
        existing.setCoinMentioned(newData.getCoinMentioned());
        existing.setSentiment(newData.getSentiment());
        existing.setAiTag(newData.getAiTag());
        existing.setDuplicate(newData.getDuplicate());
    }

    @Transactional(readOnly = true)
    public Page<News> getAllNews(int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return newsRepository.findByDuplicateFalseOrderByPubDateDesc(pageable);
    }


    @Transactional(readOnly = true)
    public Optional<News> findByArticleId(String articleId) {
        return newsRepository.findByArticleId(articleId);
    }


    @Transactional(readOnly = true)
    public List<News> searchNews(String keyword) {
        return newsRepository.searchByTitleOrDescription(keyword);
    }


    @Transactional(readOnly = true)
    public List<News> getNewsByCoin(String coin) {
        return newsRepository.findByCoinMentioned(coin.toLowerCase());
    }


    @Transactional(readOnly = true)
    public List<News> getNewsBySource(String sourceName) {
        return newsRepository.findBySourceNameIgnoreCaseAndDuplicateFalseOrderByPubDateDesc(sourceName);
    }


    @Transactional(readOnly = true)
    public Page<News> getNewsBySentiment(String sentiment, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return newsRepository.findBySentimentAndDuplicateFalseOrderByPubDateDesc(sentiment, pageable);
    }


    @Transactional(readOnly = true)
    public Page<News> getNewsByLanguage(String language, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return newsRepository.findByLanguageAndDuplicateFalseOrderByPubDateDesc(language, pageable);
    }


    @Transactional(readOnly = true)
    public List<News> getNewsByCategory(String category) {
        return newsRepository.findByCategory(category);
    }


    @Transactional(readOnly = true)
    public List<News> getRecentNews(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return newsRepository.findRecentNews(since);
    }


    @Transactional(readOnly = true)
    public List<News> getNewsBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return newsRepository.findByPubDateBetweenAndDuplicateFalseOrderByPubDateDesc(startDate, endDate);
    }


    @Transactional(readOnly = true)
    public Page<News> searchWithCriteria(String keyword, String sourceName, String language, String sentiment, String coin, String category, LocalDateTime fromDate, LocalDateTime toDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return newsRepository.findByMultipleCriteria(keyword, sourceName, language, sentiment, coin, category, fromDate, toDate, pageable);
    }


    @Transactional(readOnly = true)
    public NewsStatistics getNewsStatistics() {
        long totalCount = newsRepository.countByDuplicateFalse();
        List<Object[]> sourceStats = newsRepository.getNewsCountBySource();
        List<Object[]> sentimentStats = newsRepository.getNewsCountBySentiment();
        List<Object[]> coinStats = newsRepository.getNewsCountByCoin();

        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        List<Object[]> trendingKeywords = newsRepository.getTrendingKeywords(last24Hours);

        return new NewsStatistics(totalCount, sourceStats, sentimentStats, coinStats, trendingKeywords);
    }


    @Transactional(readOnly = true)
    public List<String> getTrendingCryptocurrencies(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        List<News> recentNews = newsRepository.findRecentNews(since);

        return recentNews.stream().filter(news -> news.getCoinMentioned() != null).flatMap(news -> List.of(news.getCoinMentioned()).stream()).collect(Collectors.groupingBy(coin -> coin, Collectors.counting())).entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed()).limit(10).map(Map.Entry::getKey).collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<String> getTopNewsSources(int limit) {
        List<Object[]> sourceStats = newsRepository.getNewsCountBySource();
        return sourceStats.stream().limit(limit).map(stat -> (String) stat[0]).collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public long getNewsCount() {
        return newsRepository.countByDuplicateFalse();
    }


    @Transactional(readOnly = true)
    public boolean articleExists(String articleId) {
        return newsRepository.existsByArticleId(articleId);
    }


    public int deleteOldNews(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        List<News> oldNews = newsRepository.findByPubDateBetweenAndDuplicateFalseOrderByPubDateDesc(LocalDateTime.of(2020, 1, 1, 0, 0), cutoffDate);

        int deletedCount = oldNews.size();
        newsRepository.deleteAll(oldNews);

        logger.info("Deleted {} old news articles older than {} days", deletedCount, daysOld);
        return deletedCount;
    }


    public List<News> getLatestNews(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "pubDate"));
        Page<News> newsPage = newsRepository.findByDuplicateFalseOrderByPubDateDesc(pageable);
        return newsPage.getContent();
    }


    public List<News> searchNews(String keyword, int limit) {
        List<News> allResults = newsRepository.searchByTitleOrDescription(keyword);
        return allResults.stream().limit(limit).collect(Collectors.toList());
    }


    public Page<News> getAllNews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "pubDate"));
        return newsRepository.findByDuplicateFalseOrderByPubDateDesc(pageable);
    }


    public static class NewsStatistics {
        private final long totalCount;
        private final List<Object[]> sourceStats;
        private final List<Object[]> sentimentStats;
        private final List<Object[]> coinStats;
        private final List<Object[]> trendingKeywords;

        public NewsStatistics(long totalCount, List<Object[]> sourceStats, List<Object[]> sentimentStats, List<Object[]> coinStats, List<Object[]> trendingKeywords) {
            this.totalCount = totalCount;
            this.sourceStats = sourceStats;
            this.sentimentStats = sentimentStats;
            this.coinStats = coinStats;
            this.trendingKeywords = trendingKeywords;
        }

        public long getTotalCount() {
            return totalCount;
        }

        public List<Object[]> getSourceStats() {
            return sourceStats;
        }

        public List<Object[]> getSentimentStats() {
            return sentimentStats;
        }

        public List<Object[]> getCoinStats() {
            return coinStats;
        }

        public List<Object[]> getTrendingKeywords() {
            return trendingKeywords;
        }
    }
}