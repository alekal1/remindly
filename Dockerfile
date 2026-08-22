FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew

COPY src ./src
RUN ./gradlew bootJar --no-daemon
RUN find build/libs -maxdepth 1 -name '*.jar' ! -name '*-plain.jar' -exec cp {} /tmp/app.jar \;

FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /tmp/app.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
EXPOSE 8080
