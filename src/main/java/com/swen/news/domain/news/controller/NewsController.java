package com.swen.news.domain.news.controller;

import com.swen.news.domain.news.dto.NewsSearchRequest;
import com.swen.news.domain.news.dto.NewsScriptResponse;
import com.swen.news.domain.news.dto.PlayNewsRequest;
import com.swen.news.domain.news.dto.NewsItem;
import com.swen.news.domain.news.service.NewsService;
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
        description = "스크립트를 Clova Dubbing을 사용해 다정한 목소리로 변환합니다."
    )
    public ResponseEntity<CommonResponse<String>> generateSpeech(
        @Parameter(description = "변환할 스크립트") @RequestParam String script
    ) {
        log.info("TTS 변환 요청");
        
        String audioUrl = newsService.generateSpeech(script);
        return ResponseEntity.ok(CommonResponse.onSuccess(HttpStatus.OK.value(), audioUrl));
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
