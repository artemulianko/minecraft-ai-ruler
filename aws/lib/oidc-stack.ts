import * as cdk from 'aws-cdk-lib';
import * as iam from 'aws-cdk-lib/aws-iam';
import {Construct} from "constructs";

export default class OidcStack extends cdk.Stack {
    public readonly gitHubOidcProvider: iam.OpenIdConnectProvider;

    constructor(scope: Construct) {
        super(scope, 'OidcStack');

        this.gitHubOidcProvider = new iam.OpenIdConnectProvider(this, 'GitHubProvider', {
            url: 'https://token.actions.githubusercontent.com',
            thumbprints: ['6938fd4d98bab03faadb97b34396831e3780aea1'],
            clientIds: [ 'sts.amazonaws.com' ],
        });

        const gitHubActionsRole = new iam.Role(this, 'GitHubActionsRole', {
            assumedBy: new iam.FederatedPrincipal(
                this.gitHubOidcProvider.openIdConnectProviderArn,
                {
                    'StringEquals': {
                        'token.actions.githubusercontent.com:sub': 'repo:artemulyanko/minecraft-ai-ruler:*',
                        'token.actions.githubusercontent.com:aud': 'sts.amazonaws.com',
                    },
                },
                'sts:AssumeRoleWithWebIdentity'
            ),
            managedPolicies: [
                // TODO: this scope must be reduced
                iam.ManagedPolicy.fromAwsManagedPolicyName('AmazonEC2ContainerRegistryFullAccess'),
                iam.ManagedPolicy.fromAwsManagedPolicyName('AmazonECS_FullAccess')
            ],
            description: 'Role for GitHub Actions to assume via OIDC',
        });

        new cdk.CfnOutput(this, 'GitHubActionsRoleArn', {
            value: gitHubActionsRole.roleArn,
            description: 'Role being used in github pipelines',
            exportName: 'GitHubActionsRoleArn'
        })
    }
}