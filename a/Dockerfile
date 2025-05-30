# === Stage 1: Build com Maven ===
FROM maven:3.9.4-eclipse-temurin-17 AS builder
WORKDIR /app

# Copia o POM e código-fonte
COPY pom.xml .
COPY src ./src

# Compila sem testes e gera o JAR
RUN mvn clean package -DskipTests

# === Stage 2: Runtime minimalista ===
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copia o JAR do stage anterior
COPY --from=builder /app/target/*.jar app.jar

# Expõe a porta que a aplicação usa
EXPOSE 8080

# Healthcheck: verifica /ping a cada 30s, timeout de 3s
HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/ping || exit 1

# Força uso de IPv4 e porta do Render
ENV JAVA_TOOL_OPTIONS="-Dserver.port=${PORT:-8080} -Djava.net.preferIPv4Stack=true"

# Inicia a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
