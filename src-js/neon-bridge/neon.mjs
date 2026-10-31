import { createRealtime, rawSql } from '@neon/realtime/server';
import { createAuthClient } from '@neon/neon-js/auth';
import { BridgeError } from './bridge.mjs';

export function createNeonAuth(authUrl, origin) {
  if (!authUrl || !origin) throw new Error('authUrl and trusted origin are required');
  function context() { return { client: createAuthClient(authUrl), cookies: new Map() }; }
  function options(ctx) {
    return { headers: { "X-Force-Fetch": "true", Origin: origin, Cookie: [...ctx.cookies].map(([k,v]) => `${k}=${v}`).join('; ') },
      onResponse({ response }) {
        for (const line of response.headers.getSetCookie()) {
          const pair = line.split(';')[0], index = pair.indexOf('=');
          const name = pair.slice(0,index), value = pair.slice(index+1);
          if (!value) ctx.cookies.delete(name); else ctx.cookies.set(name,value);
        }
      } };
  }
  async function unwrap(promise) {
    let result;
    try { result = await promise; }
    catch (error) { throw new BridgeError(error.status === 401 || error.statusCode === 401 ? 401 : 400, 'auth_failed'); }
    if (result.error) throw new BridgeError(result.error.status === 401 ? 401 : 400, 'auth_failed');
    return result.data;
  }
  async function session(ctx) {
    const data = await unwrap(ctx.client.getSession({ fetchOptions: options(ctx), query: { disableCookieCache: true } }));
    if (!data?.user) return null;
    const jwt = await unwrap(ctx.client.token({ fetchOptions: options(ctx) }));
    return { user: data.user, access_token: jwt?.token };
  }
  return {
    async signIn(credentials) {
      const ctx = context();
      await unwrap(ctx.client.signIn.email({ email: credentials.email, password: credentials.password }, options(ctx)));
      return { ...await session(ctx), context: ctx };
    },
    async signUp(credentials) {
      const ctx = context();
      await unwrap(ctx.client.signUp.email({ name: credentials.name || credentials.email, email: credentials.email, password: credentials.password }, options(ctx)));
      return { ...await session(ctx), context: ctx };
    },
    session,
    async signOut(ctx) { await unwrap(ctx.client.signOut({ fetchOptions: options(ctx) })); ctx.cookies.clear(); }
  };
}

export function createNeonSource({ url, secret, db, pool, headQuery, authorizeTopic, readinessMs = 15000 }) {
  if (typeof headQuery !== 'function' || typeof authorizeTopic !== 'function') throw new Error('authorized headQuery and authorizeTopic are required');
  const realtime = createRealtime({ url, secret, db });
  return {
    async watch(user, topic, changed) {
      const { sql, params } = await headQuery(user, topic);
      const subscription = await realtime.subscribe(rawSql(sql, params));
      let off, timeout, failure;
      const ready = new Promise((resolve, reject) => {
        const update = snapshot => {
          if (snapshot.status === 'error' || snapshot.status === 'closed') { failure = new BridgeError(409, 'resync_required'); reject(failure); changed(); }
          if (snapshot.status === 'live') { resolve(); changed(); }
        };
        off = subscription.onChange(update);
        timeout = setTimeout(() => reject(new BridgeError(504, 'realtime_timeout')), readinessMs);
        update(subscription.getSnapshot());
      }).finally(() => clearTimeout(timeout));
      return { ready, check() { if (failure) throw failure; }, close() { off?.(); subscription.unsubscribe(); } };
    },
    async read(user, topic, after, limit) {
      if (!await authorizeTopic(user, topic)) throw new BridgeError(403, 'forbidden');
      const result = await pool.query(`SELECT t.head::text, t.floor::text,
        coalesce((SELECT jsonb_agg(row_to_json(e) ORDER BY e.seq::bigint) FROM
          (SELECT seq::text, id, payload FROM xt_neon.events
           WHERE topic = $1 AND seq > $2::bigint ORDER BY xt_neon.events.seq LIMIT $3) e), '[]'::jsonb) events
        FROM xt_neon.topics t WHERE topic = $1`, [topic, after ?? '0', limit]);
      return result.rows[0] ?? { head: '0', floor: '0', events: [] };
    },
    close() { realtime.close(); }
  };
}
