import { existsSync } from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { spawnSync } from 'node:child_process';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const rootDir = path.resolve(__dirname, '..');

const requiredFiles = [
  'index.html',
  'src/main.js',
  'src/styles.css',
  'scripts/dev-server.mjs'
];

const missing = requiredFiles.filter((file) => !existsSync(path.join(rootDir, file)));
if (missing.length > 0) {
  console.error(`Fichiers manquants: ${missing.join(', ')}`);
  process.exit(1);
}

for (const file of ['src/main.js', 'scripts/dev-server.mjs']) {
  const result = spawnSync(process.execPath, ['--check', path.join(rootDir, file)], {
    stdio: 'inherit'
  });

  if (result.status !== 0) {
    process.exit(result.status || 1);
  }
}

console.log('Verification front terminee.');
