package com.swen.news.domain.news.controller;

import com.swen.news.domain.news.dto.NewsSearchRequest;
import com.swen.news.domain.news.dto.NewsScriptResponse;
import com.swen.news.domain.news.dto.PlayNewsRequest;
import com.swen.news.domain.news.dto.NewsItem;
import com.swen.news.domain.news.service.NewsService;
import com.swen.news.domain.news.service.NewsRecommendationService;
import com.swen.news.domain.news.batch.NewsEmbeddingBatchService;
import com.swen.news.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 뉴스 관련 API를 처리하는 컨트롤러입니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
@Tag(name = "뉴스 API", description = "뉴스 검색, 스크립트 생성, TTS 변환 기능을 제공합니다.")
public class NewsController {
    
    private final NewsService newsService;
    private final NewsRecommendationService newsRecommendationService;
    private final NewsEmbeddingBatchService newsEmbeddingBatchService;
    
    /**
     * 간편한 랜덤 뉴스 플레이 (GET 방식)
     * 플레이 버튼 클릭 시 바로 사용할 수 있는 엔드포인트
     */
    @GetMapping("/play")
    @Operation(
        summary = "랜덤 뉴스 플레이 (간편 버전)",
        description = "별도 설정 없이 랜덤 최신 뉴스를 1분 내로 다정한 목소리로 재생합니다. 앱의 플레이 버튼용입니다."
    )
    public ResponseEntity<CommonResponse<NewsScriptResponse>> playRandomNews(
        @Parameter(description = "스크립트 길이") @RequestParam(defaultValue = "SHORT") String scriptLength
    ) {
        log.info("랜덤 뉴스 플레이 요청 - 길이: {}", scriptLength);
        
        PlayNewsRequest request = PlayNewsRequest.builder()
            .topic(null)  // 랜덤 뉴스
            .scriptLength(scriptLength)
            .build();
        
        NewsScriptResponse response = newsService.playNews(request);
        return ResponseEntity.ok(CommonResponse.onSuccess(HttpStatus.OK.value(), response));
    }
    
    /**
     * 뉴스 플레이 - 전체 파이프라인 실행
     * 네이버 뉴스 검색 → HyperCLOVA 스크립트 생성 → Clova Dubbing TTS 변환
     * 주제를 입력하지 않으면 랜덤 최신 뉴스를 제공합니다.
     */
    @PostMapping("/play")
    @Operation(
        summary = "뉴스 플레이",
        description = "주제를 입력하면 해당 뉴스를, 입력하지 않으면 랜덤 최신 뉴스를 다정한 목소리로 재생합니다. 기본 1분 내 스크립트입니다."
    )
    public ResponseEntity<CommonResponse<NewsScriptResponse>> playNews(
        @Parameter(description = "뉴스 플레이 요청 정보 (주제는 선택사항)")
        @RequestBody(required = false) PlayNewsRequest request
    ) {
        // request가 null이면 기본값으로 설정
        if (request == null) {
            request = PlayNewsRequest.builder().build();
        }
        
        log.info("뉴스 플레이 요청 - 주제: {}, 스크립트 길이: {}", 
                 request.getTopic() != null ? request.getTopic() : "랜덤", 
                 request.getScriptLength());
        
        NewsScriptResponse response = newsService.playNews(request);
        return ResponseEntity.ok(CommonResponse.onSuccess(HttpStatus.OK.value(), response));
    }
    
    /**
     * 뉴스 검색 (단건 조회)
     */
    @GetMapping("/search")
    @Operation(
        summary = "뉴스 검색 (단건)",
        description = "GET 방식으로 뉴스 1건을 검색합니다. query가 없으면 랜덤 최신 뉴스를 검색합니다."
    )
    public ResponseEntity<CommonResponse<String>> searchNews(
        @Parameter(description = "검색어 (선택사항)") @RequestParam(required = false) String query
    ) {
        log.info("뉴스 검색 요청 - 검색어: {}", query != null ? query : "랜덤");
        
        NewsSearchRequest request = NewsSearchRequest.builder()
            .query(query)
            .display(1)  // 항상 1건만
            .start(1)
            .sort("date")
            .build();
        
        String result = newsService.searchNews(request);
        return ResponseEntity.ok(CommonResponse.onSuccess(HttpStatus.OK.value(), result));
    }
    
    /**
     * 스크립트 생성만 실행
     */
    @PostMapping("/script")
    @Operation(
        summary = "스크립트 생성",
        description = "뉴스 아이템들을 바탕으로 CLOVA Studio를 사용해 다정한 톤의 스크립트를 생성합니다."
    )
    public ResponseEntity<CommonResponse<String>> generateScript(
        @Parameter(description = "뉴스 제목") @RequestParam String title,
        @Parameter(description = "뉴스 내용") @RequestParam String description,
        @Parameter(description = "언론사") @RequestParam(defaultValue = "뉴스") String publisher,
        @Parameter(description = "원문 링크 (선택사항)") @RequestParam(required = false) String originallink,
        @Parameter(description = "네이버 링크 (선택사항)") @RequestParam(required = false) String link,
        @Parameter(description = "스크립트 길이") @RequestParam(defaultValue = "SHORT") String scriptLength
    ) {
        log.info("스크립트 생성 요청 - 제목: {}, 길이: {}", title, scriptLength);
        
        // 단일 뉴스 아이템 생성
        NewsItem newsItem = NewsItem.builder()
            .title(title)
            .description(description)
            .publisher(publisher)
            .originallink(originallink)
            .link(link)
            .pubDate(LocalDateTime.now())
            .build();
        
        List<NewsItem> newsItems = List.of(newsItem);
        String script = newsService.generateScript(newsItems, scriptLength);
        return ResponseEntity.ok(CommonResponse.onSuccess(HttpStatus.OK.value(), script));
    }
    
