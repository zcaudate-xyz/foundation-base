import * as repl from '@/libs/xt/lang/base-repl'

// xt.lang.base-client/client-basic [9] 
export function client_basic(host,port,opts){
  let net = eval("require('net')");
  let rl = eval("require('readline')");
  let conn = new net.Socket();
  conn.connect(port,host);
  conn.on("error",function (){
    return null;
  });
  conn.on("end",function (){
    return null;
  });
  conn.on("connect",function (){
    let stream = rl.createInterface(conn,conn);
    stream.on("line",function (line){
      conn.write(repl.return_eval(JSON.parse(line)) + "\n");
    });
  });
  eval("require('process')").on("unhandledRejection",function (){
    return null;
  });
  return conn;
}

// xt.lang.base-client/client-ws [32] 
export function client_ws(host,port,opts){
  let {path,secured} = opts;
  let conn = new WebSocket(
    "ws" + (secured ? "s" : "") + "://" + host + ":" + port + "/" + (path || "")
  );
  let interval = setInterval(function (){
    conn.send("ping");
  },30000);
  conn.addEventListener("message",function (msg){
    if(msg.data == "pong"){
      return;
    }
    let {body,id} = JSON.parse(msg.data);
    let out = repl.return_eval(body);
    conn.send(JSON.stringify({"id":id,"status":"ok","body":out}));
  });
  conn.addEventListener("close",function (){
    clearInterval(interval);
  });
  return conn;
}

// xt.lang.base-client/MODULE [61] 
export var MODULE = {
  "client_basic":client_basic,
  "client_ws":client_ws,
  "MODULE":MODULE
};