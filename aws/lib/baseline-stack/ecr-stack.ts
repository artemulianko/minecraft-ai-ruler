import * as cdk from 'aws-cdk-lib';
import * as ecr from 'aws-cdk-lib/aws-ecr';
import {Construct} from "constructs";

export default class EcrStack extends cdk.NestedStack {
    public readonly repository: ecr.IRepository;

    constructor(scope: Construct) {
        super(scope, 'EcrStack');

        this.repository = new ecr.Repository(this, 'MyEcsRepository', {
            repositoryName: 'minecraft-ai-server-repo',
            removalPolicy: cdk.RemovalPolicy.DESTROY,
            lifecycleRules: [
                {
                    description: 'Retain only the last 2 images',
                    maxImageCount: 2
                }
            ]
        });

        new cdk.CfnOutput(this, 'EcrRepositoryUri', {
            value: this.repository.repositoryUri,
            description: 'The URI of the ECR repository',
            exportName: 'EcrRepositoryUri'
        });

    }
}