import { test } from 'node:test';
import assert from 'node:assert/strict';
import { createBridge } from '../bridge.mjs';

function fixture(options = {}) {
  const rows = [], watchers = new Set(); let floor = '0', permitted = true, active = true;
  const auth = {
    async signIn() { return { user: { id: 'u1' }, context: {}, access_token: 'jwt' }; },
    async signUp() { return this.signIn(); },
    async session() { return active ? { user: { id: 'u1' }, access_token: 'renewed' } : null; },
    async signOut() { active = false; }
  };
  const source = {
    async watch(user, topic, changed) { watchers.add(changed); return { ready: Promise.resolve(), close() { watchers.delete(changed); } }; },
    async read(user, topic, after, limit) { return { head: String(rows.length), floor, events: rows.filter(r => after !== null && BigInt(r.seq) > BigInt(after)).slice(0,limit) }; }
  };
  const bridge = createBridge({ auth, source, authorizeTopic: async () => permitted, pollMs: 20, secureCookies: false, ...options });
  let token;
  async function call(path, body = {}, credential = token) {
    const response = await bridge.handle(new Request(`http://localhost${path}`, { method:'POST', headers: credential ? { authorization: `Bearer ${credential}` } : {}, body: JSON.stringify(body) }));
    return { status: response.status, body: await response.json(), headers: response.headers };
  }
  return { bridge, call, watchers, async login() { const result = await call('/auth/sign-in'); token = result.body.bridge_token; return result; },
    emit(payload = { 'db/sync': { T: [{ id:1 }] } }) { rows.push({ id:`e${rows.length+1}`, seq:String(rows.length+1), payload }); for (const cb of watchers) cb(); },
    revoke() { permitted = false; for (const cb of watchers) cb(); }, prune(n) { floor = String(n); } };
}

test('requires authentication and configured policies', async t => {
  assert.throws(() => createBridge(), /required/);
  const f=fixture(); t.after(()=>f.bridge.close());
  assert.equal((await f.call('/events/subscribe',{topics:['a']})).status,401);
});
test('ordered delivery, replay and retained cursor recovery', async t => {
  const f=fixture();t.after(()=>f.bridge.close()); await f.login();
  const sub=(await f.call('/events/subscribe',{topics:['a']})).body;
  f.emit(); f.emit({'db/remove':{T:[1]}});
  const one=await f.call('/events/poll',sub);
  assert.deepEqual(one.body.events.map(e=>e.seq),['1','2']);
  const replay=await f.call('/events/poll',sub);
  assert.deepEqual(replay.body.events,one.body.events);
  const resumed=await f.call('/events/subscribe',{topics:['a'],cursor:sub.cursor});
  assert.equal(resumed.status,200);
  assert.equal((await f.call('/events/poll',resumed.body)).body.events.length,2);
  f.prune(2);
  assert.equal((await f.call('/events/poll',sub)).body.code,'resync_required');
});
test('long polls wake, timeout, and release watchers', async t => {
  const f=fixture({pollMs:100});t.after(()=>f.bridge.close());await f.login();
  const sub=(await f.call('/events/subscribe',{topics:['a']})).body;
  const pending=f.call('/events/poll',sub); setTimeout(()=>f.emit(),5);
  const delivered=await pending; assert.equal(delivered.body.events.length,1);
  const empty=await f.call('/events/poll',{...sub,cursor:delivered.body.cursor});
  assert.deepEqual(empty.body.events,[]);
  await f.call('/events/unsubscribe',sub);assert.equal(f.watchers.size,0);
});
test('revocation during a poll prevents delivery and logout closes connections', async t => {
  const f=fixture({pollMs:100});t.after(()=>f.bridge.close());await f.login();
  const sub=(await f.call('/events/subscribe',{topics:['a']})).body;
  const pending=f.call('/events/poll',sub);setTimeout(()=>f.revoke(),5);
  assert.equal((await pending).status,403);
  await f.call('/auth/sign-out');assert.equal(f.watchers.size,0);
  assert.equal((await f.call('/auth/session')).status,401);
});
test('cursor tampering and origin checks fail closed',async t=>{
  const f=fixture();t.after(()=>f.bridge.close());await f.login();
  assert.equal((await f.call('/events/subscribe',{topics:['a'],cursor:'fake'})).status,409);
  assert.equal(f.watchers.size,0);
  const r=await f.bridge.handle(new Request('http://localhost/auth/sign-in',{method:'POST',headers:{origin:'https://evil.test'},body:'{}'}));
  assert.equal(r.status,403);
});
test('cookie sessions work without exposing the bridge credential',async t=>{
  const f=fixture();t.after(()=>f.bridge.close());
  const login=await f.call('/auth/sign-in',{cookie:true});assert.equal(login.body.bridge_token,null);
  const cookie=login.headers.get('set-cookie').split(';')[0];
  const r=await f.bridge.handle(new Request('http://localhost/auth/session',{method:'POST',headers:{cookie},body:'{}'}));
  assert.equal(r.status,200);
});
