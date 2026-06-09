# Skill Swap Hub — Backend API

A peer-to-peer skill exchange platform backend built with **Java 21**, **Spring Boot 3.5**, and **PostgreSQL**.

Users can **swap skills**, **teach for free**, or **teach for a price**. The platform handles user profiles, scheduling with conflict detection, real-time messaging, video call signaling, mutual feedback, and admin management.

## Features

- **Authentication**: Email/password registration + Google OAuth2 sign-in with JWT + refresh tokens
- **Rich Profiles**: Skills (teach/learn), certifications, experience, GitHub/LinkedIn/LeetCode/Codeforces links
- **Teaching Modes**: Swap, Free, or Paid — individual choice per session
- **Session Scheduling**: Conflict detection against teacher availability and existing sessions for both parties
- **Mutual Feedback**: Both teacher and student can rate (1-5) and review each completed session
- **Real-time Chat**: WebSocket (STOMP) based 1-to-1 messaging with content/file sharing
- **Video/Voice Calls**: WebRTC signaling (offer/answer/ICE) over WebSocket
- **Virtual Classroom**: Auto-generated Jitsi Meet room URLs per session
- **Notifications**: In-app notifications with WebSocket push
- **Admin Dashboard**: User management, ban/unban, report handling, platform stats
- **API Documentation**: Swagger UI at `/swagger-ui.html`

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| Build | Maven |
| Database | PostgreSQL 16 |
| Auth | Spring Security + JWT + Google OAuth2 |
| Real-time | Spring WebSocket (STOMP) |
| Docs | SpringDoc OpenAPI (Swagger UI) |
| Testing | JUnit 5 + H2 in-memory DB |

## Quick Start

### Prerequisites
- Java 21+
- Docker (for PostgreSQL)

### 1. Start the database
```bash
docker-compose up -d
```

### 2. Configure environment
```bash
cp .env.example .env
# Edit .env with your Google OAuth2 credentials (optional for testing)
```

### 3. Run the application
```bash
./mvnw spring-boot:run
```

### 4. Access Swagger UI
Open [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## API Modules

| Module | Base Path | Endpoints |
|---|---|---|
| Auth | `/api/auth` | register, login, google, refresh, logout |
| Users | `/api/users` | me, profile, search |
| Skills | `/api/skills` | CRUD for teach/learn skills |
| Certifications | `/api/certifications` | CRUD |
| Experiences | `/api/experiences` | CRUD |
| Availability | `/api/availabilities` | CRUD with overlap validation |
| Sessions | `/api/sessions` | book, accept, reject, complete, cancel |
| Feedback | `/api/feedback` | submit, view by user/session |
| Chat | `/api/chat` | send, conversations, messages, read |
| Notifications | `/api/notifications` | list, unread count, mark read |
| Reports | `/api/reports` | submit, view |
| Admin | `/api/admin` | users, ban, reports, stats |

## Architecture Highlights

- **Modular Package Structure**: Each feature (user, skill, session, chat, etc.) is a self-contained module with entity/dto/repository/service/controller layers
- **Conflict Detection Algorithm**: 5-step validation for session booking (self-check → time validation → availability coverage → teacher conflicts → student conflicts)
- **Event-Driven Notifications**: WebSocket push for real-time alerts
- **WebRTC Signaling**: Peer-to-peer call setup via WebSocket relay
- **ProblemDetail Responses**: RFC 7807 compliant error handling
