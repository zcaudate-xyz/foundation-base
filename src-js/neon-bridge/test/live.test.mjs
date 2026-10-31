import { test } from 'node:test';
import assert from 'node:assert/strict';
import { Pool } from 'pg';
const required=['NEON_BRIDGE_URL','NEON_DATA_API_URL','NEON_TEST_EMAIL','NEON_TEST_PASSWORD','NEON_TEST_TOPIC','DATABASE_URL','NEON_TEST_RPC','NEON_TEST_RPC_EXPECTED','NEON_TEST_TABLE'];
test('live Neon Auth, Data API, RPC and committed event delivery', {skip: !required.every(k=>process.env[k])}, async t=>{
  let token;
  const call=async(path,body={})=>{
    const response=await fetch(`${process.env.NEON_BRIDGE_URL}${path}`,{method:'POST',headers:{'Content-Type':'application/json',...(token?{Authorization:`Bearer ${token}`}:{})},body:JSON.stringify(body)});
    assert.equal(response.status,200);return response.json();
  };
  const login=await call('/auth/sign-in',{email:process.env.NEON_TEST_EMAIL,password:process.env.NEON_TEST_PASSWORD});
  token=login.bridge_token;assert.ok(token);
  t.after(()=>call('/auth/sign-out'));
  const session=await call('/auth/refresh');assert.equal(session.user.id,login.user.id);
  const headers={Authorization:`Bearer ${session.access_token}`,'Content-Type':'application/json'};
  const pull=await fetch(`${process.env.NEON_DATA_API_URL}/${encodeURIComponent(process.env.NEON_TEST_TABLE)}?select=*&limit=1`,{headers});
  assert.equal(pull.status,200);assert.ok(Array.isArray(await pull.json()));
  const rpc=await fetch(`${process.env.NEON_DATA_API_URL}/rpc/${encodeURIComponent(process.env.NEON_TEST_RPC)}`,{method:'POST',headers,body:process.env.NEON_TEST_RPC_ARGS || '{}'});
  assert.equal(rpc.status,200);assert.deepEqual(await rpc.json(),JSON.parse(process.env.NEON_TEST_RPC_EXPECTED));
  const sub=await call('/events/subscribe',{topics:[process.env.NEON_TEST_TOPIC]});
  const pool=new Pool({connectionString:process.env.DATABASE_URL});t.after(()=>pool.end());
  const payload={'db/sync':{NeonSmoke:[{id:crypto.randomUUID()}]}};
  const result=await pool.query('SELECT xt_neon.publish($1,$2) AS seq',[process.env.NEON_TEST_TOPIC,payload]);
  const batch=await call('/events/poll',sub);
  const event=batch.events.find(e=>e.seq===String(result.rows[0].seq));assert.deepEqual(event?.payload,payload);
  await call('/events/unsubscribe',sub);
});
