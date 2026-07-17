const protocol = require("@xtalk/lang/common-protocol.js")

const websocket = require("@xtalk/net/ws-native.js")

function WebsocketClient(raw,defaults,state){
  if(!globalThis["xt_lang_common_protocol$$IMPLEMENTATIONS"]["js.net.ws_native/WebsocketClient"]){
    globalThis["xt_lang_common_protocol$$IMPLEMENTATIONS"]["js.net.ws_native/WebsocketClient"] = true;
    protocol.register_protocol_impl(websocket.IWebsocket["on"],"js.net.ws_native/WebsocketClient",{
      "connect":connect_ws,
      "disconnect":disconnect_ws,
      "send":send_ws,
      "add_listeners":add_listeners_ws
    });
    protocol.register_protocol_impl(websocket.IWebsocketHeartbeat["on"],"js.net.ws_native/WebsocketClient",{
      "start_heartbeat":start_heartbeat_ws,
      "stop_heartbeat":stop_heartbeat_ws
    });
  }
  return {
    "::":"js.net.ws_native/WebsocketClient",
    "::/protocols":[
        websocket.IWebsocket["on"],
        websocket.IWebsocketHeartbeat["on"]
      ],
    "raw":raw,
    "defaults":defaults,
    "state":state
  };
}

function connect_ws(client,opts){
  let url = websocket.prepare_url(client,opts || {});
  let raw = new WebSocket(url);
  client["raw"] = raw;
  return new Promise(function (resolve,reject){
    let cleanup_fn = function (){
      raw.removeEventListener("open",on_open);
      raw.removeEventListener("error",on_error);
      raw.removeEventListener("close",on_close);
    };
    let on_open = function (event){
      cleanup_fn();
      resolve(client);
    };
    let on_error = function (event){
      cleanup_fn();
      reject(event);
    };
    let on_close = function (event){
      cleanup_fn();
      reject(event);
    };
    raw.addEventListener("open",on_open);
    raw.addEventListener("error",on_error);
    raw.addEventListener("close",on_close);
  });
}

function disconnect_ws(client){
  let {raw} = client;
  if(raw){
    raw.close(1000,"done");
  }
  client["raw"] = null;
  return client;
}

function send_ws(client,input){
  let {raw} = client;
  if(raw){
    return raw.send(input);
  }
}

function add_listeners_ws(client,m){
  let {raw} = client;
  if(raw){
    for(let [k,handler] of Object.entries(m)){
      raw.addEventListener(k,handler);
    };
    return Object.keys(m);
  }
}

function default_heartbeat_fn(client,name){
  return websocket.send(client,"heartbeat");
}

function start_heartbeat_ws(client,name,f,interval){
  let {defaults,state} = client;
  let heartbeats = state["heartbeats"] || {};
  let stop_fn = heartbeats[name];
  if("function" == (typeof stop_fn)){
    stop_fn();
  }
  f = (f || defaults["heartbeat_fn"] || default_heartbeat_fn);
  interval = (interval || defaults["heartbeat_interval"] || 30000);
  let timer = setInterval(function (){
    f(client,name);
  },interval);
  heartbeats[name] = (function (){
    clearInterval(timer);
    delete(heartbeats[name]);
    return true;
  });
  state["heartbeats"] = heartbeats;
  return timer;
}

function stop_heartbeat_ws(client,name){
  let {state} = client;
  let heartbeats = state["heartbeats"] || {};
  let stop_fn = heartbeats[name];
  if("function" == (typeof stop_fn)){
    stop_fn();
  }
  return client;
}

function create(defaults){
  return WebsocketClient(null,defaults,{"heartbeats":{},"callbacks":{}});
}

module.exports = {
  ["connect_ws"]:connect_ws,
  ["disconnect_ws"]:disconnect_ws,
  ["send_ws"]:send_ws,
  ["add_listeners_ws"]:add_listeners_ws,
  ["default_heartbeat_fn"]:default_heartbeat_fn,
  ["start_heartbeat_ws"]:start_heartbeat_ws,
  ["stop_heartbeat_ws"]:stop_heartbeat_ws,
  ["WebsocketClient"]:WebsocketClient,
  ["create"]:create
}