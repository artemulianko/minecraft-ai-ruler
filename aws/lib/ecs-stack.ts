import * as cdk from 'aws-cdk-lib';
import * as ecs from 'aws-cdk-lib/aws-ecs';
import {Compatibility} from 'aws-cdk-lib/aws-ecs';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as iam from 'aws-cdk-lib/aws-iam'
import * as ecr from 'aws-cdk-lib/aws-ecr';
import * as efs from 'aws-cdk-lib/aws-efs';
import * as autoscaling from 'aws-cdk-lib/aws-autoscaling';
import {Construct} from "constructs";
import {EcsVolume, EfsVolume} from "aws-cdk-lib/aws-batch";

interface EcsStackProps {
    vpc: ec2.IVpc,
    publicSubnets: ec2.ISubnet[];
    ecrRepository: ecr.IRepository;
    securityGroup: ec2.ISecurityGroup;
    fileSystem: efs.FileSystem;
}

export default class EcsStack extends cdk.Stack {
    public minecraftServerService: ecs.Ec2Service;

    private readonly cluster: ecs.Cluster;
    private readonly taskRole: iam.IRole;

    private readonly publicSubnets: ec2.ISubnet[];
    private readonly ecrRepository: ecr.IRepository;
    private readonly fileSystem: efs.FileSystem;

    constructor(scope: Construct, {
        vpc,
        fileSystem,
        publicSubnets,
        ecrRepository,
        securityGroup
    }: EcsStackProps) {
        super(scope, 'EcsStack');

        this.fileSystem = fileSystem;
        this.publicSubnets = publicSubnets;
        this.ecrRepository = ecrRepository;

        const instanceRole = new iam.Role(this, 'InstanceRole', {
            assumedBy: new iam.ServicePrincipal('ec2.amazonaws.com'),
            managedPolicies: [
                iam.ManagedPolicy.fromAwsManagedPolicyName('service-role/AmazonEC2ContainerServiceforEC2Role')
            ]
        });
        const launchTemplate = new ec2.LaunchTemplate(this, 'ASGMinecraftServerLaunchTemplate', {
            securityGroup,
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

    /**
     * Builds and configures the Minecraft server service within an ECS cluster.
     * This method creates a task definition, adds a container for the Minecraft server,
     * and sets up an ECS service with specified security groups, subnets, and desired count.
     *
     * @return {void} This method does not return a value. It initializes and sets up the ECS service.
     */
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
                GAMEMODE: "creative"
            },
            memoryLimitMiB: 2048,
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