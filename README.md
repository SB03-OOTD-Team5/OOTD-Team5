# [옷장을 부탁해] 프로젝트 [![codecov](https://graph/badge.svg)](https://codecov.io/sb03-ootd-team5)

## 🌤 프로젝트 개요

[55TD-옷장을 부탁해]는 날씨·위치·사용자 옷장을 결합해 실시간 추천과 소셜 피드를 제공하는 서비스입니다.
- 사용자가 등록한 의상과 날씨 데이터를 조합해 맞춤형 OOTD를 추천
- AI 기반 코멘트, Kafka→Elasticsearch 검색, WebSocket/SSE 실시간 경험 제공
- 프로젝트 기간: 2025.01.06 ~ 2025.02.28 (2개월)

### \<팀 문서\>
🔗 [팀 협업 문서 바로가기](https://www.notion.so/ohgiraffers/55TD-207649136c1180288dabfeb1877485d6?source=copy_link)

## 👥 팀원 구성

| 이름  | 역할                                  | Github                                    |
|-----|-------------------------------------|-------------------------------------------|
| 박인규 | 의상속성 API · DM 서비스 · DNS,HTTPS       | [Github](https://github.com/Leichtstar) |
| 강문구 | 사용자/인증 · AWS배포 · 분산환경 구축            | [Github](https://github.com/Kangmoongu) |
| 김유빈 | 피드 API · ElasticSearch · 댓글/좋아요/팔로우 | [Github](https://github.com/Im-Ubin) |
| 안여경 | 날씨/위치 · Batch · 의상 추천 · 스키마 작성      | [Github](https://github.com/yeokyeong)      |
| 조현아 | 의상 API · SpringAI 기능 전반 · SSE 알림    | [Github](https://github.com/hyohyo-zz)   |

## 🛠️ 기술 스택

### Backend & Framework
- Java 17, Spring Boot 3.5.5, Spring Data JPA, QueryDSL
- Spring Security, OAuth2 Client, Spring WebSocket(STOMP), Spring WebFlux
- Spring Batch, Spring Cache(Caffeine+Redis), MapStruct, Lombok

### Database & Cache
- PostgreSQL (AWS RDS), Redis (ElastiCache), H2 (테스트)

### Search & AI
- Elasticsearch 8.x (feed 인덱스), Spring AI 1.0.0-M6 (Vertex AI Gemini), Jsoup 1.18.1

### Messaging & Monitoring
- Apache Kafka (Confluent Cloud), Spring Kafka
- RabbitMQ 3.13 (STOMP relay), Spring SSE, Actuator, Jacoco

### Storage & External APIs
- AWS S3 (presigned upload), Gmail SMTP, Kakao Local API
- KMA / OpenWeather / Open-Meteo, Google Vertex AI, AWS CloudWatch

### Infrastructure & Deployment
- Docker & Docker Compose, Amazon ECR/ECS, AWS CloudMap
- Nginx 1.27 (reverse proxy + certbot), GitHub Actions (OIDC)

## 💻 개발 환경
- **IDE**: IntelliJ IDEA
- **Build Tool**: Gradle 8.7 / Wrapper 포함
- **DB 툴**: DataGrip
- **Container**: Docker Desktop
- **Version Control**: Git + GitHub
- **API 문서**: SpringDoc OpenAPI 2.8.13 (Swagger UI)
- **테스트 도구**: JUnit5, Mockito, Spring Boot Test, Spring Security Test

## ✅ 테스트 커버리지
- Jacoco 리포트를 기준으로 **80% 이상 커버리지**를 유지
- PR 머지 조건
  - 필수 리뷰 2명 이상 승인

## 🖐 브랜치 보호 규칙
- `main` : 배포 브랜치, CI + 리뷰 필수
- `dev` : 통합 브랜치, 기능 브랜치에서 PR
- 강제 푸시 / 히스토리 재작성 금지, PR 템플릿 체크리스트 준수

---

## 🎯 팀원별 구현 기능 상세
### 박인규

- **Github Actions CI/CD**
  - Repository에 Pull Request Open 시 Gradle 기반의 Build 및 Test 진행으로 프로젝트 정합성 확보
  - Main 브랜치에 병합 성공 혹은 별도의 ReverseProxy, Backend 배포Action요청 시 AWS로 이미지 푸시 및 강제배포하는 CD 파이프라인 구성
  <img width="1431" height="768" alt="image" src="https://github.com/user-attachments/assets/05dae565-773e-4353-baec-d6eb04fb76de" />


- **의상속성관리**
  - 관리자 권한으로 접속 시 의상의 성격을 정의하는 속성 카테고리를 관리
  - 속성 관리 탭을 통하여 등록된 속성을 키워드 검색, 페이지네이션으로 정렬하여 조회.

    <img width="1245" height="466" alt="image" src="https://github.com/user-attachments/assets/162db978-58c3-42fe-ad24-6841f20c1ff1" />


  - 새로운 속성을 부여하거나 수정,삭제

    <img width="569" height="456" alt="image" src="https://github.com/user-attachments/assets/bfb721d4-c6e6-4299-9385-4b3304012402" />


    <img width="526" height="463" alt="image" src="https://github.com/user-attachments/assets/3ac4864f-078e-493c-803b-a15c988cb64c" />

  - 등록된 속성은 의상 등록 시 카테고리에 즉시 반영

    <img width="528" height="736" alt="image" src="https://github.com/user-attachments/assets/3d0b4595-a5ab-4c96-9b26-6f205e590339" />



- **DirectMessage (Websocket) 기능**
  - 피드를 작성한 유저 페이지에서 DirectMessage 송신

    <img width="1166" height="575" alt="image" src="https://github.com/user-attachments/assets/dd5c8ae7-584a-4d92-a459-b8b58ef3ba6a" />

    <img width="1177" height="723" alt="image" src="https://github.com/user-attachments/assets/2698913d-f0e9-4d94-8e5a-5b06fc5e8134" />


  - 분산환경에서 RabbitMQ를 활용해 메세지 처리
- **날씨조회 보조 API OpenMeteo**
  - 날씨조회 주 API인 Open-weather의 Interface에 맞추어 해당 서버 및 KMA 서버 장애 시 대체 API로 활용 가능.
- **Let’sEncrypt 기반 HTTPS 인증서 발급,재발급**
  - 대표 도메인인 [https://55td.duckdns.org에](https://55td.duckdns.org에) 대하여 nginx 컨테이너에서 Let’sEncrypt로부터 certbot을 활용한 인증서 취득
  - 취득한 인증서는 AWS S3에 저장 및 인증 실패시 자체인증서 발급하여 HTTP프로토콜로 서비스
  - 30일 미만의 인증서는 자동 갱신

### 강문구

- **Security 보안 설정**
  - Spring Security를 활용하여 사용자 역할에 따른 권한 설정
  - 관리자 페이지와 일반 사용자 페이지를 위한 조건부 라우팅 처리
  - 관리자 페이지에서 사용자 계정잠금, 관리자 권한수정 구현
  - 관리자 페이지의 유저 목록 조건에 따른 커서페이지네이션 구현

  <img width="1000" height="500" alt="image" src="https://github.com/user-attachments/assets/e6d36eb7-34e6-4c3d-80af-8b6ce34c51b7" />



  <img width="1000" height="500" alt="image" src="https://github.com/user-attachments/assets/b4ecbd6d-3616-4751-bfff-129f3deb2e64" />


- **OAuth2 로그인 구현**
  - 카카오 계정을 통한 OAuth 로그인 구현

    <img width="1153" height="762" alt="image" src="https://github.com/user-attachments/assets/3e5de5f9-4844-497e-a518-293ad9b92aa7" />


  - 구글 계정을 통한 OAuth 로그인 구현

    <img width="1000" height="500" alt="image" src="https://github.com/user-attachments/assets/f27d0f30-0ec3-445a-9f3c-4a567b6ead86" />


- 사용자 프로필 설정 구현

  <img width="1000" height="500" alt="image" src="https://github.com/user-attachments/assets/ee41c48e-1a86-438f-ba0b-127d757c815b" />


- SMTP를 이용한 임시 비밀번호 발급 구현

  <img width="1852" height="471" alt="image" src="https://github.com/user-attachments/assets/e4bfb843-7e03-402a-a0dd-1c7604af9941" />


- AWS 분산 배포 구현

   <img width="1148" height="130" alt="image" src="https://github.com/user-attachments/assets/f7fb608c-47e6-42d9-b6fd-09825ce8b402" />


  탄력적 IP

  <img width="1148" height="60" alt="image" src="https://github.com/user-attachments/assets/47b74ca2-5c1c-41c8-9169-3180a782f4c0" />


  NAT Gateway

  <img width="1148" height="46" alt="image" src="https://github.com/user-attachments/assets/c97bd7f5-db77-48f3-af51-a0d70b7daa63" />


  AWS Cloud Map

  <img width="1650" height="1061" alt="image" src="https://github.com/user-attachments/assets/5de9af60-92b4-4862-ad0e-15dae7c659ba" />


  AWS Elastic Cache

  <img width="900" height="500" alt="image" src="https://github.com/user-attachments/assets/5b7d9331-afa8-4e20-a443-beafa758eeae" />


  Confluent Cloud(Message Broker)

  <img width="1000" height="360" alt="image" src="https://github.com/user-attachments/assets/d4f4f64e-4c94-4627-afdc-e9016a255882" />


### 김유빈

- **피드 목록 조회**
  - 키워드 여부에 따른 조회 로직 분기 처리, 페이지네이션 구현
  - 키워드 없이 피드 조회

    <img width="1579" height="1290" alt="image" src="https://github.com/user-attachments/assets/32548630-fd69-4b9c-a6e5-cf60a3e52a3a" />


  - 키워드 포함 피드 조회 시, Elasticsearch를 이용한 고급 검색
    - 

    <img width="1584" height="559" alt="image" src="https://github.com/user-attachments/assets/eafed897-42bd-4be5-a76c-5b16f6936c3f" />


- **피드 등록**
  - 날씨 정보와 추천된 OOTD 정보들을 포함한 피드 생성
  - 피드 생성 시, Elasticsearch 인덱스에 데이터 반영
    - Kafka를 이용한 비동기 메세지 처리
  <img width="1584" height="958" alt="image" src="https://github.com/user-attachments/assets/85fb0b34-16da-4273-b81f-c0ef088d236e" />


  <img width="1694" height="950" alt="image" src="https://github.com/user-attachments/assets/c142efca-218f-49fc-98d1-9a4ec0f71d8f" />


- **피드 수정**
   - Kafka를 이용한 비동기 메세지 처리

  <img width="1583" height="1186" alt="image" src="https://github.com/user-attachments/assets/9ab2ed68-6934-4ee0-87ba-c2e378c74c96" />


  <img width="1583" height="1136" alt="image" src="https://github.com/user-attachments/assets/2b4a14b0-9e98-417e-a95e-67a5e269d07c" />


- **피드 삭제**

  <img width="1582" height="1109" alt="image" src="https://github.com/user-attachments/assets/81fd0102-5b84-4d3f-9b46-dba34d63cfbe" />


  <img width="1580" height="1151" alt="image" src="https://github.com/user-attachments/assets/e891e2b1-1472-4013-9e89-228f0a74cf6c" />


  <img width="1579" height="412" alt="image" src="https://github.com/user-attachments/assets/285d5e39-6044-482e-b6bc-dc526ef49e63" />
- **피드 좋아요/취소**
    - 피드 좋아요/취소 시, Elasticsearch 인덱스에 데이터 반영
        - Kafka를 이용한 비동기 메시지 처리
  <img width="1560" height="766" alt="image" src="https://github.com/user-attachments/assets/d9cb3697-599c-4615-b9f4-869397277bd2" />
- **피드 댓글 조회**
    - 페이지네이션으로 최신순 정렬
- **피드 댓글 등록/삭제**
  
- **팔로우 등록/취소**

  <img width="1498" height="942" alt="image" src="https://github.com/user-attachments/assets/ff20a96a-4e0f-45bf-a0c5-3d8f1a855b9f" />


  <img width="1475" height="933" alt="image" src="https://github.com/user-attachments/assets/d06bfbc3-bbcb-41fb-bcb8-c9f94331dfaa" />


- **팔로우 요약 정보 조회**

  <img width="1431" height="994" alt="image" src="https://github.com/user-attachments/assets/38e7d1b5-216b-40cd-a012-6311a218b8c4" />


- **팔로우/팔로잉 목록 조회**

  <img width="1384" height="1071" alt="image" src="https://github.com/user-attachments/assets/4cd5c1e5-eb03-4f6a-91a0-111e78b0fd99" />


  <img width="1388" height="999" alt="image" src="https://github.com/user-attachments/assets/d6af2597-8fa3-4f49-bbf5-63df43673f9a" />



### 안여경

- **관리자 API**
  - `@PathVariable`을 사용한 동적 라우팅 기능 구현
  - `PATCH`, `DELETE` 요청을 사용하여 학생 정보를 수정하고 탈퇴하는 API 엔드포인트 개발
- **CRUD 기능**
  - 학생 정보의 CRUD 기능을 제공하는 API 구현 (Spring Data JPA)
- **회원관리 슬라이더**
  - 학생별 정보 목록을 `Carousel` 형식으로 조회하는 API 구현

### 조현아

- 의상 관리 (Clothes CRUD)
  - 의상 구매링크로 정보 추출
    - `jsoup`을 활용한 OG 태그및 이미지 파싱
    - `llm(Gemini)`을 이용해 타입, 속성 등을 구조화된 `json`으로 변환

    <img width="674" height="448" alt="image" src="https://github.com/user-attachments/assets/e8bcd16b-3885-4be2-95c2-542f8be30b82" />


    <img width="1208" height="912" alt="image" src="https://github.com/user-attachments/assets/e985c579-a5c2-4e2f-a4ff-91c350a45991" />


  - 의상 등록
    - 파일 스토리지 (Local / AWS S3)
      - `LocalStorage`: 개발 환경에서 이미지 및 파일 저장
      - `S3Storage`: 운영 환경에서 AWS S3를 통한 안정적 파일 관리(타입추론으로 바로보기지원)
      - `Presigned` URL 기반 다운로드 지원 (보안 강화 및 트래픽 절감)

  <img width="1208" height="912" alt="image" src="https://github.com/user-attachments/assets/be8ef591-5afe-4d3e-8bcd-44d8b4942836" />


  - 의상 목록 조회
    - `CacheEvictHelper`별도 구현: 특정 사용자(`ownerId`)의 의상 목록 캐시(`clothesByUser`)만 선택적으로 삭제, 캐시 동기화 시점(의상 등록, 수정, 삭제 등)에서 호출되어 최신 데이터 보장

    <img width="1739" height="871" alt="image" src="https://github.com/user-attachments/assets/f6f25a4c-772e-45b5-ac61-ef3f60de5139" />


  - 의상 삭제
    - 이미지도 함께 삭제

    <img width="893" height="655" alt="image" src="https://github.com/user-attachments/assets/e89886ff-7f9e-4afd-915d-74cfaeef84cd" />


    <img width="870" height="904" alt="image" src="https://github.com/user-attachments/assets/2ade961c-1686-4b1d-8826-067125849a86" />


  - 의상 추천(내부 알고리즘, ai 사용 추천)
    - 사용자의 프로필에 날씨 민감도로 체감온도 계산
      - 옷의 계절 속성으로 연관 옷 필터링 → 없으면 전체 의상 중 랜덤 추천
      - 사용자가 ai 사용 선택시 → 필터링된 의상 목록에서 llm 프롬프트 호출 → 추천
      - 미 선택시 → 내부 알고리즘에 의해 추천

    <img width="1556" height="949" alt="image" src="https://github.com/user-attachments/assets/3cf2a18b-9460-483f-a1ee-490ac7420904" />


    <img width="1607" height="998" alt="image" src="https://github.com/user-attachments/assets/60a9c768-c4bb-4146-b8ea-a09fc986afd0" />


- 알림
  - **Kafka**를 이용한 비동기 알림 이벤트 전송
    - 예: 팔로우, 댓글, 좋아요 등의 알림 이벤트 발행
  - **SSE (Server-Sent Events)** 기반 실시간 연결
    - 클라이언트가 SSE 스트림을 통해 실시간 알림 수신
    - 연결 복구 및 재시도 로직 포함

<img width="474" height="568" alt="image" src="https://github.com/user-attachments/assets/a1ea12ab-c3a1-4b4f-9f02-df3203fe51bf" />


<img width="472" height="437" alt="image" src="https://github.com/user-attachments/assets/eb36469e-6054-48c1-821f-5aa2805b7b65" />


---
---

## 📁 프로젝트 구조

```
src/
├── main/
│   ├── java/com/sprint/ootd5team/
│   │   ├── Ootd5TeamApplication.java
│   │   ├── base/
│   │   │   ├── config/ (Security, Kafka, Redis, Swagger, Async 등)
│   │   │   ├── batch/ (WeatherBatchConfig, Scheduler, Writer)
│   │   │   ├── llm/ (Gemini Provider, JSON Client)
│   │   │   ├── security/ (JWT, OAuth2, AuthController)
│   │   │   ├── websocket/ · sse/ · storage/ · util/
│   │   └── domain/
│   │       ├── clothes / clothesattribute / extract
│   │       ├── feed / comment / like / follow
│   │       ├── profile / user / oauthuser
│   │       ├── recommendation / weather / location
│   │       ├── notification / directmessage
│   │       └── recommendation/engine/mapper/dto
│   └── resources/
│       ├── application*.yaml
│       └──  schema.sql, clothes_data.sql, user_data.sql
└── test/
    └── java/com/sprint/ootd5team/ (통합 테스트 & TestConfig)
```

## ✨ 기능

### 핵심 기능
- [x] 사용자 관리 · OAuth2 · JWT 인증
- [x] 위치 기반 날씨 수집 및 알림
- [x] 의상 등록/속성/LLM 추출
- [x] 맞춤형 추천 + 코멘트 AI
- [x] 피드 CRUD + Elasticsearch 검색 + Kafka 이벤트
- [x] 실시간 DM(WebSocket/STOMP) & 실시간 알림(SSE)
- [x] 팔로우/좋아요/댓글 등 소셜 피처
- [x] AWS S3 파일 업로드 및 presigned URL

### 기술적 특징
- [x] 커서 기반 페이지네이션, QueryDSL 고급 쿼리
- [x] Kafka → Elasticsearch 동기화, Retry & Double-serialization handling
- [x] Redis 기반 캐시 + JWT Registry + SSE 메시지 저장
- [x] RabbitMQ STOMP Relay (prod) / SimpleBroker (dev) 자동 전환
- [x] Spring Batch + Scheduler, Notification decision 엔진
- [x] Docker 멀티스테이지 + Buildx + GitHub Actions + ECS 배포

## 📖 구현 홈페이지
🔗 https://55td.duckdns.org/

## 💌 프로젝트 회고록
🔗 [팀 노션](https://www.notion.so/ohgiraffers/55TD-207649136c1180288dabfeb1877485d6?source=copy_link)
🔗 [발표 자료](https://drive.google.com/file/d/10nTVTT13koPe5zJF0aWOdD5LMjNybvk3/view?usp=sharing)
🔗 [시연 영상](https://drive.google.com/file/d/1zPzq6EoEPq4HssuyONM1vE1W8i6DDP9V/view?usp=sharing)


---
추가 자료(ERD, API 상세 명세, 운영 Runbook)는 Notion 문서에 정리되어 있습니다.
