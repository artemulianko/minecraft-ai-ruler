# Use an official OpenJDK image with Java 21 as the base image
FROM openjdk:21-jdk-slim AS build

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
COPY . .

# Build the Forge server and the mod
RUN ./gradlew build


FROM itzg/minecraft-server AS server
ARG OPENAI_API_KEY
ENV OPENAI_API_KEY ${OPENAI_API_KEY}

# Copy mods from /data/mods in the base image to /mods in the server image
COPY --from=build /app/build/libs/*.jar /mods/
