import 'package:xtalk_substrate/transport-memory.dart' as json_transport;
import 'package:xtalk_net/ws-native.dart' as ws;
import 'dart:async';



socket_openp(socket) {
  var ready_state = socket["readyState"];
  if(null == ready_state){
    return true;
  }
  else{
    return ready_state == 1;
  }
}

add_socket_listener(socket, event, handler) {
  if((socket["addEventListener"].runtimeType).toString().contains("Function") || (socket["addEventListener"].runtimeType).toString().contains("=>") || (socket["addEventListener"]).toString().startsWith("Closure")){
    return socket.addEventListener(event,handler,false);
  }
  else if((socket["on"].runtimeType).toString().contains("Function") || (socket["on"].runtimeType).toString().contains("=>") || (socket["on"]).toString().startsWith("Closure")){
    return socket.on(event,handler);
  }
  else{
    socket["on" + event] = handler;
    return socket;
  }
}

remove_socket_listener(socket, event, handler) {
  if((socket["removeEventListener"].runtimeType).toString().contains("Function") || (socket["removeEventListener"].runtimeType).toString().contains("=>") || (socket["removeEventListener"]).toString().startsWith("Closure")){
    return socket.removeEventListener(event,handler,false);
  }
  else if((socket["off"].runtimeType).toString().contains("Function") || (socket["off"].runtimeType).toString().contains("=>") || (socket["off"]).toString().startsWith("Closure")){
    return socket.off(event,handler);
  }
  else if((socket["removeListener"].runtimeType).toString().contains("Function") || (socket["removeListener"].runtimeType).toString().contains("=>") || (socket["removeListener"]).toString().startsWith("Closure")){
    return socket.removeListener(event,handler);
  }
  else{
    socket["on" + event] = null;
    return socket;
  }
}

event_text(event) {
  return ((("Map" == (event.runtimeType).toString()) || (event.runtimeType).toString().startsWith("_Map") || (event.runtimeType).toString().startsWith("LinkedMap")) && event.containsKey("data")) ? event["data"] : (((("Map" == (event.runtimeType).toString()) || (event.runtimeType).toString().startsWith("_Map") || (event.runtimeType).toString().startsWith("LinkedMap")) && event.containsKey("text")) ? event["text"] : event);
}

websocket_url(source) {
  if("String" == (source.runtimeType).toString()){
    return source;
  }
  else{
    return source["url"];
  }
}

mark_open(state, socket, _event) {
  state["status"] = "open";
  return socket;
}

mark_error(state, event) {
  state["status"] = "error";
  state["error"] = event;
  return event;
}

mark_close(state, event) {
  if(!(state["status"] == "open")){
    state["status"] = "error";
    state["error"] = ((null == event) ? "websocket closed before open" : event);
  }
  return event;
}

await_open(state) {
  var status = state["status"];
  if(status == "open"){
    return Future.sync(() {
      return state["socket"];
    });
  }
  else if(status == "error"){
    return Future.sync(() {
      throw state["error"];
    });
  }
  else{
    return ((Future.sync(() => Future.delayed(Duration(milliseconds:  10 )).then((_) {
      return Future.sync(() {
        return Function.apply(() {
          return null;
        },<dynamic>[]);
      });
    }))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
      return await_open(state);
    },<dynamic>[value]); });
  }
}

connect_socket(socket_source) {
  var connect_fn = socket_source["connect_fn"];
  if((connect_fn.runtimeType).toString().contains("Function") || (connect_fn.runtimeType).toString().contains("=>") || (connect_fn).toString().startsWith("Closure")){
    return Function.apply(
      (connect_fn as Function),
      <dynamic>[websocket_url(socket_source)]
    );
  }
  else{
    var ctor = socket_source["WebSocket"] ?? WebSocket;
    if(!((ctor.runtimeType).toString().contains("Function") || (ctor.runtimeType).toString().contains("=>") || (ctor).toString().startsWith("Closure"))){
      throw "websocket source missing connect implementation";
    }
    if(null == websocket_url(socket_source)){
      throw "websocket source missing url";
    }
    return Function.apply(ctor,<dynamic>[websocket_url(socket_source)]);
  }
}

resolve_socket(socket_source) {
  if("String" == (socket_source.runtimeType).toString()){
    return connect_socket(<dynamic, dynamic>{"url":socket_source});
  }
  else if(("Map" == (socket_source.runtimeType).toString()) || (socket_source.runtimeType).toString().startsWith("_Map") || (socket_source.runtimeType).toString().startsWith("LinkedMap")){
    var create_fn = socket_source["create_fn"];
    if((create_fn.runtimeType).toString().contains("Function") || (create_fn.runtimeType).toString().contains("=>") || (create_fn).toString().startsWith("Closure")){
      return Function.apply((create_fn as Function),<dynamic>[]);
    }
    else if(socket_source.containsKey("url") || socket_source.containsKey("connect_fn") || socket_source.containsKey("WebSocket")){
      return connect_socket(socket_source);
    }
    else{
      return socket_source;
    }
  }
  else{
    return socket_source;
  }
}

websocket_nativep(socket) {
  return (("Map" == (socket.runtimeType).toString()) || (socket.runtimeType).toString().startsWith("_Map") || (socket.runtimeType).toString().startsWith("LinkedMap")) && (null != socket["::"]);
}

