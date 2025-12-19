# ============================================
# Build Stage
# ============================================
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .

# Download dependencies (this layer will be cached)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -DskipTests -B

# ============================================
# Runtime Stage
# ============================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/moneymanager-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Set environment
ENV SPRING_PROFILES_ACTIVE=prod

# Run application (Render provides PORT env variable)
CMD ["java", "-Dserver.port=${PORT:8080}", "-jar", "app.jar"]