const json_transport = require("@xtalk/substrate/transport-memory.js")

const ws = require("@xtalk/net/ws-native.js")

function socket_openp(socket){
  let ready_state = socket["readyState"];
  if(null == ready_state){
    return true;
  }
  else{
    return ready_state == 1;
  }
}

function add_socket_listener(socket,event,handler){
  if("function" == (typeof socket["addEventListener"])){
    return socket.addEventListener(event,handler,false);
  }
  else if("function" == (typeof socket["on"])){
    return socket.on(event,handler);
  }
  else{
    socket["on" + event] = handler;
    return socket;
  }
}

function remove_socket_listener(socket,event,handler){
  if("function" == (typeof socket["removeEventListener"])){
    return socket.removeEventListener(event,handler,false);
  }
  else if("function" == (typeof socket["off"])){
    return socket.off(event,handler);
  }
  else if("function" == (typeof socket["removeListener"])){
    return socket.removeListener(event,handler);
  }
  else{
    socket["on" + event] = null;
    return socket;
  }
}

function event_text(event){
  return (((null != event) && ("object" == (typeof event)) && !Array.isArray(event)) && (null != event["data"])) ? event["data"] : ((((null != event) && ("object" == (typeof event)) && !Array.isArray(event)) && (null != event["text"])) ? event["text"] : event);
}

function websocket_url(source){
  if("string" == (typeof source)){
    return source;
  }
  else{
    return source["url"];
  }
}

function mark_open(state,socket,_event){
  state["status"] = "open";
  return socket;
}

function mark_error(state,event){
  state["status"] = "error";
  state["error"] = event;
  return event;
}

function mark_close(state,event){
  if(!(state["status"] == "open")){
    state["status"] = "error";
    state["error"] = ((null == event) ? "websocket closed before open" : event);
  }
  return event;
}

function await_open(state){
  let status = state["status"];
  if(status == "open"){
    return Promise.resolve().then(function (){
      return state["socket"];
    });
  }
  else if(status == "error"){
    return Promise.resolve().then(function (){
      throw state["error"];
    });
  }
  else{
    return new Promise(function (resolve,reject){
      setTimeout(function (){
        new Promise(function (inner_resolve){
          inner_resolve((function (){
            return null;
          })());
        }).then(function (value){
          resolve(value);
        }).catch(function (err){
          reject(err);
        });
      },10);
    }).then(function (_){
      return await_open(state);
    });
  }
}

function connect_socket(socket_source){
  let connect_fn = socket_source["connect_fn"];
  if("function" == (typeof connect_fn)){
    return connect_fn(websocket_url(socket_source));
  }
  else{
    let ctor = socket_source["WebSocket"] || WebSocket;
    if(!("function" == (typeof ctor))){
      throw "websocket source missing connect implementation";
    }
    if(null == websocket_url(socket_source)){
      throw "websocket source missing url";
    }
    return Reflect.construct(ctor,[websocket_url(socket_source)]);
  }
}

