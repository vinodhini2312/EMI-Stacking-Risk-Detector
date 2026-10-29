# ==============================================================================
# EMI Stacking Risk Detector - Multi-Stage Production Dockerfile
# ==============================================================================

# Stage 1: Build the application JAR
FROM maven:3.9.9-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and package
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -S emiguard && adduser -S emiguard -G emiguard

# Copy compiled JAR from builder
COPY --from=builder /workspace/target/emi-stacking-risk-detector-1.0.0.jar app.jar

# Cloud deployment dynamic port handling (e.g. Render, Railway, Fly.io)
ENV PORT=8080
EXPOSE 8080

USER emiguard

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
