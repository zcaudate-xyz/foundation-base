import 'package:xtalk_substrate/substrate.dart' as main;
import 'dart:async';


ready_eventp(event, opts) {
  var ready_pred = opts["ready_pred"];
  if((ready_pred.runtimeType).toString().contains("Function") || (ready_pred.runtimeType).toString().contains("=>") || (ready_pred).toString().startsWith("Closure")){
    return ready_pred(event);
  }
  else{
    var ready_signal = opts.containsKey("ready_signal") ? opts["ready_signal"] : "ready";
    if(null == ready_signal){
      return false;
    }
    else{
      return (("Map" == (event.runtimeType).toString()) || (event.runtimeType).toString().startsWith("_Map") || (event.runtimeType).toString().startsWith("LinkedMap")) && (event["signal"] == ready_signal);
    }
  }
}

await_ready(state) {
  var ready = state["ready"];
  if(null != ready){
    return Future.sync(() {
      return ready;
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
      return await_ready(state);
    },<dynamic>[value]); });
  }
}

connection_record(node, transport_id, ready) {
  var transport = main.transport_get(node,transport_id);
  return <dynamic, dynamic>{
    "node":node,
    "transport_id":transport_id,
    "transport":transport,
    "target":(null == transport) ? null : transport["listener"],
    "ready":ready,
    "disconnect_fn":() {
        return main.detach_transport(node,transport_id);
      }
  };
}

wrap_ready_endpoint(endpoint, state, opts) {
  var wait_ready = opts.containsKey("wait_ready") ? opts["wait_ready"] : true;
  if(!((null != wait_ready) && (false != wait_ready))){
    return endpoint;
  }
  else{
    var start_fn = endpoint["start_fn"];
    if(null == start_fn){
      return endpoint;
    }
    else{
      return xt.lang.common_data.obj_assign(xt.lang.common_data.obj_assign(<dynamic, dynamic>{},endpoint),<dynamic, dynamic>{
        "start_fn":(listener) {
                return Function.apply((start_fn as Function),<dynamic>[
                  (event, ctx) {
                          if((() {
                            var dart_truthy__51338 = ready_eventp(event,opts);
                            return (null != dart_truthy__51338) && (false != dart_truthy__51338);
                          })()){
                            state["ready"] = event;
                            return event;
                          }
                          else{
                            return listener(event,ctx);
                          }
                        }
                ]);
              }
      });
    }
  }
}

source_endpoint(source, kind) {
  var current_target = null;
  var send_fn = (frame) {
    if(null == current_target){
      throw "source endpoint not started";
    }
    return current_target.postMessage(frame);
  };
  var start_fn = (listener) {
    current_target = source["create_fn"](listener);
    return current_target;
  };
  var stop_fn = (_) {
    if((null != current_target) && ((current_target["close"].runtimeType).toString().contains("Function") || (current_target["close"].runtimeType).toString().contains("=>") || (current_target["close"]).toString().startsWith("Closure"))){
      current_target.close();
    }
    if((null != current_target) && ((current_target["terminate"].runtimeType).toString().contains("Function") || (current_target["terminate"].runtimeType).toString().contains("=>") || (current_target["terminate"]).toString().startsWith("Closure"))){
      current_target.terminate();
    }
    current_target = null;
    return true;
  };
  return <dynamic, dynamic>{
    "meta":<dynamic, dynamic>{"kind":kind},
    "send_fn":send_fn,
    "start_fn":start_fn,
    "stop_fn":stop_fn
  };
}

connect_endpoint(node, transport_id, endpoint, opts) {
  var config = opts ?? <dynamic, dynamic>{};
  var state = <dynamic, dynamic>{"ready":null};
  var wait_ready = config.containsKey("wait_ready") ? config["wait_ready"] : true;
  return ((Future.sync(() => main.attach_transport(node,transport_id,wrap_ready_endpoint(endpoint,state,config)))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
    if((null != wait_ready) && (false != wait_ready)){
      return ((Future.sync(() => await_ready(state))) as Future<dynamic>).then((value) async { return await Function.apply((ready) {
        return connection_record(node,transport_id,ready);
      },<dynamic>[value]); });
    }
    else{
      return connection_record(node,transport_id,null);
    }
  },<dynamic>[value]); });
}

event_data(event) {
  return ((("Map" == (event.runtimeType).toString()) || (event.runtimeType).toString().startsWith("_Map") || (event.runtimeType).toString().startsWith("LinkedMap")) && event.containsKey("data")) ? event["data"] : event;
}

