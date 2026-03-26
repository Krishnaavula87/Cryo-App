# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# Copy parent pom first
COPY pom.xml .

# Copy all module poms
COPY common-lib/pom.xml common-lib/
COPY eureka-server/pom.xml eureka-server/
COPY gateway-service/pom.xml gateway-service/
COPY auth-service/pom.xml auth-service/
COPY freezer-service/pom.xml freezer-service/
COPY alert-service/pom.xml alert-service/
COPY export-service/pom.xml export-service/

# Copy source code
COPY common-lib/src common-lib/src
COPY auth-service/src auth-service/src

# Build
RUN mvn clean package -pl auth-service -am -DskipTests

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/auth-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]