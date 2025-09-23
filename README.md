# **AI Health Recommendation System**

An advanced **AI-powered health recommendation platform** that provides personalized health insights.  
Built with **Spring Boot microservices, Spring Cloud, Eureka Service Discovery, RabbitMQ, and Keycloak**, this system leverages the **Gemini API** to generate accurate health suggestions based on user-provided health metrics.

---

## **1. Architecture & Technologies**

The system consists of **6 microservices**:

1. **Eureka Server** – Service registry for discovering all microservices.  
2. **Config Server** – Centralized configuration management for all services.  
3. **API Gateway** – Routes requests and handles authentication via Keycloak.  
4. **User Service** – Registers and manages users.  
5. **Activity Service** – Handles activities posted by registered users.  
6. **AI Recommendation Service** – Receives health metrics from users, sends structured prompts to Gemini API, receives AI-generated recommendations, and stores them.  

### **Additional Components & Technologies:**
- **RabbitMQ** – Asynchronous communication between services.  
- **Spring Cloud** – Service discovery, configuration, and routing.  
- **Keycloak** – Authentication & role-based access control.  
- **PostgreSQL & MongoDB** – Persistent data storage.  
- **Gemini API** – AI-powered health recommendation engine.  

---

## **2. Features**

- 🔹 **Personalized Health Recommendations** – Based on metrics like calories burned, heart rate, blood pressure, and activity type.  
- 🔹 **Activity Management** – Users can post and track their health-related activities.  
- 🔹 **AI-Powered Insights** – Gemini API provides recommendations in a predefined prompt format.  
- 🔹 **Microservices Architecture** – Each service is independent, scalable, and maintainable.  
- 🔹 **Asynchronous Communication** – RabbitMQ ensures smooth, decoupled operations.  
- 🔹 **Secure User Management** – Keycloak handles authentication and authorization.  
- 🔹 **Persistent Storage** – Ensures reliable storage of user data, activities, and AI recommendations.  

---

## **3. How It Works**

1. **User Registration & Authentication** – Users register via User Service; authentication handled through Keycloak.  
2. **Activity Posting** – Users post activities via Activity Service.  
3. **Health Metric Submission** – Metrics like calories burned, heart rate, blood pressure, and activity type are sent to AI Recommendation Service.  
4. **Asynchronous Processing** – Data is sent through RabbitMQ to the AI service.  
5. **AI Recommendation Generation** – AI Service formats the prompt for Gemini API, receives recommendations, and stores them.  
6. **Response Delivery** – Recommendations are returned to users via API Gateway.  

---

## **4. Security**

- **Keycloak Integration** – Role-based access control and secure authentication.  
- **JWT Tokens** – Used for service-to-service communication and user sessions.  

---

## **5. Future Enhancements**

- 📊 **Real-time dashboards** for user health metrics.  
- 🔮 **Predictive analytics** for early detection of potential health risks.  
- ⌚ **Integration with wearable devices** for automated data collection.  

---
