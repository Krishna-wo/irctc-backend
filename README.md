# 🚂 IRCTC Backend Clone

A production-grade railway reservation system built with 
Spring Boot, PostgreSQL, and Redis — designed to handle 
real-world concurrency, caching, and distributed locking.

## 🏗️ Architecture

> ER Diagram — Coming Day 2

## ⚙️ Tech Stack

| Technology | Purpose |
|---|---|
| Java 17 + Spring Boot 3 | Core backend framework |
| PostgreSQL | Primary database |
| Redis | Caching + Distributed locking |
| Docker + Docker Compose | Containerization |
| JWT | Authentication |
| Flyway | Database migrations |
| Swagger | API documentation |

## 📦 Modules

- **Auth Module** — JWT based authentication and authorization
- **Train & Route Management** — Complete train scheduling system
- **Seat Booking Engine** — Concurrent seat booking with Redis locking
- **Train Search** — Redis cached search for high traffic
- **Booking State Machine** — Handles payment failures gracefully

## 🎯 System Design Highlights

- Handles concurrent seat booking using Redis distributed locks
- Booking state machine prevents data inconsistency on payment failure
- Redis caching on search reduces DB load significantly
- Idempotent booking API prevents duplicate bookings
- Package by Feature architecture — each module ready for microservice extraction

## 🗄️ Database Schema

> Coming Day 2 after Flyway migrations are finalized

## 🚀 Running Locally

> Coming Day 2 after Docker setup

## 📊 Progress

- [x] Day 1 — Project setup and architecture
- [ ] Day 2 — Database schema and Flyway migrations
- [ ] Day 3 — Docker setup and PostgreSQL connection
- [ ] Day 4 — Auth module with JWT
- [ ] Day 5 — Train and Station APIs