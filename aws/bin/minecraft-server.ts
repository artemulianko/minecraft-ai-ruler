#!/usr/bin/env node
import 'source-map-support/register';
import * as cdk from 'aws-cdk-lib';
import MinecraftServerEcsStack from "../lib/minecraft-server-ecs-stack";
import BaselineStack from "../lib/baseline-stack";
import EfsStack from "../lib/efs-stack";

const app = new cdk.App();

// Baseline stack
const baselineStack = new BaselineStack(app);

// Persistent data stack
const efsStack = new EfsStack(app, {vpc: baselineStack.vpc});

// Workloads stack
new MinecraftServerEcsStack(app, {
    vpc: baselineStack.vpc,
    fileSystem: efsStack.fileSystem,
    ecrRepository: baselineStack.ecrRepository,
    secret: baselineStack.secret,
})