package com.example.kapt.controller;

import com.example.kapt.service.NewsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class NewsTestController {

    private final NewsService newsService;

    public NewsTestController(NewsService newsService) {
        this.newsService = newsService;
    }

    @PostMapping("/news/fetch")
    public ResponseEntity<Map<String, Object>> testNewsFetch() {
        try {

            newsService.fetchAndSaveLatestNews(5);

            return ResponseEntity.ok(Map.of("status", "success", "message", "News fetch triggered successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @GetMapping("/news/count")
    public ResponseEntity<Map<String, Object>> getNewsCount() {
        try {

            var recentNews = newsService.getRecentNews(24);

            return ResponseEntity.ok(Map.of("status", "success", "recentNewsCount", recentNews.size(), "message", "News count retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
}
