## AIBE2 Project2 Team2 — 고수의 민족

### 팀 소개
<a href="https://github.com/prgrms-aibe-devcourse/AIBE2-Project2-Team2/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=prgrms-aibe-devcourse/AIBE2-Project2-Team2" />
</a>

#### 팀원

| 문선우 | 김민규 | 이동현 | 김기현 | 임하빈 |
|:------:|:------:|:------:|:------:|:------:|
| <img src="https://avatars.githubusercontent.com/msw-Hub" width="100"/> | <img src="https://avatars.githubusercontent.com/Gyuuuuuuu" width="100"/> | <img src="https://avatars.githubusercontent.com/haehdgus" width="100"/> | <img src="https://avatars.githubusercontent.com/FIFLove" width="100"/> | <img src="https://avatars.githubusercontent.com/heavenzhr16" width="100"/> |
| [@msw-Hub](https://github.com/msw-Hub) | [@Gyuuuuuuu](https://github.com/Gyuuuuuuu) | [@haehdgus](https://github.com/haehdgus) | [@FIFLove](https://github.com/FIFLove) | [@heavenzhr16](https://github.com/heavenzhr16) |

---

### 프로젝트 개요
- 전문가와 사용자를 연결하는 컨텐츠 마켓플레이스입니다. 전문가가 컨텐츠(서비스)를 등록/수정/삭제하고, 사용자는 카테고리/검색을 통해 탐색하고 매칭·결제를 통해 이용합니다.
- 결제는 카카오페이 연동, 인증은 JWT 기반, 파일 업로드는 Firebase Storage를 사용합니다.

---

### 주요 기능
- 컨텐츠: 생성/수정/삭제, 상세/목록 조회, 이미지(일괄 업로드/삭제/전체 수정)
- 카테고리: 자기참조 트리 기반 n-레벨 카테고리, 트리 조회 및 리프 ID 조회
- 매칭/견적: 매칭 생성·상태 변경, 견적 산출/조회
- 결제: 카카오페이 Ready → Approve → Cancel 플로우 및 결제/매칭 상태 동기화
- 인증/인가: JWT 로그인/검증, Redis로 토큰 상태 관리(JTI/블랙리스트)
- 채팅: STOMP(WebSocket) 기반 메시징(핸드셰이크 JWT 검증, 로깅)
- 마이페이지: 매칭 이력/결제 이력/내 정보
- 검색/리뷰/신고/알림: 검색 API, 후기 작성, 신고 접수, 메일 발송 등

---

### 기술 스택
- Backend: Spring Boot 2.7.14 (Java 11)
  - spring-boot-starter-web, security, validation, data-jpa
  - QueryDSL 5.x
  - Redis (spring-boot-starter-data-redis)
  - WebSocket (spring-boot-starter-websocket), STOMP
  - JWT: jjwt 0.12.x
  - OpenFeign (카카오 OAuth/프로필)
  - Firebase Admin SDK (Storage)
  - MySQL, H2(러닝타임 프로파일에 따라)
  - springdoc-openapi-ui 1.7.0
- Frontend: React 19 + Vite 7
  - react-router-dom 7, axios, zustand, dayjs/date-fns
  - @stomp/stompjs, react-hot-toast, framer-motion, lucide-react
  - Tailwind CSS 4 (+ @tailwindcss/vite)

배지(요약)

| 구분 | 기술 |
| :--- | :--- |
| **Backend** | ![Java](https://img.shields.io/badge/Java%2011-007396?style=for-the-badge&logo=openjdk&logoColor=white) ![Spring%20Boot](https://img.shields.io/badge/Spring%20Boot%202.7-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white) ![Spring%20Security](https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white) ![JPA](https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white) ![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white) ![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white) ![WebSocket](https://img.shields.io/badge/WebSocket-010101?style=for-the-badge&logo=socketdotio&logoColor=white) ![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white) ![OpenFeign](https://img.shields.io/badge/OpenFeign-0F2B46?style=for-the-badge) ![Firebase](https://img.shields.io/badge/Firebase%20Admin-FFCA28?style=for-the-badge&logo=firebase&logoColor=black) ![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black) |
| **Frontend** | ![React](https://img.shields.io/badge/React%2019-61DAFB?style=for-the-badge&logo=react&logoColor=black) ![Vite](https://img.shields.io/badge/Vite%207-646CFF?style=for-the-badge&logo=vite&logoColor=white) ![TailwindCSS](https://img.shields.io/badge/TailwindCSS%204-38B2AC?style=for-the-badge&logo=tailwindcss&logoColor=white) ![React%20Router](https://img.shields.io/badge/React%20Router%207-CA4245?style=for-the-badge&logo=reactrouter&logoColor=white) ![Axios](https://img.shields.io/badge/Axios-5A29E4?style=for-the-badge&logo=axios&logoColor=white) ![Zustand](https://img.shields.io/badge/Zustand-000000?style=for-the-badge) ![STOMP](https://img.shields.io/badge/STOMP-000000?style=for-the-badge) ![Framer%20Motion](https://img.shields.io/badge/Framer%20Motion-0055FF?style=for-the-badge&logo=framer&logoColor=white) ![Lucide](https://img.shields.io/badge/Lucide-000000?style=for-the-badge) ![dayjs](https://img.shields.io/badge/day.js-FE5196?style=for-the-badge) |
| **외부 서비스** | ![KakaoPay](https://img.shields.io/badge/KakaoPay-FFEB00?style=for-the-badge&logoColor=black) ![Kakao%20OAuth](https://img.shields.io/badge/Kakao%20OAuth-FFCD00?style=for-the-badge) ![Firebase%20Storage](https://img.shields.io/badge/Firebase%20Storage-FFCA28?style=for-the-badge&logo=firebase&logoColor=black) ![Gmail%20SMTP](https://img.shields.io/badge/Gmail%20SMTP-EA4335?style=for-the-badge&logo=gmail&logoColor=white) |
| **문서화/도구** | ![Apache%20Maven](https://img.shields.io/badge/Apache%20Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white) ![OpenAPI%2FSwagger](https://img.shields.io/badge/OpenAPI%2FSwagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black) ![ESLint](https://img.shields.io/badge/ESLint-4B32C3?style=for-the-badge&logo=eslint&logoColor=white) |

---

### 시스템 아키텍처
- Controller → Service → Repository → Entity/DTO의 레이어드 구조
- JWT + Redis로 인증 상태 관리, Kakao/OpenFeign, Firebase, WebSocket(STOMP) 채팅

---

### 폴더 구조
```
backend/
  src/main/java/org/example/backend/
    content/        # 컨텐츠 CRUD/이미지
    category/       # 카테고리 트리 API
    payment/        # 카카오페이 서비스/컨트롤러
    matching/       # 매칭 도메인(컨트롤러/서비스)
    Estimate/       # 견적(Estimate) 컨트롤러/서비스/DTO
    matchinghistory/# 매칭 히스토리(사용자/전문가 별 이력)
    mypage/         # 마이페이지(매칭/결제 이력/내 정보)
    search/         # 검색 API
    review/         # 후기(리뷰)
    report/         # 신고
    expert/         # 전문가 등록/프로필/포트폴리오
    login/          # 로그인/회원가입
    openFeign/      # 카카오 OAuth/유저 정보 조회 Feign Client
    notification/   # 메일 등 알림
    redis/          # Redis 서비스 (토큰 관리)
    chat/           # STOMP 핸드셰이크/인터셉터/이벤트
    jwt/            # JWT 유틸/필터
    config/         # Security, Firebase 등 설정
    repository/     # JPA 리포지토리
    entity/         # 엔티티(카테고리 자기참조 포함)
  src/main/resources/application.properties

frontend/
  src/
    router/         # 라우팅/페이지(컨텐츠 작성·수정·결제·채팅·마이페이지 등)
    components/     # UI 컴포넌트
    lib/            # axios, 유틸
  package.json
```

---

### 백엔드 설정
- 애플리케이션 프로퍼티 예시(`backend/src/main/resources/application.properties`)
  - DB: `jdbc:mysql://localhost:3311/team2` (user: `root`, password: `team2`)
  - Redis: `localhost:6380`
  - JWT: `jwt.secret`
  - Kakao OAuth: `oauth.kakao.client-id`, `oauth.kakao.redirect-uri`
  - KakaoPay: `kakao.pay.secret-key`, `kakao.pay.cid`
  - Mail(SMTP): Gmail 설정

보안 권장: 운영/개발별 외부화된 설정 사용을 권장합니다.
- `backend/src/main/resources/application-local.properties`(예시)로 분리하고, `--spring.profiles.active=local`로 실행
- 또는 환경변수/OS 별 Secret Manager 사용

예시(application-local.properties 샘플)
```
jwt.secret=변경필수_충분히_긴_랜덤_시크릿

spring.datasource.url=jdbc:mysql://localhost:3311/team2?serverTimezone=Asia/Seoul&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=team2

spring.redis.host=localhost
spring.redis.port=6380

oauth.kakao.client-id=YOUR_CLIENT_ID
oauth.kakao.redirect-uri=http://localhost:8080/api/auth/kakao/callback

kakao.pay.secret-key=YOUR_DEV_OR_SECRET_KEY
kakao.pay.cid=TC0ONETIME

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=YOUR_GMAIL
spring.mail.password=YOUR_APP_PASSWORD
```

---

#### Firebase 설정
- 자격증명 파일을 `backend/config/team2maldive-firebase-adminsdk-fbsvc-a8049c6c52.json` 경로에 배치
- 버킷명은 `team2maldive.firebasestorage.app`로 설정되어 있으므로 환경에 맞게 수정 가능(`FirebaseConfig`)

---

### 실행 방법
사전 요구사항
- Java 11, Maven
- Node.js (LTS), pnpm/npm
- MySQL(로컬 3311 포트, DB: team2), Redis(6380 포트)

백엔드
```
cd backend
mvn spring-boot:run
```
- 기본 포트: 8080
- Swagger UI: http://localhost:8080/swagger-ui.html

프론트엔드
```
cd frontend
npm install
npm run dev
```
- 기본 포트: 5173

---

### 주요 API 요약(발췌)
- 컨텐츠(`ContentController`, base: `/api/content`)
  - POST `/` 컨텐츠 등록 (전문가 권한)
  - GET `/` 전체 목록 조회
  - GET `/{id}` 상세 조회
  - PUT `/{id}` 수정 (작성자/관리자)
  - DELETE `/{id}` 삭제 (작성자/관리자)
  - POST `/{contentId}/images/batch` 이미지+썸네일 일괄 업로드 (multipart)
  - DELETE `/images` 이미지 일괄 삭제
  - PUT `/{contentId}/images` 이미지 전체 수정

- 카테고리(`CategoryController`, base: `/api/categories`)
  - GET `/tree` 전체 트리 조회
  - GET `/{categoryId}/leaf-category-ids` 특정 노드 하위의 리프 카테고리 ID 목록

- 결제(카카오페이, base: `/api/payment`)
  - POST `/kakao/ready` 파라미터: `matchingId`, `userId` → Ready 응답(tid/결제URL 등)
  - GET `/kakao/success` 파라미터: `pg_token`, `matchingId` → 승인 처리 및 결제 상태 갱신
  - POST `/kakao/cancel` 파라미터: `matchingId`, `reason?` → 결제 취소 및 상태 갱신

---

### 인증/보안
- JWT 로그인 엔드포인트: `/api/auth/login`
- 공개 엔드포인트: Swagger, `/api/categories/**`, GET `/api/content/**`, `/api/auth/kakao/callback`, `/ws/**` 등(`SecurityConfig` 참고)
- CORS 허용: `http://localhost:5173`, `http://localhost:8080`, `http://localhost:3000` 등

---

### WebSocket(STOMP)
- 핸드셰이크 시 JWT 검증: 쿠키 `token` 또는 쿼리파라미터 `token`로 전달(`JwtHandshakeInterceptor`)
- 로깅 인터셉터 활성화(`WebSocketLoggingInterceptor`)
- 주의: 실제 STOMP endpoint/broker 설정 클래스는 환경에 맞춰 구성 필요

---

### 아키텍처
- 백엔드 레이어드
  - Controller → Service → Repository → Entity/DTO
  - Security: `JwtLoginFilter`(로그인), `JwtFilter`(요청 검증), `SecurityConfig`(CORS/인가 규칙)
  - Token 상태: `RedisService`로 JTI/활성 토큰/블랙리스트 관리
  - 결제: `KakaoPayService`에서 Ready/Approve/Cancel 호출, `Payment` 엔티티/`Matching` 상태 동기화
  - 파일: `FirebaseImageService`로 Storage 업로드
  - 카테고리: `Category` 자기참조 구조, `CategoryService` 트리/리프 수집
- 프론트엔드
  - React Router 기반 페이지 구성, 전역 상태는 `zustand`, 통신은 `axios`
  - 컨텐츠 작성/수정 스텝퍼, 결제 페이지, 채팅, 마이페이지 등 도메인 라우팅 분리

---

### 트러블슈팅 경험
- 컨텐츠 API 응답 가공
  - 프론트 요구 스키마에 맞춘 Response DTO 설계 및 서비스 계층에서 변환
  - 연관 엔티티 조합 시 N+1 방지 및 필요 데이터만 선별
- 카테고리 트리 설계
  - 1/2/3차 테이블 분리 대신 자기참조(`Category.parent`, `children`)로 n-레벨 확장성 확보
  - 트리 조회/리프 수집 API로 프론트 필터링/검색에 대응
- 카카오페이 연동
  - Ready → Approve → Cancel 플로우, `tid` 기반 Payment 엔티티 상태 관리
  - 승인 성공 시 매칭 상태와 결제 상태 동기화
- JWT + Redis 토큰 관리
  - JTI/이메일 기준 활성 토큰 세트 저장/블랙리스트 처리

---

### 향후 개선 계획
- 환경변수/시크릿 관리 표준화(.properties 외부화, 프로필 분리, Vault/Parameter Store)
- WebSocket endpoint/broker 정식 구성 및 채팅 메시지 저장/조회 API 보강
- 통합 테스트/슬라이스 테스트 확충, Testcontainers로 MySQL/Redis 통합 테스트
- 카테고리/검색 성능 개선(계층쿼리 최적화, 인덱스, 캐싱)
- 결제 예외/리트라이/서킷브레이커(Resilience4j) 적용
- CI/CD 파이프라인 구축 및 품질 게이트(ESLint/Spotless/Checkstyle)

---

### 개발/테스트
- 백엔드 테스트: `mvn test`
- 프론트 빌드: `npm run build`

---

### 주의 사항
- 민감정보(`jwt.secret`, `kakao.*`, `spring.mail.*`, Firebase 자격증명)는 반드시 외부 설정으로 분리하세요.

---

### 프로젝트 기간
- 2025년 7월 15일 ~ 28일
---

### 라이선스
사내/과제용 저장소로 별도 라이선스 명시가 없으며, 팀 내 활용을 전제로 합니다.
