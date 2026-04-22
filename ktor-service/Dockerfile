FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /home/gradle/src

# Gradle Wrapper
COPY gradlew .
COPY gradle/ gradle/
RUN chmod +x gradlew

# Gradle-9.3.0 в отдельном слое
RUN ./gradlew --version --no-daemon

# только файлы конфигурации зависимостей
COPY build.gradle.kts settings.gradle.kts gradle.properties* ./

COPY src/ src/

# BuildKit
# Флаг --mount сохранит скачанные библиотеки (Ktor, Kotlin и т.д.) между запусками
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew buildFatJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine AS run
WORKDIR /app

COPY --from=build /home/gradle/src/build/libs/*-all.jar /app/app.jar

EXPOSE 3000

ENTRYPOINT ["java", "-Xmx256m", "-XX:+UseSerialGC", "-jar", "/app/app.jar"]