package com.swen.news.global.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Value("${spring.security.oauth2.client.registration.naver.client-id:N/A}")
    private String clientId;
    
    @Value("${spring.security.oauth2.client.registration.naver.redirect-uri:N/A}")
    private String redirectUri;

    @GetMapping("/hello")
    public String hello() {
        return "Hello, World!";
    }
    
    @GetMapping("/oauth-config")
    public Map<String, String> getOAuthConfig() {
        return Map.of(
            "clientId", clientId,
            "redirectUri", redirectUri,
            "authUrl", "http://localhost:8080/oauth2/authorization/naver",
            "serverPort", "8080"
        );
    }
}
