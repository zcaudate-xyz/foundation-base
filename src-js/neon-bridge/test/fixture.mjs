import { createServer } from 'node:http';
const server = createServer(async (req,res)=>{
  let text='';for await(const chunk of req) text+=chunk;
  const input=text?JSON.parse(text):{};
  res.setHeader('Content-Type','application/json');
  if(req.url==='/auth/sign-in') res.end(JSON.stringify({user:{id:'u1'},access_token:'jwt',bridge_token:'opaque'}));
  else if(req.url==='/auth/refresh' || req.url==='/auth/session') {
    if(req.headers.authorization!=='Bearer opaque') {res.statusCode=401;res.end('{}');}
    else res.end(JSON.stringify({user:{id:'u1'},access_token:'renewed'}));
  } else if(req.url==='/data/rpc/echo') res.end(JSON.stringify({value:input.value,authorization:req.headers.authorization}));
  else if(req.url==='/events/subscribe' && input.cursor==='expired') {res.statusCode=409;res.end(JSON.stringify({code:'resync_required'}));}
  else if(req.url==='/events/subscribe') res.end(JSON.stringify({connection:'c',cursor:'first'}));
  else if(req.url==='/events/poll') res.end(JSON.stringify({cursor:'second',events:[{topic:'t',seq:'1',payload:{'db/sync':{UserAccount:[{id:'00000000-0000-0000-0000-000000000000',nickname:'live'}]}}}]}));
  else if(req.url==='/events/unsubscribe') res.end(JSON.stringify({closed:true}));
  else if(req.url==='/auth/sign-out') res.end(JSON.stringify({signed_out:true}));
  else {res.statusCode=404;res.end('{}');}
});
server.listen(Number(process.env.NEON_FIXTURE_PORT || 19081),'127.0.0.1',()=>console.log('Neon HTTP fixture ready'));
process.once('SIGTERM',()=>server.close());
