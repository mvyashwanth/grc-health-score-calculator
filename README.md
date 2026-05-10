# Tool-86 — Health Score Calculator

AI-powered health score management system built as a capstone internship project.

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Browser (Port 80)                         │
│                    React 18 + Vite + Tailwind                    │
└────────────────────────────┬────────────────────────────────────┘
                             │ HTTP/REST
┌────────────────────────────▼────────────────────────────────────┐
│                  Spring Boot Backend (Port 8080)                  │
│         JWT Auth │ Redis Cache │ Flyway │ JPA │ Swagger           │
└──────┬────────────────────────────────────┬─────────────────────┘
       │                                    │
┌──────▼──────┐                   ┌─────────▼────────┐
│ PostgreSQL  │                   │  Flask AI Service │
│    :5432    │                   │     (Port 5000)   │
└─────────────┘                   │  Groq LLaMA-3.3   │
                                  └──────────┬─────────┘
┌─────────────┐                             │
│   Redis 7   │◄────────────────────────────┘
│    :6379    │   (AI response cache)
└─────────────┘
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | React 18, Vite, Tailwind CSS, Axios, Recharts |
| Backend | Java 17, Spring Boot 3.x, Spring Security + JWT |
| Database | PostgreSQL 15, Flyway migrations |
| Cache | Redis 7 |
| AI Service | Python 3.11, Flask 3.x, Groq API (LLaMA-3.3-70b) |
| DevOps | Docker, Docker Compose |

## Prerequisites

- Docker Desktop (latest) — [docs.docker.com](https://docs.docker.com)
- Git — [git-scm.com](https://git-scm.com)
- Groq API key (free) — [console.groq.com](https://console.groq.com)

## Setup (5 minutes)

### 1. Clone the repository
```bash
git clone <your-repo-url>
cd tool-86
```

### 2. Create your .env file
```bash
cp .env.example .env
```

Edit `.env` and fill in your values:
```env
DB_NAME=tool86db
DB_USER=tool86user
DB_PASSWORD=your_strong_password
JWT_SECRET=your_jwt_secret_minimum_32_characters
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your@email.com
MAIL_PASSWORD=your_app_password
GROQ_API_KEY=gsk_your_groq_key_here
```

### 3. Start all services
```bash
docker-compose up --build
```

Wait ~2 minutes for first build. You'll see:
```
tool86-backend   | Started Tool86Application
tool86-ai        | Gunicorn booting
tool86-frontend  | nginx: ready
```

### 4. Access the application

| Service | URL |
|---------|-----|
| Frontend | http://localhost |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| AI Health | http://localhost:5000/health |

**Default login:** `admin` / `Admin@123`

## Environment Variables Reference

| Variable | Description | Example |
|----------|-------------|---------|
| DB_NAME | PostgreSQL database name | tool86db |
| DB_USER | PostgreSQL username | tool86user |
| DB_PASSWORD | PostgreSQL password | strongpassword |
| JWT_SECRET | JWT signing secret (32+ chars) | mysecret... |
| MAIL_HOST | SMTP host | smtp.gmail.com |
| MAIL_PORT | SMTP port | 587 |
| MAIL_USERNAME | Email sender address | you@gmail.com |
| MAIL_PASSWORD | Email app password | xxxx xxxx |
| GROQ_API_KEY | Groq API key | gsk_... |
| AI_SERVICE_URL | AI service URL (internal) | http://ai-service:5000 |

## Key Features

- **Health Score Calculation** — algorithmic scoring (0–100) based on 10+ metrics
- **AI Description** — LLaMA-3.3-70b generates health summary on record creation
- **AI Recommendations** — 3 prioritized, actionable health recommendations
- **AI Report** — structured clinical report with risk level
- **Dashboard** — KPI cards, score distribution chart, recent records
- **Search & Filter** — debounced real-time search
- **CSV Export** — one-click export of all records
- **Audit Log** — every create/update/delete tracked
- **JWT Auth** — secure login with role-based access
- **Redis Cache** — 10-minute cache on GET endpoints
- **30 Demo Records** — seeded automatically on startup

## Running Tests

```bash
# Backend (JUnit 5)
cd backend
mvn test

# AI Service (pytest)
cd ai-service
pip install -r requirements.txt
pytest tests/
```

## Demo Day Checklist

- [ ] `docker-compose down -v && docker-compose up --build` — clean state
- [ ] Login as admin — confirm 30 seeded records visible
- [ ] Create new record — watch AI description appear
- [ ] Click AI Recommend — read aloud 3 recommendations
- [ ] Click Generate Report — show risk level
- [ ] Export CSV — download and open
- [ ] Show 401: `curl http://localhost:8080/api/health-records` (no token)
- [ ] Show 400 injection: send "ignore previous instructions" in describe
- [ ] Reference SECURITY.md

---
Sprint: 14 April – 9 May 2026 | Team: 5 Members | Demo Day: 9 May 2026
