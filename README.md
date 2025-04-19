# Minecraft server with LLM integration

## Minecraft mod with LLM integration - AI Ruler Mod
```cd ./forge```

This project demonstrates a basic integration of a Minecraft server with an LLM model (GPT), including the server image build and the mod implementation.

## AWS Infrastructure as Code (IaC) for Hosting a Minecraft Server
```cd ./aws```

This project sets up an **AWS ECS-backed Minecraft Server** using **Cloud Development Kit (CDK)**.
It is designed to deploy a simple and cost-effective infrastructure.

Features:
- **Baseline Stack**:
    - **VPC Stack**: Configured with a single Availability Zone (1 AZ) and 1 public subnet for cost efficiency.
    - **ECR Stack**: Hosts the Docker image of the Minecraft server.
    - **Secrets Stack**: Securely stores sensitive data like API keys.
    - **OIDC Stack**: Enables integration with GitHub pipelines for CI/CD.
- **1-Zone EFS**: Provides cost-effective persistent storage for server data.
- **Minecraft ECS Stack**: Runs the Minecraft server as a containerized application on EC2 instances.
