# 1. Build Stage
FROM gradle:8.5-jdk21 AS builder
ARG SERVICE_NAME
WORKDIR /build
COPY ./services/${SERVICE_NAME} ./service
COPY ./proto /proto
WORKDIR /build/service
RUN gradle build --no-daemon -x test

# 2. Runtime Stage
FROM eclipse-temurin:21-jdk
ARG SERVICE_NAME
WORKDIR /app
COPY --from=builder /build/service/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.profiles.active=compose", "-jar", "app.jar"]