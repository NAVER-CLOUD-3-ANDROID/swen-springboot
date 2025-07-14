# Dockerfile

# 1단계: 빌드 스테이지
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app

# 종속성 캐시 최적화를 위한 선복사
COPY build.gradle settings.gradle /app/
RUN gradle dependencies --no-daemon

# 전체 프로젝트 복사 및 빌드
COPY . .
RUN gradle bootJar --no-daemon

# 2단계: 실행 스테이지 (가볍게 유지)
FROM openjdk:17-jdk-slim
WORKDIR /app

# 빌드된 JAR 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 실행 시 환경 변수로 Spring 프로필 설정
ENTRYPOINT ["java", "-jar", "app.jar"]