messageport_endpoint(port) {
  var current_callback = null;
  var send_fn = (frame) {
    return port.postMessage(frame);
  };
  var start_fn = (listener) {
    current_callback = ((event) {
      return listener(event_data(event),null);
    });
    if((port["start"].runtimeType).toString().contains("Function") || (port["start"].runtimeType).toString().contains("=>") || (port["start"]).toString().startsWith("Closure")){
      port.start();
    }
    port.addEventListener("message",current_callback,false);
    return port;
  };
  var stop_fn = (_) {
    if((null != current_callback) && ((port["removeEventListener"].runtimeType).toString().contains("Function") || (port["removeEventListener"].runtimeType).toString().contains("=>") || (port["removeEventListener"]).toString().startsWith("Closure"))){
      port.removeEventListener("message",current_callback,false);
    }
    if((port["close"].runtimeType).toString().contains("Function") || (port["close"].runtimeType).toString().contains("=>") || (port["close"]).toString().startsWith("Closure")){
      port.close();
    }
    current_callback = null;
    return true;
  };
  return <dynamic, dynamic>{
    "meta":<dynamic, dynamic>{"kind":"messageport"},
    "send_fn":send_fn,
    "start_fn":start_fn,
    "stop_fn":stop_fn
  };
}

sharedworker_endpoint(shared_or_port) {
  var port = shared_or_port.containsKey("port") ? shared_or_port["port"] : shared_or_port;
  return messageport_endpoint(port);
}

worker_endpoint(worker_source) {
  var current_worker = null;
  var current_callback = null;
  var send_fn = (frame) {
    var worker = current_worker ?? (worker_source.containsKey("create_fn") ? null : worker_source);
    if(null == worker){
      throw "worker endpoint not started";
    }
    var post_request = worker["postRequest"];
    if((post_request.runtimeType).toString().contains("Function") || (post_request.runtimeType).toString().contains("=>") || (post_request).toString().startsWith("Closure")){
      return post_request(frame);
    }
    else{
      return worker.postMessage(frame);
    }
  };
  var start_fn = (listener) {
    if(worker_source.containsKey("create_fn")){
      current_worker = worker_source["create_fn"](listener);
      return current_worker;
    }
    else{
      current_worker = worker_source;
      current_callback = ((event) {
        return listener(event_data(event),null);
      });
      current_worker.addEventListener("message",current_callback,false);
      return current_worker;
    }
  };
  var stop_fn = (_) {
    if((null != current_worker) && (null != current_callback) && ((current_worker["removeEventListener"].runtimeType).toString().contains("Function") || (current_worker["removeEventListener"].runtimeType).toString().contains("=>") || (current_worker["removeEventListener"]).toString().startsWith("Closure"))){
      current_worker.removeEventListener("message",current_callback,false);
    }
    if((null != current_worker) && ((current_worker["terminate"].runtimeType).toString().contains("Function") || (current_worker["terminate"].runtimeType).toString().contains("=>") || (current_worker["terminate"]).toString().startsWith("Closure"))){
      current_worker.terminate();
    }
    current_worker = null;
    current_callback = null;
    return true;
  };
  return <dynamic, dynamic>{
    "meta":<dynamic, dynamic>{"kind":"webworker"},
    "send_fn":send_fn,
    "start_fn":start_fn,
    "stop_fn":stop_fn
  };
}

self_endpoint(worker_self) {
  var current_callback = null;
  var send_fn = (frame) {
    worker_self.postMessage(frame);
    return Future.sync(() {
      return true;
    });
  };
  var start_fn = (listener) {
    current_callback = ((event) {
      return listener(event_data(event),null);
    });
    worker_self.addEventListener("message",current_callback,false);
    return worker_self;
  };
  var stop_fn = (_) {
    if((null != current_callback) && ((worker_self["removeEventListener"].runtimeType).toString().contains("Function") || (worker_self["removeEventListener"].runtimeType).toString().contains("=>") || (worker_self["removeEventListener"]).toString().startsWith("Closure"))){
      worker_self.removeEventListener("message",current_callback,false);
    }
    current_callback = null;
    return true;
  };
  return <dynamic, dynamic>{
    "meta":<dynamic, dynamic>{"kind":"webworker.self"},
    "send_fn":send_fn,
    "start_fn":start_fn,
    "stop_fn":stop_fn
  };
}

