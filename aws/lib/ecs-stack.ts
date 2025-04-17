import * as cdk from 'aws-cdk-lib';
import * as ecs from 'aws-cdk-lib/aws-ecs';
import {Compatibility} from 'aws-cdk-lib/aws-ecs';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as iam from 'aws-cdk-lib/aws-iam'
import * as ecr from 'aws-cdk-lib/aws-ecr';
import * as autoscaling from 'aws-cdk-lib/aws-autoscaling';
import {Construct} from "constructs";

interface EcsStackProps {
    vpc: ec2.IVpc,
    subnets: ec2.ISubnet[];
    ecrRepository: ecr.IRepository;
    securityGroup: ec2.ISecurityGroup;
}

export default class EcsStack extends cdk.NestedStack {
    public minecraftServerService: ecs.Ec2Service;

    private readonly cluster: ecs.Cluster;
    private readonly taskRole: iam.IRole;

    private readonly subnets: ec2.ISubnet[];
    private readonly ecrRepository: ecr.IRepository;
    private readonly securityGroup: ec2.ISecurityGroup;

    constructor(scope: Construct, {
        vpc,
        subnets,
        ecrRepository,
        securityGroup
    }: EcsStackProps) {
        super(scope, 'EcsStack');

        this.subnets = subnets;
        this.ecrRepository = ecrRepository;
        this.securityGroup = securityGroup;

        const instanceRole = new iam.Role(this, 'InstanceRole', {
            assumedBy: new iam.ServicePrincipal('ec2.amazonaws.com'),
            managedPolicies: [
                iam.ManagedPolicy.fromAwsManagedPolicyName('service-role/AmazonEC2ContainerServiceforEC2Role')
            ]
        });
        const launchTemplate = new ec2.LaunchTemplate(this, 'ASGMinecraftServerLaunchTemplate', {
            instanceType: new ec2.InstanceType('t2.medium'),
            machineImage: ecs.EcsOptimizedImage.amazonLinux2(),
            userData: ec2.UserData.forLinux(),
            role: instanceRole,
        });
        const autoScalingGroup = new autoscaling.AutoScalingGroup(this, 'ASGMinecraftServer', {
            vpc,
            launchTemplate,
            desiredCapacity: 1 // Important to have 1
        });
        const capacityProvider = new ecs.AsgCapacityProvider(this, 'AsgCapacityProvider', {
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
        const taskDefinition = new ecs.TaskDefinition(this, 'MinecraftServerTaskDefinition', {
            compatibility: Compatibility.EC2,
            networkMode: ecs.NetworkMode.AWS_VPC,
            taskRole: this.taskRole
        });

        taskDefinition.addContainer('MinecraftServer', {
            image: ecs.ContainerImage.fromEcrRepository(this.ecrRepository),
            // image: ecs.ContainerImage.fromRegistry('itzg/minecraft-server'),
            environment: {
                EULA: "TRUE",
                ONLINE_MODE: "false",
                GAMEMODE: "creative"
            },
            memoryLimitMiB: 2048,
            cpu: 512,
            logging: ecs.LogDrivers.awsLogs({streamPrefix: 'MinecraftServer'}),
            portMappings: [{
                containerPort: 25565,
                hostPort: 25565
            }]
        })

        this.minecraftServerService = new ecs.Ec2Service(this, 'MinecraftServerService', {
            cluster: this.cluster,
            taskDefinition,
            // Open minecraft server port
            securityGroups: [this.securityGroup],
            // Put into private subnet
            vpcSubnets: {subnets: this.subnets},
            desiredCount: 1
        })
    }
}