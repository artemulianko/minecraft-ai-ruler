#!/usr/bin/env node
import 'source-map-support/register';
import * as cdk from 'aws-cdk-lib';
import VpcStack from "../lib/vpc-stack";
import EcrStack from "../lib/ecr-stack";
import OidcStack from "../lib/oidc-stack";
import EcsStack from "../lib/ecs-stack";
import EfsStack from "../lib/efs-stack";

const app = new cdk.App();

new OidcStack(app);
const vpcStack = new VpcStack(app);
const ecrStack = new EcrStack(app);
const efsStack = new EfsStack(app, {vpc: vpcStack.vpc});

new EcsStack(app, {
  vpc: vpcStack.vpc,
  fileSystem: efsStack.fileSystem,
  publicSubnets: vpcStack.vpc.publicSubnets,
  ecrRepository: ecrStack.repository,
  securityGroup: vpcStack.minecraftSG,
})