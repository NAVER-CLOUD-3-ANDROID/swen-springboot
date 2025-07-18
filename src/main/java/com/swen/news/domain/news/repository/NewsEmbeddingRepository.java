package com.swen.news.domain.news.repository;

import com.swen.news.domain.news.entity.NewsEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 뉴스 임베딩 리포지토리
 */
@Repository
public interface NewsEmbeddingRepository extends JpaRepository<NewsEmbedding, Long> {

    /**
     * URL로 뉴스 임베딩 조회
     */
    Optional<NewsEmbedding> findByNewsUrl(String newsUrl);

    /**
     * 최근 생성된 뉴스 임베딩 조회 (벡터 검색용)
     */
    @Query("SELECT ne FROM NewsEmbedding ne WHERE ne.createdAt >= :since ORDER BY ne.createdAt DESC")
    List<NewsEmbedding> findRecentEmbeddings(@Param("since") LocalDateTime since);

    /**
     * 특정 발행사의 뉴스 임베딩 조회
     */
    List<NewsEmbedding> findByPublisherOrderByCreatedAtDesc(String publisher);

    /**
     * URL이 아닌 다른 뉴스들 조회 (추천 시 현재 뉴스 제외용)
     */
    @Query("SELECT ne FROM NewsEmbedding ne WHERE ne.newsUrl != :excludeUrl ORDER BY ne.createdAt DESC")
    List<NewsEmbedding> findAllExcludingUrl(@Param("excludeUrl") String excludeUrl);

    /**
     * 제목이나 설명에 특정 키워드가 포함된 뉴스 조회
     */
    @Query("SELECT ne FROM NewsEmbedding ne WHERE " +
           "ne.title LIKE %:keyword% OR ne.description LIKE %:keyword% " +
           "ORDER BY ne.createdAt DESC")
    List<NewsEmbedding> findByKeyword(@Param("keyword") String keyword);
}
