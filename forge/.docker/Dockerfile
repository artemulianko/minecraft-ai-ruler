# Use an official OpenJDK image with Java 21 as the base image
FROM openjdk:21-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Install required tools
RUN apt-get update && \
    apt-get install -y wget unzip git && \
    rm -rf /var/lib/apt/lists/*

# Copy the Gradle files
COPY build.gradle settings.gradle gradle.properties ./
COPY gradle ./gradle
COPY gradlew ./

# Make the gradlew script executable
RUN chmod +x ./gradlew

# Copy the source code
COPY src ./src
COPY .env ./

# Build the Forge server and the mod
RUN ./gradlew build

# Create a directory for the server
RUN mkdir -p /data

# Expose Minecraft server port
EXPOSE 25565

# Set the volume for server data
VOLUME ["/data"]

# Working directory for server
WORKDIR /data

# Copy the built mod jar to the server mods directory
RUN mkdir -p /data/mods && \
    cp /app/build/libs/*.jar /data/mods/
