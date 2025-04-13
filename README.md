# Minecraft server with OpenAI integration

## forge
Server with mod itself

## AWS Infrastructure as Code (IaC) for Hosting a Minecraft Server

This project sets up an **AWS ECS-backed Minecraft Server** using **Cloud Development Kit (CDK)**. It is designed to deploy a scalable, highly available infrastructure for hosting your own Minecraft server.
### Features
The infrastructure includes the following components:
- **VPC** with **private** and **public subnets** spread across 2 Availability Zones (**2 AZs**) for high availability.
- **Elastic Container Registry (ECR)** to store custom Docker container images (optional if using default images).
- **ECS (Amazon Elastic Container Service)** stack to manage and run the Minecraft server.
- **Application Load Balancer (ALB)** for routing traffic to the Minecraft server tasks.
- **Auto Scaling** for instances running ECS, allowing scalability as server capacity changes.
