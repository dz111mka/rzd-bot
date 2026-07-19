FROM gradle:8.10-jdk21 AS build

WORKDIR /workspace

COPY settings.gradle build.gradle ./
COPY gradle ./gradle
COPY gradlew gradlew.bat ./
COPY src ./src

RUN gradle bootJar --no-daemon

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=docker
ENV TZ=Europe/Moscow

COPY --from=build /workspace/build/libs/rzdbot-1.0.0.jar ./app.jar

ENTRYPOINT ["java", "-XX:+HeapDumpOnOutOfMemoryError", "-XX:MinRAMPercentage=40", "-XX:MaxRAMPercentage=40", "-jar", "./app.jar"]
