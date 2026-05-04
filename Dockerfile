# Multi-stage Dockerfile for ModResorts Spring Boot Application
# Build Stage: Use Maven with Eclipse Temurin JDK 8
FROM maven:3.8.6-openjdk-8-slim AS builder

# Set working directory
WORKDIR /workspace

# Copy Maven configuration files first for dependency caching
COPY pom.xml .

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src
COPY WebContent ./WebContent

# Build the application (skip tests for faster builds)
RUN mvn clean package -DskipTests -B

# Runtime Stage: Use explicit base image provided
FROM eclipse-temurin:8-jdk

# Set working directory
WORKDIR /app

# Create non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copy the built WAR file from builder stage
COPY --from=builder /workspace/target/*.war app.war

# Set ownership to non-root user
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 8080

# Set JVM options for containerized environment
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Set timezone
ENV TZ=UTC

# Set Spring profile for Docker environment
ENV SPRING_PROFILES_ACTIVE=docker

# Run the Spring Boot application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.war"]
