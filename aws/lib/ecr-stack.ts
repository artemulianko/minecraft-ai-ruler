import * as cdk from 'aws-cdk-lib';
import * as ecr from 'aws-cdk-lib/aws-ecr';
import {Construct} from "constructs";
import * as iam from "aws-cdk-lib/aws-iam";

export default class EcrStack extends cdk.Stack {
    public readonly repository: ecr.IRepository;

    constructor(scope: Construct) {
        super(scope, 'EcrStack');

        this.repository = new ecr.Repository(this, 'MyEcsRepository', {
            repositoryName: 'minecraft-ai-server-repo',
            removalPolicy: cdk.RemovalPolicy.DESTROY,
            lifecycleRules: [
                {
                    description: 'Retain only the last 10 images',
                    maxImageCount: 10
                }
            ]
        });

        const ecrAccessUser = new iam.User(this, 'EcrAccessUser', {
            userName: 'EcrAccessUser',
            managedPolicies: [
                iam.ManagedPolicy.fromAwsManagedPolicyName('AmazonEC2ContainerRegistryFullAccess')
            ]
        });

        // Output the IAM user name
        new cdk.CfnOutput(this, 'EcrAccessUserName', {
            value: ecrAccessUser.userName,
            description: 'The IAM user name for accessing the ECR repository',
            exportName: 'EcrAccessUserName'
        });
        
        // Output the repository URI
        new cdk.CfnOutput(this, 'EcrRepositoryUri', {
            value: this.repository.repositoryUri,
            description: 'The URI of the ECR repository',
            exportName: 'EcrRepositoryUri'
        });

    }
}