import assert from 'node:assert/strict';
import { randomUUID, randomBytes } from 'node:crypto';
import { spawn } from 'node:child_process';
import { setTimeout as sleep } from 'node:timers/promises';
import { withLock, deleteUser, foundationRoot, localRoot, privateWrite } from './environments.mjs';
import { resolve as pathResolve } from 'node:path';

export async function bridgeCall(config, path, body = {}, token, expected = 200) {
  const response = await fetch(`${config.NEON_BRIDGE_URL}${path}`, {
    method: 'POST', headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) },
    body: JSON.stringify(body), signal: AbortSignal.timeout(120000),
  });
  assert.equal(response.status, expected, `${path}: unexpected HTTP status`);
  return response.json();
}
export async function dataCall(config, path, token, body, expected = 200) {
  const response = await fetch(`${config.NEON_DATA_API_URL.replace(/\/$/, '')}/${path}`, {
    method: body === undefined ? 'GET' : 'POST',
    headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json',
      'Accept-Profile': 'neon_scratch', 'Content-Profile': 'neon_scratch' },
    body: body === undefined ? undefined : JSON.stringify(body), signal: AbortSignal.timeout(30000),
  });
  assert.equal(response.status, expected, `${path}: unexpected HTTP status`);
  return response.json();
}
export async function withFixtures(config, fn) {
  return withLock(config, async connection => {
    const run = randomUUID(), users = [], ids = [];
    const topic = `foundation-live:${run}`;
    try {
      for (const suffix of ['owner', 'other']) {
        const email = `foundation-${suffix}-${run}@example.test`, password = randomBytes(24).toString('base64url');
        const login = await bridgeCall(config, '/auth/sign-up', { email, password, name: `Foundation ${suffix}` });
        assert.ok(login.user?.id, 'Fixture signup must create an active user');
        users.push({ ...login, email, password });
        await connection.query('INSERT INTO xt_neon_test.fixture_users(user_id,email,kind) VALUES($1,$2,$3)', [login.user.id, email, 'test']);
      }
      const owner = users[0], other = users[1], row = randomUUID(); ids.push(row);
      await connection.query('INSERT INTO neon_scratch."NeonSmoke"(id,owner_id,nickname) VALUES($1,$2,$3)', [row, owner.user.id, 'initial']);
      await connection.query('INSERT INTO xt_neon.topics(topic) VALUES($1)', [topic]);
      await connection.query('INSERT INTO xt_neon_test.topic_members(topic,user_id) VALUES($1,$2)', [topic, owner.user.id]);
      return await fn({ connection, run, topic, row, owner, other });
    } finally {
      await connection.query('ROLLBACK');
      for (const user of users) await bridgeCall(config, '/auth/sign-out', {}, user.bridge_token).catch(() => {});
      await connection.query('DELETE FROM neon_scratch."NeonSmoke" WHERE id=ANY($1::uuid[])', [ids]);
      await connection.query('DELETE FROM xt_neon.events WHERE topic=$1', [topic]);
      await connection.query('DELETE FROM xt_neon.topics WHERE topic=$1', [topic]);
      await connection.query('DELETE FROM xt_neon_test.topic_members WHERE topic=$1', [topic]);
      for (const user of users) {
        deleteUser(config, user.user.id);
        await connection.query('DELETE FROM xt_neon_test.fixture_users WHERE user_id=$1', [user.user.id]);
      }
    }
  });
}