connect_port(node, opts) {
  var config = opts ?? <dynamic, dynamic>{};
  var port = config["port"] ?? config["source"];
  var transport_id = config["transport_id"] ?? "port";
  if(null == port){
    throw "connect-port requires `port` or `source`";
  }
  return connect_endpoint(node,transport_id,messageport_endpoint(port),config);
}

connect_sharedworker(node, opts) {
  var config = opts ?? <dynamic, dynamic>{};
  var source = config["source"] ?? config["sharedworker"];
  var transport_id = config["transport_id"] ?? "worker";
  if(null == source){
    throw "connect-sharedworker requires `source` or `sharedworker`";
  }
  return connect_endpoint(
    node,
    transport_id,
    source.containsKey("create_fn") ? source_endpoint(source,"sharedworker") : sharedworker_endpoint(source),
    config
  );
}

connect_worker(node, opts) {
  var config = opts ?? <dynamic, dynamic>{};
  var source = config["source"] ?? config["worker"];
  var transport_id = config["transport_id"] ?? "worker";
  if(null == source){
    throw "connect-worker requires `source` or `worker`";
  }
  return connect_endpoint(
    node,
    transport_id,
    source.containsKey("create_fn") ? source_endpoint(source,"webworker") : worker_endpoint(source),
    config
  );
}

boot_self(node, opts) {
  var config = opts ?? <dynamic, dynamic>{};
  var target = config["target"];
  var transport_id = config["transport_id"] ?? "host";
  var ready = config["ready"];
  if(null == target){
    throw "boot-self requires `target`";
  }
  return ((Future.sync(() => main.attach_transport(node,transport_id,self_endpoint(target)))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
    if(null == ready){
      return connection_record(node,transport_id,null);
    }
    else{
      return ((Future.sync(() => (main.transport_get(node,transport_id))["send_fn"](ready))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
        return connection_record(node,transport_id,ready);
      },<dynamic>[value]); });
    }
  },<dynamic>[value]); });
}

disconnect(connection) {
  return main.detach_transport(connection["node"],connection["transport_id"]);
}

blob_url(script) {
  var blob = new Blob(
    <dynamic>[script],
    <dynamic, dynamic>{"type":"text/javascript"}
  );
  return __globals__["URL"].createObjectURL(blob);
}

webworker_source(script, opts) {
  var worker_opts = opts ?? <dynamic, dynamic>{};
  return <dynamic, dynamic>{
    "create_fn":(listener) {
        var url = blob_url(script);
        try{
          var worker = new Worker(url,worker_opts);
          worker.addEventListener("message",(e) {
            return listener(e["data"]);
          },false);
          __globals__["URL"].revokeObjectURL(url);
          return worker;
        }
        catch(err){
          __globals__["URL"].revokeObjectURL(url);
          throw err;
        }
      }
  };
}

sharedworker_source(script, opts) {
  var worker_opts = opts ?? <dynamic, dynamic>{};
  return <dynamic, dynamic>{
    "create_fn":(listener) {
        var url = blob_url(script);
        try{
          var shared = new SharedWorker(url,worker_opts);
          var port = shared["port"];
          port.start();
          port.addEventListener("message",(e) {
            return listener(e["data"]);
          },false);
          __globals__["URL"].revokeObjectURL(url);
          return port;
        }
        catch(err){
          __globals__["URL"].revokeObjectURL(url);
          throw err;
        }
      }
  };
}

sharedworker_url_source(url) {
  return <dynamic, dynamic>{
    "create_fn":(listener) {
        var shared = new SharedWorker(url);
        var port = shared["port"];
        port.start();
        port.addEventListener("message",(e) {
          return listener(e["data"]);
        },false);
        return port;
      }
  };
}

node_worker_source(script, opts) {
  var config = opts ?? <dynamic, dynamic>{};
  var eval_flag = config["eval"];
  var eval_mode = (null == eval_flag) ? true : eval_flag;
  var Worker = require("worker_threads")["Worker"];
  return <dynamic, dynamic>{
    "create_fn":(listener) {
        var worker = new Worker(
          script,
          ((null != eval_mode) && (false != eval_mode)) ? <dynamic, dynamic>{"eval":true,"type":"module"} : <dynamic, dynamic>{}
        );
        worker.on("message",(data) {
          return listener(data);
        });
        return worker;
      }
  };
}