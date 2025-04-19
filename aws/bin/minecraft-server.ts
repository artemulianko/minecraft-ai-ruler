#!/usr/bin/env node
import 'source-map-support/register';
import * as cdk from 'aws-cdk-lib';
import MinecraftServerEcsStack from "../lib/minecraft-server-ecs-stack";
import BaselineStack from "../lib/baseline-stack";

const app = new cdk.App();

// Persistent data stack
const baselineStack = new BaselineStack(app);

// Workload stack
new MinecraftServerEcsStack(app, {
  vpc: baselineStack.vpc,
  fileSystem: baselineStack.fileSystem,
  ecrRepository: baselineStack.ecrRepository,
  secret: baselineStack.secret,
})