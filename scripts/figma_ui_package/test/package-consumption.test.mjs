import assert from 'node:assert/strict';
import { createRequire } from 'node:module';
import test from 'node:test';

const require = createRequire(import.meta.url);

test('the installed figma-ui package loads through CommonJS', () => {
  const ui = require('@xtalk/figma-ui');
  assert.equal(typeof ui.Button, 'function');
});

test('the installed figma-ui package loads through ESM', async () => {
  const ui = await import('@xtalk/figma-ui');
  assert.equal(typeof ui.Button, 'function');
});
