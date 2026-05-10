# SECURITY.md — Tool-86 Health Score Calculator

## Executive Summary
Tool-86 is an AI-powered health scoring web application. This document outlines the threat model,
security tests conducted, findings, and residual risks for the Sprint 14 April – 9 May 2026.

---

## Threat Model

| # | Threat | Component | Severity |
|---|--------|-----------|----------|
| 1 | JWT token theft / replay attack | Backend | HIGH |
| 2 | SQL Injection via API parameters | Backend / DB | HIGH |
| 3 | Prompt Injection via health data fields | AI Service | HIGH |
| 4 | Brute-force login attacks | Auth Controller | MEDIUM |
| 5 | Unauthenticated API access | All endpoints | HIGH |
| 6 | Sensitive data exposure in logs | Backend | MEDIUM |
| 7 | Rate limit bypass on AI endpoints | AI Service | MEDIUM |
| 8 | Hardcoded secrets in source code | All | CRITICAL |
| 9 | XSS via stored AI-generated content | Frontend | MEDIUM |
| 10 | CORS misconfiguration | Backend | MEDIUM |

---

## Security Controls Implemented

### Authentication & Authorization
- **JWT (HS256)** with 24-hour expiration on all protected endpoints
- **Spring Security** with role-based access (@PreAuthorize) — USER and ADMIN roles
- **BCrypt (cost 12)** password hashing — resistant to brute-force
- **Stateless sessions** — no server-side session state

### Input Validation & Injection Prevention
- **@Valid + Bean Validation** on all Spring controller request bodies
- **Prompt injection detection** in AI service middleware — detects 10+ patterns
- **HTML stripping** on all AI service string inputs
- **Parameterized queries** via Spring Data JPA — SQL injection not possible

### API Rate Limiting
- **flask-limiter** enforces 30 requests/minute per IP on AI service
- Returns HTTP 429 on violation

### Secrets Management
- All secrets stored in `.env` file — never committed to Git
- `.env` listed in `.gitignore` — verified on Day 1
- `application.yml` uses `${ENV_VAR}` references only

### Security Headers
- CORS locked to configured origins
- CSRF disabled (stateless JWT API — no cookies)

---

## Tests Conducted

| Test | Method | Result |
|------|--------|--------|
| Unauthenticated access to /api/health-records | curl without token | 401 Unauthorized ✅ |
| Login with wrong password | POST /api/auth/login | 401 Unauthorized ✅ |
| SQL injection in search param | `?q='; DROP TABLE--` | 200, no effect ✅ |
| Prompt injection in title field | `ignore previous instructions` | 400 Bad Request ✅ |
| Rate limit on AI endpoint | 35 rapid requests | 429 after 30 ✅ |
| Admin endpoint as USER role | GET /api/admin | 403 Forbidden ✅ |
| Empty body to AI endpoints | POST /describe with `{}` | 400 Bad Request ✅ |
| HTML injection in fields | `<script>alert(1)</script>` | Stripped by sanitizer ✅ |

---

## Findings Fixed

| Finding | Severity | Fix Applied |
|---------|----------|-------------|
| Groq API key was in test_groq.py | CRITICAL | Moved to .env, .gitignore verified |
| Missing 401 on unauthenticated requests | HIGH | SecurityConfig updated |
| No rate limiting on AI endpoints | MEDIUM | flask-limiter added |
| Prompt injection not detected | HIGH | sanitizer.py middleware added |

---

## Residual Risks

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Groq free-tier outages | Medium | Low (fallback exists) | Fallback template returns is_fallback:true |
| JWT not on revocation list | Low | Medium | Short expiry (24h); logout clears client token |
| No HTTPS in dev | Medium | Medium | Deploy behind HTTPS reverse proxy in production |

---

## Team Sign-Off

| Member | Role | Signed |
|--------|------|--------|
| Member 1 | Java Developer 1 | ☐ |
| Member 2 | Java Developer 2 | ☐ |
| Member 3 | AI Developer 1 | ☐ |
| Member 4 | AI Developer 2 | ☐ |
| Member 5 | Security Reviewer | ☐ |

_Last updated: Sprint Week 3_
