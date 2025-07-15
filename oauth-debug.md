# 네이버 OAuth2 디버깅 가이드

## 현재 설정값
- **Client ID**: `E5bgwo_wmURw9dPqnKaW`
- **Redirect URI**: `http://localhost:8080/login/oauth2/code/naver`
- **Authorization URL**: `http://localhost:8080/oauth2/authorization/naver`

## 에러 분석
URL: `https://nid.naver.com/login/ext/error.inapp?inapp_view=true&disp_stat=207&client_id=E5bgwo_wmURw9dPqnKaW&locale=ko_KR`

**에러 코드 `disp_stat=207`의 의미:**
- 207 = "서비스 설정 오류"
- 주로 Redirect URI 불일치 또는 서비스 URL 설정 문제

## 네이버 개발자 센터에서 확인해야 할 사항

### 1. 애플리케이션 기본 정보
- 애플리케이션 이름: `SWEN` (또는 유사)
- 서비스 URL: `http://localhost:8080`

### 2. 서비스 환경
- 서비스 환경: `PC웹`으로 설정되어 있는지 확인
- 서비스 URL: `http://localhost:8080`

### 3. Callback URL 설정
- **반드시**: `http://localhost:8080/login/oauth2/code/naver`
- 정확히 입력했는지 확인 (공백, 오타 없이)

### 4. 제공 정보 설정
- 이메일 주소: 필수 ✓
- 이름: 필수 ✓
- 프로필 사진: 선택 ✓

### 5. 서비스 상태
- 개발 중: 검수 상태가 "개발 중"인지 확인
- 서비스 중지되지 않았는지 확인

## 해결 방법

### 1단계: 네이버 개발자 센터 재설정
1. https://developers.naver.com 접속
2. 내 애플리케이션 > 해당 앱 선택
3. "API 설정" 탭 확인
4. 서비스 URL과 Callback URL 재입력
5. 저장 후 몇 분 대기

### 2단계: 새 애플리케이션 생성 (필요시)
기존 앱에 문제가 있다면 새로 생성:
1. 애플리케이션 이름: `SWEN-TEST`
2. 사용 API: `네이버 로그인`
3. 서비스 URL: `http://localhost:8080`
4. Callback URL: `http://localhost:8080/login/oauth2/code/naver`

### 3단계: 설정 확인
- 새 Client ID와 Client Secret을 .env 파일에 업데이트

## 테스트 URL
1. 설정 확인: `http://localhost:8080/api/test/oauth-config`
2. 로그인 시작: `http://localhost:8080/oauth2/authorization/naver`
