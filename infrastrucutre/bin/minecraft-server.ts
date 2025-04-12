#!/usr/bin/env node
import 'source-map-support/register';
import * as cdk from 'aws-cdk-lib';
import { MinecraftServerStack } from '../lib/minecraft-server-stack';

const app = new cdk.App();
new MinecraftServerStack(app, 'MinecraftAiServerStack', {
  env: { 
    account: process.env.CDK_DEFAULT_ACCOUNT, 
    region: process.env.CDK_DEFAULT_REGION || 'us-east-1'
  },
});