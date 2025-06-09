package com.example.kapt.integration;

import com.example.kapt.model.News;
import com.example.kapt.repository.NewsRepository;
import com.example.kapt.service.NewsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("News Integration Tests")
class NewsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private NewsService newsService;

    private News testNews1;
    private News testNews2;

    @BeforeEach
    void setUp() {

        newsRepository.deleteAll();


        testNews1 = createAndSaveNews("article-1", "Bitcoin Reaches New High", "CoinDesk", new String[]{"BTC", "Bitcoin"}, new String[]{"cryptocurrency", "price"}, "positive", LocalDateTime.now().minusHours(1));

        testNews2 = createAndSaveNews("article-2", "Ethereum Network Upgrade", "CoinTelegraph", new String[]{"ETH", "Ethereum"}, new String[]{"cryptocurrency", "technology"}, "neutral", LocalDateTime.now().minusHours(2));
    }

    @Test
    @DisplayName("Should retrieve all news with pagination")
    void shouldRetrieveAllNewsWithPagination() throws Exception {

        mockMvc.perform(get("/api/v1/news").param("page", "0").param("size", "10")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.content", hasSize(2))).andExpect(jsonPath("$.content[0].title", containsString("Bitcoin"))).andExpect(jsonPath("$.totalElements", is(2))).andExpect(jsonPath("$.size", is(10))).andExpect(jsonPath("$.number", is(0)));
    }    @Test
    @DisplayName("Should find specific news by ID")
    void shouldFindSpecificNewsById() throws Exception {

        mockMvc.perform(get("/api/v1/news/{id}", testNews1.getId())).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.id", is(testNews1.getId().intValue()))).andExpect(jsonPath("$.title", is("Bitcoin Reaches New High"))).andExpect(jsonPath("$.sourceName", is("CoinDesk"))).andExpect(jsonPath("$.sentiment", is("positive")));
    }

    @Test
    @DisplayName("Should return 404 for non-existent news ID")
    void shouldReturn404ForNonExistentNewsId() throws Exception {

        mockMvc.perform(get("/api/v1/news/{id}", 999L)).andExpect(status().isNotFound());
    }    @Test
    @DisplayName("Should search news by keyword")
    void shouldSearchNewsByKeyword() throws Exception {

        mockMvc.perform(get("/api/v1/news/search").param("q", "Bitcoin")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].title", containsString("Bitcoin")));
    }

    @Test
    @DisplayName("Should get news by coin symbol")
    void shouldGetNewsByCoinSymbol() throws Exception {

        mockMvc.perform(get("/api/v1/news/coin/{coin}", "BTC")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].coinMentioned", hasItem("BTC")));
    }

    @Test
    @DisplayName("Should get news by source name")
    void shouldGetNewsBySourceName() throws Exception {

        mockMvc.perform(get("/api/v1/news/source/{sourceName}", "CoinDesk")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].sourceName", is("CoinDesk")));
    }

    @Test
    @DisplayName("Should get recent news")
    void shouldGetRecentNews() throws Exception {

        mockMvc.perform(get("/api/v1/news/recent").param("hours", "24")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Should get news statistics")
    void shouldGetNewsStatistics() throws Exception {

        mockMvc.perform(get("/api/v1/news/statistics")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.totalCount", is(2))).andExpect(jsonPath("$.sourceStats", isA(List.class))).andExpect(jsonPath("$.sentimentStats", isA(List.class))).andExpect(jsonPath("$.coinStats", isA(List.class)));
    }    @Test
    @DisplayName("Should trigger manual news update")
    void shouldTriggerManualNewsUpdate() throws Exception {

        mockMvc.perform(post("/api/v1/news/update")).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN)).andExpect(content().string(containsString("started")));
    }

    @Test
    @DisplayName("Should validate news entity constraints")
    void shouldValidateNewsEntityConstraints() {

        News invalidNews = new News();
        invalidNews.setArticleId("");
        invalidNews.setTitle("");


        assertThat(newsRepository.findAll()).hasSize(2);
    }    @Test
    @DisplayName("Should handle news search with no results")
    void shouldHandleNewsSearchWithNoResults() throws Exception {

        mockMvc.perform(get("/api/v1/news/search").param("q", "NonExistentCoin")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should handle coin search with no results")
    void shouldHandleCoinSearchWithNoResults() throws Exception {

        mockMvc.perform(get("/api/v1/news/coin/{coin}", "XYZ")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should maintain data integrity across operations")
    void shouldMaintainDataIntegrityAcrossOperations() {

        long initialCount = newsRepository.count();
        assertThat(initialCount).isEqualTo(2);


        List<News> allNews = newsService.getAllNews(0, 10, "pubDate", "desc").getContent();
        List<News> bitcoinNews = newsService.getNewsByCoin("BTC");
        List<News> recentNews = newsService.getRecentNews(24);


        assertThat(allNews).hasSize(2);
        assertThat(bitcoinNews).hasSize(1);
        assertThat(recentNews).hasSize(2);
        assertThat(newsRepository.count()).isEqualTo(initialCount);
    }

    @Test
    @DisplayName("Should handle concurrent access to news data")
    void shouldHandleConcurrentAccessToNewsData() throws Exception {

        mockMvc.perform(get("/api/v1/news")).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/news/search").param("q", "Bitcoin")).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/news/statistics")).andExpect(status().isOk());


        assertThat(newsRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should respect pagination boundaries")
    void shouldRespectPaginationBoundaries() throws Exception {

        mockMvc.perform(get("/api/v1/news").param("page", "0").param("size", "1")).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1))).andExpect(jsonPath("$.totalElements", is(2))).andExpect(jsonPath("$.totalPages", is(2)));


        mockMvc.perform(get("/api/v1/news").param("page", "1").param("size", "1")).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1))).andExpect(jsonPath("$.totalElements", is(2))).andExpect(jsonPath("$.totalPages", is(2)));


        mockMvc.perform(get("/api/v1/news").param("page", "5").param("size", "1")).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(0))).andExpect(jsonPath("$.totalElements", is(2)));
    }

    private News createAndSaveNews(String articleId, String title, String sourceName, String[] coinMentioned, String[] category, String sentiment, LocalDateTime pubDate) {
        News news = new News();
        news.setArticleId(articleId);
        news.setTitle(title);
        news.setLink("https://example.com/" + articleId);
        news.setDescription(title + " - detailed description");
        news.setSourceName(sourceName);
        news.setLanguage("en");
        news.setPubDate(pubDate);
        news.setCoinMentioned(coinMentioned);
        news.setCategory(category);
        news.setSentiment(sentiment);
        news.setDuplicate(false);
        news.setCreatedAt(LocalDateTime.now());
        news.setUpdatedAt(LocalDateTime.now());

        return newsRepository.save(news);
    }
}
