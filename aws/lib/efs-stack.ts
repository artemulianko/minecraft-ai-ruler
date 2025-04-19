import * as cdk from 'aws-cdk-lib';
import {RemovalPolicy} from 'aws-cdk-lib';
import * as efs from 'aws-cdk-lib/aws-efs';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import {Construct} from "constructs";

interface EfsStackProps {
    vpc: ec2.IVpc;
}

export default class EfsStack extends cdk.Stack {
    public readonly fileSystem: efs.FileSystem;

    constructor(scope: Construct, {vpc}: EfsStackProps) {
        super(scope, 'EfsStack');

        const securityGroup = new ec2.SecurityGroup(this, 'EFSSecurityGroup', {
            vpc,
            allowAllOutbound: true,
            description: 'Security group for EFS allowing NFS access',
        });
        securityGroup.addIngressRule(
            ec2.Peer.ipv4(vpc.vpcCidrBlock),
            ec2.Port.NFS,
            'Allow NFS access in VPC'
        );

        this.fileSystem = new efs.FileSystem(this, 'ServerEfs', {
            vpc,
            securityGroup,
            performanceMode: efs.PerformanceMode.GENERAL_PURPOSE,
            removalPolicy: RemovalPolicy.DESTROY,
        });
    }
}