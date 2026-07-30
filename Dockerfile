# ─────────────────────────────────────────────────────────────
# Stage 1: Build the React frontend
# ─────────────────────────────────────────────────────────────
FROM node:20-alpine AS frontend-build

WORKDIR /app/frontend

COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci --silent

COPY frontend/ ./
RUN npm run build

# ─────────────────────────────────────────────────────────────
# Stage 2: Build the Spring Boot backend
# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS backend-build

WORKDIR /app/backend

# Copy Maven wrapper and POM first for layer caching
COPY backend/pom.xml ./
# Install dependencies (cached unless pom.xml changes)
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B 2>/dev/null || true

COPY backend/src ./src
# Embed frontend build into Spring Boot JAR
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static

RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -B --no-transfer-progress && \
    mv target/aidb-assistant-*.jar target/app.jar

# ─────────────────────────────────────────────────────────────
# Stage 3: Runtime image
# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Security: run as non-root user
RUN addgroup -S aidb && adduser -S aidb -G aidb
USER aidb

WORKDIR /app

# Copy the built JAR from backend-build stage
COPY --from=backend-build /app/backend/target/app.jar ./app.jar

EXPOSE 8080

# JVM tuning for container environments
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1
