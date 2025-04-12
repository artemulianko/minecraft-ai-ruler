import * as cdk from 'aws-cdk-lib';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as iam from 'aws-cdk-lib/aws-iam';
import { Construct } from 'constructs';

export class MinecraftServerStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    // Create VPC
    const vpc = new ec2.Vpc(this, 'MinecraftVPC', {
      maxAzs: 2,
      natGateways: 0,
      subnetConfiguration: [
        {
          cidrMask: 24,
          name: 'public',
          subnetType: ec2.SubnetType.PUBLIC,
        }
      ]
    });

    // Security Group
    const securityGroup = new ec2.SecurityGroup(this, 'MinecraftSecurityGroup', {
      vpc,
      description: 'Allow SSH (22) and Minecraft (25565) traffic',
      allowAllOutbound: true
    });

    // Add SSH ingress rule
    securityGroup.addIngressRule(
      ec2.Peer.anyIpv4(),
      ec2.Port.tcp(22),
      'Allow SSH access from anywhere'
    );

    // Add Minecraft ingress rule
    securityGroup.addIngressRule(
      ec2.Peer.anyIpv4(),
      ec2.Port.tcp(25565),
      'Allow Minecraft access from anywhere'
    );

    // IAM Role for EC2 Instance
    const role = new iam.Role(this, 'MinecraftServerRole', {
      assumedBy: new iam.ServicePrincipal('ec2.amazonaws.com'),
      managedPolicies: [
        iam.ManagedPolicy.fromAwsManagedPolicyName('AmazonSSMManagedInstanceCore')
      ]
    });

    // EC2 Instance
    const instance = new ec2.Instance(this, 'MinecraftServer', {
      vpc,
      instanceType: ec2.InstanceType.of(ec2.InstanceClass.T3, ec2.InstanceSize.MEDIUM),
      machineImage: ec2.MachineImage.latestAmazonLinux2023(),
      securityGroup,
      role,
      keyName: 'minecraft-server-key', // Make sure to create this key pair in your AWS account
      userData: ec2.UserData.forLinux(),
    });

    // User data script to install Docker and run Minecraft server
    const userDataScript = `
#!/bin/bash
yum update -y
yum install -y docker
systemctl start docker
systemctl enable docker
usermod -a -G docker ec2-user

# Create Minecraft server directory
mkdir -p /home/ec2-user/minecraft-server
cd /home/ec2-user/minecraft-server

# Create Dockerfile
cat > Dockerfile << 'EOL'
FROM openjdk:17-slim

WORKDIR /data

# Install necessary packages
RUN apt-get update && \\
    apt-get install -y wget && \\
    apt-get clean && \\
    rm -rf /var/lib/apt/lists/*

# Copy Minecraft server files from local build
COPY ./forge-server /data

# Make run script executable
RUN chmod +x /data/run.sh || echo "No run.sh file found"

# Expose Minecraft port
EXPOSE 25565

# Set entrypoint
ENTRYPOINT ["sh", "-c", "cd /data && java -Xmx2G -Xms1G -jar forge-*.jar nogui"]
EOL

# Create docker-compose.yml
cat > docker-compose.yml << 'EOL'
version: '3'
services:
  minecraft:
    build: .
    ports:
      - "25565:25565"
    volumes:
      - ./data:/data
    environment:
      - EULA=TRUE
    restart: unless-stopped
EOL

# Install Docker Compose
curl -L "https://github.com/docker/compose/releases/download/v2.23.3/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# Important: For a real deployment, you would copy your Minecraft mod files here
# The following steps are placeholders for your actual file copying process

# In a real scenario, you would copy files from S3 or other storage:
# aws s3 cp s3://your-bucket/minecraft-ai-server.zip .
# unzip minecraft-ai-server.zip

echo "Setup complete! To complete deployment:"
echo "1. Copy your Minecraft server files to /home/ec2-user/minecraft-server/forge-server"
echo "2. Run 'docker-compose up -d' to start the server"
`;

    // Add user data script to instance
    instance.addUserData(userDataScript);

    // Output the instance's public IP
    new cdk.CfnOutput(this, 'MinecraftServerPublicIP', {
      value: instance.instancePublicIp,
      description: 'The public IP address of the Minecraft server',
      exportName: 'MinecraftServerPublicIP',
    });

    new cdk.CfnOutput(this, 'SSHCommand', {
      value: `ssh -i "minecraft-server-key.pem" ec2-user@${instance.instancePublicIp}`,
      description: 'Command to SSH into the server',
    });
  }
}