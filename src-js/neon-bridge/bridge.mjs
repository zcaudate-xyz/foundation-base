import { randomBytes, createHmac, timingSafeEqual } from 'node:crypto';

export class BridgeError extends Error {
  constructor(status, code) { super(code); this.status = status; this.code = code; }
}
const fail = (status, code) => { throw new BridgeError(status, code); };
const id = () => randomBytes(32).toString('base64url');
const json = (body, status = 200, headers = {}) => Response.json(body, { status, headers: { 'Cache-Control': 'no-store', ...headers } });

// Request/Response handler: mount behind HTTPS on the application's own origin.
export function createBridge({ auth, source, authorizeTopic, clearInterval: clear = clearInterval,
  pollMs = 25000, idleMs = 90000, sessionMs = 3600000, maxConnections = 1000,
  maxTopics = 32, batchSize = 256, secureCookies = true, allowedOrigins = [] } = {}) {
  if (!auth || !source || !['signIn','signUp','session','signOut'].every(k => typeof auth[k] === 'function') || !['watch','read'].every(k => typeof source[k] === 'function') || typeof authorizeTopic !== 'function') throw new Error('auth, source and authorizeTopic are required');
  const sessions = new Map(), connections = new Map(), key = randomBytes(32);
  let closed = false;
  const cookie = (value, age = sessionMs / 1000) => `xt_neon_session=${value}; HttpOnly; SameSite=Strict; Path=/; Max-Age=${Math.floor(age)}${secureCookies ? '; Secure' : ''}`;
  const sign = value => {
    const data = Buffer.from(JSON.stringify(value)).toString('base64url');
    return `${data}.${createHmac('sha256', key).update(data).digest('base64url')}`;
  };
  const decode = value => {
    try {
      const [data, signature] = value.split('.');
      const expected = createHmac('sha256', key).update(data).digest();
      const received = Buffer.from(signature, 'base64url');
      if (expected.length !== received.length || !timingSafeEqual(expected, received)) throw Error();
      return JSON.parse(Buffer.from(data, 'base64url').toString());
    } catch { fail(409, 'resync_required'); }
  };
  function dropConnection(c) {
    c.closed = true;
    for (const sub of c.topics.values()) sub.close();
    c.wake?.(); connections.delete(c.id);
  }
  function dropSession(token) {
    for (const c of connections.values()) if (c.session === token) dropConnection(c);
    sessions.delete(token);
  }
  const sweep = setInterval(() => {
    const now = Date.now();
    for (const c of connections.values()) if (now - c.touched > idleMs) dropConnection(c);
    for (const [token, s] of sessions) if (s.expires <= now) dropSession(token);
  }, Math.min(idleMs, 30000));
  sweep.unref?.();
  function credential(request) {
    const bearer = request.headers.get('authorization');
    return bearer?.startsWith('Bearer ') ? bearer.slice(7) : request.headers.get('cookie')?.split(';').map(x => x.trim()).find(x => x.startsWith('xt_neon_session='))?.slice(16);
  }
  async function identify(request) {
    const token = credential(request), s = sessions.get(token);
    if (!s || s.expires <= Date.now()) { if (s) dropSession(token); fail(401, 'unauthorized'); }
    try {
      const result = await auth.session(s.context);
      if (!result?.user) throw Error();
      if (result.user.id !== s.user.id) throw Error();
      s.user = result.user; s.access = result.access_token;
      return { token, s };
    } catch { dropSession(token); fail(401, 'unauthorized'); }
  }
  async function allowed(s, topics) {
    for (const topic of topics) if (!(await authorizeTopic(s.user, topic))) fail(403, 'forbidden');
  }
  async function handle(request) {
    if (closed) return json({ code: 'closed' }, 503);
    let currentConnection;
    try {
      if (request.method !== 'POST') fail(405, 'method_not_allowed');
      const origin = request.headers.get('origin');
      if (origin && !allowedOrigins.includes(origin)) fail(403, 'origin_denied');
      const raw = await request.text();
      if (raw.length > 65536) fail(413, 'body_too_large');
      let body; try { body = JSON.parse(raw || '{}'); } catch { fail(400, 'invalid_json'); }
      const path = new URL(request.url).pathname.replace(/\/$/, '');
      if (path === '/auth/sign-in' || path === '/auth/sign-up') {
        if (sessions.size >= maxConnections) fail(429, 'capacity');
        const result = await auth[path.endsWith('sign-in') ? 'signIn' : 'signUp'](body);
        if (!result?.user) return json({ user: null, verification_required: true });
        const token = id();
        sessions.set(token, { user: result.user, context: result.context, access: result.access_token, expires: Date.now() + sessionMs });
        // Browser callers opt into HttpOnly cookies. Native callers receive the opaque credential.
        return json({ user: result.user, access_token: result.access_token,
          bridge_token: body.cookie ? null : token }, 200, { 'Set-Cookie': cookie(token) });
      }
      const { token, s } = await identify(request);
      if (path === '/auth/session' || path === '/auth/refresh') return json({ user: s.user, access_token: s.access });
      if (path === '/auth/sign-out') {
        dropSession(token);
        try { await auth.signOut(s.context); } finally { /* local credential is always revoked */ }
        return json({ signed_out: true }, 200, { 'Set-Cookie': cookie('', 0) });
      }
      if (path === '/events/subscribe') {
        const topics = body.topics;
        if (!Array.isArray(topics) || !topics.length || topics.length > maxTopics || topics.some(t => typeof t !== 'string' || !t.length || t.length > 256)) fail(400, 'invalid_topics');
        await allowed(s, topics);
        if (connections.size >= maxConnections) fail(429, 'capacity');
        const c = { id: id(), session: token, topics: new Map(), touched: Date.now(), closed: false, polling: false };
        connections.set(c.id, c);
        try {
          const resume = body.cursor ? decode(body.cursor) : null;
          if (resume && (resume.user !== s.user.id || typeof resume.positions !== 'object')) fail(409, 'resync_required');
          const positions = {};
          for (const topic of new Set(topics)) {
            const sub = await source.watch(s.user, topic, () => c.wake?.());
            c.topics.set(topic, sub);
            await sub.ready;
            const state = await source.read(s.user, topic, null, batchSize);
            positions[topic] = resume?.positions[topic] ?? state.head;
            if (BigInt(positions[topic]) < BigInt(state.floor)) fail(409, 'resync_required');
          }
          return json({ connection: c.id, cursor: sign({ user: s.user.id, connection: c.id, positions }) });
        } catch (error) { dropConnection(c); throw error; }
      }
      const c = connections.get(body.connection);
      currentConnection = c;
      if (!c || c.session !== token || c.closed) fail(409, 'resync_required');
      c.touched = Date.now();
      await allowed(s, [...c.topics.keys()]);
      if (path === '/events/unsubscribe') { dropConnection(c); return json({ closed: true }); }
      if (path !== '/events/poll') fail(404, 'not_found');
      if (c.polling) fail(409, 'poll_in_progress');
      const cursor = decode(body.cursor);
      if (cursor.user !== s.user.id || cursor.connection !== c.id) fail(409, 'resync_required');
      c.polling = true;
      let timer, abort;
      try {
        const deadline = Date.now() + pollMs;
        while (true) {
          if (c.closed || request.signal.aborted) fail(409, 'closed');
          // Install wakeup before reading, so changes during the read cannot be lost.
          const wake = new Promise(resolve => { c.wake = resolve; abort = resolve; request.signal.addEventListener('abort', abort, { once: true }); timer = setTimeout(resolve, Math.max(0, deadline - Date.now())); });
          const events = [];
          for (const topic of c.topics.keys()) {
            c.topics.get(topic).check?.();
            const after = cursor.positions[topic];
            if (typeof after !== 'string') fail(409, 'resync_required');
            const result = await source.read(s.user, topic, after, batchSize);
            if (BigInt(after) < BigInt(result.floor) || BigInt(after) > BigInt(result.head)) fail(409, 'resync_required');
            for (const event of result.events) {
              events.push({ ...event, topic }); cursor.positions[topic] = String(event.seq);
            }
          }
          if (events.length || Date.now() >= deadline) {
            await allowed(s, [...c.topics.keys()]);
            await identify(request);
            if (c.closed) fail(409, 'closed');
            return json({ events, cursor: sign(cursor) });
          }
          await wake;
          clearTimeout(timer); request.signal.removeEventListener('abort', abort);
        }
      } finally { clearTimeout(timer); if (abort) request.signal.removeEventListener('abort', abort); c.wake = null; c.polling = false; }
    } catch (error) {
      if (currentConnection && [401,403].includes(error.status) || currentConnection && error.code === 'resync_required') dropConnection(currentConnection);
      return json({ code: error instanceof BridgeError ? error.code : 'upstream_error' }, error instanceof BridgeError ? error.status : 502);
    }
  }
  return { handle, async close() { closed = true; clear(sweep); for (const c of connections.values()) dropConnection(c); sessions.clear(); await source.close?.(); }, stats: () => ({ sessions: sessions.size, connections: connections.size }) };
}
