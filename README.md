# 뉴스 TTS + RAG 추천 서비스 API

네이버 뉴스 API, HyperCLOVA, Clova Voice를 연동한 뉴스 읽어주기 + AI 관련 뉴스 추천 서비스입니다.

## 🚀 주요 기능

### 1. 🎵 플레이 버튼 기능 (메인)
- **간편 랜덤 뉴스**: `GET /api/v1/news/play` - 클릭 한 번으로 1분 내 다정한 목소리로 랜덤 최신 뉴스 재생
- **🤖 AI 관련 뉴스 추천**: RAG 기반으로 재생한 뉴스와 관련된 뉴스 3-5개 자동 추천
- **맞춤 뉴스**: 주제 입력 시 해당 뉴스 1개만 처리
- **고정 설정**: 다정하고 깔끔한 목소리, 기본 1분 내 스크립트

### 2. 🤖 RAG 추천 시스템
- **벡터 검색**: HyperCLOVA Embedding으로 뉴스 의미 벡터화 후 유사도 검색
- **자동 학습**: 사용할수록 추천 품질 향상
- **콜드 스타트 해결**: 초기에는 키워드 기반 실시간 추천
- **자동 수집**: 스케줄러로 매일 최신 뉴스 자동 임베딩

### 3. 개별 기능
- 뉴스 검색만 실행
- 스크립트 생성만 실행  
- TTS 변환만 실행
- 관련 뉴스 추천만 실행

## 📡 API 엔드포인트

### 🎵 플레이 버튼 (메인 기능)

#### 간편 버전 (GET) - 앱 플레이 버튼용
```http
GET /api/v1/news/play
```
**🔥 앱의 플레이 버튼에서 바로 사용하세요! 1분 내 다정한 목소리로 랜덤 뉴스 재생 + 관련 뉴스 추천**

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
        "recommendedNews": [
            {
                "title": "관련 뉴스 1",
                "description": "AI 기반 추천된 관련 뉴스",
                "publisher": "테크뉴스",
                "link": "관련뉴스링크"
            }
        ],
        "status": "COMPLETED",
        "createdAt": "2025-01-15T10:30:00"
    },
    "timestamp": "2025-01-15T10:30:00"
}
```

### 🤖 관련 뉴스 추천

```http
POST /api/v1/news/recommendations
```

**요청 예시:**
```http
POST /api/v1/news/recommendations
Content-Type: application/x-www-form-urlencoded

title=현재뉴스제목&description=뉴스내용&publisher=언론사&link=뉴스링크
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

### 📝 스크립트 생성
```http
POST /api/v1/news/script?title=뉴스제목&description=뉴스내용&scriptLength=SHORT
```

### 🎤 TTS 변환
```http
POST /api/v1/news/tts?script=변환할텍스트
```

### 🔧 관리자 API
```http
# 뉴스 임베딩 배치 작업 수동 실행
POST /api/v1/news/admin/embedding/batch

# 벡터DB 통계 조회
GET /api/v1/news/admin/embedding/stats
```

## ⚙️ 설정

### 필수 환경 변수
```env
# 네이버 API
NAVER_CLIENT_ID=네이버_클라이언트_ID
NAVER_CLIENT_SECRET=네이버_클라이언트_시크릿

# HyperCLOVA (스크립트 생성 + 임베딩)
NAVER_HYPERCLOVA_API_KEY=하이퍼클로바_API_키
NAVER_HYPERCLOVA_EMBEDDING_API_KEY=하이퍼클로바_임베딩_API_키

# Clova Voice (TTS)
CLOVA_VOICE_CLIENT_ID=클로바보이스_클라이언트_ID
CLOVA_VOICE_CLIENT_SECRET=클로바보이스_클라이언트_시크릿

# NCP Object Storage
NCP_ACCESS_KEY=NCP_액세스_키
NCP_SECRET_KEY=NCP_시크릿_키
NCP_BUCKET_NAME=버킷이름

# 데이터베이스
DB_URL=jdbc:mysql://localhost:3306/swen_db
DB_USERNAME=사용자명
DB_PASSWORD=비밀번호
```

### RAG 추천 설정
```yaml
# application.yml
vector-db:
  similarity-threshold: 0.6  # 유사도 임계값
  max-recommendations: 5     # 최대 추천 개수

scheduler:
  news-embedding:
    enabled: true    # 자동 뉴스 수집 활성화
```

## 🎨 음성 스타일

- **고정 설정**: 다정하고 깔끔한 톤 (nsujin 보이스)
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
┌─────────────────┐    ┌─────────────────┐
│ 1. Naver News   │ →  │ 4. RAG 추천     │
│ 2. HyperCLOVA   │    │ - 벡터 검색     │
│ 3. Clova Voice  │    │ - 유사도 계산   │
└─────────────────┘    │ - 관련 뉴스 추천│
       ↓               └─────────────────┘
   Audio + 추천뉴스 Response
```

## 🔧 기술 스택

- **Spring Boot 3.5.3**
- **Spring Cloud OpenFeign** (외부 API 클라이언트)
- **MySQL 8.0** + **Spring Data JPA**
- **HyperCLOVA X** (스크립트 생성 + 임베딩)
- **Apache Commons Math** (벡터 연산)
- **Lombok**
- **SpringDoc OpenAPI** (Swagger)
- **글로벌 응답/예외 처리 시스템**

## ⚡ 예외 처리

모든 API는 글로벌 예외 처리 시스템을 사용합니다:

### 뉴스 도메인 에러 코드
- `NEWS_001`: 뉴스 검색 실패
- `NEWS_101`: 스크립트 생성 실패  
- `NEWS_201`: TTS 변환 실패
- `NEWS_401`: 임베딩 생성 실패
- `NEWS_403`: 뉴스 추천 실패
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

## 🤖 RAG 추천 시스템

### 동작 방식
1. **뉴스 재생** → 스크립트 생성 → TTS 변환
2. **벡터 검색** → HyperCLOVA 임베딩으로 관련 뉴스 찾기
3. **자동 학습** → 재생/추천된 뉴스를 벡터DB에 저장
4. **품질 향상** → 사용할수록 추천 정확도 개선

### 콜드 스타트 해결
- **첫 사용자**: 키워드 기반 실시간 검색으로 추천
- **시간 경과**: RAG 벡터 검색으로 고품질 추천
- **자동 수집**: 스케줄러로 매일 최신 뉴스 학습

## 📝 라이센스

이 프로젝트는 MIT 라이센스 하에 배포됩니다.
