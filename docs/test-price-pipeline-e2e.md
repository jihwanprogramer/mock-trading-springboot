# Test price pipeline — Redis + RabbitMQ verification

Manual/compose E2E for: `POST /api/test/price` → Redis `stockPrice::{code}` → Rabbit `stock.exchange` / `stock.price` → `StockPriceListener` → `TradeService.onPriceUpdated`.

Security note: `/api/test/price` is currently `permitAll` on all profiles (tracked separately; do not change here).

## Prerequisites

- Docker + Docker Compose
- JDK 17 (or compatible toolchain)
- Env for MySQL app user: `DB_USERNAME`, `DB_PASSWORD` (and optional JWT/Hantu/Naver vars — compose has defaults for JWT)

## Alignments (code evidence)

| Concern | Value | Consumers |
|--------|--------|-----------|
| Redis key | `stockPrice::{stockCode}` | `TestPricePublishService`, `KoreaWebSocketClient`, `LivePriceService`, `MarketOrderService`, `AccountService` |
| Exchange | `stock.exchange` (`RabbitConfig.EXCHANGE_NAME`) | publisher + config |
| Routing / queue | `stock.price` (`ROUTING_KEY` / `QUEUE_NAME`) | publisher + `StockPriceListener` |
| DTO | `StockPriceEventDto(stockId, currentPrice)` | publisher → Jackson AMQP → listener → `onPriceUpdated(stockId, currentPrice)` |

Known unrelated mismatch: `RealtimePriceController` still reads `stockPrice:` (single colon) — not on this write path.

## Compose runbook

```bash
# from repo root
export DB_USERNAME=mockstalk
export DB_PASSWORD=mockstalk

# infra only (faster) or full stack
docker compose up -d mysql redis rabbitmq
# wait for healthy ports 3306 / 6379 / 5672

# run app locally against compose services (prefer local profile for plain Rabbit)
export DB_HOST=127.0.0.1 DB_PORT=3306 DB_NAME=mockstalk
export REDIS_HOST=127.0.0.1 REDIS_PORT=6379
export RABBITMQ_HOST=127.0.0.1 RABBITMQ_PORT=5672
export RABBITMQ_USERNAME=guest RABBITMQ_PASSWORD=guest
export JWT_SECRET=local-dev-jwt-secret-change-me
export ACCOUNT_JWT_SECRET=local-dev-account-jwt-secret
export HANTU_APPKEY=x HANTU_APPSECRET=x HANTU_ACCESS_TOKEN=x
export NAVER_CLIENT_ID=x NAVER_CLIENT_SECRET=x

./gradlew bootRun --args='--spring.profiles.active=local'
```

`application-local.properties` sets `spring.rabbitmq.ssl.enabled=false` (compose Rabbit is plain AMQP).

### Exercise pipeline

1. Ensure a stock row exists (note `id` and `stock_code`).
2. Optionally place a limit order so `completeOrders:{stockId}` is populated (matching path).
3. Publish test price:

```bash
curl -sS -X POST "http://localhost:8081/api/test/price?stockId=<id>&currentPrice=10000"
```

4. Check Redis:

```bash
docker exec -it mockstalk-redis redis-cli GET "stockPrice::<stock_code>"
```

5. Check Rabbit / matching: management UI `http://localhost:15672` (guest/guest) queue `stock.price`; if a pending limit order was cached, confirm settle via API/DB.

## Automated IT (Testcontainers)

```bash
./gradlew test --tests 'com.example.mockstalk.domain.trade.TestPricePipelineIT'
```

- Starts Redis 7 + RabbitMQ 3 containers.
- Runs `TestPricePublishService` → asserts Redis key → asserts `StockPriceListener` invokes `TradeService.onPriceUpdated`.
- Does **not** rewrite matching logic.
- Uses `@Testcontainers(disabledWithoutDocker = true)` — **skips** when Docker is missing.

Unit coverage (no Docker): `TestPricePublishServiceTest`, `StockPriceListenerTest`, `TradeServiceOnPriceUpdatedTest`.
