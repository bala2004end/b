# ==========================================
# Build Stage
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build
WORKDIR /app

# Copy the pom.xml and install dependencies first (caches them)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the actual source code and build the JAR
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Run Stage
# ==========================================
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the standard Spring Boot port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
