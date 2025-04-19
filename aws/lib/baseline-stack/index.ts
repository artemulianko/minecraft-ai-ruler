import * as cdk from 'aws-cdk-lib';
import {Construct} from "constructs";
import OidcStack from "./oidc-stack";
import SecretsStack from "./secrets-stack";
import VpcStack from "./vpc-stack";
import EcrStack from "./ecr-stack";
import EfsStack from "./efs-stack";
import * as ec2 from "aws-cdk-lib/aws-ec2";
import * as ecr from "aws-cdk-lib/aws-ecr";
import * as efs from "aws-cdk-lib/aws-efs";
import * as sm from "aws-cdk-lib/aws-secretsmanager";

/**
 * The BaselineStack class is a custom AWS CDK stack that provisions foundational
 * resources including a VPC, ECR repository, EFS file system, and AWS Secrets Manager secret.
 * It is composed of multiple sub-stacks to logically separate resource creation.
 *
 * This stack is designed to provide the baseline infrastructure necessary for minecraft server.
 *
 * @extends cdk.Stack
 */
export default class BaselineStack extends cdk.Stack {
    public readonly vpc: ec2.IVpc;
    public readonly ecrRepository: ecr.IRepository;
    public readonly fileSystem: efs.FileSystem;
    public readonly secret: sm.Secret;

    constructor(scope: Construct) {
        super(scope, 'BaselineStack');

        new OidcStack(this);
        const secretStack = new SecretsStack(this);
        const vpcStack = new VpcStack(this);
        const ecrStack = new EcrStack(this);
        const efsStack = new EfsStack(this, {vpc: vpcStack.vpc});

        this.vpc = vpcStack.vpc;
        this.ecrRepository = ecrStack.repository;
        this.fileSystem = efsStack.fileSystem;
        this.secret = secretStack.secret;
    }
}