import * as cdk from 'aws-cdk-lib';
import * as iam from 'aws-cdk-lib/aws-iam';
import * as sm from 'aws-cdk-lib/aws-secretsmanager';
import {Construct} from "constructs";

export default class SecretsStack extends cdk.NestedStack {
    public readonly secret: sm.Secret;
    public readonly secretReadPolicy: iam.PolicyStatement;

    constructor(scope: Construct) {
        super(scope, 'SecretsStack');

        this.secret = new sm.Secret(this, 'MinecraftServerSecret', {
            description: 'Secrets for Minecraft Server',
            secretObjectValue: {}
        });

        this.secretReadPolicy = new iam.PolicyStatement({
            actions: ['secretsmanager:GetSecretValue'],
            resources: [this.secret.secretArn],
        });
    }
}