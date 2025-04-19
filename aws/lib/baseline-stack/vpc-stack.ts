import * as cdk from 'aws-cdk-lib';
import {Construct} from "constructs";
import * as ec2 from "aws-cdk-lib/aws-ec2";

export default class VpcStack extends cdk.NestedStack {
    public readonly vpc: ec2.Vpc;

    constructor(scope: Construct) {
        super(scope, 'VpcStack');

        this.vpc = new ec2.Vpc(this, 'MinecraftVPC', {
            maxAzs: 1,
            subnetConfiguration: [
                {
                    cidrMask: 24,
                    name: 'public',
                    subnetType: ec2.SubnetType.PUBLIC,
                },
            ]
        });

        new cdk.CfnOutput(this, "VpcId", {
            value: this.vpc.vpcId,
            description: "The ID of the VPC",
        });
    }
}