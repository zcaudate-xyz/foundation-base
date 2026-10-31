import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mkdtempSync, readFileSync, statSync, rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { parseEnv } from 'node:util';
import { environment, ensureProject, ensureBranch, privateWrite, envText, organization, deleteUser } from '../environments.mjs';
import { serve } from '../http.mjs';

test('environment selectors reject unrelated branches and use separate local endpoints', () => {
  assert.deepEqual(environment(), { name: 'min', branch: 'neon-min', port: 18787, origin: 'http://localhost:18787' });
  assert.equal(environment('dev').port, 18788);
  assert.throws(() => environment('production'), /min or dev/);
});
test('repeated provisioning reuses the recorded project and persistent branch', () => {
  const calls = [], project = { id: 'p', name: 'StatsTrade Foundation', region_id: 'aws-ap-southeast-1', pg_version: 18 };
  const cli = args => {
    calls.push(args);
    if (args[0] === 'projects') return [project];
    if (args[0] === 'branches') return [{ id: 'b', name: 'neon-min' }];
    throw new Error('Unexpected mutation');
  };
  assert.equal(ensureProject(cli, { project_id: 'p' }), project);
  assert.equal(ensureBranch(project, environment(), cli).id, 'b');
  assert.ok(calls.every(args => args[1] === 'list'));
  assert.equal(calls[0][3], organization);
});
test('provisioning refuses a missing recorded project, wrong region, or ambiguous name', () => {
  assert.throws(() => ensureProject(() => [], { project_id: 'missing' }), /refusing/);
  const invalid = { id: 'p', name: 'StatsTrade Foundation', region_id: 'aws-ap-southeast-2', pg_version: 18 };
  assert.throws(() => ensureProject(() => [invalid]), /Singapore/);
  assert.throws(() => ensureProject(() => [invalid, invalid]), /Multiple/);
});
test('new branches come from the empty project default and are never given an expiry', () => {
  let created;
  const cli = args => args[1] === 'list' ? [{ id: 'main', default: true }] : (created = args, { branch: { id: 'new', name: 'neon-dev' } });
  assert.equal(ensureBranch({ id: 'p' }, environment('dev'), cli).id, 'new');
  assert.equal(created[created.indexOf('--parent') + 1], 'main');
  assert.equal(created[created.indexOf('--name') + 1], 'neon-dev');
  assert.ok(created.includes('--no-secrets'));
  assert.ok(!created.includes('--expires-at'));
});
test('secret files remain private even when replacing an existing file', () => {
  const dir = mkdtempSync(join(tmpdir(), 'foundation-neon-'));
  try {
    const path = join(dir, 'config.env'), values = { DATABASE_URL: 'postgres://user:secret@localhost/db', OTHER: 'spaces # quote "' };
    privateWrite(path, envText(values)); privateWrite(path, envText(values));
    assert.deepEqual({ ...parseEnv(readFileSync(path, 'utf8')) }, values);
    assert.equal(statSync(path).mode & 0o777, 0o600);
  } finally { rmSync(dir, { recursive: true }); }
});
test('cleanup tolerates an already deleted fixture user and propagates other failures', () => {
  const config = { NEON_PROJECT_ID: 'p', NEON_BRANCH_ID: 'b' };
  assert.doesNotThrow(() => deleteUser(config, 'u', () => { throw Object.assign(new Error('missing'), { notFound: true }); }));
  assert.throws(() => deleteUser(config, 'u', () => { throw new Error('unauthorized'); }), /unauthorized/);
});
test('health reports database failure without leaking a server error or credential', async t => {
  let ready = false;
  const server = serve({ handle: () => new Response('{}') }, { port: 0, health: async () => { if (!ready) throw Error('secret connection string'); } });
  await new Promise(resolve => server.once('listening', resolve));
  t.after(() => new Promise(resolve => server.close(resolve)));
  const url = `http://127.0.0.1:${server.address().port}/health`;
  const live = await fetch(url.replace('/health', '/live')); assert.equal(live.status, 200);
  assert.deepEqual(await live.json(), { status: 'alive' });
  const bad = await fetch(url); assert.equal(bad.status, 503); assert.deepEqual(await bad.json(), { status: 'unavailable' });
  ready = true;
  const good = await fetch(url); assert.equal(good.status, 200); assert.deepEqual(await good.json(), { status: 'ready' });
});
