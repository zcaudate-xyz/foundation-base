import { execFileSync } from 'node:child_process';
import { readFileSync, writeFileSync, mkdirSync, chmodSync, existsSync, unlinkSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { parseEnv } from 'node:util';
import { randomBytes } from 'node:crypto';
import { Pool } from 'pg';
import { createNeonAuth } from './neon.mjs';

export const packageRoot = dirname(fileURLToPath(import.meta.url));
export const foundationRoot = resolve(packageRoot, '../..');
export const composeRoot = resolve(foundationRoot, 'docker/neon');
export const localRoot = resolve(composeRoot, '.local');
export const organization = 'org-lucky-feather-94178133';
export const projectName = 'StatsTrade Foundation';

export function environment(name = 'min') {
  if (!['min', 'dev'].includes(name)) throw new Error('NEON_ENV must be min or dev');
  const port = name === 'min' ? 18787 : 18788;
  return { name, branch: `neon-${name}`, port, origin: `http://localhost:${port}` };
}
export function privateWrite(path, content) {
  mkdirSync(dirname(path), { recursive: true, mode: 0o700 });
  writeFileSync(path, content, { mode: 0o600 });
  chmodSync(path, 0o600);
}
export function envText(values) {
  return Object.entries(values).map(([key, input]) => {
    const value = String(input);
    if (/[\r\n\0]/.test(value) || (value.includes("'") && value.includes('"'))) throw new Error('Environment values must fit a single dotenv quoted value');
    const quote = value.includes("'") ? '"' : "'";
    return `${key}=${quote}${value}${quote}`;
  }).join('\n') + '\n';
}
export function readEnvironment(name) {
  const selected = environment(name), path = resolve(localRoot, `${selected.name}.env`);
  if (!existsSync(path)) throw new Error(`Missing ${selected.name} configuration; run make neon-setup`);
  const config = parseEnv(readFileSync(path, 'utf8'));
  for (const key of ['DATABASE_URL_UNPOOLED', 'NEON_DATA_API_URL', 'NEON_AUTH_BASE_URL', 'NEON_REALTIME_URL', 'NEON_REALTIME_SECRET', 'NEON_PROJECT_ID', 'NEON_BRANCH_ID']) {
    if (!config[key]) throw new Error(`Missing ${key}; run make neon-setup`);
  }
  if (config.NEON_BRANCH !== selected.branch) throw new Error('Neon branch configuration mismatch');
  return { ...config, NEON_ENV: selected.name, NEON_BRIDGE_URL: selected.origin, APP_ORIGIN: selected.origin };
}
export function neon(args, { input, json = true } = {}) {
  try {
    const result = execFileSync('neon', [...args, ...(json ? ['--output', 'json'] : [])], {
      input, encoding: 'utf8', stdio: ['pipe', 'pipe', 'pipe'], timeout: 300000,
      env: { ...process.env, CI: '1' }, maxBuffer: 4 * 1024 * 1024,
    }).trim();
    return json && result ? JSON.parse(result) : result;
  } catch (error) {
    // Never interpolate CLI output: connection-string and env commands return secrets.
    const failure = new Error(`Neon command failed: ${args.slice(0, 2).join(' ')} (exit ${error.status ?? 'timeout'}). Check CLI authentication and service availability.`);
    failure.notFound = /(?:HTTP\s*404|\b404\b|user[^\n]*(?:not found|does not exist))/i.test(String(error.stderr || ''));
    throw failure;
  }
}
export function ensureProject(cli = neon, saved) {
  const projects = cli(['projects', 'list', '--org-id', organization]);
  const matches = (projects.projects || projects).filter(p => saved ? p.id === saved.project_id : p.name === projectName);
  if (matches.length > 1) throw new Error('Multiple Foundation projects found; select one in the local project descriptor');
  if (saved && !matches.length) throw new Error('Recorded Foundation project is unavailable; refusing to create a replacement');
  let project = matches[0];
  if (!project) {
    const result = cli(['projects', 'create', '--name', projectName, '--org-id', organization,
      '--region-id', 'aws-ap-southeast-1', '--pg-version', '18', '--cu', '0.25', '--no-secrets']);
    project = result.project || result;
  }
  if (project.name !== projectName || project.region_id !== 'aws-ap-southeast-1' || project.pg_version !== 18) {
    throw new Error('Foundation project must use the expected name, Singapore region and Postgres 18');
  }
  return project;
}
export function ensureBranch(project, selected, cli = neon) {
  const result = cli(['branches', 'list', '--project-id', project.id]);
  const branches = result.branches || result;
  const found = branches.find(b => b.name === selected.branch);
  if (found) return found;
  const parent = branches.find(b => b.default);
  if (!parent) throw new Error('Foundation project has no default branch');
  const created = cli(['branches', 'create', '--project-id', project.id, '--parent', parent.id,
    '--name', selected.branch, '--cu', '0.25', '--no-secrets']);
  return created.branch || created;
}
export async function withLock(config, fn) {
  const pool = new Pool({ connectionString: config.DATABASE_URL_UNPOOLED, connectionTimeoutMillis: 30000 });
  const connection = await pool.connect();
  try {
    const result = await connection.query("SELECT pg_try_advisory_lock(hashtext('foundation-neon-scaffold')) AS locked");
    if (!result.rows[0].locked) throw new Error('This Neon environment is already running tests or fixture maintenance');
    return await fn(connection);
  } finally {
    await connection.query("SELECT pg_advisory_unlock(hashtext('foundation-neon-scaffold'))").catch(() => {});
    connection.release(); await pool.end();
  }
}
export async function bootstrap(config, { reset = false } = {}) {
  return withLock(config, async connection => {
    if (reset) {
      // Only tables owned by this scaffold, never managed Auth or an entire branch.
      const users = await connection.query('SELECT user_id FROM xt_neon_test.fixture_users');
      for (const row of users.rows) deleteUser(config, row.user_id);
      await connection.query('DROP SCHEMA IF EXISTS neon_scratch CASCADE; DROP SCHEMA IF EXISTS xt_neon_test CASCADE; DROP SCHEMA IF EXISTS xt_neon CASCADE');
    }
    const bridgeFile = resolve(localRoot, `${config.NEON_ENV}.bridge.env`);
    const previous = existsSync(bridgeFile) ? parseEnv(readFileSync(bridgeFile, 'utf8')) : {};
    const password = previous.DATABASE_URL ? decodeURIComponent(new URL(previous.DATABASE_URL).password) : randomBytes(32).toString('base64url');
    const role = await connection.query("SELECT 1 FROM pg_roles WHERE rolname='neon_bridge'");
    if (!role.rowCount) await connection.query('CREATE ROLE neon_bridge LOGIN');
    const quoted = await connection.query("SELECT format('ALTER ROLE neon_bridge PASSWORD %L', $1::text) AS sql", [password]);
    await connection.query(quoted.rows[0].sql);
    await connection.query(readFileSync(resolve(packageRoot, 'events.sql'), 'utf8'));
    await connection.query(readFileSync(resolve(packageRoot, 'scratch.sql'), 'utf8'));
    const bridgeURL = new URL(config.DATABASE_URL_UNPOOLED);
    bridgeURL.username = 'neon_bridge'; bridgeURL.password = password;
    privateWrite(bridgeFile, envText({
      DATABASE_URL: bridgeURL.href, NEON_DATABASE: bridgeURL.pathname.slice(1),
      NEON_AUTH_BASE_URL: config.NEON_AUTH_BASE_URL, NEON_REALTIME_URL: config.NEON_REALTIME_URL,
      NEON_REALTIME_SECRET: config.NEON_REALTIME_SECRET, APP_ORIGIN: config.APP_ORIGIN,
    }));
    if (config.NEON_ENV === 'dev') await seedDevelopment(config, connection);
  });
}
export async function seedDevelopment(config, connection) {
  const auth = createNeonAuth(config.NEON_AUTH_BASE_URL, config.APP_ORIGIN);
  for (const [suffix, row] of [['OWNER', '11111111-1111-4111-8111-111111111111'], ['OTHER', '22222222-2222-4222-8222-222222222222']]) {
    const email = config[`NEON_DEV_${suffix}_EMAIL`], password = config[`NEON_DEV_${suffix}_PASSWORD`];
    if (!email || !password) throw new Error('Missing development fixture credentials');
    const existing = await connection.query('SELECT user_id FROM xt_neon_test.fixture_users WHERE email=$1 AND kind=$2', [email, 'dev']);
    const session = existing.rowCount ? await auth.signIn({ email, password }) : await auth.signUp({ email, password, name: `Foundation ${suffix.toLowerCase()}` });
    if (!session?.user?.id) throw new Error('Development fixture signup did not create an active user');
    await connection.query('INSERT INTO xt_neon_test.fixture_users(user_id,email,kind) VALUES($1,$2,$3) ON CONFLICT DO NOTHING', [session.user.id, email, 'dev']);
    await connection.query('INSERT INTO neon_scratch."NeonSmoke"(id,owner_id,nickname) VALUES($1,$2,$3) ON CONFLICT DO NOTHING', [row, session.user.id, suffix.toLowerCase()]);
    const topic = `foundation-dev:${suffix.toLowerCase()}`;
    await connection.query('INSERT INTO xt_neon.topics(topic) VALUES($1) ON CONFLICT DO NOTHING', [topic]);
    await connection.query('INSERT INTO xt_neon_test.topic_members(topic,user_id) VALUES($1,$2) ON CONFLICT DO NOTHING', [topic, session.user.id]);
    await auth.signOut(session.context);
  }
}
export function deleteUser(config, id, cli = neon) {
  try { cli(['api', `/projects/${config.NEON_PROJECT_ID}/branches/${config.NEON_BRANCH_ID}/auth/users/${encodeURIComponent(id)}`, '-X', 'DELETE']); }
  catch (error) { if (!error.notFound) throw error; }
}
export async function setup() {
  mkdirSync(localRoot, { recursive: true, mode: 0o700 });
  const file = resolve(localRoot, 'project.json');
  const project = ensureProject(neon, existsSync(file) ? JSON.parse(readFileSync(file, 'utf8')) : null);
  privateWrite(file, JSON.stringify({ project_id: project.id, organization_id: organization }, null, 2) + '\n');
  for (const name of ['min', 'dev']) {
    const selected = environment(name), branch = ensureBranch(project, selected);
    console.log(`Configuring ${selected.branch} (${branch.id})`);
    const target = ['--project-id', project.id, '--branch', branch.id];
    neon(['config', 'apply', ...target, '--config', resolve(packageRoot, 'neon.ts'), '--no-env-pull'], { json: false });
    neon(['neon-auth', 'config', 'email-password', 'update', ...target, '--enabled', '--no-require-email-verification',
      '--no-send-verification-email-on-sign-up', '--no-send-verification-email-on-sign-in']);
    neon(['data-api', 'update', ...target, '--database', 'neondb', '--db-schemas', 'neon_scratch']);
    const path = resolve(localRoot, `${name}.env`);
    const previous = existsSync(path) ? parseEnv(readFileSync(path, 'utf8')) : {};
    const stage = `${path}.pull`;
    privateWrite(stage, '');
    neon(['env', 'pull', ...target, '--file', stage, '--service', 'postgres', '--service', 'auth', '--service', 'data-api', '--service', 'realtime'], { json: false });
    const pulled = parseEnv(readFileSync(stage, 'utf8')); unlinkSync(stage);
    for (const key of ['DATABASE_URL', 'DATABASE_URL_UNPOOLED']) {
      if (pulled[key]) { const url = new URL(pulled[key]); url.searchParams.set('sslmode', 'verify-full'); pulled[key] = url.href; }
    }
    const development = name === 'dev' ? Object.fromEntries(['OWNER', 'OTHER'].flatMap(suffix => [
      [`NEON_DEV_${suffix}_EMAIL`, previous[`NEON_DEV_${suffix}_EMAIL`] || `${suffix.toLowerCase()}@foundation-neon.example.test`],
      [`NEON_DEV_${suffix}_PASSWORD`, previous[`NEON_DEV_${suffix}_PASSWORD`] || randomBytes(24).toString('base64url')],
    ])) : {};
    privateWrite(path, envText({ ...pulled, ...development, NEON_ENV: name, NEON_PROJECT_ID: project.id, NEON_BRANCH_ID: branch.id,
      NEON_BRANCH: selected.branch, APP_ORIGIN: selected.origin, NEON_BRIDGE_URL: selected.origin }));
    await bootstrap(readEnvironment(name));
    console.log(`${selected.branch} configured; bridge port ${selected.port}`);
  }
}
export function compose(name, args) {
  const selected = environment(name);
  execFileSync('docker', ['compose', '-p', `foundation-neon-${selected.name}`, '-f', resolve(composeRoot, 'docker-compose.yml'), ...args], {
    stdio: 'inherit', env: { ...process.env, NEON_ENV: selected.name, NEON_BRIDGE_PORT: String(selected.port) },
  });
}
export async function main(command, name = 'min') {
  environment(name);
  if (command === 'setup') return setup();
  if (command === 'down') return compose(name, ['down']);
  const config = readEnvironment(name);
  if (command === 'config') { process.stdout.write(JSON.stringify(config)); return; }
  if (command === 'reset') return bootstrap(config, { reset: true });
  if (command === 'up') {
    compose(name, ['up', '-d', '--build', '--wait', '--wait-timeout', '120']);
    const { verifyEnvironment } = await import('./environment-live.mjs');
    await verifyEnvironment(config);
    console.log(`${config.NEON_BRANCH}: SQL, Auth, Data API and Realtime ready`);
    return;
  }
  if (command === 'status') {
    const response = await fetch(`${config.NEON_BRIDGE_URL}/health`, { signal: AbortSignal.timeout(30000) });
    if (!response.ok) throw new Error('Bridge database health check failed');
    console.log(`${config.NEON_BRANCH}: bridge ready at ${config.NEON_BRIDGE_URL}`);
    return;
  }
  if (command === 'test') {
    const { verifyEnvironment } = await import('./environment-live.mjs');
    await verifyEnvironment(config, { adapters: true });
    console.log(`${config.NEON_BRANCH}: live integration checks passed`);
    return;
  }
  throw new Error('Command must be setup, up, down, status, reset or test');
}
if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  main(process.argv[2], process.argv[3] || process.env.NEON_ENV || 'min').catch(error => {
    // SQL/HTTP libraries sometimes put secrets in error messages. Detailed errors
    // from this module are safe; upstream errors are reported without their body.
    const safe = /^(Neon command failed:|Missing |Adapter check failed:|This Neon environment |Neon bridge readiness |NEON_ENV must |Neon branch configuration |Multiple Foundation |Recorded Foundation |Foundation project must |Command must )/.test(error.message || '');
    console.error(safe ? error.message : `Neon ${process.argv[2]} failed (${error.code || error.name}); verify environment configuration`);
    process.exitCode = 1;
  });
}
