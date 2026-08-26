# Frontend

# Frontend build image
FROM node:24-alpine AS frontend-builder

# Working directory for the frontend build process
WORKDIR /app/frontend

# Copy frontend dependencies configuration
COPY frontend/package*.json ./

# Download frontend dependencies
RUN npm ci

# Copy frontend source code and compile
COPY frontend/ ./
RUN npm run build

# Backend

# Compiling image
FROM maven:3.9-eclipse-temurin-25 AS builder

# Working directory for the build process
WORKDIR /project

# Copy dependencies configuration file
COPY backend/pcmod/pom.xml /project/

# Download project dependencies
RUN mvn dependency:go-offline

# Copy project source code
COPY backend/pcmod/src /project/src

# Copy compiled frontend to backend static resources
COPY --from=frontend-builder /app/frontend/build/client /project/src/main/resources/static

# Compile project and package it into a JAR file, no test
RUN mvn -B package -DskipTests

# Run PCMod Application

# JRE image
FROM eclipse-temurin:25-jre

# JAR directory
WORKDIR /usr/src/app/

# Copy JAR
COPY --from=builder /project/target/*.jar ./app.jar

# Listening port
EXPOSE 443

# Command to run the application
CMD [ "java", "-jar", "app.jar" ]