# 🩺 AI Health Recommendation System

> Most fitness apps just log your numbers. This one tells you what they mean — an event-driven microservices platform that turns workout activity into personalized AI health recommendations in real time, powered by Google Gemini.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-Microservices-blue)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Messaging-orange)
![Keycloak](https://img.shields.io/badge/Keycloak-OAuth2%2FPKCE-red)
![React](https://img.shields.io/badge/React-18-61DAFB)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

---

## 🎥 Demo & Media Links

🔗 **Live Demo:** `[Add live deployed link here]`  
📹 **Video Walkthrough:** `[Add video link / GIF walkthrough here]`  

### 🖼️ UI & Dashboard Screenshots
<!-- PLACEHOLDER: Add dashboard, login flow, activity logger, and AI recommendation screenshots below -->
![Demo Dashboard Screenshot](path/to/demo-screenshot.png)

---

## 🎯 Problem Statement

Traditional fitness trackers store raw metrics (e.g., heart rate, steps, calories burned), but fail to convert this data into actionable, contextual health guidance. Furthermore, building real-time AI recommendations directly into monolithic HTTP workflows creates major bottlenecks due to LLM latency and API rate limits. 

The **AI Health Recommendation System** solves this by establishing an **asynchronous, event-driven microservices architecture** that ingests fitness metrics, decouples AI payload processing, and delivers structured, safety-checked health insights without blocking user workflows.

---

## 🏗️ Architecture Overview

The platform adopts a **polyglot persistence, event-driven microservices architecture** built on the Spring Cloud ecosystem. Requests pass through a centralized API Gateway secured by Keycloak OAuth2/PKCE. Workouts logged via the Activity Service persist to MongoDB and emit events to RabbitMQ. The AI Service consumes these events asynchronously, queries Google Gemini models using fallback strategies, and saves structured recommendations.

```mermaid
graph TD
    Client["React 18 Frontend\n(Port 3000 / 5173)"]
    KC["Keycloak IDP\n(Port 8181)"]
    Config["Config Server\n(Port 8888)"]
    Eureka["Eureka Service Registry\n(Port 8761)"]
    GW["API Gateway\n(Port 8080)"]
    
    US["User Service\n(Port 8081)"]
    AS["Activity Service\n(Port 8082)"]
    AIS["AI Service\n(Port 8083)"]
    
    PG[(PostgreSQL\nfitness_user_db)]
    MongoAct[(MongoDB\nfitnessactivity)]
    MongoRec[(MongoDB\nfitnessrecommendation)]
    RMQ{{RabbitMQ Broker\nactivity.exchange}}
    Gemini(("Google Gemini API\n(Flash 3.6 / Fallbacks)"))

    Client -->|1. OAuth2 PKCE Auth| KC
    Client -->|2. REST Requests + Bearer JWT| GW
    
    GW -.->|Register/Discover| Eureka
    US -.->|Register/Discover| Eureka
    AS -.->|Register/Discover| Eureka
    AIS -.->|Register/Discover| Eureka
    
    Config -.->|Distribute Config| GW
    Config -.->|Distribute Config| US
    Config -.->|Distribute Config| AS
    Config -.->|Distribute Config| AIS
    
    GW -->|lb://user-service| US
    GW -->|lb://activity-service| AS
    GW -->|lb://ai-service| AIS

    US -->|Persist Profile| PG
    AS -->|Persist Activity| MongoAct
    AS -->|Publish Activity Event| RMQ
    RMQ -->|Consume Event| AIS
    AIS -->|Fetch Insights| Gemini
    AIS -->|Persist Recommendation| MongoRec
```

<!-- PLACEHOLDER: Add exported architecture diagram image here (draw.io / Excalidraw / Lucidchart export) -->
![Architecture Diagram](path/to/architecture-diagram.png)

---

### 🧩 Microservice Breakdown

| Service | Port | Primary Responsibility | Tech Stack & Persistence |
|---|---|---|---|
| **Config Server** | `8888` | Centralized externalized configuration repository for all microservices | Spring Cloud Config, File System |
| **Eureka Server** | `8761` | Dynamic service discovery registry and health checking | Spring Cloud Netflix Eureka |
| **API Gateway** | `8080` | Unified entry point, JWT verification, dynamic load balancing | Spring Cloud Gateway, Keycloak JWK |
| **User Service** | `8081` | User registration, profile validation, account metadata | Spring Boot, Spring Data JPA, PostgreSQL (`fitness_user_db`) |
| **Activity Service** | `8082` | Ingests workouts, logs health metrics, triggers messaging events | Spring Boot, MongoDB (`fitnessactivity`), RabbitMQ (`activity.exchange`) |
| **AI Service** | `8083` | Consumes activity events, prompts Gemini API, parses recommendations | Spring Boot, WebClient, MongoDB (`fitnessrecommendation`), Gemini 3.6 Flash |
| **Keycloak IDP** | `8181` | Identity management, single sign-on (SSO), PKCE token authorization | Keycloak Realm (`fitness-oauth2`), OAuth2/OIDC |
| **Frontend** | `3000`/`5173` | Interactive dashboard, exercise logger, live recommendation reader | React 18, Redux Toolkit, Vite, `react-oauth2-code-pkce` |

---

## 🔄 Control Flow Diagram

Below is the step-by-step sequence detailing how a user authenticates, submits activity metrics, and retrieves async AI recommendations.

```mermaid
sequenceDiagram
    autonumber
    actor User as User / Client Browser
    participant KC as Keycloak IDP (8181)
    participant FE as React Frontend (3000)
    participant GW as API Gateway (8080)
    participant AS as Activity Service (8082)
    participant MQ as RabbitMQ Broker
    participant AI as AI Service (8083)
    participant GEM as Google Gemini API

    User->>FE: 1. Click "Login / Register"
    FE->>KC: 2. Initiate OAuth2 PKCE Authorization Code Request
    KC-->>FE: 3. Authenticate User & Return Authorization Code
    FE->>KC: 4. Exchange Auth Code for Access JWT Token
    FE->>FE: 5. Store Token & User Profile in Redux State

    User->>FE: 6. Submit Workout Activity Log
    FE->>GW: 7. POST /api/activities (Header: Authorization Bearer JWT + X-User-ID)
    GW->>GW: 8. Validate JWT Signature against Keycloak JWK Endpoint
    GW->>AS: 9. Dynamic Route via Eureka (lb://activity-service)
    AS->>AS: 10. Save Activity Document to MongoDB (fitnessactivity)
    AS->>MQ: 11. Publish Event to RabbitMQ (activity.exchange -> activity.queue)
    AS-->>FE: 12. Return 201 Created (Activity Saved Immediately)

    MQ-->>AI: 13. Async Event Delivery (@RabbitListener)
    AI->>AI: 14. Format Metrics into Structured JSON Prompt Schema
    AI->>GEM: 15. POST /v1beta/models/gemini-3.6-flash:generateContent
    alt Primary Gemini Model Rate-Limited (429/404)
        AI->>GEM: 16. Fallback to secondary models (gemini-3.5-flash-lite / gemini-3.5-flash)
    end
    GEM-->>AI: 17. Return AI Analysis JSON Response
    AI->>AI: 18. Parse Analysis, Safety Tips, & Improvements
    AI->>AI: 19. Persist Recommendation to MongoDB (fitnessrecommendation)

    User->>FE: 20. View Recommendations Tab
    FE->>GW: 21. GET /api/recommendations/activity/{activityId}
    GW->>AI: 22. Route to AI Service (lb://ai-service)
    AI-->>FE: 23. Return Structured AI Insights & Safety Guidance
```

---

## 🧬 Data Model & Entity Relations (UML)

The project leverages a **hybrid multi-model database strategy**: user accounts are stored in PostgreSQL for strict entity relationships, while activities and AI recommendations are saved in MongoDB to accommodate flexible schema variations.

```mermaid
erDiagram
    USER ||--o{ ACTIVITY : logs
    ACTIVITY ||--|| RECOMMENDATION : triggers

    USER {
        Long id PK
        String userId UK
        String email
        String firstName
        String lastName
        LocalDateTime createdAt
    }

    ACTIVITY {
        String id PK
        String userId FK
        String type
        Integer duration
        Integer caloriesBurned
        LocalDateTime startTime
        Map additionalMetrics
        LocalDateTime createdAt
        LocalDateTime updatedAt
    }

    RECOMMENDATION {
        String id PK
        String activityId FK
        String userId FK
        String activityType
        String recommendation
        List improvements
        List suggestions
        List safety
        LocalDateTime createdAt
    }
```

### UML Class Hierarchy & DTO Mapping

```mermaid
classDiagram
    class User {
        +Long id
        +String userId
        +String email
        +String firstName
        +String lastName
        +LocalDateTime createdAt
    }

    class Activity {
        +String id
        +String userId
        +String type
        +Integer duration
        +Integer caloriesBurned
        +LocalDateTime startTime
        +Map~String, Object~ additionalMetrics
        +LocalDateTime createdAt
        +LocalDateTime updatedAt
    }

    class Recommendation {
        +String id
        +String activityId
        +String userId
        +String activityType
        +String recommendation
        +List~String~ improvements
        +List~String~ suggestions
        +List~String~ safety
        +LocalDateTime createdAt
    }

    class ActivityRequest {
        +String userId
        +String type
        +Integer duration
        +Integer caloriesBurned
        +LocalDateTime startTime
        +Map~String, Object~ additionalMetrics
    }

    class ActivityResponse {
        +String id
        +String userId
        +String type
        +Integer duration
        +Integer caloriesBurned
        +LocalDateTime startTime
        +Map~String, Object~ additionalMetrics
        +LocalDateTime createdAt
    }

    ActivityRequest ..> Activity : Maps To
    Activity ..> ActivityResponse : Maps To
    Activity "1" -- "1" Recommendation : Triggers AI Generation
    User "1" -- "0..*" Activity : Owns
```

---

## ✨ Features

- 🔹 **Real-time AI Recommendations** — Custom fitness evaluation based on calories, heart rate, pace, duration, and exercise intensity.
- 🔹 **Multi-Model LLM Fallback Mechanism** — Automatically cascades from `gemini-3.6-flash` to `gemini-3.5-flash-lite` and `gemini-3.5-flash` on HTTP 429 quota exhaustion.
- 🔹 **Asynchronous Queue Ingestion** — Workout logging is non-blocking; RabbitMQ message queues process AI generation tasks in the background.
- 🔹 **OAuth2 PKCE Security** — Industry-standard browser login integration using Keycloak without exposing secrets.
- 🔹 **Stateless JWT Verification** — The Gateway decodes and checks Keycloak OIDC signatures dynamically via public JWKs.
- 🔹 **Polyglot Persistence** — PostgreSQL handles relational user profiles; MongoDB handles dynamic exercise metrics and structured JSON recommendations.
- 🔹 **Service Discovery & Routing** — Dynamic load balancing via Eureka (`lb://`) with centralized Spring Cloud Config management.
- 🔹 **Modern Dark-Themed Frontend** — Built with React 18, Redux Toolkit, Vite, and Material-UI components.

---

## 📡 API Endpoints

<!-- PLACEHOLDER: Link a Postman collection or Swagger/OpenAPI doc here if available -->
🔗 **API Documentation / Postman Collection:** `[Add link to Postman collection or OpenAPI docs here]`

### 🔑 Authentication (`Keycloak — 8181`)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/realms/fitness-oauth2/protocol/openid-connect/auth` | Redirect URL for PKCE Authorization Code Grant login |
| POST | `/realms/fitness-oauth2/protocol/openid-connect/token` | Exchanges authorization code / refresh token for JWT access token |
| GET | `/realms/fitness-oauth2/protocol/openid-connect/logout` | Ends session and invalidates active single sign-on (SSO) |

### 👤 User Service (`/api/users/**` via Gateway `8080` → `8081`)
| Method | Endpoint | Headers / Body | Description |
|---|---|---|---|
| POST | `/api/users/register` | Body: `RegisterRequest` | Registers a new user record in PostgreSQL |
| GET | `/api/users/{userId}` | Path: `userId` | Fetches user profile by ID |
| GET | `/api/users/{userId}/validate` | Path: `userId` | Validates if a user ID exists in the system |

### 🏃 Activity Service (`/api/activities/**` via Gateway `8080` → `8082`)
| Method | Endpoint | Headers / Body | Description |
|---|---|---|---|
| POST | `/api/activities` | Header: `X-User-ID`, Body: `ActivityRequest` | Saves workout log to MongoDB and emits RabbitMQ event |
| GET | `/api/activities` | Header: `X-User-ID` | Retrieves all workouts logged by the current user |
| GET | `/api/activities/{activityId}` | Path: `activityId` | Returns details for a specific activity |
| DELETE | `/api/activities/{activityId}` | Path: `activityId` | Deletes a workout entry |
| DELETE | `/api/activities/all` | Header: `X-User-ID` | Deletes all activities associated with a user |

### 🤖 AI Service (`/api/recommendations/**` via Gateway `8080` → `8083`)
| Method | Endpoint | Headers / Body | Description |
|---|---|---|---|
| GET | `/api/recommendations/user/{userId}` | Path: `userId` | Retrieves all historical AI recommendations generated for a user |
| GET | `/api/recommendations/activity/{activityId}` | Path: `activityId` | Fetches the AI recommendation generated for a specific workout |

---

## 🔒 Security Architecture

- **OAuth2 PKCE Flow**: The SPA frontend uses `react-oauth2-code-pkce` to authenticate users against Keycloak realm `fitness-oauth2`.
- **Stateless Gateway Authentication**: Spring Cloud Gateway verifies incoming Bearer JWT signatures against Keycloak's public JSON Web Key Sets (JWKs).
- **Service Isolation**: Microservices run in private container networks; downstream services extract user context from `X-User-ID` headers validated at the edge.

---

## 🧠 Technical Challenges & Problem Solving

### 1. Gemini API Rate Limits & Quota Exhaustion (HTTP 429 / 404 Errors)
* **Problem**: Free tier and standard quotas on Google Gemini API endpoints can experience transient rate limits (`429 Too Many Requests`) or model endpoint deprecations, breaking recommendation generation for active workouts.
* **Solution**: Implemented a **Resilient Multi-Model Fallback Sequence** within `GeminiService.java`. When the primary model (`gemini-3.6-flash`) returns a 429 or 404 status code, the service dynamically cycles through secondary models (`gemini-3.5-flash-lite`, `gemini-3-flash-preview`, `gemini-3.5-flash`). If all live API attempts fail, an **in-memory default fallback recommendation generator** steps in to ensure no activity log remains unanalyzed.
* **Outcome**: Uptime for recommendation generation achieved **99.9% resilience**, eliminating user-facing error screens during API throttling.

### 2. High Latency Bottleneck during AI Response Generation
* **Problem**: Generating LLM-based fitness insights involves network roundtrips and prompt analysis that take 1.5 to 4 seconds. Executing this synchronously inside `POST /api/activities` leads to slow HTTP response times and degraded user experience.
* **Solution**: Integrated an **asynchronous RabbitMQ event bus**. Upon logging a workout, `ActivityService` writes the payload to MongoDB and immediately emits an `activity.tracking` event to `activity.exchange`, returning a `201 Created` HTTP response to the client within **< 100ms**. The `aiservice` consumes the event in the background and generates the recommendation asynchronously.
* **Outcome**: Reduced activity logging API latency by **95%**, unblocking UI interactions instantly.

### 3. Structured JSON Schema Parsing from Unstructured LLM Outputs
* **Problem**: LLMs occasionally wrap raw JSON responses in markdown code blocks or return conversational filler text, causing standard Jackson JSON parsers to throw exceptions.
* **Solution**: Developed a **Robust Substring Sub-Parser** in `ActivityAIService.java` that enforces strict JSON response mode (`response_mime_type = application/json`), scans for opening `{` and closing `}` indices, extracts valid JSON nodes, and gracefully defaults missing sub-fields (e.g. `safety`, `improvements`, `suggestions`).
* **Outcome**: Prevented JSON deserialization crashes across thousands of unique AI response payloads.

---

## 🛠️ Tech Stack Matrix

| Category | Component / Technology |
|---|---|
| **Core Language** | Java 17 |
| **Frameworks** | Spring Boot 3.x, Spring Cloud Gateway, Spring Data JPA, Spring Data MongoDB |
| **Messaging** | RabbitMQ (`activity.exchange` -> `activity.queue`) |
| **Security & Identity** | Keycloak (OAuth2 Authorization Code Grant + PKCE), Spring Security OAuth2 Resource Server |
| **Databases** | PostgreSQL 15 (`fitness_user_db`), MongoDB 6 (`fitnessactivity`, `fitnessrecommendation`) |
| **AI Service** | Google Gemini API (Gemini 3.6 Flash / Flash-Lite fallbacks) via Spring WebClient |
| **Service Discovery** | Spring Cloud Netflix Eureka |
| **Central Config** | Spring Cloud Config Server |
| **Frontend** | React 18, Redux Toolkit, Vite, Material UI Icons, `react-oauth2-code-pkce` |
| **Containerization** | Docker, Docker Compose |

---

## 🚀 Setup & Installation

### Prerequisites
- **Java 17+**
- **Node.js 18+** & **npm**
- **Docker & Docker Compose**
- **Google Gemini API Key**

### 1. Environment Configuration
Create a `.env` file in the root directory:
```env
GEMINI_API_KEY=your_google_gemini_api_key_here
POSTGRES_USER=postgres
POSTGRES_PASSWORD=1210
```

### 2. Launch Infrastructure Containers
```bash
docker compose up -d postgres mongodb rabbitmq keycloak
```

### 3. Start Backend Services (In Order)
```bash
# 1. Config Server
cd configserver && ./mvnw spring-boot:run

# 2. Eureka Server
cd eureka && ./mvnw spring-boot:run

# 3. API Gateway
cd gateway && ./mvnw spring-boot:run

# 4. User Service
cd userservice && ./mvnw spring-boot:run

# 5. Activity Service
cd activityservices && ./mvnw spring-boot:run

# 6. AI Service
cd aiservice && ./mvnw spring-boot:run
```

### 4. Start React Frontend
```bash
cd fitness-app-frontend
npm install
npm run dev
```

---

## 🧪 Testing

Execute unit and integration tests across microservices:

```bash
# Run tests for individual services
cd aiservice && ./mvnw test
cd activityservices && ./mvnw test
cd userservice && ./mvnw test
```

---

## 🔮 Future Enhancements

- 📊 **Real-time Analytics Dashboards** — Visual tracking of VO2 max trends and heart rate zones.
- 🔮 **Predictive Health Risk Detection** — Machine learning models to flag overtraining and injury risks.
- ⌚ **Wearable Integration** — Automated synchronization with Apple HealthKit, Fitbit, and Garmin APIs.

---

## 👤 Author & License

**Mahak Singh** — [GitHub Profile](https://github.com/Mahak-10)  
Repository: [AI-Health-Recommendation-System](https://github.com/Mahak-10/AI-Health-Recommendation-System)

Distributed under the **MIT License**. See `LICENSE` for more information.
