import * as cdk from 'aws-cdk-lib';
import * as ecs from 'aws-cdk-lib/aws-ecs';
import {Compatibility} from 'aws-cdk-lib/aws-ecs';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as iam from 'aws-cdk-lib/aws-iam'
import * as ecr from 'aws-cdk-lib/aws-ecr';
import * as efs from 'aws-cdk-lib/aws-efs';
import * as sm from 'aws-cdk-lib/aws-secretsmanager';
import * as autoscaling from 'aws-cdk-lib/aws-autoscaling';
import {Construct} from "constructs";

interface EcsStackProps {
    vpc: ec2.IVpc,
    ecrRepository: ecr.IRepository;
    fileSystem: efs.FileSystem;
    secret: sm.Secret;
}

/**
 * Represents an AWS CDK stack for deploying a Minecraft server on ECS using EC2 instances.
 * This stack provisions resources such as an appropriate ECS cluster, task definition, Elastic File System (EFS),
 * and other infrastructure necessary to run a Minecraft server.
 *
 * Key Features:
 * - Provisions an ECS cluster with a capacity provider backed by an auto-scaling EC2 Auto Scaling Group.
 * - Creates a task definition and ECS service specifically designed to run a Minecraft server container.
 * - Configures file storage through EFS for persistent server data.
 * - Associates an IAM role for task execution permissions, including accessing Secrets Manager and ECR.
 * - Exposes public subnets for server accessibility and configures security groups for Minecraft traffic.
 */
export default class MinecraftServerEcsStack extends cdk.Stack {
    private minecraftServerService: ecs.Ec2Service;

    private readonly cluster: ecs.Cluster;
    private readonly taskRole: iam.IRole;

    private readonly publicSubnets: ec2.ISubnet[];
    private readonly ecrRepository: ecr.IRepository;
    private readonly fileSystem: efs.FileSystem;
    private readonly secret: sm.Secret;

    constructor(scope: Construct, {
        vpc,
        fileSystem,
        ecrRepository,
        secret,
    }: EcsStackProps) {
        super(scope, 'MinecraftServerEcsStack');

        this.secret = secret;
        this.fileSystem = fileSystem;
        this.publicSubnets = vpc.publicSubnets;
        this.ecrRepository = ecrRepository;

        const instanceRole = new iam.Role(this, 'InstanceRole', {
            assumedBy: new iam.ServicePrincipal('ec2.amazonaws.com'),
            managedPolicies: [
                iam.ManagedPolicy.fromAwsManagedPolicyName('service-role/AmazonEC2ContainerServiceforEC2Role'),
                iam.ManagedPolicy.fromAwsManagedPolicyName('AmazonElasticFileSystemClientReadWriteAccess')
            ]
        });
        instanceRole.addToPolicy(new iam.PolicyStatement({
            actions: ['secretsmanager:GetSecretValue'],
            resources: [secret.secretArn],
        }));

        const serverSg = new ec2.SecurityGroup(this, 'MinecraftSecurityGroup', {
            vpc,
            description: 'Allow Minecraft (25565) traffic',
            allowAllOutbound: true,
        });
        serverSg.addIngressRule(
            ec2.Peer.anyIpv4(),
            ec2.Port.tcp(25565),
            'Allow Minecraft access from anywhere'
        );


        const launchTemplate = new ec2.LaunchTemplate(this, 'ASGMinecraftServerLaunchTemplate', {
            securityGroup: serverSg,
            instanceType: new ec2.InstanceType('t3.small'),
            machineImage: ecs.EcsOptimizedImage.amazonLinux2(),
            userData: ec2.UserData.forLinux(),
            role: instanceRole,
        });

        const autoScalingGroup = new autoscaling.AutoScalingGroup(this, 'ASGMinecraftServer', {
            vpc,
            launchTemplate,
            vpcSubnets: {subnets: this.publicSubnets},
        });

        const capacityProvider = new ecs.AsgCapacityProvider(this, 'ASGCapacityProvider', {
            capacityProviderName: 'ASGCapacityProvider',
            autoScalingGroup,
            machineImageType: ecs.MachineImageType.AMAZON_LINUX_2,
        });

        this.cluster = new ecs.Cluster(this, 'EcsCluster', {vpc});
        this.cluster.addAsgCapacityProvider(capacityProvider)

        this.taskRole = new iam.Role(this, 'EcsStackTaskRole', {
            assumedBy: new iam.ServicePrincipal('ecs-tasks.amazonaws.com'),
            description: 'IAM Role for ECS tasks to access ECR and other AWS services',
            managedPolicies: [
                iam.ManagedPolicy.fromAwsManagedPolicyName('service-role/AmazonECSTaskExecutionRolePolicy')
            ]
        });

        this.buildMinecraftServerService()

        new cdk.CfnOutput(this, 'EcsClusterName', {
            value: this.cluster.clusterName,
            description: 'The name of the ECS cluster',
            exportName: 'EcsClusterName',
        });

        new cdk.CfnOutput(this, 'EcsServiceArn', {
            value: this.minecraftServerService.serviceArn,
            description: 'The ARN of the ECS service',
            exportName: 'EcsServiceArn',
        });
    }

    private buildMinecraftServerService(): void {
        const serverVolumeName = 'ServerVolume';
        const taskDefinition = new ecs.TaskDefinition(this, 'MinecraftServerTaskDefinition', {
            compatibility: Compatibility.EC2,
            networkMode: ecs.NetworkMode.BRIDGE,
            taskRole: this.taskRole,
        });
        taskDefinition.addVolume({
            name: serverVolumeName,
            efsVolumeConfiguration: {
                fileSystemId: this.fileSystem.fileSystemId,
            }
        })

        const container = taskDefinition.addContainer('MinecraftServer', {
            image: ecs.ContainerImage.fromEcrRepository(this.ecrRepository),
            // image: ecs.ContainerImage.fromRegistry('itzg/minecraft-server'),
            environment: {
                EULA: "TRUE",
                ONLINE_MODE: "false",
                GAMEMODE: "creative",
                MEMORY: '1G',
            },
            secrets: {
                OPENAI_API_KEY: ecs.Secret.fromSecretsManager(this.secret, 'openai-api-key'),
            },
            memoryLimitMiB: 1024,
            cpu: 1024,
            logging: ecs.LogDrivers.awsLogs({streamPrefix: 'MinecraftServer'}),
            portMappings: [{
                containerPort: 25565,
                hostPort: 25565
            }]
        });
        container.addMountPoints({
                sourceVolume: serverVolumeName,
                containerPath: '/data',
                readOnly: false,
            }
        )

        this.minecraftServerService = new ecs.Ec2Service(this, 'MinecraftServerService', {
            cluster: this.cluster,
            taskDefinition,
            desiredCount: 1
        })
    }
}