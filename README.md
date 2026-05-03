# Event-Driven Microservices with Kotlin, Spring Boot, and Apache Kafka

Demo project for the JetBrains blog tutorial
**"Building Event-Driven Microservices with Kotlin and Apache Kafka"**.

The project follows a single e-commerce scenario — processing customer orders
across three independent services — and evolves it through three branches, each
corresponding to a section of the tutorial.


## The Scenario

An e-commerce backend receives orders and needs to coordinate between three services:

- **Order Service** — accepts incoming orders via REST
- **Inventory Service** — reserves stock for each confirmed order
- **Notification Service** — sends a confirmation to the customer

The tutorial starts with the services talking to each other synchronously over HTTP,
then introduces Apache Kafka to decouple them, and finally improves the Kafka
integration for production-readiness.

## Branch Guide

### `starter` — Synchronous REST

```
POST /orders
     │
     ├─── RestClient ──► POST /inventory/reserve   (Inventory Service :8081)
     │                          ↓ success / failure
     └─── RestClient ──► POST /notifications        (Notification Service :8082)
```

The Order Service calls the other two services **synchronously and in sequence**
before returning a response to the caller. This is the starting point that the
tutorial uses to illustrate the problem: tight coupling, cascading failures under
load, and latency that compounds as you add more downstream services.

Key files to read:

- [`order-service/src/main/kotlin/com/example/orderservice/service/OrderService.kt`](order-service/src/main/kotlin/com/example/orderservice/service/OrderService.kt) — orchestrates the synchronous calls
- [`order-service/src/main/kotlin/com/example/orderservice/client/InventoryClient.kt`](order-service/src/main/kotlin/com/example/orderservice/client/InventoryClient.kt) — Spring `RestClient` wrapper
- [`order-service/src/main/kotlin/com/example/orderservice/client/NotificationClient.kt`](order-service/src/main/kotlin/com/example/orderservice/client/NotificationClient.kt) — Spring `RestClient` wrapper


## Prerequisites

| Tool | Version |
|------|---------|
| JDK | 25+ |
| Docker & Docker Compose | any recent version |

No Kafka installation needed. Docker Compose handles it.


## Running the code

The starter requires no Docker. All three services talk over plain HTTP. The other two branches require running `docker compose up` to start the Kafka container.

### 1. Check out the branch

```bash
git checkout starter
```

### 2. Start the services (three separate terminals)

```bash
# Terminal 1 — Inventory Service must start first (Order Service calls it)
./gradlew :inventory-service:bootRun

# Terminal 2
./gradlew :notification-service:bootRun

# Terminal 3
./gradlew :order-service:bootRun
```

### 3. Place a test order

```bash
curl -s -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":"CUST-001","productId":"PRODUCT-001","quantity":5}' \
  | jq .
```

### 4. Try an out-of-stock order

```bash
curl -s -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":"CUST-001","productId":"PRODUCT-001","quantity":999}' \
  | jq .
```