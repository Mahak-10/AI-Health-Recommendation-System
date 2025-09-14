# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY mvnw pom.xml ./
COPY .mvn/ .mvn/

# Download dependencies first (cached layer)
RUN ./mvnw dependency:resolve -B

# Copy source code and build
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
