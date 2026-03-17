# Stage 1: Build
FROM --platform=linux/amd64 maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -e -U -DskipTests dependency:resolve dependency:resolve-plugins
COPY src ./src
RUN mvn -q -e clean package -DskipTests

# Stage 2: Runtime
FROM --platform=linux/amd64 eclipse-temurin:21-jre-alpine
WORKDIR /app
USER 1001
COPY --from=build /app/target/essentia-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

