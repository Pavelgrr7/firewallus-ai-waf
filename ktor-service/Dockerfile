FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /home/gradle/src

COPY . .

RUN chmod +x gradlew
RUN ./gradlew buildFatJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine AS run
WORKDIR /app

COPY --from=build /home/gradle/src/build/libs/*-all.jar /app/app.jar

EXPOSE 3000

ENTRYPOINT ["java", "-Xmx256m", "-XX:+UseSerialGC", "-jar", "/app/app.jar"]
