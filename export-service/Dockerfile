# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app

COPY pom.xml .
COPY common-lib/pom.xml common-lib/
COPY eureka-server/pom.xml eureka-server/
COPY gateway-service/pom.xml gateway-service/
COPY freezer-service/pom.xml freezer-service/
COPY alert-service/pom.xml alert-service/
COPY export-service/pom.xml export-service/
COPY auth-service/pom.xml auth-service/

COPY common-lib/src common-lib/src
COPY export-service/src export-service/src

RUN mvn clean package -pl export-service -am -DskipTests

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/export-service/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]