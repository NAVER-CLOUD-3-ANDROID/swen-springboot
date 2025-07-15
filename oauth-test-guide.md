# 네이버 소셜 로그인 테스트 가이드

## 1. 애플리케이션 실행 확인
- URL: `http://localhost:8080/api/health`
- 예상 응답: 서버 상태 정보

## 2. 네이버 로그인 테스트

### 방법 1: 직접 OAuth2 URL 접근
1. 브라우저에서 접속: `http://localhost:8080/oauth2/authorization/naver`
2. 네이버 로그인 페이지로 자동 리다이렉트
3. 네이버 계정으로 로그인
4. 권한 동의 후 콜백 URL로 리다이렉트: `http://localhost:8080/login/oauth2/code/naver`
5. 성공 시 프론트엔드 URL로 리다이렉트: `http://localhost:3000/auth/callback?success=true&userId=1`

### 방법 2: API를 통한 로그인 URL 받기
1. URL: `http://localhost:8080/api/auth/naver`
2. 응답에서 `loginUrl` 받기
3. 해당 URL로 브라우저에서 접속

## 3. 사용자 정보 조회 테스트
- 로그인 후 생성된 사용자 정보 조회
- URL: `http://localhost:8080/api/auth/user/{userId}`

## 4. 스웨거를 통한 테스트
- URL: `http://localhost:8080/swagger-ui.html`
- AuthController 섹션에서 각 API 테스트 가능

## 5. 예상 플로우
```
1. 사용자가 로그인 시작 URL 클릭
2. 네이버 로그인 페이지로 리다이렉트
3. 사용자가 네이버 계정으로 로그인
4. 네이버에서 권한 동의 요청
5. 동의 후 우리 서버의 콜백 URL로 리다이렉트
6. OAuth2SuccessHandler에서 사용자 정보 처리
7. 데이터베이스에 사용자 정보 저장
8. 프론트엔드로 리다이렉트 (성공 시)
```

## 6. 문제 해결
- 로그 확인: 콘솔에서 OAuth2 관련 로그 확인
- 데이터베이스 확인: users 테이블에 데이터 생성 여부 확인
- 네이버 개발자 센터에서 리다이렉트 URI 설정 확인
