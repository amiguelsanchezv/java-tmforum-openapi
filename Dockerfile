# Multi-stage build to optimize image size
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

# Copy Maven configuration files
COPY pom.xml .
# Download dependencies (cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Compile and package the application
RUN mvn clean package -DskipTests

# Final image
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Create non-root user for security and logs directory
RUN addgroup -S spring && adduser -S spring -G spring && \
    mkdir -p /app/logs && chown -R spring:spring /app

# Change to spring user
USER spring:spring

# Copy the JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/actuator/health || exit 1

# Default environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

