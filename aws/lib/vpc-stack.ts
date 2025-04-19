import * as cdk from 'aws-cdk-lib';
import {Construct} from "constructs";
import * as ec2 from "aws-cdk-lib/aws-ec2";

export default class VpcStack extends cdk.Stack {
    public readonly vpc: ec2.Vpc;
    public readonly minecraftSG: ec2.SecurityGroup;

    constructor(scope: Construct) {
        super(scope, 'VpcStack');

        this.vpc = new ec2.Vpc(this, 'MinecraftVPC', {
            maxAzs: 1,
            ipAddresses: ec2.IpAddresses.cidr('10.0.0.0/16'),
            subnetConfiguration: [
                {
                    cidrMask: 24,
                    name: 'public',
                    subnetType: ec2.SubnetType.PUBLIC,
                },
            ]
        });

        this.minecraftSG = new ec2.SecurityGroup(this, 'MinecraftSecurityGroup', {
            vpc: this.vpc,
            description: 'Allow Minecraft (25565) traffic',
            allowAllOutbound: true,
        });

        this.minecraftSG.addIngressRule(
            ec2.Peer.anyIpv4(),
            ec2.Port.tcp(25565),
            'Allow Minecraft access from anywhere'
        );

        new cdk.CfnOutput(this, "VpcId", {
            value: this.vpc.vpcId,
            description: "The ID of the VPC",
        });
    }
}