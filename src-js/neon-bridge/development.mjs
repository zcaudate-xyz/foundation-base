import { Pool } from 'pg';
import { pathToFileURL } from 'node:url';
import { createBridge } from './bridge.mjs';
import { createNeonAuth, createNeonSource } from './neon.mjs';
import { serve } from './http.mjs';

export async function startDevelopment(env = process.env) {
  for (const name of ['DATABASE_URL', 'NEON_AUTH_BASE_URL', 'NEON_REALTIME_URL', 'NEON_REALTIME_SECRET', 'APP_ORIGIN']) {
    if (!env[name]) throw new Error(`Missing ${name}; run neon-setup first`);
  }
  const pool = new Pool({ connectionString: env.DATABASE_URL, connectionTimeoutMillis: 30000 });
  const authorizeTopic = async (user, topic) => (await pool.query(
    'SELECT 1 FROM xt_neon_test.topic_members WHERE topic=$1 AND user_id=$2', [topic, user.id])).rowCount > 0;
  const source = createNeonSource({ url: env.NEON_REALTIME_URL, secret: env.NEON_REALTIME_SECRET,
    db: env.NEON_DATABASE || 'neondb', pool, authorizeTopic, readinessMs: 120000,
    headQuery(user, topic) { return {
      sql: 'SELECT t.topic, t.head, t.floor FROM xt_neon.topics t JOIN xt_neon_test.topic_members m ON m.topic=t.topic WHERE t.topic=$1 AND m.user_id=$2',
      params: [topic, user.id],
    }; },
  });
  const bridge = createBridge({ source, authorizeTopic,
    auth: createNeonAuth(env.NEON_AUTH_BASE_URL, env.APP_ORIGIN),
    allowedOrigins: [env.APP_ORIGIN], secureCookies: new URL(env.APP_ORIGIN).protocol === 'https:' });
  const server = serve(bridge, { host: env.HOST || '127.0.0.1', port: Number(env.PORT || 18787),
    health: async () => { await pool.query('SELECT 1'); } });
  await new Promise((resolve, reject) => { server.once('listening', resolve); server.once('error', reject); });
  let stopping;
  const stop = () => stopping ||= (async () => {
    await bridge.close();
    await new Promise(resolve => server.close(resolve));
    await pool.end();
  })();
  return { server, stop };
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  startDevelopment().then(({ stop }) => {
    console.log('Foundation Neon bridge ready');
    for (const signal of ['SIGTERM', 'SIGINT']) process.once(signal, () => stop().catch(() => { process.exitCode = 1; }));
  }).catch(() => { console.error('Neon bridge startup failed; check environment configuration and database connectivity'); process.exitCode = 1; });
}
