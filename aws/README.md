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

- A VPC with public and private subnets, including a NAT gateway
- Security group allowing SSH (port 22) and Minecraft (port 25565) traffic
- ECS cluster with t2.small instances running Amazon Linux 2
- ECS service with a Minecraft server container
- Network Load Balancer properly configured for TCP traffic on port 25565

## Post-Deployment Steps

After deployment:

1. The Minecraft server will be automatically deployed and started on ECS.

2. Get the DNS name of the Network Load Balancer from the CloudFormation outputs.

3. Connect to your Minecraft server using the NLB DNS name on port 25565.

4. To access the ECS instance for debugging (if needed):

```bash
# Find the instance ID running your ECS task
aws ecs list-container-instances --cluster <your-cluster-name>

# Connect to the instance 
aws ssm start-session --target <instance-id>
# OR
ssh -i "minecraft-server-key.pem" ec2-user@<instance-ip>
```

## Cleanup

To destroy all resources created by this stack:

```bash
npm run destroy
```

## Configuration

To modify the server configuration:

- Change instance type in `lib/ecs-stack.ts`
- Adjust container settings in `lib/ecs-stack.ts`
- Add additional environment variables for the Minecraft server in the container definition
- For advanced Minecraft configuration, you can customize the container image or create a custom one based on `itzg/minecraft-server`

## Security Considerations

- The security group is configured to allow connections from any IP address. For production use, consider restricting this to known IP ranges.
- SSH access is open to all IPs. For production use, restrict this to your trusted IP addresses.
- Consider enabling AWS WAF for additional protection against common attacks.
- The ECS task has appropriate IAM permissions with least privilege.