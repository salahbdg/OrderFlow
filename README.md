# OrderFlow
E-Commerce Order Management System

## Description

Customers place orders, inventory is reserved, payments are processed, and order status is tracked in real-time. Each step publishes Kafka events consumed by downstream services.
Why it's a great learning project:

Order lifecycle (PENDING → CONFIRMED → SHIPPED → DELIVERED) is a perfect state machine — great for domain modeling
Multiple bounded contexts (Orders, Inventory, Payments, Notifications) that communicate exclusively via Kafka events — this is how microservices actually work
Angular frontend can display live order status using polling or WebSockets
Forces you to handle eventual consistency and saga patterns

Real-world equivalent: What engineers build at Amazon, Shopify, or any logistics company

## Dev Phases

Phase 1 → System Design & Architecture Diagram
Phase 2 → Hexagonal folder structure (explained layer by layer)
Phase 3 → Domain model (entities, value objects, domain events)
Phase 4 → Application layer (use cases / ports)
Phase 5 → Infrastructure layer (JPA, Kafka, REST adapters)
Phase 6 → Kafka event flow (producers → topics → consumers)
Phase 7 → Angular frontend (services, components, HTTP client)
Phase 8 → Docker & Docker Compose (all services containerized)
Phase 9 → CI/CD pipeline with GitHub Actions
Phase 10 → Code review pass (how a senior engineer would review this)


Before we write a single line of code, we do what senior engineers always do first:

> **"Design before you code. A bad architecture costs 10x more to fix later than to get right upfront."**

---

## Phase 1 — System Design

### The Business Domain (Understanding What We're Building)

OrderFlow is an order management system with these core business capabilities:

```
A customer browses products → places an order → 
payment is processed → inventory is reserved → 
order is confirmed → customer is notified → 
order is shipped → order is delivered
```

Each arrow in that flow is a **domain event**. This is exactly where Kafka comes in — not as a database, not as an API, but as the **nervous system** of your application, carrying events between components.

---

### System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                             │
│                                                                 │
│                    ┌───────────────┐                            │
│                    │  Angular SPA  │                            │
│                    │  (port 4200)  │                            │
│                    └──────┬────────┘                            │
└───────────────────────────┼─────────────────────────────────────┘
                            │ HTTP/REST
┌───────────────────────────┼─────────────────────────────────────┐
│                    BACKEND LAYER                                │
│                            │                                    │
│              ┌─────────────▼──────────────┐                     │
│              │     API Gateway / BFF       │                     │
│              │   Spring Boot (port 8080)   │                     │
│              │                             │                     │
│              │  ┌─────────────────────┐   │                     │
│              │  │   Order Service     │   │                     │
│              │  ├─────────────────────┤   │                     │
│              │  │   Product Service   │   │                     │
│              │  ├─────────────────────┤   │                     │
│              │  │  Payment Service    │   │                     │
│              │  ├─────────────────────┤   │                     │
│              │  │Notification Service │   │                     │
│              │  └─────────────────────┘   │                     │
│              └──────────┬──┬──────────────┘                     │
└─────────────────────────┼──┼────────────────────────────────────┘
                          │  │
              ┌───────────┘  └────────────┐
              │ Publishes events           │ Consumes events
┌─────────────▼────────────────────────────▼──────────────────────┐
│                      MESSAGING LAYER                            │
│                                                                 │
│    ┌──────────────────────────────────────────────────┐         │
│    │                    Apache Kafka                  │         │
│    │                                                  │         │
│    │  [order.created]  [order.confirmed]              │         │
│    │  [payment.processed]  [inventory.reserved]       │         │
│    │  [order.shipped]  [order.delivered]              │         │
│    └──────────────────────────────────────────────────┘         │
└─────────────────────────────────────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────────┐
│                     DATA LAYER                                  │
│                                                                 │
│              ┌────────────────────────┐                         │
│              │      PostgreSQL        │                         │
│              │                        │                         │
│              │  orders  │  products   │                         │
│              │  payments│  customers  │                         │
│              └────────────────────────┘                         │
└─────────────────────────────────────────────────────────────────┘
```

---

### The Kafka Event Flow (The Heart of the System)

This is the most important thing to understand before coding. Here's exactly what happens when a customer places an order:

```
CUSTOMER places order via Angular
         │
         ▼
