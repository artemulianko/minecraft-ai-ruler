# Minecraft AI Server Infrastructure

This project contains AWS CDK infrastructure code to deploy a Minecraft server with the AI Ruler mod to Amazon EC2 using Docker.

## Prerequisites

- AWS CLI configured with appropriate credentials
- Node.js 18.x or later
- AWS CDK v2 installed globally (`npm install -g aws-cdk`)
- An EC2 key pair named `minecraft-server-key` created in your AWS account

## Setup

1. Install dependencies:

```bash
npm install
```

2. Build the project:

```bash
npm run build
```

3. Deploy the infrastructure:

```bash
npm run deploy
```

## What Gets Deployed

- A VPC with public subnets
- Security group allowing SSH (port 22) and Minecraft (port 25565) traffic
- EC2 instance (t3.medium) with Amazon Linux 2023
- User data script that installs Docker and sets up the Minecraft server

## Post-Deployment Steps

After deployment, you'll need to:

1. Copy your Minecraft server files (with the AI mod) to the EC2 instance:

```bash
# Example using scp (replace with your key path and instance IP)
scp -i "minecraft-server-key.pem" -r /path/to/your/forge-server ec2-user@<instance-ip>:/home/ec2-user/minecraft-server/
```

2. SSH into the instance:

```bash
ssh -i "minecraft-server-key.pem" ec2-user@<instance-ip>
```

3. Build and start the Docker container:

```bash
cd /home/ec2-user/minecraft-server
docker-compose up -d
```

## Cleanup

To destroy all resources created by this stack:

```bash
npm run destroy
```

## Configuration

To modify the server configuration:

- Change instance type in `lib/minecraft-server-stack.ts`
- Adjust Docker settings in the UserData script
- Modify Minecraft server settings in your forge-server files before copying them to the EC2 instance