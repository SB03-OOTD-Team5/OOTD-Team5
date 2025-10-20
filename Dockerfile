# ------------------------------------------------------------
# 1단계: 빌드 환경
# ------------------------------------------------------------
FROM public.ecr.aws/amazoncorretto/amazoncorretto:17-alpine3.20-jdk AS builder

WORKDIR /app

# 디버그/헬스체크용 curl
RUN apk add --no-cache curl

# Gradle 래퍼/설정 먼저 복사 → 캐시 최적화
COPY gradlew .
COPY gradle gradle
COPY build.gradle* .
COPY settings.gradle* .

# gradlew 권한
RUN chmod +x ./gradlew

# 의존성만 먼저 받아 캐시 생성 (실패해도 빌드 진행)
RUN ./gradlew dependencies --no-daemon || true

# 소스 복사
COPY src src

# jar 빌드 (CI에서 테스트 이미 돈다고 가정)
RUN ./gradlew bootJar --no-daemon -x test

# ------------------------------------------------------------
# 2단계: 실행 환경
# ------------------------------------------------------------
FROM public.ecr.aws/amazoncorretto/amazoncorretto:17-alpine3.20-jdk

WORKDIR /app

RUN apk add --no-cache curl \
 && adduser -D -h /home/appuser -s /bin/sh appuser \
 && mkdir -p /home/appuser \
 && chown -R appuser:appuser /app /home/appuser

COPY --from=builder /app/build/libs/*.jar /app/app.jar
RUN chown appuser:appuser /app/app.jar

# JVM 옵션 외부 설정 가능
ENV JVM_OPTS=""
EXPOSE 8080

# 보안: non-root로 실행
USER appuser

ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar /app/app.jar"]