[REST POST /api/orders]
         │
         ▼
Order Service validates & creates order (status: PENDING)
         │
         ├──► Saves to PostgreSQL
         │
         └──► Publishes ──► Topic: order.created
                                    │
                    ┌───────────────┼───────────────┐
                    │               │               │
                    ▼               ▼               ▼
            Payment Service  Inventory Service  Notification Service
            (consumer)       (consumer)         (consumer)
                    │               │               │
                    ▼               ▼               ▼
            Process payment  Reserve stock    Send "order received"
            Publish:         Publish:         email/notification
            payment.processed inventory.reserved
                    │               │
                    └───────┬───────┘
                            ▼
                    Order Service (consumer)
                    Both events received?
                            │
                    ┌───────┴────────┐
                   YES               NO
                    │                │
                    ▼                ▼
            Update order        Compensate
            status: CONFIRMED   (cancel/refund)
                    │
                    └──► Publish: order.confirmed
                                    │
                                    ▼
                            Notification Service
                            "Your order is confirmed!"
```

> **Senior Engineer Note:** This pattern is called a **Saga** — it's how distributed systems maintain consistency without a single database transaction spanning multiple services. You'll hear this term constantly in production systems.

---

### Bounded Contexts (How We Split the Domain)

In real companies, teams own bounded contexts. Here are ours:

| Context | Responsibility | Owns |
|---|---|---|
| **Order** | Order lifecycle management | orders table, order status |
| **Product** | Product catalog & inventory | products table, stock levels |
| **Payment** | Payment processing | payments table, payment status |
| **Notification** | Customer communication | notification logs |
| **Customer** | Customer identity | customers table |

---

### Technology Decisions (and WHY — always know the why)

| Decision | Technology | Why, not just what |
|---|---|---|
| REST API | Spring Boot | Synchronous ops: create order, query status |
| Async events | Kafka | Decouples services; payment/inventory don't block the HTTP response |
| Database | PostgreSQL | ACID transactions within a service boundary |
| Architecture | Hexagonal | Domain logic is testable without Spring, Kafka, or Postgres |
| Containers | Docker | Same environment dev → staging → production |
| CI/CD | GitHub Actions | Automate test → build → deploy on every push |

---

## Phase 2 — Repository Setup & Git Strategy

### Git Branching Strategy (How Real Teams Work)

```
main          ← production-ready code only, protected branch
  │
  └── develop ← integration branch, all features merge here
        │
        ├── feature/order-domain-model
        ├── feature/kafka-integration
        ├── feature/angular-order-dashboard
        └── fix/payment-idempotency
```

> **Senior Engineer Note:** In companies, `main` has branch protection rules. No one pushes directly to `main` — ever. Pull requests require at least one reviewer approval and passing CI checks. We'll enforce this with GitHub Actions.

### Setting Up Your Repository

Run these commands in your terminal. I'll explain each one:

```bash
# Clone the repo you just created
git clone https://github.com/YOUR_USERNAME/OrderFlow.git
cd OrderFlow

# Create the develop branch immediately - never work on main
git checkout -b develop

# Create the top-level folder structure
mkdir -p backend frontend docker docs

# Create a root README
touch README.md .gitignore

# Add a professional .gitignore
cat > .gitignore << 'EOF'
# Java / Maven
backend/target/
*.class
*.jar
*.war

# IntelliJ
.idea/
*.iml

# VSCode
.vscode/
frontend/.angular/
frontend/node_modules/
frontend/dist/

# Docker
docker/data/

# Environment files — NEVER commit secrets
.env
*.env.local

# OS
.DS_Store
Thumbs.db
EOF

# Create a README that looks professional
cat > README.md << 'EOF'
# OrderFlow

A production-grade order management system demonstrating hexagonal architecture,
event-driven design with Apache Kafka, and modern DevOps practices.

## Stack
- **Backend:** Spring Boot 3 + Java 21
- **Frontend:** Angular 17
- **Database:** PostgreSQL 15
- **Messaging:** Apache Kafka
- **DevOps:** Docker, GitHub Actions

## Architecture
OrderFlow follows Hexagonal Architecture (Ports & Adapters), ensuring the domain
logic is completely independent of frameworks, databases, and messaging infrastructure.

## Getting Started
See [docs/setup.md](docs/setup.md) for local development setup.
EOF

