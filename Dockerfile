# Build stage - multi-arch (supports ARM64/Graviton + AMD64)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build application
COPY src ./src
RUN mvn clean package -DskipTests -B

# Run stage - multi-arch (supports ARM64/Graviton + AMD64)
FROM eclipse-temurin:21-jre

# Security: run as non-root user
RUN groupadd -g 1001 appgroup && \
    useradd -u 1001 -g appgroup -s /bin/false appuser

WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

EXPOSE 8080

# Health check for ECS/ALB/Docker
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
    CMD curl -sf http://localhost:8080/api/v1/actuator/health || exit 1

# JVM optimizations for low-memory containers (t4g.micro 1GB)
# JAVA_OPTS can be overridden via docker-compose environment
ENTRYPOINT ["sh", "-c", "java \
    -XX:+UseContainerSupport \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+UseStringDeduplication \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod} \
    ${JAVA_OPTS:--XX:MaxRAMPercentage=60.0 -XX:InitialRAMPercentage=40.0} \
    -jar app.jar"]
