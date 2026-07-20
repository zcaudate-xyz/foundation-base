var IWebsocket = Function.apply((xt.lang.common_protocol.create_protocol_fn as Function),<dynamic>[
  "xt.net.ws_native/IWebsocket",
  <dynamic, dynamic>{
  "connect":<dynamic, dynamic>{"name":"connect","arglist":<dynamic>["client","opts"]},
  "disconnect":<dynamic, dynamic>{"name":"disconnect","arglist":<dynamic>["client"]},
  "send":<dynamic, dynamic>{"name":"send","arglist":<dynamic>["client","input"]},
  "add_listeners":<dynamic, dynamic>{"name":"add_listeners","arglist":<dynamic>["client","m"]}
}
]);

connect(client, opts) {
  var method_fn = xt.lang.common_protocol.protocol_method(client,"xt.net.ws_native/IWebsocket","connect");
  return Function.apply((method_fn as Function),<dynamic>[client,opts]);
}

disconnect(client) {
  var method_fn = xt.lang.common_protocol.protocol_method(client,"xt.net.ws_native/IWebsocket","disconnect");
  return Function.apply((method_fn as Function),<dynamic>[client]);
}

send(client, input) {
  var method_fn = xt.lang.common_protocol.protocol_method(client,"xt.net.ws_native/IWebsocket","send");
  return Function.apply((method_fn as Function),<dynamic>[client,input]);
}

add_listeners(client, m) {
  var method_fn = xt.lang.common_protocol.protocol_method(client,"xt.net.ws_native/IWebsocket","add_listeners");
  return Function.apply((method_fn as Function),<dynamic>[client,m]);
}

var IWebsocketHeartbeat = Function.apply((xt.lang.common_protocol.create_protocol_fn as Function),<dynamic>[
  "xt.net.ws_native/IWebsocketHeartbeat",
  <dynamic, dynamic>{
  "start_heartbeat":<dynamic, dynamic>{
    "name":"start_heartbeat",
    "arglist":<dynamic>["client","name","f","interval"]
  },
  "stop_heartbeat":<dynamic, dynamic>{"name":"stop_heartbeat","arglist":<dynamic>["client","name"]}
}
]);

start_heartbeat(client, name, f, interval) {
  var method_fn = xt.lang.common_protocol.protocol_method(
    client,
    "xt.net.ws_native/IWebsocketHeartbeat",
    "start_heartbeat"
  );
  return Function.apply((method_fn as Function),<dynamic>[client,name,f,interval]);
}

stop_heartbeat(client, name) {
  var method_fn = xt.lang.common_protocol.protocol_method(
    client,
    "xt.net.ws_native/IWebsocketHeartbeat",
    "stop_heartbeat"
  );
  return Function.apply((method_fn as Function),<dynamic>[client,name]);
}

prepare_url(client, input) {
  var path = input["path"];
  var url = input["url"];
  if(!(null == url)){
    return url;
  }
  var defaults = client["defaults"];
  var basepath = defaults["basepath"];
  var host = defaults["host"];
  var port = defaults["port"];
  var secured = defaults["secured"];
  return "ws" + (((null != secured) && (false != secured)) ? "s" : "") + "://" + host + ":" + (port ?? 80).toString() + (basepath ?? "") + (path ?? "");
}