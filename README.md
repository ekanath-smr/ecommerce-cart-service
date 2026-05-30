# 🛒 Cart Service – E-Commerce Microservice

## 📌 Overview

The **Cart Service** is a core microservice in a distributed e-commerce system responsible for managing user carts, handling item operations, validating stock before checkout, and coordinating order creation.

It is designed with **strong consistency at the cart level**, **fault-tolerant communication**, **service discovery**, **load-balanced inter-service communication**, and **production-grade concurrency handling**.

---

## 🔗 Links

* 📂 GitHub Repository: https://github.com/ekanath-smr/ecommerce-cart-service
* 📘 Swagger UI: http://localhost:8060/swagger-ui/index.html
* 📄 OpenAPI Docs: http://localhost:8060/v3/api-docs

---

## 🚀 Key Features

### 🛒 Cart Management

* Create or fetch active cart per user
* Add items to cart with price snapshot
* Update item quantity
* Remove items from cart
* Clear entire cart

### 💰 Pricing & Snapshot Handling

* Stores product name & price snapshot at time of addition
* Prevents inconsistencies due to price changes
* Maintains accurate subtotal and total price calculations

### ⚙️ Intelligent Cart Operations

* Automatic cart creation if not present
* Recalculation of totals on every modification

### 🌐 Cloud-Native Architecture

* Service registration with Eureka Service Discovery
* Dynamic service lookup without hardcoded endpoints
* API Gateway integration for centralized routing
* Client-side load balancing across multiple service instances

---

## 🔐 Security & Access Control

* JWT-based authentication
* Role-Based Access Control (RBAC)

| Role  | Permissions                      |
| ----- | -------------------------------- |
| USER  | Manage cart, checkout            |
| ADMIN | Extendable for future operations |

---

## 🔗 Inter-Service Communication

* **Product Service** → Fetch product details
* **Inventory Service** → Validate stock before adding/updating items
* **Order Service** → Create order during checkout
* Service discovery via Eureka
* Client-side load balancing for distributed deployments

**Design Principle:**

* Cart Service acts as an **orchestrator**
* Other services act as **source of truth**
* Service-to-service communication is resolved dynamically through Eureka

---

## ⚡ Fault Tolerance & Resilience

Integrated **Resilience4j**:

* Circuit Breaker → Prevents cascading failures
* Retry → Handles transient failures (except checkout)
* Service Discovery → Removes hardcoded service dependencies
* Client-Side Load Balancing → Distributes traffic across healthy service instances

---

## 🧠 Consistency & Distributed Design

### Strong Consistency

* Stock validation before adding/updating items
* Transactional updates within cart operations

### Checkout Strategy (Orchestration + Idempotency)

Checkout flow:

1. Validate cart (non-empty, active)
2. Create order via Order Service
3. Use idempotency key: `cart-{cartId}-v{version}`
4. On success:

   * Clear cart
5. On failure:

   * Propagate stock errors
   * Handle service failures gracefully

*Note: Designed for future migration to event-driven Saga using Kafka.*

### Service Discovery & Routing

Request Flow:

Client
→ API Gateway
→ Cart Service
→ Eureka Service Discovery
→ Product / Inventory / Order Services

Benefits:

* Dynamic service registration
* Load-balanced communication
* Easier horizontal scaling
* Reduced operational coupling

---

## 🏗️ Tech Stack

* Java 21
* Spring Boot
* Spring Security (JWT + RBAC)
* Spring Data JPA (Hibernate)
* MySQL
* OpenFeign
* Spring Cloud Eureka Client
* Spring Cloud Gateway
* Spring Cloud LoadBalancer
* Resilience4j
* Spring Retry
* Lombok
* Maven
* Swagger / OpenAPI

---

## 📂 Project Structure

```text
src/main/java/com/example/ecommerce_cart_service
├── controllers
├── services
├── clients
├── repositories
├── models
├── dtos
├── mappers
├── security
├── advices
└── configs

src/test/java
└── services
```

---

## 🔄 API Endpoints

### Cart APIs

```http
GET    /cart
POST   /cart/items
PUT    /cart/items/{productId}
DELETE /cart/items/{productId}
DELETE /cart
```

### Checkout

```http
POST   /cart/checkout
```

---

## ⚙️ Configuration

### Database

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/cartService
spring.jpa.hibernate.ddl-auto=update
```

### External Services

```properties
product.service.url=http://localhost:8080
inventory.service.url=http://localhost:8070
order.service.url=http://localhost:8090
```

### Resilience4j

```properties
resilience4j.circuitbreaker.instances.productService.failureRateThreshold=50
resilience4j.retry.instances.productService.maxAttempts=3
```

---

## 🛡️ Error Handling

Centralized using `@RestControllerAdvice`.

Handles:

* CartItemNotFoundException
* ProductNotFoundException
* InsufficientStockException
* CartAlreadyCheckedOutException
* InvalidCartOperationException
* ExternalServiceUnavailableException

---

## 📈 Logging Strategy

| Level | Usage               |
| ----- | ------------------- |
| INFO  | Business operations |
| WARN  | Invalid operations  |
| ERROR | Failures            |

---

## 🔄 Example Checkout Flow

1. User initiates checkout
2. Cart validated
3. Order Service called with idempotency key
4. On success → cart cleared
5. On failure → error propagated

---

## 🚧 Future Enhancements

* Kafka-based event-driven checkout (Saga orchestration)
* Distributed tracing (Zipkin)
* Centralized logging (ELK)
* Redis caching for cart reads
* Rate limiting
* Docker & Kubernetes deployment
* Monitoring with Prometheus & Grafana

---

## 💡 Design Highlights

* Snapshot-based pricing model
* Idempotent checkout design
* Service Discovery using Eureka
* API Gateway-based routing
* Client-side load balancing
* Retry handling for concurrency issues
* Clean separation of concerns (clients, services, mappers)
* Production-grade logging and exception handling

---

## 🏆 Resume Highlights

* Designed Cart microservice using Spring Boot & MySQL
* Implemented JWT-based authentication and RBAC
* Built Feign-based inter-service communication
* Integrated Eureka Service Discovery
* Implemented API Gateway routing and client-side load balancing
* Integrated Resilience4j (Circuit Breaker + Retry)
* Built idempotent checkout mechanism
* Implemented stock validation via Inventory Service
* Ensured transactional consistency using Spring @Transactional
* Added optimistic locking and retry handling for concurrent cart updates

---

## 🧑‍💻 Author

**Ekanath S M R**
Backend Engineer | Java + Spring Boot Developer

GitHub:
https://github.com/ekanath-smr
