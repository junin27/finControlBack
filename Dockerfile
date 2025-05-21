# === Stage 1: Build com Maven ===
FROM maven:3.9.4-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# === Stage 2: Runtime minimalista ===
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
ENV JAVA_TOOL_OPTIONS="-Dserver.port=${PORT:-8080}"
ENTRYPOINT ["java","-jar","app.jar"]
