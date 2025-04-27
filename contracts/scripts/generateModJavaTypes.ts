import {JavaFileGenerator} from '@asyncapi/modelina'
import * as fs from "node:fs";
import * as path from "node:path";

const root = path.resolve(__dirname, '../..');

const generator = new JavaFileGenerator({
  collectionType: 'Array',
  modelType: 'record',
});

const contractsBaseFiles = {
  actions: path.resolve(root, 'contracts/src/actions.ts'),
  events: path.resolve(root, 'contracts/src/events.ts')
}

console.log('Generating contracts for Java.');

try {
  (async () => {
    for (const [contractType, baseFile] of Object.entries(contractsBaseFiles)) {
      const input = fs.readFileSync(baseFile, 'utf8');
      console.log(`Generating contracts for ${contractType}...`);

      await generator.generateToFiles({
        input,
        baseFile,
      }, path.resolve(root, `forge/src/main/java/com/minecraftai/airulermod/generated/${contractType}`), {
        packageName: `com.minecraftai.airulermod.generated.${contractType}`
      });
    }

    console.log('Contracts generated successfully.');
  })()
} catch (e) {
  console.error('Contracts generation failed.');
}