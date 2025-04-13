import * as cdk from 'aws-cdk-lib';
import {Construct} from "constructs";
import EcsStack from "./ecs-stack";
import NlbStack from "./nlb-stack"; // File still named alb-stack.ts but exports NlbStack
import * as ecr from 'aws-cdk-lib/aws-ecr';
import * as ec2 from 'aws-cdk-lib/aws-ec2';

interface MinecraftServerStackProps {
    vpc: ec2.IVpc;
    privateSubnets: ec2.ISubnet[];
    publicSubnets: ec2.ISubnet[];
    ecrRepository: ecr.IRepository;
    minecraftServerSg: ec2.ISecurityGroup;
}

export default class MinecraftServerStack extends cdk.Stack {
    constructor(scope: Construct, { vpc, privateSubnets, publicSubnets, ecrRepository, minecraftServerSg }: MinecraftServerStackProps) {
        super(scope, 'MinecraftServerStack');

        const ecsStack = new EcsStack(this, {
            vpc,
            ecrRepository,
            securityGroup: minecraftServerSg,
            subnets: privateSubnets,
        })

        new NlbStack(this, {
            vpc,
            publicSubnets,
            ecsService: ecsStack.minecraftServerService,
        })
    }
}