ensure_promise(value) {
  if((() {
    var dart_truthy__51493 = (null != value) && (("Future" == (value.runtimeType).toString()) || (value.runtimeType).toString().startsWith("Future<"));
    return (null != dart_truthy__51493) && (false != dart_truthy__51493);
  })()){
    return value;
  }
  else{
    return Future.sync(() {
      return value;
    });
  }
}

websocket_source(socket_source) {
  var current_socket = null;
  var current_native = null;
  var current_message_callback = null;
  var current_open_callback = null;
  var current_error_callback = null;
  var current_close_callback = null;
  var send_fn = (text) {
    if(null == current_socket){
      throw "websocket endpoint not started";
    }
    if(null != current_native){
      return ws.send(current_native,text);
    }
    else{
      return current_socket.send(text);
    }
  };
  var start_fn = (listener) {
    return ((Future.sync(() => ensure_promise(resolve_socket(socket_source)))) as Future<dynamic>).then((value) async { return await Function.apply((socket) {
      current_socket = socket;
      current_native = (((null != websocket_nativep(socket)) && (false != websocket_nativep(socket))) ? socket : null);
      current_message_callback = ((event) {
        return listener(event,null);
      });
      if(null != current_native){
        ws.add_listeners(
          current_native,
          <dynamic, dynamic>{"message":current_message_callback}
        );
        if((() {
          var dart_truthy__51494 = socket_openp(current_socket);
          return (null != dart_truthy__51494) && (false != dart_truthy__51494);
        })()){
          return current_socket;
        }
        else{
          var state = <dynamic, dynamic>{
            "status":"opening",
            "socket":current_socket,
            "error":"websocket failed to open"
          };
          current_open_callback = ((_event) {
            return mark_open(state,current_socket,_event);
          });
          current_error_callback = ((event) {
            return mark_error(state,event);
          });
          current_close_callback = ((event) {
            return mark_close(state,event);
          });
          ws.add_listeners(current_native,<dynamic, dynamic>{
            "open":current_open_callback,
            "error":current_error_callback,
            "close":current_close_callback
          });
          return ((Future.sync(() => await_open(state))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
            return current_socket;
          },<dynamic>[value]); });
        }
      }
      else{
        add_socket_listener(current_socket,"message",current_message_callback);
        if((() {
          var dart_truthy__51495 = socket_openp(current_socket);
          return (null != dart_truthy__51495) && (false != dart_truthy__51495);
        })()){
          return current_socket;
        }
        else{
          var state = <dynamic, dynamic>{
            "status":"opening",
            "socket":current_socket,
            "error":"websocket failed to open"
          };
          current_open_callback = ((_event) {
            return mark_open(state,current_socket,_event);
          });
          current_error_callback = ((event) {
            return mark_error(state,event);
          });
          current_close_callback = ((event) {
            return mark_close(state,event);
          });
          add_socket_listener(current_socket,"open",current_open_callback);
          add_socket_listener(current_socket,"error",current_error_callback);
          add_socket_listener(current_socket,"close",current_close_callback);
          return ((Future.sync(() => await_open(state))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
            return current_socket;
          },<dynamic>[value]); });
        }
      }
    },<dynamic>[value]); });
  };
  var stop_fn = (_) {
    if((null != current_socket) && (null != current_message_callback)){
      if(null != current_native){
        ws.add_listeners(current_native,<dynamic, dynamic>{"message":null});
      }
      else{
        remove_socket_listener(current_socket,"message",current_message_callback);
      }
    }
    if((null != current_socket) && (null != current_open_callback)){
      if(null != current_native){
        ws.add_listeners(current_native,<dynamic, dynamic>{"open":null});
      }
      else{
        remove_socket_listener(current_socket,"open",current_open_callback);
      }
    }
    if((null != current_socket) && (null != current_error_callback)){
      if(null != current_native){
        ws.add_listeners(current_native,<dynamic, dynamic>{"error":null});
      }
      else{
        remove_socket_listener(current_socket,"error",current_error_callback);
      }
    }
    if((null != current_socket) && (null != current_close_callback)){
      if(null != current_native){
        ws.add_listeners(current_native,<dynamic, dynamic>{"close":null});
      }
      else{
        remove_socket_listener(current_socket,"close",current_close_callback);
      }
    }
    if(null != current_native){
      ws.disconnect(current_native);
    }
    else{
      if((null != current_socket) && ((current_socket["close"].runtimeType).toString().contains("Function") || (current_socket["close"].runtimeType).toString().contains("=>") || (current_socket["close"]).toString().startsWith("Closure"))){
        current_socket.close();
      }
    }
    current_socket = null;
    current_native = null;
    current_message_callback = null;
    current_open_callback = null;
    current_error_callback = null;
    current_close_callback = null;
    return true;
  };
  return <dynamic, dynamic>{
    "meta":<dynamic, dynamic>{"kind":"websocket"},
    "write_fn":send_fn,
    "start_fn":start_fn,
    "stop_fn":stop_fn
  };
}

websocket_endpoint(socket_source) {
  var endpoint = json_transport.text_endpoint(websocket_source(socket_source));
  endpoint["meta"] = <dynamic, dynamic>{"kind":"websocket","encoding":"json"};
  return endpoint;
}