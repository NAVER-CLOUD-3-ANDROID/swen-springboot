package com.swen.news.domain.news.exception;

import com.swen.news.domain.news.code.NewsErrorCode;
import com.swen.news.global.exception.CommonException;

/**
 * 뉴스 도메인 예외 클래스입니다.
 * 글로벌 예외 처리 시스템을 활용합니다.
 */
public class NewsException extends CommonException {
    
    public NewsException(NewsErrorCode errorCode) {
        super(errorCode);
    }
}
