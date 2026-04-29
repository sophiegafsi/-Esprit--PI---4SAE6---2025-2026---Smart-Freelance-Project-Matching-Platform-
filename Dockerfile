# ─────────────────────────────────────────────
# Stage 1: Build
# ─────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom first (layer caching for dependencies)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -q

# Copy source and build
COPY src ./src
RUN ./mvnw package -DskipTests -q

# ─────────────────────────────────────────────
# Stage 2: Runtime (slim image)
# ─────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

# Non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8088

ENTRYPOINT ["java", "-jar", "app.jar"]
