# 1. 빌드 스테이지 (Gradle Build)
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Gradle 래퍼 및 설정 복사 (캐시 활용)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 실행 권한 부여 및 의존성 사전 다운로드
RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon || true

# 소스코드 전체 복사 및 bootJar 빌드 (테스트는 런타임 이미지 크기를 위해 스킵)
COPY src src
RUN ./gradlew bootJar -x test --no-daemon

# 2. 실행 스테이지 (Lean JRE Runtime)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Non-root 보안 계정 생성 및 권한 설정
RUN addgroup -S mindcup && adduser -S mindcup -G mindcup
USER mindcup

# 빌드 결과물 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 애플리케이션 포트 8080 노출
EXPOSE 8080

# JVM 메모리 상한 지정 (-Xms128m -Xmx320m) : Render 512MB RAM 제약 환경 OOM 방지 및 기동 안정성 확보
ENTRYPOINT ["java", "-Xms128m", "-Xmx320m", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
