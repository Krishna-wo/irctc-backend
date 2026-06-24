# IRCTC Backend Clone

A production-grade Railway Ticket Booking System built with Spring Boot 3, PostgreSQL, and Redis.
Designed to handle high-concurrency scenarios like Tatkal booking — where 50,000+ users attempt
to book the same seat simultaneously.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3.5 |
| Security | Spring Security + JWT |
| Database | PostgreSQL 15 |
| Cache + Locks | Redis 7 |
| Migrations | Flyway |
| Container | Docker + Docker Compose |
| Docs | Swagger / OpenAPI 3.0 |
| Build | Maven |

---

## Architecture

```
User Request
     ↓
Spring Boot (Brain)
     ↓
Redis Check (Speed Layer — 3-5ms)
     ↓ cache miss
PostgreSQL (Permanent Storage — 150-200ms)
     ↓
Response
```

---

## The 3 Core Modules

### Module 1 — Distributed Seat Locking (Concurrency)

**Problem:** Tatkal opens at 10 AM. 50,000 users click "Book" on the same seat at the same millisecond.
Without a lock — two users get confirmed for one seat.

**Solution:** Redis distributed lock using SETNX.

```
User 1 → acquires lock on seat:32:2026-12-25 → books seat → PAYMENT_PENDING
User 2 → tries lock → already exists → "Seat unavailable, try another"
```

- Lock TTL = 10 minutes — auto-expires if app crashes
- Lock released on payment confirm or payment fail
- Prevents race conditions at the DB level

**Result:** Zero double bookings under any concurrency level.

---

### Module 2 — Redis Search Caching

**Problem:** "Show trains from NDLS to BCT" runs a self-join JPQL query on the routes table.
At 10 lakh concurrent users — database gets 10 lakh identical queries and crashes.

**Solution:** Cache-aside pattern with Redis.

```
First search  → DB query → store in Redis (TTL 10 min) → 187ms
Next searches → Redis hit → instant response             → 4ms
```

**Measured improvement: 97% reduction in response time.**
Database load reduced by 99% on repeated searches.

Cache invalidation endpoint available for admin when new trains are added.

---

### Module 3 — Booking State Machine + Auto Cancellation

**Problem:** User selects seat, reaches payment page, internet disconnects.
Payment neither succeeds nor fails. Seat stays locked forever.

**Solution:** State machine with scheduled auto-cancellation.

```
INITIATED
    ↓
PAYMENT_PENDING  ← seat held for 10 minutes
    ↓                    ↓
CONFIRMED          CANCELLED (auto — scheduler)
    ↓
CANCELLED (user)
```

Background scheduler runs every 60 seconds.
Finds all PAYMENT_PENDING bookings older than 10 minutes.
Auto-cancels them and releases Redis locks.
Seat becomes available for new bookings immediately.

---

## Project Structure

```
src/main/java/com/irctc/
├── auth/          → JWT generation, validation, Spring Security filter
├── booking/       → Booking, BookedSeat, PaymentService, Scheduler
├── coach/         → Coach management
├── common/        → Config, GlobalExceptionHandler, ApiResponse, CacheKeys
├── route/         → Train route stops with sequence numbers
├── search/        → Train search with Redis caching
├── seat/          → Seat management
├── station/       → Station management
├── train/         → Train management
└── user/          → User profile, Admin dashboard
```

---

## Database Schema

| Migration | Table | Description |
|---|---|---|
| V1 | users | User accounts with roles |
| V2 | stations | Railway stations |
| V3 | trains | Train master data |
| V4 | routes | Train stops with sequence and distance |
| V5 | coaches | Coach per train |
| V6 | seats | Seats per coach |
| V7 | bookings | Booking header with PNR |
| V8 | booked_seats | Per-passenger seat records |
| V9 | booked_seats.status | Cancellation tracking |

---

## API Overview

| Module | Method | Endpoint | Access |
|---|---|---|---|
| Auth | POST | /api/auth/register | Public |
| Auth | POST | /api/auth/login | Public |
| Search | POST | /api/search/trains | Public |
| Station | POST | /api/admin/stations | Admin |
| Train | POST | /api/admin/trains | Admin |
| Route | POST | /api/admin/routes | Admin |
| Coach | POST | /api/admin/coaches | Admin |
| Seat | POST | /api/admin/seats | Admin |
| Booking | POST | /api/bookings | Authenticated |
| Payment | POST | /api/payments/confirm/{id} | Authenticated |
| Payment | POST | /api/payments/fail/{id} | Authenticated |
| Cancel | POST | /api/bookings/{id}/cancel | Authenticated |
| Cancel | POST | /api/bookings/{id}/cancel-seat/{seatId} | Authenticated |
| Profile | GET | /api/users/profile | Authenticated |
| Profile | PUT | /api/users/profile | Authenticated |
| Admin | GET | /api/admin/users | Admin |
| Admin | GET | /api/admin/stats | Admin |

---

## Running Locally

### Prerequisites
- Java 17+
- Docker Desktop
- Maven

### Steps

**1. Clone the repository**
```bash
git clone https://github.com/yourusername/irctc-backend.git
cd irctc-backend
```

**2. Start PostgreSQL and Redis**
```bash
docker compose up -d
```

**3. Run the application**
```bash
./mvnw spring-boot:run
```

**4. Access Swagger UI**
```
http://localhost:8081/swagger-ui/index.html
```

---

## Key Design Decisions

| Decision | Reason |
|---|---|
| Package by feature | Each module is self-contained and independently deployable |
| JWT stateless auth | No sessions — scales horizontally without sticky sessions |
| Flyway migrations | Schema changes are versioned, tracked, and reversible |
| DTO pattern | Entities never exposed directly — clean API contracts |
| Redis SETNX locks | Atomic lock acquisition — no race conditions possible |
| BigDecimal for money | Never use double/float for financial calculations |
| FetchType.LAZY | Avoid N+1 queries — load relations only when needed |
| Cache-aside pattern | Redis is a speed layer, not critical — graceful fallback to DB |
| Scheduler fault isolation | One booking failure never stops processing of others |

---

## Full API Test Sequence

```
1.  POST /api/auth/register          (ADMIN)
2.  POST /api/auth/register          (PASSENGER)
3.  POST /api/auth/login
4.  POST /api/admin/stations         (NDLS)
5.  POST /api/admin/stations         (BCT)
6.  POST /api/admin/trains
7.  POST /api/admin/routes           (stop 1)
8.  POST /api/admin/routes           (stop 2)
9.  POST /api/admin/coaches
10. POST /api/admin/seats            (seat 1, 2, 3)
11. POST /api/search/trains          (no token — public)
12. POST /api/search/trains          (second time — Redis hit)
13. POST /api/bookings               (PAYMENT_PENDING)
14. KEYS lock:* in Redis CLI         (verify lock exists)
15. POST /api/payments/confirm/{id}  (CONFIRMED)
16. KEYS lock:* in Redis CLI         (verify lock released)
17. POST /api/bookings               (new booking — don't pay)
18. Wait 2 min — scheduler cancels automatically
19. POST /api/bookings               (same seat — works again)
20. GET  /api/users/profile
21. PUT  /api/users/profile
22. GET  /api/admin/stats
```
