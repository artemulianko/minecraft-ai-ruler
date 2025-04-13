import * as cdk from 'aws-cdk-lib';
import {Construct} from "constructs";
import * as elbv2 from 'aws-cdk-lib/aws-elasticloadbalancingv2';
import {ApplicationProtocol} from 'aws-cdk-lib/aws-elasticloadbalancingv2';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as ecs from 'aws-cdk-lib/aws-ecs';

interface AlbStackProps {
    vpc: ec2.IVpc;
    securityGroup: ec2.ISecurityGroup;
    publicSubnets: ec2.ISubnet[];
    ecsService: ecs.Ec2Service
}

export default class AlbStack extends Construct {
    constructor(scope: Construct, { vpc, securityGroup, publicSubnets, ecsService }: AlbStackProps) {
        super(scope, 'AlbStack');

        // Create Application Load Balancer
        const alb = new elbv2.ApplicationLoadBalancer(this, 'MinecraftALB', {
            vpc,
            internetFacing: true, // Publicly accessible
            securityGroup: securityGroup,
            vpcSubnets: { subnets: publicSubnets }
        });


        // ALB Listener
        const listener = alb.addListener('MinecraftListener', {
            port: 25565, // Minecraft port
            open: true, // Allow connections
            protocol: ApplicationProtocol.HTTP,

        });

        // Register ECS service with ALB
        listener.addTargets('MinecraftTarget', {
            port: 25565, // Forward traffic to Minecraft port
            protocol: ApplicationProtocol.HTTP,
            targets: [ecsService], // ECS service as target
            healthCheck: {
                port: '25565', // Health check on Minecraft server port
                interval: cdk.Duration.seconds(30),
                path: '/', // Optional, Minecraft server may not return HTTP responses
                timeout: cdk.Duration.seconds(5),
            },
        });

        // Output for ALB DNS Name
        new cdk.CfnOutput(this, 'ALBPublicDNS', {
            value: alb.loadBalancerDnsName,
            description: 'Public DNS name of the ALB',
            exportName: 'MinecraftALBPublicDNS',
        });
    }
}