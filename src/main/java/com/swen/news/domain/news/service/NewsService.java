package com.swen.news.domain.news.service;

import com.swen.news.domain.news.dto.NewsItem;
import com.swen.news.domain.news.dto.NewsSearchRequest;
import com.swen.news.domain.news.dto.NewsScriptResponse;
import com.swen.news.domain.news.dto.PlayNewsRequest;

import java.util.List;

/**
 * 뉴스 서비스 인터페이스입니다.
 */
public interface NewsService {
    
    /**
     * 뉴스 플레이 - 전체 파이프라인 실행
     * 1. 네이버 뉴스 검색 (주제가 없으면 랜덤 최신 뉴스)
     * 2. HyperCLOVA로 스크립트 생성
     * 3. Clova Dubbing으로 TTS 변환
     *
     * @param request 플레이 요청 (주제는 선택사항)
     * @return 뉴스 스크립트 응답
     */
    NewsScriptResponse playNews(PlayNewsRequest request);
    
    /**
     * 네이버 뉴스 검색만 실행
     *
     * @param request 검색 요청
     * @return 검색 결과 JSON
     */
    String searchNews(NewsSearchRequest request);
    
    /**
     * 뉴스를 바탕으로 스크립트 생성
     *
     * @param newsItems 뉴스 아이템 리스트
     * @param scriptLength 스크립트 길이
     * @return 생성된 스크립트
     */
    String generateScript(List<NewsItem> newsItems, String scriptLength);
    
    /**
     * 스크립트를 음성으로 변환
     *
     * @param script 변환할 스크립트
     * @return 오디오 파일 URL
     */
    String generateSpeech(String script);
}