git add .
git commit -m "chore: initial repository setup with folder structure"
git push origin develop
```

---

## Phase 3 — Spring Boot Project Initialization

### Creating the Backend Project

Go to [start.spring.io](https://start.spring.io) and configure it exactly like this:

```
Project:      Maven
Language:     Java
Spring Boot:  3.2.x
Group:        com.orderflow
Artifact:     backend
Packaging:    Jar
Java:         21

Dependencies to add:
✅ Spring Web
✅ Spring Data JPA
✅ PostgreSQL Driver
✅ Spring for Apache Kafka
✅ Validation
✅ Lombok
✅ Spring Boot DevTools
✅ Actuator
```

Download and extract it into your `backend/` folder.

> **Why Java 21?** It's the current LTS (Long Term Support) release. In production, you always target LTS versions — never the latest non-LTS release. Companies can't afford to upgrade Java every 6 months.

> **Why Actuator?** Every production Spring Boot service exposes `/actuator/health` and `/actuator/metrics`. Your load balancer uses health checks to know if your app is alive. This is not optional in real systems.

---

## Phase 4 — Hexagonal Architecture Folder Structure

This is where most tutorials fail you. Let me show you the real structure and explain every folder:

```
backend/src/main/java/com/orderflow/
│
├── 📁 domain/                          ← THE CORE. Zero dependencies on Spring/JPA/Kafka
│   │
│   ├── 📁 model/                       ← Your business entities
│   │   ├── Order.java                  ← Not a JPA entity. Pure Java object.
│   │   ├── OrderItem.java
│   │   ├── OrderStatus.java            ← Enum: PENDING, CONFIRMED, SHIPPED, DELIVERED
│   │   ├── Product.java
│   │   ├── Customer.java
│   │   └── Money.java                  ← Value Object (amount + currency)
│   │
│   ├── 📁 event/                       ← Domain events (things that happened)
│   │   ├── OrderCreatedEvent.java
│   │   ├── OrderConfirmedEvent.java
│   │   ├── PaymentProcessedEvent.java
│   │   └── InventoryReservedEvent.java
│   │
│   ├── 📁 exception/                   ← Domain-specific exceptions
│   │   ├── OrderNotFoundException.java
│   │   ├── InsufficientStockException.java
│   │   └── PaymentFailedException.java
│   │
│   └── 📁 port/                        ← THE CONTRACTS (interfaces only)
│       ├── 📁 in/                      ← Driving ports (what the app can DO)
│       │   ├── CreateOrderUseCase.java
│       │   ├── GetOrderUseCase.java
│       │   ├── UpdateOrderStatusUseCase.java
│       │   └── ProcessPaymentUseCase.java
│       └── 📁 out/                     ← Driven ports (what the app NEEDS)
│           ├── OrderRepositoryPort.java
│           ├── ProductRepositoryPort.java
│           ├── PaymentGatewayPort.java
│           └── OrderEventPublisherPort.java
│
├── 📁 application/                     ← Orchestrates domain logic. Uses ports.
│   │
│   └── 📁 service/                     ← Implements the USE CASES
│       ├── OrderService.java           ← Implements CreateOrderUseCase, GetOrderUseCase
│       ├── PaymentService.java
│       └── InventoryService.java
│
└── 📁 infrastructure/                  ← The outside world. Spring, JPA, Kafka live here.
    │
    ├── 📁 adapter/
    │   │
    │   ├── 📁 in/                      ← How requests ENTER the system
    │   │   ├── 📁 rest/                ← HTTP adapters
    │   │   │   ├── OrderController.java
    │   │   │   ├── ProductController.java
    │   │   │   └── 📁 dto/             ← Request/Response objects (NOT domain objects)
    │   │   │       ├── CreateOrderRequest.java
    │   │   │       └── OrderResponse.java
    │   │   └── 📁 kafka/              ← Kafka consumer adapters
    │   │       ├── PaymentEventConsumer.java
    │   │       └── InventoryEventConsumer.java
    │   │
    │   └── 📁 out/                    ← How the system REACHES the outside world
    │       ├── 📁 persistence/        ← Database adapters
    │       │   ├── OrderPersistenceAdapter.java   ← Implements OrderRepositoryPort
    │       │   ├── 📁 entity/         ← JPA entities (NOT domain objects)
    │       │   │   ├── OrderEntity.java
    │       │   │   └── OrderItemEntity.java
    │       │   ├── 📁 repository/     ← Spring Data JPA interfaces
    │       │   │   └── OrderJpaRepository.java
    │       │   └── 📁 mapper/         ← Converts between domain ↔ JPA entity
    │       │       └── OrderMapper.java
    │       └── 📁 messaging/          ← Kafka producer adapters
    │           ├── KafkaOrderEventPublisher.java  ← Implements OrderEventPublisherPort
    │           └── 📁 config/
    │               └── KafkaConfig.java
    │
    └── 📁 config/                     ← Spring configuration classes
        ├── BeanConfig.java            ← Wires ports to adapters
        └── SecurityConfig.java