function run(command, args, env) {
  return new Promise((resolve, reject) => {
    const child = spawn(command, args, { cwd: foundationRoot, env: { ...process.env, ...env }, stdio: ['ignore', 'pipe', 'pipe'] });
    let output = '';
    child.stdout.on('data', chunk => { output += chunk; });
    child.stderr.on('data', chunk => { output += chunk; });
    child.on('error', reject);
    const timer = setTimeout(() => { child.kill('SIGTERM'); reject(new Error('Adapter test timed out')); }, 300000);
    child.on('close', code => {
      clearTimeout(timer);
      // code.test can exit zero after failed checks. Require the actual summary,
      // an executed fact, and no thrown/failed checks.
      if (code !== 0 || !/:passed\s+[1-9]/.test(output) || /:(?:failed|throw|timeout)\s+[1-9]/.test(output) || /FAILED|THROWN|:errored/.test(output)) {
        for (const [key, value] of Object.entries(env)) {
          if (/PASSWORD|TOKEN|SECRET/.test(key) && value) output = output.split(value).join('[secret]');
        }
        privateWrite(pathResolve(localRoot, 'last-adapter-failure.log'), output);
        reject(new Error(`Adapter check failed: ${args.at(-1)} (exit ${code})`));
      } else {
        console.log(output.split('\n').filter(line => /passed \(|SUMMARY/.test(line)).join('\n'));
        resolve();
      }
    });
  });
}
export async function verifyEnvironment(config, { adapters = false } = {}) {
  const deadline = Date.now() + 120000;
  while (true) {
    try {
      const response = await fetch(`${config.NEON_BRIDGE_URL}/health`, { signal: AbortSignal.timeout(5000) });
      if (response.ok) break;
    } catch { /* A cold endpoint/container may still be starting. */ }
    if (Date.now() >= deadline) throw new Error('Neon bridge readiness timed out');
    await sleep(1000);
  }
  await withFixtures(config, async ({ connection, topic, row, owner, other }) => {
    assert.equal((await connection.query('SELECT 1 AS value')).rows[0].value, 1);
    const current = await bridgeCall(config, '/auth/session', {}, owner.bridge_token);
    assert.equal(current.user.id, owner.user.id);
    const refreshed = await bridgeCall(config, '/auth/refresh', {}, owner.bridge_token);
    assert.ok(refreshed.access_token);
    assert.deepEqual(await dataCall(config, `NeonSmoke?id=eq.${row}&select=id,nickname`, refreshed.access_token), [{ id: row, nickname: 'initial' }]);
    assert.deepEqual(await dataCall(config, `NeonSmoke?id=eq.${row}&select=id`, other.access_token), []);
    assert.deepEqual(await dataCall(config, 'rpc/echo', refreshed.access_token, { value: 'foundation-live' }), { value: 'foundation-live' });
    await bridgeCall(config, '/events/subscribe', { topics: [topic] }, other.bridge_token, 403);
    await dataCall(config, 'rpc/set_nickname', other.access_token, { row_id: row, value: 'forbidden', topic }, 403);

    const subscription = await bridgeCall(config, '/events/subscribe', { topics: [topic] }, owner.bridge_token);
    const changed = await dataCall(config, 'rpc/set_nickname', owner.access_token, { row_id: row, value: 'committed', topic });
    assert.deepEqual(changed, { id: row, owner_id: owner.user.id, nickname: 'committed' });
    const batch = await bridgeCall(config, '/events/poll', subscription, owner.bridge_token);
    assert.deepEqual(batch.events.map(event => event.payload), [{ 'db/sync': { NeonSmoke: [changed] } }]);
    await bridgeCall(config, '/events/unsubscribe', subscription, owner.bridge_token);

    await connection.query('BEGIN');
    const rollback = await connection.query('SELECT xt_neon.publish($1,$2) AS seq', [topic, { 'db/remove': { NeonSmoke: [row] } }]);
    await connection.query('ROLLBACK');
    assert.equal((await connection.query('SELECT count(*)::int AS count FROM xt_neon.events WHERE topic=$1 AND seq=$2', [topic, rollback.rows[0].seq])).rows[0].count, 0);
    const published = await connection.query('SELECT xt_neon.publish($1,$2) AS seq', [topic, { 'db/remove': { NeonSmoke: [row] } }]);
    const resumed = await bridgeCall(config, '/events/subscribe', { topics: [topic], cursor: batch.cursor }, owner.bridge_token);
    const replay = await bridgeCall(config, '/events/poll', resumed, owner.bridge_token);
    assert.equal(replay.events[0].seq, String(published.rows[0].seq));
    assert.deepEqual(replay.events[0].payload, { 'db/remove': { NeonSmoke: [row] } });
    await bridgeCall(config, '/events/unsubscribe', resumed, owner.bridge_token);
    await connection.query('SELECT xt_neon.prune($1,$2)', [topic, published.rows[0].seq]);
    await bridgeCall(config, '/events/subscribe', { topics: [topic], cursor: batch.cursor }, owner.bridge_token, 409);

    if (adapters) {
      const env = { NEON_LIVE_DATA_URL: config.NEON_DATA_API_URL, NEON_LIVE_BRIDGE_URL: config.NEON_BRIDGE_URL,
        NEON_ENV: config.NEON_ENV,
        NEON_LIVE_EMAIL: owner.email, NEON_LIVE_PASSWORD: owner.password,
        NEON_LIVE_USER_ID: owner.user.id, NEON_LIVE_ROW_ID: row, NEON_LIVE_TOPIC: topic,
        NEON_LIVE_NICKNAME: 'committed' };
      await run('lein', ['test', ':only', 'scaffold.neon-jdbc-test'], env);
      await run('lein', ['test', ':only', 'xt.db.system.impl-neon-live-test'], env);
      for (const lang of ['python', 'ruby']) {
        await connection.query('INSERT INTO neon_scratch."NeonSmoke"(id,owner_id,nickname) VALUES($1,$2,$3) ON CONFLICT(id) DO UPDATE SET nickname=excluded.nickname', [row, owner.user.id, 'committed']);
        await runSeedgen(lang);
        await run('lein', ['test', ':only', `xtbench.${lang}.db.system.impl-neon-live-test`], env);
      }
    }
    await bridgeCall(config, '/auth/sign-out', {}, owner.bridge_token);
    await bridgeCall(config, '/auth/session', {}, owner.bridge_token, 401);
  });
}
function runSeedgen(lang) {
  return new Promise((resolve, reject) => {
    const child = spawn('lein', ['seedgen', 'benchadd', '[xt.db.system.impl-neon-live-test]', ':lang', lang, ':write', 'true'], {
      cwd: foundationRoot, stdio: ['ignore', 'pipe', 'pipe'],
    });
    child.stdout.resume(); child.stderr.resume();
    child.on('error', reject);
    child.on('close', code => code === 0 ? resolve() : reject(new Error(`Seed generation failed: ${lang}`)));
  });
}
