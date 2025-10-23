# ------------------------------------------------------------
# 1단계: 빌드 환경
# ------------------------------------------------------------
FROM amazoncorretto:17 AS builder

WORKDIR /app

# curl 설치
RUN yum update -y && \
    yum install -y curl && \
    yum clean all

# 프로젝트 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

# Gradle wrapper로 jar 빌드
RUN ./gradlew bootJar

# ------------------------------------------------------------
# 2단계: 실행 환경
# ------------------------------------------------------------
FROM amazoncorretto:17-alpine3.21-jdk

WORKDIR /app

RUN apk add --no-cache curl

# jar만 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 빌드 시크릿(GEMINI_KEY_JSON)을 /tmp/gemini.json으로 받아 이미지에 복사
RUN --mount=type=secret,id=gemini,target=/tmp/gemini.json \
    mkdir -p /etc/secrets && \
    cp /tmp/gemini.json /etc/secrets/gemini.json && \
    chmod 400 /etc/secrets/gemini.json

# 구글 SDK가 읽을 경로 지정
ENV GOOGLE_APPLICATION_CREDENTIALS=/etc/secrets/gemini.json

# JVM 옵션 외부 설정 가능
ENV JVM_OPTS=""
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar app.jar"]