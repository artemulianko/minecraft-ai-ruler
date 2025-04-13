import * as cdk from 'aws-cdk-lib';
import {Construct} from "constructs";
import {NetworkLoadBalancer} from 'aws-cdk-lib/aws-elasticloadbalancingv2';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as ecs from 'aws-cdk-lib/aws-ecs';

interface NlbStackProps {
    vpc: ec2.IVpc;
    publicSubnets: ec2.ISubnet[];
    ecsService: ecs.Ec2Service
}


export default class NlbStack extends Construct {
    constructor(scope: Construct, { vpc, publicSubnets, ecsService }: NlbStackProps) {
        super(scope, 'NlbStack');

        const nlb = new NetworkLoadBalancer(this, 'MinecraftNLB', {
            vpc,
            internetFacing: true, // Publicly accessible
            vpcSubnets: { subnets: publicSubnets }
        });

        const listener = nlb.addListener('MinecraftListener', {
            port: 25565, // Minecraft port
        });

        listener.addTargets('MinecraftTarget', {
            port: 25565, // Forward traffic to Minecraft port
            targets: [ecsService], // ECS service as target
            healthCheck: {
                enabled: true,
                port: '25565',
                interval: cdk.Duration.seconds(30),
                healthyThresholdCount: 2,
                unhealthyThresholdCount: 2,
                timeout: cdk.Duration.seconds(5),
            },
        });

        new cdk.CfnOutput(this, 'NLBPublicDNS', {
            value: nlb.loadBalancerDnsName,
            description: 'Public DNS name of the Network Load Balancer',
            exportName: 'MinecraftNLBPublicDNS',
        });
    }
}