import { Pool } from 'pg';
import { createBridge } from './bridge.mjs';
import { createNeonAuth, createNeonSource } from './neon.mjs';
import { serve } from './http.mjs';

// Example policy table is application-owned: app.topic_members(topic text, user_id text).
// Grant the bridge role SELECT; do not expose xt_neon through the Data API.
const pool = new Pool({ connectionString: process.env.DATABASE_URL });
const authorizeTopic = async (user, topic) => {
  const result = await pool.query('SELECT 1 FROM app.topic_members WHERE topic = $1 AND user_id = $2', [topic, user.id]);
  return result.rowCount > 0;
};
const source = createNeonSource({ url: process.env.NEON_REALTIME_URL, secret: process.env.NEON_REALTIME_SECRET,
  db: process.env.NEON_DATABASE || 'neondb', pool, authorizeTopic,
  headQuery(user, topic) {
    return { sql: `SELECT t.topic, t.head, t.floor FROM xt_neon.topics t
      JOIN app.topic_members m ON m.topic = t.topic WHERE t.topic = $1 AND m.user_id = $2`, params: [topic, user.id] };
  }
});
const origin = process.env.APP_ORIGIN;
const bridge = createBridge({ source, authorizeTopic, auth: createNeonAuth(process.env.NEON_AUTH_BASE_URL, origin),
  allowedOrigins: [origin], secureCookies: !origin?.startsWith('http://localhost') });
const server = serve(bridge, { port: Number(process.env.PORT || 8787) });
async function stop() { await bridge.close(); server.close(); await pool.end(); }
process.once('SIGINT', stop); process.once('SIGTERM', stop);
