FROM maven:3.9.16-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -ntp clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S autotrack && adduser -S autotrack -G autotrack
COPY --from=build /workspace/target/AutoTrack.jar app.jar
USER autotrack
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
