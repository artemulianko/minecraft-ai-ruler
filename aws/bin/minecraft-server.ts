#!/usr/bin/env node
import 'source-map-support/register';
import * as cdk from 'aws-cdk-lib';
import MinecraftServerStack from '../lib/minecraft-server-stack';
import VpcStack from "../lib/vpc-stack";
import EcrStack from "../lib/ecr-stack";

const app = new cdk.App();

const vpcStack = new VpcStack(app);
const ecrStack = new EcrStack(app);

new MinecraftServerStack(app, {
  vpc: vpcStack.vpc,
  privateSubnets: vpcStack.vpc.privateSubnets,
  publicSubnets: vpcStack.vpc.publicSubnets,
  minecraftServerSg: vpcStack.minecraftSG,
  ecrRepository: ecrStack.repository,
});