```

---

### The Mental Model — Understand This and Everything Clicks

```
      ┌─────────────────────────────────────────┐
      │              DOMAIN                      │
      │   (Pure Java, no framework imports)      │
      │                                          │
      │   Order, Product, Money                  │
      │   OrderCreatedEvent                      │
      │   CreateOrderUseCase (interface)  ◄──────┼── "I define WHAT can happen"
      │   OrderRepositoryPort (interface) ──────►┼── "I define WHAT I need"
      └─────────────────────────────────────────┘
                        ▲ depends on
      ┌─────────────────┴───────────────────────┐
      │            APPLICATION                  │
      │   (Orchestrates. Knows domain only.)    │
      │                                         │
      │   OrderService                          │
      │   - implements CreateOrderUseCase       │
      │   - uses OrderRepositoryPort            │
      │   - uses OrderEventPublisherPort        │
      └─────────────────────────────────────────┘
                        ▲ depends on
      ┌─────────────────┴───────────────────────┐
      │          INFRASTRUCTURE                 │
      │   (Knows Spring, JPA, Kafka, HTTP)      │
      │                                         │
      │   OrderController → calls use case      │
      │   OrderPersistenceAdapter → implements  │
      │                    repository port      │
      │   KafkaOrderEventPublisher → implements │
      │                    event publisher port │
      └─────────────────────────────────────────┘
```

> **The Golden Rule of Hexagonal Architecture:** Dependency arrows **only point inward**. Infrastructure knows about Application and Domain. Domain knows about **nothing** except itself.

---

## Your First Action Items

Here's exactly what to do right now, in order:

**1. Create the Spring Boot project** at start.spring.io with the config above

**2. Move it into your repo:**
```bash
# Unzip the downloaded project into backend/
cp -r ~/Downloads/backend/* ./backend/

# Move into backend and verify structure
cd backend
ls src/main/java/com/orderflow/
```

**3. Create your feature branch:**
```bash
git checkout develop
git checkout -b feature/project-structure-and-domain-model
```

**4. Create the folder structure:**
```bash
mkdir -p src/main/java/com/orderflow/{domain/{model,event,exception,port/{in,out}},application/service,infrastructure/{adapter/{in/{rest/dto,kafka},out/{persistence/{entity,repository,mapper},messaging/config}},config}}
```

**5. Verify it built correctly:**
```bash
./mvnw clean compile
# Should say: BUILD SUCCESS
```

## About Kafka

Senior Engineer Mindset: Kafka is not a message queue. It's a distributed commit log. The difference matters: a queue deletes messages after consumption. Kafka retains them. Every consumer reads at its own pace. You can replay events from the beginning of time. This is why Kafka is used for financial systems, audit logs, and anywhere event history matters.

Complete event flow
Angular → POST /api/orders
              │
              ▼
        OrderController
              │
              ▼
        OrderService.createOrder()
              │
        ┌─────┴──────┐
        │            │
        ▼            ▼
  Save to DB    Publish to Kafka
                      │
           Topic: order.created
                      │
          ┌───────────┴───────────┐
          │                       │
          ▼                       ▼
  PaymentEventConsumer    NotificationConsumer
  (simulates payment)     (logs notification)
          │
          ▼
  Publish to Kafka
  Topic: payment.processed
          │
          ▼
  OrderService.updateStatus(CONFIRMED)
          │
          ▼
  Publish to Kafka
  Topic: order.confirmed
          │
          ▼
  NotificationConsumer
  "Your order is confirmed!"
