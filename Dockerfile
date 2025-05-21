# === Stage 1: Build com Maven ===
FROM maven:3.8.6-openjdk-17 AS builder

# Cria pasta de trabalho e copia apenas o pom + fontes
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Compila o projeto e gera o JAR (skipTests para acelerar)
RUN mvn clean package -DskipTests

# === Stage 2: Runtime minimalista ===
FROM eclipse-temurin:17-jdk-alpine

# Pasta onde o JAR será rodado
WORKDIR /app

# Copia o JAR gerado no estágio anterior
COPY --from=builder /app/target/*.jar app.jar

# Permite que o container escute na porta que o Render informar
ENV JAVA_TOOL_OPTIONS="-Dserver.port=${PORT:-8080}"

# Comando padrão para iniciar sua API
ENTRYPOINT ["java", "-jar", "app.jar"]
