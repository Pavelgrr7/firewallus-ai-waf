FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY gradlew .
COPY gradle/ gradle/
RUN chmod +x gradlew
RUN --mount=type=cache,id=spring-gradle,target=/root/.gradle \
    ./gradlew --version --no-daemon

COPY build.gradle settings.gradle ./

COPY src/ src/

# Результат /root/.gradle на уровне хоста Docker
RUN --mount=type=cache,id=spring-gradle,target=/root/.gradle \
    ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=builder --chown=spring:spring /app/build/libs/app.jar app.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+UseSerialGC", "-jar", "app.jar"]