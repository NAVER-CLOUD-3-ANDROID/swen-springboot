# 뉴스 TTS 서비스 API

네이버 뉴스 API, HyperCLOVA, Clova Dubbing을 연동한 뉴스 읽어주기 서비스입니다.

## 🚀 주요 기능

### 1. 🎵 플레이 버튼 기능 (메인)
- **간편 랜덤 뉴스**: `GET /api/v1/news/play` - 클릭 한 번으로 1분 내 다정한 목소리로 랜덤 최신 뉴스 재생
- **맞춤 뉴스**: 주제 입력 시 해당 뉴스 1개만 처리
- **고정 설정**: 다정하고 깔끔한 목소리, 기본 1분 내 스크립트

### 2. 개별 기능
- 뉴스 검색만 실행
- 스크립트 생성만 실행  
- TTS 변환만 실행

## 📡 API 엔드포인트

### 🎵 플레이 버튼 (메인 기능)

#### 간편 버전 (GET) - 앱 플레이 버튼용
```http
GET /api/v1/news/play
```
**🔥 앱의 플레이 버튼에서 바로 사용하세요! 1분 내 다정한 목소리로 랜덤 뉴스 재생**

#### 상세 설정 (POST)
```http
POST /api/v1/news/play
```

**요청 예시 (모든 필드 선택사항):**
```json
{
    "topic": "AI 인공지능",        // 없으면 랜덤 최신 뉴스
    "scriptLength": "SHORT"       // 기본값: "SHORT" (1분 내)
}
```

**응답 예시 (글로벌 응답 시스템):**
```json
{
    "isSuccess": true,
    "status": 200,
    "data": {
        "scriptId": "uuid-string",
        "script": "생성된 뉴스 스크립트 내용...",
        "sourceNews": [
            {
                "title": "뉴스 제목",
                "description": "뉴스 요약",
                "publisher": "언론사",
                "link": "뉴스 링크"
            }
        ],
        "audioUrl": "생성된 음성 파일 URL",
        "status": "COMPLETED",
        "createdAt": "2025-01-15T10:30:00"
    },
    "timestamp": "2025-01-15T10:30:00"
}
```

**에러 응답 예시:**
```json
{
    "isSuccess": false,
    "status": 500,
    "data": {
        "code": "NEWS_001",
        "reason": "뉴스 검색에 실패했습니다."
    },
    "timestamp": "2025-01-15T10:30:00"
}
```

### 🔍 뉴스 검색 (단건)

```http
GET /api/v1/news/search
```

**사용 예시:**
```http
# 특정 주제 뉴스 1건
GET /api/v1/news/search?query=AI

# 랜덤 최신 뉴스 1건  
GET /api/v1/news/search
```

**응답:** 항상 1건의 뉴스만 반환

### 📝 스크립트 생성
```http
POST /api/v1/news/script?title=뉴스제목&description=뉴스내용&scriptLength=SHORT
```

**링크 정보 포함 (선택사항):**
```http
POST /api/v1/news/script?title=뉴스제목&description=내용&originallink=원문링크&link=네이버링크&scriptLength=SHORT
```

### 🎤 TTS 변환
```http
POST /api/v1/news/tts
```

## ⚙️ 설정

### 필수 환경 변수
- 노션 참고

## 🎨 음성 스타일

- **고정 설정**: 다정하고 깔끔한 톤 (nara 보이스)
- 별도 설정 불필요, 자동으로 최적화된 목소리 제공

## 📏 스크립트 길이 옵션

- **SHORT**: 1분 내 간단한 요약 (기본값)
- **MEDIUM**: 3분 분량의 적절한 길이
- **LONG**: 5분 분량의 상세한 내용

## 🚀 실행 방법

### 1. 로컬 실행
```bash
# 데이터베이스 생성
mysql -u root -p -e "CREATE DATABASE swen_db;"

# 애플리케이션 실행
./gradlew bootRun
```

### 2. Docker 실행
```bash
docker-compose -f docker-compose.dev.yml up
```

## 📚 API 문서

애플리케이션 실행 후 Swagger UI에서 상세한 API 문서를 확인할 수 있습니다:
- **URL**: http://localhost:8080/swagger-ui.html

## 🧪 테스트

`news-api-test.http` 파일을 사용하여 API를 테스트할 수 있습니다.

## 🏗️ 아키텍처

```
Frontend (Play Button)
       ↓
   News Controller
       ↓
    News Service
       ↓
┌─────────────────┐
│ 1. Naver News   │ → 뉴스 검색
│ 2. HyperCLOVA   │ → 스크립트 생성
│ 3. Clova Dubbing│ → TTS 변환
└─────────────────┘
       ↓
   Audio Response
```

## 🔧 기술 스택

- **Spring Boot 3.5.3**
- **Spring Cloud OpenFeign** (외부 API 클라이언트)
- **MySQL 8.0**
- **Lombok**
- **SpringDoc OpenAPI** (Swagger)
- **Jackson** (JSON 처리)
- **글로벌 응답/예외 처리 시스템**

## ⚡ 예외 처리

모든 API는 글로벌 예외 처리 시스템을 사용합니다:

### 뉴스 도메인 에러 코드
- `NEWS_001`: 뉴스 검색 실패
- `NEWS_101`: 스크립트 생성 실패  
- `NEWS_201`: TTS 변환 실패
- `NEWS_302`: 외부 API 서비스 불가

### 공통 응답 형식
모든 API 응답은 `CommonResponse<T>` 형태로 래핑됩니다:
```json
{
    "isSuccess": boolean,
    "status": number,
    "data": T,
    "timestamp": "2025-01-15T10:30:00"
}
```

## 📝 라이센스

이 프로젝트는 MIT 라이센스 하에 배포됩니다.
