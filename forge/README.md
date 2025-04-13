# Running Minecraft Development Environment

Here's how to set up and run your Minecraft development environment:

## Prerequisites

1. Create a `.env` file in your project root directory. Just copy from .env.dist
2. Set `OPENAI_API_KEY` value

## Running Different Components

### 1. Run Client
```shell script
./gradlew runClient
```
This command launches the Minecraft client in development mode.

### 2. Run Game Test Server
```shell script
./gradlew runGameTestServer
```
This command starts a server specifically for running game tests.

### 3. Run Server
```shell script
./gradlew runServer
```
This command starts a Minecraft server in development mode.

## Server Configuration

After the first server start, you'll need to modify some settings in the `run/server.properties` file:

1. Set `online-mode` to false:
```properties
online-mode=false
```

2. Set gamemode to creative:
```properties
gamemode=creative
```

These settings allow you to:
- Connect to the server without Minecraft account authentication
- Start players in creative mode by default

Remember to restart the server after making these changes for them to take effect.
