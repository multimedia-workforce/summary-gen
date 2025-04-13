# 1. Build Stage
FROM gradle:8.13-jdk21 AS builder
ARG SERVICE_NAME
WORKDIR /build
COPY ./services/${SERVICE_NAME} ./service
COPY ./services/shared ./shared
COPY ./proto /proto
WORKDIR /build/service

RUN gradle build --no-daemon -x test

# 2. Runtime Stage
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=builder /build/service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=compose", "-jar", "app.jar"]