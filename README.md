# Mockstalk
**Spring Boot 기반 모의 주식 투자 서비스**
가상 자산으로 주식을 주문하고, 시세가 조건에 맞을 때 체결까지 반영하는 모의 투자 서비스입니다.
실시간 주식 데이터를 활용해 사용자가 가상의 자산으로 주식을 거래하고, 주문부터 체결 및 자산 관리까지 경험할 수 있는 모의 투자 서비스입니다.
이 저장소에서 제가 맡은 핵심은 주문 체결입니다. 같은 지정가 주문에 시세 이벤트가 겹치면 체결이 여러 번 저장될 수 있었습니다. 주문 단위 Redisson 락을 잡고, 락 안에서 DB 상태를 다시 읽은 뒤, 주문 상태와 Trade를 한 트랜잭션으로 저장합니다. 커밋이 끝난 뒤에 락을 해제합니다.
주문 유형에 따른 체결 처리, Redis 기반 데이터 관리, RabbitMQ를 활용한 비동기 메시지 처리 등을 구현했습니다.
| 항목 | 내용 |
|---|---|
| 기간 | 2025.05 ~ 2025.07 |
| 팀 | 6인. 백엔드와 프론트엔드를 나눠 개발 |
| 제가 맡은 역할 | 팀장, 주문·체결 백엔드 |
| 서버 | Java 17, Spring Boot 3.5, MySQL, Redis, RabbitMQ, Redisson |
<br>
## 1. 프로젝트 소개
## 1. Project Overview
Mockstalk은 사용자가 계좌를 만들고 시세를 확인한 뒤, 시장가 또는 지정가로 모의 매수·매도를 하는 서비스입니다. 체결 내역과 보유 종목, 관심 종목, 종목 게시글, 뉴스 검색을 함께 제공합니다. 시세와 뉴스에는 한국투자증권 Open API와 네이버 뉴스 검색 API를 사용합니다.
### 프로젝트 소개
주문 API가 요청을 저장하는 것만으로는 체결이 끝나지 않습니다. 시장가는 저장 직후 현재가로 체결하고, 지정가는 이후 시세가 조건을 만족할 때 체결합니다. 같은 주문을 두 경로가 동시에 처리하면 Trade가 중복될 수 있어, 체결 시점의 동시성 제어가 필요했습니다.
Mockstalk은 한국투자증권 Open API와 네이버 뉴스 검색 API를 연동한 모의 주식 투자 서비스입니다.
주문 상태 이름은 직관과 다릅니다. 지정가 대기 주문은 `PENDING`이 아니라 `COMPLETED`로 저장합니다. 체결이 끝나면 `SETTLE`입니다. `PENDING`과 `CANCELED`도 상태 값으로 있습니다.
사용자는 계좌를 생성하고 주식 시세를 확인한 뒤 시장가 및 지정가 주문을 통해 모의 거래를 진행할 수 있습니다. 체결 내역과 보유 종목을 조회할 수 있으며, 관심 종목과 커뮤니티 등 투자에 필요한 기능도 제공합니다.
## 2. 주요 기능
단순히 주문 요청을 저장하는 데 그치지 않고, 주문 상태에 따른 체결 처리와 계좌 잔고 반영까지 이어지는 백엔드 흐름을 구현하는 데 중점을 두었습니다.
서비스 전체 기능은 다음과 같습니다. 주문과 체결을 제외한 회원, 차트, 커뮤니티, 뉴스는 팀 기능입니다.
| 항목 | 내용 |
|---|---|
| 프로젝트명 | Mockstalk |
| 프로젝트 유형 | 모의 주식 투자 서비스 |
| 개발 기간 | 2025.05 ~ 2025.07 |
| 팀 구성 | 6인 |
| 담당 역할 | 팀장 및 백엔드 개발 |
| 주요 기술 | Java, Spring Boot, MySQL, Redis, RabbitMQ, Redisson |
- 회원가입, 로그인, JWT 발급과 재발급, 로그아웃 시 토큰 블랙리스트
- 한국투자증권 Open API로 현재가와 차트 데이터를 조회
- 시장가·지정가 매수와 매도, 체결 내역 조회
- 계좌, 잔고, 보유 종목, 수익률 랭킹
- 관심 종목, 종목 게시글과 댓글, 네이버 뉴스 검색
### 주요 기능
## 3. 기술 스택 및 선택 이유
- **회원 및 인증:** 회원가입, 로그인, JWT 기반 인증 및 토큰 재발급
- **주식 데이터:** 한국투자증권 Open API 기반 시세 및 차트 데이터 조회
- **주문 및 체결:** 시장가·지정가 매수/매도 주문과 체결 내역 관리
- **계좌 관리:** 계좌 생성, 잔고 및 보유 종목 조회
- **자산 랭킹:** 사용자별 수익률을 기준으로 랭킹 제공
- **관심 종목:** 계좌별 관심 종목 등록 및 관리
- **커뮤니티:** 종목별 게시글과 댓글
- **뉴스:** 네이버 뉴스 검색 API 연동
기술을 고른 이유는 체결 경로에서 맡은 역할입니다.
<br>
| 기술 | 이 프로젝트에서 쓴 이유 |
|---|---|
| Spring Boot 3.5, Java 17 | REST API와 주문 서비스를 구성 |
| Spring Data JPA, QueryDSL | 주문·체결 저장과 커서 페이지 조회 |
| MySQL 8 | 사용자, 계좌, 주문, 체결의 기준 데이터 |
| Redis | 현재가, 종목 ID 매핑, 지정가 대기 주문. DB를 매 시세마다 전체 조회하지 않기 위해 사용 |
| RabbitMQ | 시세 수신과 체결 판단을 분리. 수신 스레드가 체결 트랜잭션을 직접 끝내지 않게 함 |
| Redisson | 주문 ID마다 락을 걸어, 같은 주문의 체결이 겹치지 않게 함 |
| JUnit 5, Mockito, Spring Boot Test | 단위 테스트 |
| H2 | 테스트 프로파일의 기본 DB. 운영 MySQL 설정을 바꾸지 않음 |
| Testcontainers | 체결 통합 테스트에서 MySQL 8.0, Redis 7, RabbitMQ 3-management를 실제 기동 |
## 2. System Architecture
## 4. 전체 시스템 아키텍처
### 전체 시스템 구성
아래는 팀 서비스 전체입니다. 제가 구현하고 통합 테스트로 확인한 구간은 시세 이후의 주문 체결과 락입니다.
서비스는 Spring Boot 애플리케이션을 중심으로 MySQL, Redis, RabbitMQ를 연결하는 구조로 구성했습니다.
외부 API를 통한 주식 데이터 수집과 애플리케이션 내부의 주문·체결 처리를 분리하고, Redis를 이용해 시세와 주문 관련 데이터를 관리합니다.
```mermaid
flowchart TB
    USER["Client"]
    API["Spring Boot Application"]
    subgraph client [Client]
        USER[사용자]
    end
    USER --> API
    subgraph APP["Backend"]
        AUTH["Auth / User"]
        STOCK["Stock / Price"]
        ORDER["Order"]
        TRADE["Trade"]
        ACCOUNT["Account / Holdings"]
        LISTENER["StockPriceListener"]
    subgraph app [Spring Boot]
        AUTH[회원 / JWT]
        STOCK[시세 / 차트]
        ORDER[주문]
        TRADE[체결]
        ACCOUNT[계좌 / 보유]
        NEWS[뉴스]
    end
    API --> AUTH
    API --> STOCK
    API --> ORDER
    API --> ACCOUNT
    USER --> AUTH
    USER --> STOCK
    USER --> ORDER
    USER --> ACCOUNT
    USER --> NEWS
    subgraph INFRA["Data & Messaging"]
        MYSQL[("MySQL")]
        REDIS[("Redis")]
        RABBIT["RabbitMQ"]
    end
    KIS[한국투자증권 Open API]
    NAVER[네이버 뉴스 API]
    KIS --> STOCK
    NAVER --> NEWS
    ORDER --> REDIS
    ORDER --> MYSQL
    ORDER --> MYSQL[(MySQL)]
    TRADE --> MYSQL
    ACCOUNT --> MYSQL
    STOCK --> REDIS[(Redis)]
    ORDER --> REDIS
    ACCOUNT --> REDIS
    RABBIT --> LISTENER
    LISTENER --> TRADE
    KIS["한국투자증권 API / WebSocket"]
    NAVER["네이버 뉴스 API"]
    KIS --> STOCK
    STOCK --> REDIS
    NAVER --> API
```
> 위 다이어그램은 전체 구성 요소를 나타낸 개략도입니다. 시세 수신 후 지정가 체결과 시장가 주문 경로는 아래에서 구분합니다.
인증, 차트, 뉴스, 커뮤니티, 랭킹은 이 그림의 주변 기능입니다. 체결의 실제 호출 순서는 다음 절의 다이어그램이 기준입니다.
### 주문 및 체결 데이터 흐름
## 5. 시세 수신부터 지정가 주문 체결까지
**시장가 주문**
```mermaid
flowchart LR
    WS[KoreaWebSocketClient]
