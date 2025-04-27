import {TypeScriptFileGenerator} from '@asyncapi/modelina'
import * as fs from "node:fs";
import * as path from "node:path";

const root = path.resolve(__dirname, '../..');

const generator = new TypeScriptFileGenerator({
  moduleSystem: 'ESM',
  modelType: 'interface',
});

const contractsBaseFiles = {
  actions: path.resolve(root, 'contracts/src/actions.ts'),
  events: path.resolve(root, 'contracts/src/events.ts')
}

console.log('Generating contracts for TypeScript.');

try {
  (async () => {
    for (const [contractType, baseFile] of Object.entries(contractsBaseFiles)) {
      const input = fs.readFileSync(baseFile, 'utf8');
      console.log(`Generating contracts for ${contractType}...`);

      await generator.generateToFiles({
        input,
        baseFile,
      }, path.resolve(root, `agents/src/generated/${contractType}`), {exportType: 'named'});
    }

    console.log('Contracts generated successfully.');
  })()
} catch (e) {
  console.error('Contracts generation failed.');
}