    /**
     * TTS 변환만 실행
     */
    @PostMapping("/tts")
    @Operation(
        summary = "TTS 음성 변환",
        description = "스크립트를 Clova Voice TTS를 사용해 다정한 목소리로 변환합니다."
    )
    public ResponseEntity<CommonResponse<String>> generateSpeech(
        @Parameter(description = "변환할 스크립트") @RequestParam String script
    ) {
        log.info("TTS 변환 요청");
        
        String audioUrl = newsService.generateSpeech(script);
        return ResponseEntity.ok(CommonResponse.onSuccess(HttpStatus.OK.value(), audioUrl));
    }
    
    /**
     * 관련 뉴스 추천 API
     */
    @PostMapping("/recommendations")
    @Operation(
        summary = "관련 뉴스 추천",
        description = "현재 뉴스와 관련된 뉴스들을 RAG 기반으로 추천합니다."
    )
    public ResponseEntity<CommonResponse<List<NewsItem>>> getRecommendations(
        @Parameter(description = "뉴스 제목") @RequestParam String title,
        @Parameter(description = "뉴스 내용") @RequestParam String description,
        @Parameter(description = "언론사") @RequestParam(defaultValue = "뉴스") String publisher,
        @Parameter(description = "뉴스 링크") @RequestParam String link
    ) {
        log.info("뉴스 추천 요청 - 제목: {}", title);
        
        NewsItem currentNews = NewsItem.builder()
            .title(title)
            .description(description)
            .publisher(publisher)
            .link(link)
            .pubDate(LocalDateTime.now())
            .build();
        
        List<NewsItem> recommendations = newsRecommendationService.findSimilarNewsByContent(currentNews);
        return ResponseEntity.ok(CommonResponse.onSuccess(HttpStatus.OK.value(), recommendations));
    }
    
    /**
     * 벡터DB 성장 모니터링 API 추가
     */
    @GetMapping("/admin/embedding/stats")
    @Operation(
        summary = "벡터DB 통계 조회",
        description = "벡터DB에 저장된 뉴스 개수와 성장 현황을 조회합니다."
    )
    public ResponseEntity<CommonResponse<Object>> getEmbeddingStats() {
        log.info("벡터DB 통계 조회 요청");
        
        try {
            // 간단한 통계 정보 (실제로는 Repository에서 조회)
            Object stats = java.util.Map.of(
                "message", "벡터DB 통계는 실제 구현 시 NewsEmbeddingRepository.count() 등을 사용하여 조회",
                "totalCount", "TODO: 실제 카운트",
                "todayAdded", "TODO: 오늘 추가된 수",
                "lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            
            return ResponseEntity.ok(CommonResponse.onSuccess(HttpStatus.OK.value(), stats));
        } catch (Exception e) {
            log.error("벡터DB 통계 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.onFailure(HttpStatus.INTERNAL_SERVER_ERROR.value(), "통계 조회에 실패했습니다."));
        }
    }
    
    /**
     * 뉴스 임베딩 배치 작업 수동 실행 (관리자용)
     */
    @PostMapping("/admin/embedding/batch")
    @Operation(
        summary = "뉴스 임베딩 배치 작업 수동 실행",
        description = "관리자용 API로 최신 뉴스를 수집하여 임베딩을 생성합니다."
    )
    public ResponseEntity<CommonResponse<String>> runEmbeddingBatch() {
        log.info("뉴스 임베딩 배치 작업 수동 실행 요청");
        
        try {
            newsEmbeddingBatchService.collectAndEmbedLatestNews();
            String message = "뉴스 임베딩 배치 작업이 시작되었습니다. 백그라운드에서 처리됩니다.";
            return ResponseEntity.ok(CommonResponse.onSuccess(HttpStatus.OK.value(), message));
        } catch (Exception e) {
            log.error("뉴스 임베딩 배치 작업 실행 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.onFailure(HttpStatus.INTERNAL_SERVER_ERROR.value(), "배치 작업 실행에 실패했습니다."));
        }
    }
    
    /**
     * 헬스 체크
     */
    @GetMapping("/health")
    @Operation(
        summary = "헬스 체크",
        description = "뉴스 API 서비스 상태를 확인합니다."
    )
    public ResponseEntity<CommonResponse<String>> healthCheck() {
        String message = "뉴스 API 서비스가 정상적으로 작동 중입니다.";
        return ResponseEntity.ok(CommonResponse.onSuccess(HttpStatus.OK.value(), message));
    }
}
