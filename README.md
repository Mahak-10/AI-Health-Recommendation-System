# 🩺 AI Health Recommendation System

> Most fitness apps just log your numbers. This one tells you what they mean — turning your activity, heart rate, and vitals into clear, personalized health recommendations in real time.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-Microservices-blue)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Messaging-orange)
![Keycloak](https://img.shields.io/badge/Keycloak-Auth-red)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

## 🎥 Demo

<!-- PLACEHOLDER: Add a live deployed link or demo GIF here -->
🔗 **Live Demo:** [Add link here]
📹 **Video Walkthrough:** [Add video link here]

![Demo Screenshot](path/to/demo-screenshot.png)
<!-- PLACEHOLDER: Add screenshot of the app / Postman flow / dashboard -->

---

## 🏗️ Architecture

<!-- PLACEHOLDER: Add architecture diagram image here (draw.io / Excalidraw export) -->
![Architecture Diagram](path/to/architecture-diagram.png)

The system consists of **6 microservices**:

| Service | Responsibility |
|---|---|
| **Eureka Server** | Service registry for discovering all microservices |
| **Config Server** | Centralized configuration management for all services |
| **API Gateway** | Routes requests and handles authentication via Keycloak |
| **User Service** | Registers and manages users |
| **Activity Service** | Handles activities posted by registered users |
| **AI Recommendation Service** | Receives health metrics, sends structured prompts to Gemini API, stores AI-generated recommendations |

**Supporting infrastructure:**
- **RabbitMQ** – Asynchronous communication between services
- **Spring Cloud** – Service discovery, configuration, and routing
- **Keycloak** – Authentication & role-based access control
- **PostgreSQL & MongoDB** – Persistent data storage
- **Gemini API** – AI-powered health recommendation engine

---

## ✨ Features

- 🔹 **Personalized Health Recommendations** — based on metrics like calories burned, heart rate, blood pressure, and activity type
- 🔹 **Activity Management** — users can post and track health-related activities
- 🔹 **AI-Powered Insights** — Gemini API generates recommendations via structured prompts
- 🔹 **Microservices Architecture** — each service is independent, scalable, and maintainable
- 🔹 **Asynchronous Communication** — RabbitMQ decouples service interactions
- 🔹 **Secure User Management** — Keycloak handles authentication and authorization
- 🔹 **Persistent Storage** — reliable storage of user data, activities, and recommendations

---

## ⚙️ How It Works

1. **User Registration & Authentication** — users register via User Service; auth handled through Keycloak
2. **Activity Posting** — users post activities via Activity Service
3. **Health Metric Submission** — metrics (calories burned, heart rate, blood pressure, activity type) sent to AI Recommendation Service
4. **Asynchronous Processing** — data flows through RabbitMQ to the AI service
5. **AI Recommendation Generation** — service formats the Gemini API prompt, receives and stores recommendations
6. **Response Delivery** — recommendations returned to users via API Gateway

---

## 📡 API Endpoints

<!-- PLACEHOLDER: Fill in your actual endpoints per service, or link a Postman/Swagger doc -->
📄 **Full API Docs:** [Add Postman collection or Swagger/OpenAPI link here]

### User Service
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/users/register` | Register a new user |
| POST | `/api/users/login` | Authenticate user |
| GET | `/api/users/{id}` | Get user profile by ID |

### Activity Service
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/activities` | Post a new health activity |
| GET | `/api/activities/{userId}` | Get all activities for a user |

### AI Recommendation Service
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/recommendations` | Submit health metrics, receive AI recommendation |
| GET | `/api/recommendations/{userId}` | Get past recommendations for a user |

<!-- Add/edit rows above to match your actual controllers -->

---

## 🔒 Security

- **Keycloak Integration** — role-based access control and secure authentication
- **JWT Tokens** — used for service-to-service communication and user sessions

---

## 🛠️ Tech Stack

**Backend:** Java, Spring Boot, Spring Cloud
**Messaging:** RabbitMQ
**Auth:** Keycloak, JWT
**Database:** PostgreSQL, MongoDB
**AI:** Google Gemini API
**Service Discovery/Config:** Eureka, Spring Cloud Config

---

## 🚀 Setup & Installation

<!-- PLACEHOLDER: Add/verify actual setup steps -->
```bash
# Clone the repo
git clone https://github.com/Mahak-10/AI-Health-Recommendation-System.git

# Start Eureka Server
cd eureka-server && ./mvnw spring-boot:run

# Start Config Server
cd config-server && ./mvnw spring-boot:run

# Start remaining services (API Gateway, User, Activity, AI Recommendation)
...
```

**Prerequisites:** Java 17+, Maven, Docker (for RabbitMQ/Keycloak/PostgreSQL/MongoDB), Gemini API key

---

## 🧪 Testing

<!-- PLACEHOLDER: Mention test coverage / how to run tests -->
```bash
./mvnw test
```

---

## 🧩 Challenges Solved

<!-- PLACEHOLDER: 2-3 sentences on a real technical challenge — e.g. handling async Gemini API latency via RabbitMQ, or securing inter-service calls with JWT -->

---

## 🔮 Future Enhancements

- 📊 Real-time dashboards for user health metrics
- 🔮 Predictive analytics for early detection of potential health risks
- ⌚ Integration with wearable devices for automated data collection

---

## 👤 Author

**Mahak Singh** — [GitHub](https://github.com/Mahak-10)
