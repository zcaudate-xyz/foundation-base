var IWebsocket = xt.lang.common_protocol.create_protocol_fn("xt.net.ws_native/IWebsocket",{
  "connect":{"name":"connect","arglist":["client","opts"]},
  "disconnect":{"name":"disconnect","arglist":["client"]},
  "send":{"name":"send","arglist":["client","input"]},
  "add_listeners":{"name":"add_listeners","arglist":["client","m"]}
});

function connect(client,opts){
  let method_fn = xt.lang.common_protocol.protocol_method(client,"xt.net.ws_native/IWebsocket","connect");
  return method_fn(client,opts);
}

function disconnect(client){
  let method_fn = xt.lang.common_protocol.protocol_method(client,"xt.net.ws_native/IWebsocket","disconnect");
  return method_fn(client);
}

function send(client,input){
  let method_fn = xt.lang.common_protocol.protocol_method(client,"xt.net.ws_native/IWebsocket","send");
  return method_fn(client,input);
}

function add_listeners(client,m){
  let method_fn = xt.lang.common_protocol.protocol_method(client,"xt.net.ws_native/IWebsocket","add_listeners");
  return method_fn(client,m);
}

var IWebsocketHeartbeat = xt.lang.common_protocol.create_protocol_fn("xt.net.ws_native/IWebsocketHeartbeat",{
  "start_heartbeat":{
    "name":"start_heartbeat",
    "arglist":["client","name","f","interval"]
  },
  "stop_heartbeat":{"name":"stop_heartbeat","arglist":["client","name"]}
});

function start_heartbeat(client,name,f,interval){
  let method_fn = xt.lang.common_protocol.protocol_method(
    client,
    "xt.net.ws_native/IWebsocketHeartbeat",
    "start_heartbeat"
  );
  return method_fn(client,name,f,interval);
}

function stop_heartbeat(client,name){
  let method_fn = xt.lang.common_protocol.protocol_method(
    client,
    "xt.net.ws_native/IWebsocketHeartbeat",
    "stop_heartbeat"
  );
  return method_fn(client,name);
}

function prepare_url(client,input){
  let {path,url} = input;
  if(!(null == url)){
    return url;
  }
  let {defaults} = client;
  let {basepath,host,port,secured} = defaults;
  return "ws" + (secured ? "s" : "") + "://" + host + ":" + String(port || 80) + (basepath || "") + (path || "");
}

module.exports = {
  ["IWebsocket"]:IWebsocket,
  ["connect"]:connect,
  ["disconnect"]:disconnect,
  ["send"]:send,
  ["add_listeners"]:add_listeners,
  ["IWebsocketHeartbeat"]:IWebsocketHeartbeat,
  ["start_heartbeat"]:start_heartbeat,
  ["stop_heartbeat"]:stop_heartbeat,
  ["prepare_url"]:prepare_url
}