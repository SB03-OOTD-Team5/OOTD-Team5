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

# JVM 옵션 외부 설정 가능
ENV JVM_OPTS=""
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar app.jar"]
