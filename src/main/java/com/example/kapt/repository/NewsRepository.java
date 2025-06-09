package com.example.kapt.repository;

import com.example.kapt.model.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    Optional<News> findByArticleId(String articleId);

    Page<News> findByDuplicateFalseOrderByPubDateDesc(Pageable pageable);

    List<News> findBySourceNameIgnoreCaseAndDuplicateFalseOrderByPubDateDesc(String sourceName);

    Page<News> findByLanguageAndDuplicateFalseOrderByPubDateDesc(String language, Pageable pageable);

    Page<News> findBySentimentAndDuplicateFalseOrderByPubDateDesc(String sentiment, Pageable pageable);

    List<News> findByPubDateAfterAndDuplicateFalseOrderByPubDateDesc(LocalDateTime dateTime);

    List<News> findByPubDateBetweenAndDuplicateFalseOrderByPubDateDesc(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT n FROM News n WHERE LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) AND n.duplicate = false ORDER BY n.pubDate DESC")
    List<News> findByTitleContainingKeyword(@Param("keyword") String keyword);

    @Query("SELECT n FROM News n WHERE LOWER(n.description) LIKE LOWER(CONCAT('%', :keyword, '%')) AND n.duplicate = false ORDER BY n.pubDate DESC")
    List<News> findByDescriptionContainingKeyword(@Param("keyword") String keyword);

    @Query("SELECT n FROM News n WHERE " +
            "(LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "n.duplicate = false ORDER BY n.pubDate DESC")
    List<News> searchByTitleOrDescription(@Param("keyword") String keyword);    @Query("SELECT n FROM News n JOIN n.coinMentioned cm WHERE cm = :coin AND n.duplicate = false ORDER BY n.pubDate DESC")
    List<News> findByCoinMentioned(@Param("coin") String coin);

    @Query("SELECT n FROM News n JOIN n.category cat WHERE cat = :category AND n.duplicate = false ORDER BY n.pubDate DESC")
    List<News> findByCategory(@Param("category") String category);

    @Query("SELECT n FROM News n JOIN n.aiTag tag WHERE tag = :tag AND n.duplicate = false ORDER BY n.pubDate DESC")
    List<News> findByAiTag(@Param("tag") String tag);

    @Query("SELECT n FROM News n WHERE n.pubDate >= :since AND n.duplicate = false ORDER BY n.pubDate DESC")
    List<News> findRecentNews(@Param("since") LocalDateTime since);

    @Query("SELECT n.sourceName, COUNT(n) FROM News n WHERE n.duplicate = false GROUP BY n.sourceName ORDER BY COUNT(n) DESC")
    List<Object[]> getNewsCountBySource();

    @Query("SELECT n.sentiment, COUNT(n) FROM News n WHERE n.duplicate = false AND n.sentiment IS NOT NULL GROUP BY n.sentiment")
    List<Object[]> getNewsCountBySentiment();    @Query("SELECT 'BTC', COUNT(n) FROM News n WHERE n.duplicate = false AND n.coinMentioned IS NOT NULL")
    List<Object[]> getNewsCountByCoin();    @Query("SELECT 'trending', COUNT(n) FROM News n WHERE n.duplicate = false AND n.keywords IS NOT NULL AND n.pubDate >= :since")
    List<Object[]> getTrendingKeywords(@Param("since") LocalDateTime since);

    boolean existsByArticleId(String articleId);

    long countByDuplicateFalse();

    List<News> findByDuplicateTrue();

    @Query("SELECT n FROM News n WHERE n.sourcePriority IS NOT NULL AND n.duplicate = false ORDER BY n.sourcePriority ASC, n.pubDate DESC")
    List<News> findBySourcePriorityOrderByPriorityAndDate(Pageable pageable);    @Query("SELECT n FROM News n WHERE " +
            "(:keyword IS NULL OR LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(n.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:sourceName IS NULL OR n.sourceName = :sourceName) AND " +
            "(:language IS NULL OR n.language = :language) AND " +
            "(:sentiment IS NULL OR n.sentiment = :sentiment) AND " +
            "(:coin IS NULL OR :coin MEMBER OF n.coinMentioned) AND " +
            "(:category IS NULL OR :category MEMBER OF n.category) AND " +
            "(:fromDate IS NULL OR n.pubDate >= :fromDate) AND " +
            "(:toDate IS NULL OR n.pubDate <= :toDate) AND " +
            "n.duplicate = false " +
            "ORDER BY n.pubDate DESC")
    Page<News> findByMultipleCriteria(
            @Param("keyword") String keyword,
            @Param("sourceName") String sourceName,
            @Param("language") String language,
            @Param("sentiment") String sentiment,
            @Param("coin") String coin,
            @Param("category") String category,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );
}
