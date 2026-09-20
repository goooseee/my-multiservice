FROM gradle:9.7.1-jdk21 AS builder

WORKDIR /app

ENV GRADLE_USER_HOME=/gradle-cache

COPY . .

ARG MODULE_NAME

RUN --mount=type=cache,target=/gradle-cache,sharing=shared \
    gradle :${MODULE_NAME}:bootJar -x test --no-daemon --build-cache

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

ARG MODULE_NAME
COPY --from=builder /app/${MODULE_NAME}/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]