function resolve_socket(socket_source){
  if("string" == (typeof socket_source)){
    return connect_socket({"url":socket_source});
  }
  else if((null != socket_source) && ("object" == (typeof socket_source)) && !Array.isArray(socket_source)){
    let create_fn = socket_source["create_fn"];
    if("function" == (typeof create_fn)){
      return create_fn();
    }
    else if((null != socket_source["url"]) || (null != socket_source["connect_fn"]) || (null != socket_source["WebSocket"])){
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

function websocket_nativep(socket){
  return ((null != socket) && ("object" == (typeof socket)) && !Array.isArray(socket)) && (null != socket["::"]);
}

function ensure_promise(value){
  if(value instanceof Promise){
    return value;
  }
  else{
    return Promise.resolve().then(function (){
      return value;
    });
  }
}

function websocket_source(socket_source){
  let current_socket = null;
  let current_native = null;
  let current_message_callback = null;
  let current_open_callback = null;
  let current_error_callback = null;
  let current_close_callback = null;
  let send_fn = function (text){
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
  let start_fn = function (listener){
    return ensure_promise(resolve_socket(socket_source)).then(function (socket){
      current_socket = socket;
      current_native = (websocket_nativep(socket) ? socket : null);
      current_message_callback = (function (event){
        return listener(event,null);
      });
      if(null != current_native){
        ws.add_listeners(current_native,{"message":current_message_callback});
        if(socket_openp(current_socket)){
          return current_socket;
        }
        else{
          let state = {
            "status":"opening",
            "socket":current_socket,
            "error":"websocket failed to open"
          };
          current_open_callback = (function (_event){
            return mark_open(state,current_socket,_event);
          });
          current_error_callback = (function (event){
            return mark_error(state,event);
          });
          current_close_callback = (function (event){
            return mark_close(state,event);
          });
          ws.add_listeners(current_native,{
            "open":current_open_callback,
            "error":current_error_callback,
            "close":current_close_callback
          });
          return await_open(state).then(function (_){
            return current_socket;
          });
        }
      }
      else{
        add_socket_listener(current_socket,"message",current_message_callback);
        if(socket_openp(current_socket)){
          return current_socket;
        }
        else{
          let state = {
            "status":"opening",
            "socket":current_socket,
            "error":"websocket failed to open"
          };
          current_open_callback = (function (_event){
            return mark_open(state,current_socket,_event);
          });
          current_error_callback = (function (event){
            return mark_error(state,event);
          });
          current_close_callback = (function (event){
            return mark_close(state,event);
          });
          add_socket_listener(current_socket,"open",current_open_callback);
          add_socket_listener(current_socket,"error",current_error_callback);
          add_socket_listener(current_socket,"close",current_close_callback);
          return await_open(state).then(function (_){
            return current_socket;
          });
        }
      }
    });
  };
  let stop_fn = function (_){
    if((null != current_socket) && (null != current_message_callback)){
      if(null != current_native){
        ws.add_listeners(current_native,{"message":null});
      }
      else{
        remove_socket_listener(current_socket,"message",current_message_callback);
      }
    }
    if((null != current_socket) && (null != current_open_callback)){
      if(null != current_native){
        ws.add_listeners(current_native,{"open":null});
      }
      else{
        remove_socket_listener(current_socket,"open",current_open_callback);
      }
    }
    if((null != current_socket) && (null != current_error_callback)){
      if(null != current_native){
        ws.add_listeners(current_native,{"error":null});
      }
      else{
        remove_socket_listener(current_socket,"error",current_error_callback);
      }
    }
    if((null != current_socket) && (null != current_close_callback)){
      if(null != current_native){
        ws.add_listeners(current_native,{"close":null});
      }
      else{
        remove_socket_listener(current_socket,"close",current_close_callback);
      }
    }
    if(null != current_native){
      ws.disconnect(current_native);
    }
    else{
      if((null != current_socket) && ("function" == (typeof current_socket["close"]))){
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
  return {
    "meta":{"kind":"websocket"},
    "write_fn":send_fn,
    "start_fn":start_fn,
    "stop_fn":stop_fn
  };
}

function websocket_endpoint(socket_source){
  let endpoint = json_transport.text_endpoint(websocket_source(socket_source));
  endpoint["meta"] = {"kind":"websocket","encoding":"json"};
  return endpoint;
}

module.exports = {
  ["socket_openp"]:socket_openp,
  ["add_socket_listener"]:add_socket_listener,
  ["remove_socket_listener"]:remove_socket_listener,
  ["event_text"]:event_text,
  ["websocket_url"]:websocket_url,
  ["mark_open"]:mark_open,
  ["mark_error"]:mark_error,
  ["mark_close"]:mark_close,
  ["await_open"]:await_open,
  ["connect_socket"]:connect_socket,
  ["resolve_socket"]:resolve_socket,
  ["websocket_nativep"]:websocket_nativep,
  ["ensure_promise"]:ensure_promise,
  ["websocket_source"]:websocket_source,
  ["websocket_endpoint"]:websocket_endpoint
}