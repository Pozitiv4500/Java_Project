package com.example.kapt.controller;

import com.example.kapt.model.News;
import com.example.kapt.service.NewsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NewsController.class)
@DisplayName("NewsController Tests")
class NewsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private NewsService newsService;

    private News testNews;
    private List<News> testNewsList;

    @BeforeEach
    void setUp() {
        testNews = createTestNews();
        testNewsList = List.of(testNews);
    }

    @Test
    @DisplayName("Should get all news with default pagination")
    void shouldGetAllNewsWithDefaultPagination() throws Exception {

        Page<News> newsPage = new PageImpl<>(testNewsList, PageRequest.of(0, 20), 1);
        when(newsService.getAllNews(anyInt(), anyInt(), anyString(), anyString())).thenReturn(newsPage);
        mockMvc.perform(get("/api/v1/news")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.content", hasSize(1))).andExpect(jsonPath("$.content[0].id", is(1))).andExpect(jsonPath("$.content[0].title", is("Test Bitcoin News"))).andExpect(jsonPath("$.totalElements", is(1))).andExpect(jsonPath("$.size", is(20)));

        verify(newsService).getAllNews(anyInt(), anyInt(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should get all news with custom pagination")
    void shouldGetAllNewsWithCustomPagination() throws Exception {

        Page<News> newsPage = new PageImpl<>(testNewsList, PageRequest.of(1, 10), 1);
        when(newsService.getAllNews(anyInt(), anyInt(), anyString(), anyString())).thenReturn(newsPage);
        mockMvc.perform(get("/api/v1/news").param("page", "1").param("size", "10")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.content", hasSize(1))).andExpect(jsonPath("$.size", is(10))).andExpect(jsonPath("$.number", is(1)));

        verify(newsService).getAllNews(anyInt(), anyInt(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should get news by ID successfully")
    void shouldGetNewsByIdSuccessfully() throws Exception {

        String articleId = "test-article-1";
        when(newsService.findByArticleId(articleId)).thenReturn(Optional.of(testNews));


        mockMvc.perform(get("/api/v1/news/{id}", articleId)).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.id", is(1))).andExpect(jsonPath("$.title", is("Test Bitcoin News"))).andExpect(jsonPath("$.sourceName", is("TestSource")));

        verify(newsService).findByArticleId(articleId);
    }

    @Test
    @DisplayName("Should return 404 when news not found by ID")
    void shouldReturn404WhenNewsNotFoundById() throws Exception {

        String articleId = "non-existent-article";
        when(newsService.findByArticleId(articleId)).thenReturn(Optional.empty());


        mockMvc.perform(get("/api/v1/news/{id}", articleId)).andExpect(status().isNotFound());

        verify(newsService).findByArticleId(articleId);
    }

    @Test
    @DisplayName("Should search news by keyword")
    void shouldSearchNewsByKeyword() throws Exception {

        String keyword = "bitcoin";
        when(newsService.searchNews(keyword)).thenReturn(testNewsList);
        mockMvc.perform(get("/api/v1/news/search").param("q", keyword)).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].title", is("Test Bitcoin News")));

        verify(newsService).searchNews(keyword);
    }

    @Test
    @DisplayName("Should return bad request for empty search keyword")
    void shouldReturnBadRequestForEmptySearchKeyword() throws Exception {
        mockMvc.perform(get("/api/v1/news/search").param("q", "")).andExpect(status().isBadRequest());

        verify(newsService, never()).searchNews(anyString());
    }

    @Test
    @DisplayName("Should get news by coin")
    void shouldGetNewsByCoin() throws Exception {

        String coin = "BTC";
        when(newsService.getNewsByCoin(coin)).thenReturn(testNewsList);


        mockMvc.perform(get("/api/v1/news/coin/{coin}", coin)).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].coinMentioned", hasItem("BTC")));

        verify(newsService).getNewsByCoin(coin);
    }

    @Test
    @DisplayName("Should get news by source")
    void shouldGetNewsBySource() throws Exception {

        String sourceName = "TestSource";
        when(newsService.getNewsBySource(sourceName)).thenReturn(testNewsList);


        mockMvc.perform(get("/api/v1/news/source/{sourceName}", sourceName)).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].sourceName", is("TestSource")));

        verify(newsService).getNewsBySource(sourceName);
    }

    @Test
    @DisplayName("Should get recent news")
    void shouldGetRecentNews() throws Exception {

        int hours = 24;
        when(newsService.getRecentNews(hours)).thenReturn(testNewsList);


        mockMvc.perform(get("/api/v1/news/recent").param("hours", String.valueOf(hours))).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$", hasSize(1)));

        verify(newsService).getRecentNews(hours);
    }

    @Test
    @DisplayName("Should get recent news with default hours")
    void shouldGetRecentNewsWithDefaultHours() throws Exception {

        when(newsService.getRecentNews(24)).thenReturn(testNewsList);


        mockMvc.perform(get("/api/v1/news/recent")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$", hasSize(1)));

        verify(newsService).getRecentNews(24);
    }

    @Test
    @DisplayName("Should get news statistics")
    void shouldGetNewsStatistics() throws Exception {
        List<Object[]> sourceStats = Collections.singletonList(new Object[]{"Source1", 50L});
        List<Object[]> sentimentStats = Collections.singletonList(new Object[]{"positive", 30L});
        List<Object[]> coinStats = Collections.singletonList(new Object[]{"BTC", 25L});
        List<Object[]> trendingKeywords = Collections.singletonList(new Object[]{"keyword", 10L});

        NewsService.NewsStatistics stats = new NewsService.NewsStatistics(100L, sourceStats, sentimentStats, coinStats, trendingKeywords);
        when(newsService.getNewsStatistics()).thenReturn(stats);


        mockMvc.perform(get("/api/v1/news/statistics")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.totalCount", is(100)));

        verify(newsService).getNewsStatistics();
    }

    @Test
    @DisplayName("Should trigger manual news update")
    void shouldTriggerManualNewsUpdate() throws Exception {
        doNothing().when(newsService).fetchAndSaveLatestNews(anyInt());

        mockMvc.perform(post("/api/v1/news/update")).andExpect(status().isOk()).andExpect(content().contentType("text/plain;charset=UTF-8")).andExpect(content().string(containsString("started successfully")));

        verify(newsService).fetchAndSaveLatestNews(anyInt());
    }

    @Test
    @DisplayName("Should handle service exception during manual update")
    void shouldHandleServiceExceptionDuringManualUpdate() throws Exception {
        doThrow(new RuntimeException("Service error")).when(newsService).fetchAndSaveLatestNews(anyInt());

        mockMvc.perform(post("/api/v1/news/update")).andExpect(status().isInternalServerError());

        verify(newsService).fetchAndSaveLatestNews(anyInt());
    }

    @Test
    @DisplayName("Should validate pagination parameters")
    void shouldValidatePaginationParameters() throws Exception {

        mockMvc.perform(get("/api/v1/news").param("page", "-1")).andExpect(status().isBadRequest());


        mockMvc.perform(get("/api/v1/news").param("size", "0")).andExpect(status().isBadRequest());


        mockMvc.perform(get("/api/v1/news").param("size", "1000")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle invalid hours parameter for recent news")
    void shouldHandleInvalidHoursParameterForRecentNews() throws Exception {

        mockMvc.perform(get("/api/v1/news/recent").param("hours", "-1")).andExpect(status().isBadRequest());


        mockMvc.perform(get("/api/v1/news/recent").param("hours", "0")).andExpect(status().isBadRequest());
    }

    private News createTestNews() {
        News news = new News();
        news.setId(1L);
        news.setArticleId("test-article-123");
        news.setTitle("Test Bitcoin News");
        news.setLink("https://example.com/test-news");
        news.setDescription("Test description about Bitcoin");
        news.setSourceName("TestSource");
        news.setLanguage("en");
        news.setPubDate(LocalDateTime.now().minusHours(1));
        news.setCoinMentioned(new String[]{"BTC", "Bitcoin"});
        news.setCategory(new String[]{"cryptocurrency"});
        news.setSentiment("positive");
        news.setDuplicate(false);
        news.setCreatedAt(LocalDateTime.now());
        news.setUpdatedAt(LocalDateTime.now());
        return news;
    }
}
