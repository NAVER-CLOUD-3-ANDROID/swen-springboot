package com.swen.news.domain.news.entity;

import com.swen.news.global.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 뉴스 임베딩 벡터 저장 엔티티
 */
@Entity
@Table(name = "news_embeddings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NewsEmbedding extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 500)
    private String newsUrl;

    @Column(nullable = false, length = 1000)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 100)
    private String publisher;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String embeddingVector; // JSON 형태로 저장

    @Column(nullable = false)
    private Integer vectorDimension;

    @Builder
    public NewsEmbedding(String newsUrl, String title, String description, 
                        String publisher, String embeddingVector, Integer vectorDimension) {
        this.newsUrl = newsUrl;
        this.title = title;
        this.description = description;
        this.publisher = publisher;
        this.embeddingVector = embeddingVector;
        this.vectorDimension = vectorDimension;
    }
}
