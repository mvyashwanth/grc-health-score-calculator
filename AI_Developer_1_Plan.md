# Tool-86 Health Score Calculator (AI Developer 1 Plan)

You have been assigned the **AI Developer 1** role (Ganesh V) for the Tool-86 Health Score Calculator Capstone Project. This plan covers creating the full application structure, and implementing the specific day-by-day tasks assigned to your role.

## User Review Required

> [!IMPORTANT]
> The prompt provides the full project spec, but you are specifically assigned the **AI Developer 1** role. This plan covers creating the base project structure for the entire team, but focuses ONLY on writing code for your specific tasks (Flask setup, Prompts, `/describe`, `/recommend`, `/generate-report`, `/health`, Redis Caching, ZAP fixes, Sentence-Transformers, ChromaDB integration, and Dockerizing). 
> 
> **Question**: Should I implement code *only* for the AI Developer 1 tasks, or would you like me to tackle other roles' tasks as well (e.g., complete the Flask API by doing AI Dev 2's tasks, or build the frontend/backend)?

## Proposed Changes

### Setup Complete Folder Structure (Day 1)
Create the mandatory folder structure for the entire team:
```text
your-project-folder/
|-- backend/ (src, pom.xml, etc.)
|-- ai-service/ (routes, services, prompts, app.py, Dockerfile, requirements.txt)
|-- frontend/ (src, package.json)
|-- docker-compose.yml
|-- .env.example
+-- README.md
```

### AI Developer 1 Tasks
I will implement the following items step-by-step according to your timeline:

*   **Day 1:** Setup Flask — `routes/`, `services/`, `prompts/` folders, `requirements.txt`, `app.py` entry point.
*   **Day 2:** Write primary prompt template — test with 5 real inputs, refine until all 5 outputs are consistently good.
*   **Day 3:** Build `POST /describe` — validate input, load prompt, call Groq, return structured JSON with `generated_at`.
*   **Day 4:** Build `POST /recommend` — 3 recommendations as JSON array, each with `action_type`, `description`, `priority`.
*   **Day 5:** Integrate `AiServiceClient` into Java Service — call AI on create `@Async`, attach result, handle null gracefully.
*   **Day 6:** Build `POST /generate-report` — structured JSON with title, summary, overview, key items, recommendations.
*   **Day 7:** Build `GET /health` — model, avg response time, uptime. Add Redis AI cache — SHA256 key, 15 min TTL.
*   **Day 8:** Fix all ZAP findings — add security headers, re-scan confirms zero Critical/High remaining.
*   **Day 9:** Final AI optimisation — all endpoints under 2s average, fallback template on Groq error (`{is_fallback: true}`).
*   **Day 10:** Write `ai-service/README.md` — setup, env vars, run instructions, API reference with examples for all 3 endpoints.
*   **Day 11:** Pre-load `sentence-transformers` at startup. Full ZAP active scan — fix all Critical/High findings.
*   **Day 12:** Seed ChromaDB with 10 domain knowledge documents. Run all prompts against 30 demo records — all outputs demo-ready.
*   **Day 13:** Package AI — Dockerfile builds cleanly, `requirements.txt` exact versions, `.env.example` complete.
*   **Day 14-20:** Demo Prep & Execution. 

## Open Questions

1.  **Scope**: Do you only want me to generate the `ai-service` code assigned to "AI Developer 1", or do we want to cross over into "AI Developer 2" territory (e.g. creating `GroqClient`, configuring `flask-limiter`, writing automated tests) so you have a complete, working `ai-service` codebase?
2.  **Environment Settings**: Can you provide the Groq API Key, or should I just construct the `.env.example` file and leave the `.env` configuration to you?

## Verification Plan

### Automated Tests
*   We can run standard `pytest` scripts (which AI Developer 2 is assigned to write) to verify functionality.

### Manual Verification
*   Start the Flask API using `flask run`.
*   Send cURL or Postman requests to `/describe`, `/recommend`, `/generate-report`, and `/health` to manually inspect the JSON schema and performance.
*   Inspect logs to ensure Redis caching hits occur.
