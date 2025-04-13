import * as cdk from 'aws-cdk-lib';
import {Construct} from "constructs";
import * as ec2 from "aws-cdk-lib/aws-ec2";

export default class VpcStack extends cdk.Stack {
    public readonly vpc: ec2.Vpc;
    public readonly minecraftSG: ec2.SecurityGroup;

    constructor(scope: Construct) {
        super(scope, 'VpcStack');

        this.vpc = new ec2.Vpc(this, 'MinecraftVPC', {
            maxAzs: 2,
            natGateways: 1,
            ipAddresses: ec2.IpAddresses.cidr('10.0.0.0/16'),
            subnetConfiguration: [
                {
                    cidrMask: 24,
                    name: 'public',
                    subnetType: ec2.SubnetType.PUBLIC,
                },
                {
                    cidrMask: 24,
                    name: 'internal',
                    subnetType: ec2.SubnetType.PRIVATE_WITH_EGRESS,
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
        this.minecraftSG.addIngressRule(
            ec2.Peer.anyIpv4(),
            ec2.Port.tcp(22),
            'Allow SSH access from anywhere'
        );

        new cdk.CfnOutput(this, "VpcId", {
            value: this.vpc.vpcId,
            description: "The ID of the VPC",
        });

        new cdk.CfnOutput(this, "PublicSubnets", {
            value: this.vpc.publicSubnets.map(subnet => subnet.subnetId).join(", "),
            description: "The IDs of the public subnets",
        });

        new cdk.CfnOutput(this, "PrivateSubnets", {
            value: this.vpc.privateSubnets.map(subnet => subnet.subnetId).join(", "),
            description: "The IDs of the private subnets",
        });

    }
}