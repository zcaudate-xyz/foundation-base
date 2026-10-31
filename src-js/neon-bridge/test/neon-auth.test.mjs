import { test } from 'node:test';
import assert from 'node:assert/strict';
import { createServer } from 'node:http';
import { createNeonAuth } from '../neon.mjs';

test('official SDK forwards session cookies and retrieves JWT', async t => {
  const requests=[];
  const server=createServer((req,res)=>{
    requests.push({path:req.url,cookie:req.headers.cookie,origin:req.headers.origin});
    res.setHeader('Content-Type','application/json');
    const user={id:'u1',name:'User',email:'u@example.test',emailVerified:true,createdAt:new Date().toISOString(),updatedAt:new Date().toISOString()};
    if(req.url.includes('sign-in/email') || req.url.includes('sign-up/email')) {
      res.setHeader('Set-Cookie','better-auth.session_token=secret; HttpOnly; Path=/');
      res.end(JSON.stringify({token:'secret',user}));
    } else if(req.url.includes('get-session')) {
      res.end(JSON.stringify(req.headers.cookie?.includes('secret') ? {user,session:{id:'s',userId:'u1',token:'secret',expiresAt:new Date(Date.now()+3600000).toISOString()}} : null));
    } else if(req.url.includes('/token')) res.end(JSON.stringify({token:'jwt'}));
    else if(req.url.includes('sign-out')) res.end(JSON.stringify({success:true}));
    else {res.statusCode=404;res.end('{}');}
  });
  await new Promise(resolve=>server.listen(0,'127.0.0.1',resolve));
  t.after(()=>new Promise(resolve=>server.close(resolve)));
  const auth=createNeonAuth(`http://127.0.0.1:${server.address().port}/auth`,'http://localhost');
  const login=await auth.signIn({email:'u@example.test',password:'password'});
  assert.equal(login.user?.id,'u1'); assert.equal(login.access_token,'jwt');
  assert.equal((await auth.session(login.context)).user.id,'u1');
  await auth.signOut(login.context);
  assert.ok(requests.find(r=>r.path.includes('sign-out') && r.cookie?.includes('secret')));
  assert.ok(requests.every(r=>r.origin==='http://localhost'));
});
