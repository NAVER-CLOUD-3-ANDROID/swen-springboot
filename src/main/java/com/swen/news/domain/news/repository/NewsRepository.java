package com.swen.news.domain.news.repository;

import org.springframework.stereotype.Repository;

/**
 * 뉴스 데이터 액세스를 위한 레포지토리입니다.
 * 필요에 따라 JPA Repository로 확장 가능합니다.
 */
@Repository
public interface NewsRepository {
    // 향후 뉴스 데이터 저장이 필요한 경우 여기에 메서드 추가
}
