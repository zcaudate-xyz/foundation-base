import 'dart:io' as io;

import 'package:xtalk_net/ws-native.dart' as websocket;

DartWebsocketClient(raw, defaults, state, callbacks) {
  if(!(() {
    var dart_truthy__41985 = (__globals__["xt.lang.common_protocol/IMPLEMENTATIONS"] ??= <dynamic, dynamic>{})["dart.net.ws_native/DartWebsocketClient"];
    return (null != dart_truthy__41985) && (false != dart_truthy__41985);
  })()){
    (__globals__["xt.lang.common_protocol/IMPLEMENTATIONS"] ??= <dynamic, dynamic>{})["dart.net.ws_native/DartWebsocketClient"] = true;
    xt.lang.common_protocol.register_protocol_impl(websocket.IWebsocket["on"],"dart.net.ws_native/DartWebsocketClient",<dynamic, dynamic>{
      "connect":connect_ws,
      "disconnect":disconnect_ws,
      "send":send_ws,
      "add_listeners":add_listeners_ws
    });
    xt.lang.common_protocol.register_protocol_impl(websocket.IWebsocketHeartbeat["on"],"dart.net.ws_native/DartWebsocketClient",<dynamic, dynamic>{
      "start_heartbeat":start_heartbeat_ws,
      "stop_heartbeat":stop_heartbeat_ws
    });
  }
  return <dynamic, dynamic>{
    "::":"dart.net.ws_native/DartWebsocketClient",
    "::/protocols":<dynamic>[
        websocket.IWebsocket["on"],
        websocket.IWebsocketHeartbeat["on"]
      ],
    "raw":raw,
    "defaults":defaults,
    "state":state,
    "callbacks":callbacks
  };
}

dispatch_ws(client, event, payload) {
  var handler = (client["callbacks"])[event];
  if(null != handler){
    if((handler.runtimeType).toString().contains("Function") || (handler.runtimeType).toString().contains("=>") || (handler).toString().startsWith("Closure")){
      Function.apply((handler as Function),<dynamic>[payload]);
    }
  }
  return payload;
}

connect_ws(client, opts) {
  var url = websocket.prepare_url(client,opts ?? <dynamic, dynamic>{});
  return io.WebSocket.connect(url).then((raw) {
    client["raw"] = raw;
    if((() {
      var dart_truthy__41984 = (client["defaults"])["background"];
      return (null != dart_truthy__41984) && (false != dart_truthy__41984);
    })()){
      raw.listen((message) {
        return dispatch_ws(client,"message",<dynamic, dynamic>{"data":message});
      });
    }
    return client;
  });
}

disconnect_ws(client) {
  var raw = client["raw"];
  if(null != raw){
    raw.close();
  }
  client["raw"] = null;
  return client;
}

send_ws(client, input) {
  var raw = client["raw"];
  if(null == raw){
    return client;
  }
  if(null == (client["callbacks"])["message"]){
    throw "dart websocket missing message listener";
  }
  var defaults = client["defaults"];
  if((() {
    var dart_truthy__41986 = defaults["background"];
    return (null != dart_truthy__41986) && (false != dart_truthy__41986);
  })()){
    raw.add(input);
    return client;
  }
  var response_future = raw.first;
  raw.add(input);
  return response_future.then((message) {
    dispatch_ws(client,"message",<dynamic, dynamic>{"data":message});
    if((() {
      var dart_truthy__41987 = defaults["close_after_message"];
      return (null != dart_truthy__41987) && (false != dart_truthy__41987);
    })()){
      client["raw"] = null;
      return raw.close().then((_) {
        return client;
      });
    }
    return client;
  });
}

add_listeners_ws(client, m) {
  for(var entry_41988 in m.entries){
    var event = entry_41988.key;
    var handler = entry_41988.value;
    client["callbacks"][event] = handler;
  };
  return List<dynamic>.from(( m ).keys);
}

start_heartbeat_ws(client, name, f, interval) {
  return null;
}

stop_heartbeat_ws(client, name) {
  return client;
}

create(defaults) {
  var client = DartWebsocketClient(
    null,
    defaults ?? <dynamic, dynamic>{},
    <dynamic, dynamic>{},
    <dynamic, dynamic>{}
  );
  client["::/override"] = <dynamic, dynamic>{
    "connect":connect_ws,
    "disconnect":disconnect_ws,
    "send":send_ws,
    "add_listeners":add_listeners_ws,
    "start_heartbeat":start_heartbeat_ws,
    "stop_heartbeat":stop_heartbeat_ws
  };
  return client;
}