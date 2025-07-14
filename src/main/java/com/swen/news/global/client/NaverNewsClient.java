package com.swen.news.global.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 네이버 뉴스 검색 API 클라이언트입니다.
 */
@FeignClient(
    name = "naver-news-client",
    url = "${external-api.naver.news.base-url}"
)
public interface NaverNewsClient {
    
    /**
     * 뉴스 검색 API 호출
     *
     * @param clientId 네이버 API 클라이언트 ID
     * @param clientSecret 네이버 API 클라이언트 시크릿
     * @param query 검색어
     * @param display 검색 결과 개수
     * @param start 검색 시작 위치
     * @param sort 정렬 방식
     * @return 검색 결과
     */
    @GetMapping("/v1/search/news.json")
    String searchNews(
        @RequestHeader("X-Naver-Client-Id") String clientId,
        @RequestHeader("X-Naver-Client-Secret") String clientSecret,
        @RequestParam("query") String query,
        @RequestParam("display") Integer display,
        @RequestParam("start") Integer start,
        @RequestParam("sort") String sort
    );
}
