FROM gradle:8.4-jdk11-alpine as builder

WORKDIR /app

COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.properties .
COPY openapi.json .
COPY src/ src/
COPY gradle/ gradle/

RUN gradle build --no-daemon -x test

FROM eclipse-temurin:11-jre-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar
COPY --from=builder /app/openapi.json ./openapi.json

EXPOSE 8080

ENV DB_URL=jdbc:postgresql://postgres:5432/booking_db
ENV DB_USER=booking_user
ENV DB_PASSWORD=booking_pass

CMD ["java", "-server", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]