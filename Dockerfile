FROM gradle:8.7-jdk21-alpine AS builder

WORKDIR /app

COPY build.gradle .
COPY settings.gradle .
COPY src ./src
COPY config ./config

RUN java -version

RUN gradle clean build -x check -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

RUN addgroup -S spring && adduser -S spring -G spring
USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]