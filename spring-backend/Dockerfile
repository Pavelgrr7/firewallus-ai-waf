FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /home/gradle/src

COPY . .

RUN chmod +x gradlew
RUN ./gradlew bootJar --no-daemon -x test

RUN rm -f build/libs/*-plain.jar

RUN mv build/libs/*.jar app.jar

# легкий JRE для запуска
FROM eclipse-temurin:21-jre-alpine AS run
WORKDIR /app


COPY --from=build /home/gradle/src/app.jar /app/app.jar
# Копируем собранный JAR. У Spring он обычно называется <имя>-0.0.1-SNAPSHOT.jar
# Docker не любит копировать по маске, если под нее попадает два файла (а Spring генерит еще -plain.jar),
# поэтому мы просто берем нужный:
#RUN #find /home/gradle/src/build/libs/ -name "*.jar" ! -name "*-plain.jar" -exec cp {} /app/app.jar \;

EXPOSE 8080

ENTRYPOINT ["java", "-Xmx256m", "-XX:+UseSerialGC", "-jar", "/app/app.jar"]
