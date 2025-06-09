package com.example.kapt.repository;

import com.example.kapt.model.News;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("News Repository Tests")
@Transactional
class NewsRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NewsRepository newsRepository;
    private News testNews1;
    private News testNews2;
    private News duplicateNews;


    private final LocalDateTime baseTime = LocalDateTime.of(2024, 1, 15, 12, 0, 0);

    @BeforeEach
    void setUp() {

        newsRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();


        testNews1 = createNews("article-1", "Bitcoin Price Surges to New Heights", "CoinDesk", "positive", "en", baseTime.minusHours(2), false);

        testNews2 = createNews("article-2", "Ethereum Network Upgrades Successfully", "CoinTelegraph", "neutral", "en", baseTime.minusHours(1), false);

        duplicateNews = createNews("article-3", "Duplicate News Article", "TestSource", "positive", "en", baseTime.minusMinutes(30), true);


        testNews1 = entityManager.persistAndFlush(testNews1);
        testNews2 = entityManager.persistAndFlush(testNews2);
        duplicateNews = entityManager.persistAndFlush(duplicateNews);

        entityManager.clear();
    }

    @Test
    @DisplayName("Should find news by article ID")
    void shouldFindNewsByArticleId() {

        Optional<News> found = newsRepository.findByArticleId("article-1");


        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Bitcoin Price Surges to New Heights");
        assertThat(found.get().getSourceName()).isEqualTo("CoinDesk");
    }

    @Test
    @DisplayName("Should return empty when article ID not found")
    void shouldReturnEmptyWhenArticleIdNotFound() {

        Optional<News> found = newsRepository.findByArticleId("non-existent-id");


        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find non-duplicate news with pagination")
    void shouldFindNonDuplicateNewsWithPagination() {

        Pageable pageable = PageRequest.of(0, 10);


        Page<News> newsPage = newsRepository.findByDuplicateFalseOrderByPubDateDesc(pageable);


        assertThat(newsPage.getContent()).hasSize(2);
        assertThat(newsPage.getTotalElements()).isEqualTo(2);
        assertThat(newsPage.getContent().get(0).getTitle()).isEqualTo("Ethereum Network Upgrades Successfully");
        assertThat(newsPage.getContent().get(1).getTitle()).isEqualTo("Bitcoin Price Surges to New Heights");

        assertThat(newsPage.getContent()).noneMatch(news -> news.getDuplicate());
    }

    @Test
    @DisplayName("Should find news by source name ignoring case")
    void shouldFindNewsBySourceNameIgnoringCase() {

        List<News> coinDeskNews = newsRepository.findBySourceNameIgnoreCaseAndDuplicateFalseOrderByPubDateDesc("coindesk");
        List<News> coinTelegraphNews = newsRepository.findBySourceNameIgnoreCaseAndDuplicateFalseOrderByPubDateDesc("COINTELEGRAPH");


        assertThat(coinDeskNews).hasSize(1);
        assertThat(coinDeskNews.get(0).getSourceName()).isEqualTo("CoinDesk");

        assertThat(coinTelegraphNews).hasSize(1);
        assertThat(coinTelegraphNews.get(0).getSourceName()).isEqualTo("CoinTelegraph");
    }

    @Test
    @DisplayName("Should find news by language")
    void shouldFindNewsByLanguage() {

        Pageable pageable = PageRequest.of(0, 10);


        Page<News> englishNews = newsRepository.findByLanguageAndDuplicateFalseOrderByPubDateDesc("en", pageable);


        assertThat(englishNews.getContent()).allMatch(news -> "en".equals(news.getLanguage())).noneMatch(news -> news.getDuplicate());
    }

    @Test
    @DisplayName("Should find news by sentiment")
    void shouldFindNewsBySentiment() {

        Pageable pageable = PageRequest.of(0, 10);


        Page<News> positiveNews = newsRepository.findBySentimentAndDuplicateFalseOrderByPubDateDesc("positive", pageable);
        Page<News> neutralNews = newsRepository.findBySentimentAndDuplicateFalseOrderByPubDateDesc("neutral", pageable);


        assertThat(positiveNews.getContent()).hasSize(1);
        assertThat(positiveNews.getContent().get(0).getSentiment()).isEqualTo("positive");

        assertThat(neutralNews.getContent()).hasSize(1);
        assertThat(neutralNews.getContent().get(0).getSentiment()).isEqualTo("neutral");
    }

    @Test
    @DisplayName("Should find recent news after specific date")
    void shouldFindRecentNewsAfterSpecificDate() {

        LocalDateTime oneHourAgo = baseTime.minusHours(1);


        List<News> recentNews = newsRepository.findByPubDateAfterAndDuplicateFalseOrderByPubDateDesc(oneHourAgo);


        assertThat(recentNews).hasSize(0);


        LocalDateTime slightlyBefore = baseTime.minusHours(1).minusMinutes(5);
        List<News> recentNewsInclusive = newsRepository.findByPubDateAfterAndDuplicateFalseOrderByPubDateDesc(slightlyBefore);
        assertThat(recentNewsInclusive).hasSize(1);
        assertThat(recentNewsInclusive.get(0).getTitle()).isEqualTo("Ethereum Network Upgrades Successfully");
        assertThat(recentNewsInclusive.get(0).getPubDate()).isAfter(slightlyBefore);
    }

    @Test
    @DisplayName("Should find news between dates")
    void shouldFindNewsBetweenDates() {

        LocalDateTime threeHoursAgo = baseTime.minusHours(3);
        LocalDateTime oneHourAgo = baseTime.minusHours(1);


        List<News> newsBetween = newsRepository.findByPubDateBetweenAndDuplicateFalseOrderByPubDateDesc(threeHoursAgo, oneHourAgo);


        assertThat(newsBetween).hasSize(2);
        assertThat(newsBetween.get(0).getTitle()).isEqualTo("Ethereum Network Upgrades Successfully");
        assertThat(newsBetween.get(1).getTitle()).isEqualTo("Bitcoin Price Surges to New Heights");
    }

    @Test
    @DisplayName("Should search news by title containing keyword")
    void shouldSearchNewsByTitleContainingKeyword() {

        List<News> bitcoinNews = newsRepository.findByTitleContainingKeyword("bitcoin");
        List<News> ethereumNews = newsRepository.findByTitleContainingKeyword("ethereum");


        assertThat(bitcoinNews).hasSize(1);
        assertThat(bitcoinNews.get(0).getTitle()).contains("Bitcoin");

        assertThat(ethereumNews).hasSize(1);
        assertThat(ethereumNews.get(0).getTitle()).contains("Ethereum");
    }

    @Test
    @DisplayName("Should search news by description containing keyword")
    void shouldSearchNewsByDescriptionContainingKeyword() {

        List<News> priceNews = newsRepository.findByDescriptionContainingKeyword("price");


        assertThat(priceNews).hasSize(1);
        assertThat(priceNews.get(0).getDescription()).containsIgnoringCase("price");
    }

    @Test
    @DisplayName("Should search news by title or description")
    void shouldSearchNewsByTitleOrDescription() {

        List<News> cryptoNews = newsRepository.searchByTitleOrDescription("cryptocurrency");


        assertThat(cryptoNews).hasSize(2);
    }

    @Test
    @DisplayName("Should count all non-duplicate news")
    void shouldCountAllNonDuplicateNews() {

        long count = newsRepository.count();


        assertThat(count).isEqualTo(3);


        long nonDuplicateCount = newsRepository.findByDuplicateFalseOrderByPubDateDesc(PageRequest.of(0, Integer.MAX_VALUE)).getTotalElements();
        assertThat(nonDuplicateCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Should find recent news within time range")
    void shouldFindRecentNewsWithinTimeRange() {

        LocalDateTime twoHoursAgo = baseTime.minusHours(2);


        List<News> recentNews = newsRepository.findRecentNews(twoHoursAgo);


        assertThat(recentNews).hasSize(2);
        assertThat(recentNews).allMatch(news -> news.getPubDate().isAfter(twoHoursAgo) || news.getPubDate().isEqual(twoHoursAgo));
        assertThat(recentNews).noneMatch(news -> news.getDuplicate());
    }

    private News createNews(String articleId, String title, String sourceName, String sentiment, String language, LocalDateTime pubDate, boolean duplicate) {
        News news = new News();
        news.setArticleId(articleId);
        news.setTitle(title);
        news.setLink("https://example.com/" + articleId);
        news.setDescription("Test description about " + title.toLowerCase() + " and cryptocurrency market trends");
        news.setSourceName(sourceName);
        news.setLanguage(language);
        news.setPubDate(pubDate);
        news.setSentiment(sentiment);
        news.setDuplicate(duplicate);
        news.setCreatedAt(LocalDateTime.now());
        news.setUpdatedAt(LocalDateTime.now());


        news.setCoinMentioned(title.toLowerCase().contains("bitcoin") ? new String[]{"BTC", "Bitcoin"} : new String[]{"ETH", "Ethereum"});
        news.setCategory(new String[]{"cryptocurrency", "news"});

        return news;
    }
}
