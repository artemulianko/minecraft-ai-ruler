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

        this.fileSystem = new efs.FileSystem(this, 'ServerEfs', {
            vpc,
            oneZone: true,
            performanceMode: efs.PerformanceMode.GENERAL_PURPOSE,
            removalPolicy: RemovalPolicy.DESTROY,
        });
    }
}