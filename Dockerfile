# ─── Stage 1: Build the JAR ───────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy Maven settings and POM first (for layer caching)
COPY settings.xml settings.xml
COPY pom.xml pom.xml
RUN mvn dependency:go-offline -s settings.xml -B

# Copy source and build
COPY src ./src
RUN mvn clean package -s settings.xml -DskipTests -B

# ─── Stage 2: Run the JAR ─────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy only the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose default port (Render overrides via PORT env var)
EXPOSE 8080

# Run with the prod profile
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
