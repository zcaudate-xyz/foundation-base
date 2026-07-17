const main = require("@xtalk/substrate/substrate.js")

function ready_eventp(event,opts){
  let ready_pred = opts["ready_pred"];
  if("function" == (typeof ready_pred)){
    return ready_pred(event);
  }
  else{
    let ready_signal = (null != opts["ready_signal"]) ? opts["ready_signal"] : "ready";
    if(null == ready_signal){
      return false;
    }
    else{
      return ((null != event) && ("object" == (typeof event)) && !Array.isArray(event)) && (event["signal"] == ready_signal);
    }
  }
}

function await_ready(state){
  let ready = state["ready"];
  if(null != ready){
    return Promise.resolve().then(function (){
      return ready;
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
      return await_ready(state);
    });
  }
}

function connection_record(node,transport_id,ready){
  let transport = main.transport_get(node,transport_id);
  return {
    "node":node,
    "transport_id":transport_id,
    "transport":transport,
    "target":(null == transport) ? null : transport["listener"],
    "ready":ready,
    "disconnect_fn":function (){
        return main.detach_transport(node,transport_id);
      }
  };
}

function wrap_ready_endpoint(endpoint,state,opts){
  let wait_ready = (null != opts["wait_ready"]) ? opts["wait_ready"] : true;
  if(!wait_ready){
    return endpoint;
  }
  else{
    let start_fn = endpoint["start_fn"];
    if(null == start_fn){
      return endpoint;
    }
    else{
      return Object.assign(Object.assign({},endpoint),{
        "start_fn":function (listener){
                return start_fn(function (event,ctx){
                  if(ready_eventp(event,opts)){
                    state["ready"] = event;
                    return event;
                  }
                  else{
                    return listener(event,ctx);
                  }
                });
              }
      });
    }
  }
}

function source_endpoint(source,kind){
  let current_target = null;
  let send_fn = function (frame){
    if(null == current_target){
      throw "source endpoint not started";
    }
    return current_target.postMessage(frame);
  };
  let start_fn = function (listener){
    current_target = source["create_fn"](listener);
    return current_target;
  };
  let stop_fn = function (_){
    if((null != current_target) && ("function" == (typeof current_target["close"]))){
      current_target.close();
    }
    if((null != current_target) && ("function" == (typeof current_target["terminate"]))){
      current_target.terminate();
    }
    current_target = null;
    return true;
  };
  return {
    "meta":{"kind":kind},
    "send_fn":send_fn,
    "start_fn":start_fn,
    "stop_fn":stop_fn
  };
}

function connect_endpoint(node,transport_id,endpoint,opts){
  let config = opts || {};
  let state = {"ready":null};
  let wait_ready = (null != config["wait_ready"]) ? config["wait_ready"] : true;
  return main.attach_transport(node,transport_id,wrap_ready_endpoint(endpoint,state,config)).then(function (_){
    if(wait_ready){
      return await_ready(state).then(function (ready){
        return connection_record(node,transport_id,ready);
      });
    }
    else{
      return connection_record(node,transport_id,null);
    }
  });
}

function event_data(event){
  return (((null != event) && ("object" == (typeof event)) && !Array.isArray(event)) && (null != event["data"])) ? event["data"] : event;
}

function messageport_endpoint(port){
  let current_callback = null;
  let send_fn = function (frame){
    return port.postMessage(frame);
  };
  let start_fn = function (listener){
    current_callback = (function (event){
      return listener(event_data(event),null);
    });
    if("function" == (typeof port["start"])){
      port.start();
    }
    port.addEventListener("message",current_callback,false);
    return port;
  };
  let stop_fn = function (_){
    if((null != current_callback) && ("function" == (typeof port["removeEventListener"]))){
      port.removeEventListener("message",current_callback,false);
    }
    if("function" == (typeof port["close"])){
      port.close();
    }
    current_callback = null;
    return true;
  };
  return {
    "meta":{"kind":"messageport"},
    "send_fn":send_fn,
    "start_fn":start_fn,
    "stop_fn":stop_fn
  };
}

function sharedworker_endpoint(shared_or_port){
  let port = (null != shared_or_port["port"]) ? shared_or_port["port"] : shared_or_port;
  return messageport_endpoint(port);
}

function worker_endpoint(worker_source){
  let current_worker = null;
  let current_callback = null;
  let send_fn = function (frame){
    let worker = current_worker || ((null != worker_source["create_fn"]) ? null : worker_source);
    if(null == worker){
      throw "worker endpoint not started";
    }
    let post_request = worker["postRequest"];
    if("function" == (typeof post_request)){
      return post_request(frame);
    }
    else{
      return worker.postMessage(frame);
    }
  };
  let start_fn = function (listener){
    if(null != worker_source["create_fn"]){
      current_worker = worker_source["create_fn"](listener);
      return current_worker;
    }
    else{
      current_worker = worker_source;
      current_callback = (function (event){
        return listener(event_data(event),null);
      });
      current_worker.addEventListener("message",current_callback,false);
      return current_worker;
    }
  };
  let stop_fn = function (_){
    if((null != current_worker) && (null != current_callback) && ("function" == (typeof current_worker["removeEventListener"]))){
      current_worker.removeEventListener("message",current_callback,false);
    }
    if((null != current_worker) && ("function" == (typeof current_worker["terminate"]))){
      current_worker.terminate();
    }
    current_worker = null;
    current_callback = null;
    return true;
  };
  return {
    "meta":{"kind":"webworker"},
    "send_fn":send_fn,
    "start_fn":start_fn,
    "stop_fn":stop_fn
  };
}

function self_endpoint(worker_self){
  let current_callback = null;
  let send_fn = function (frame){
    worker_self.postMessage(frame);
    return Promise.resolve().then(function (){
      return true;
    });
  };
  let start_fn = function (listener){
    current_callback = (function (event){
      return listener(event_data(event),null);
    });
    worker_self.addEventListener("message",current_callback,false);
    return worker_self;
  };
  let stop_fn = function (_){
    if((null != current_callback) && ("function" == (typeof worker_self["removeEventListener"]))){
      worker_self.removeEventListener("message",current_callback,false);
    }
    current_callback = null;
    return true;
  };
  return {
    "meta":{"kind":"webworker.self"},
    "send_fn":send_fn,
    "start_fn":start_fn,
    "stop_fn":stop_fn
  };
}

function connect_port(node,opts){
  let config = opts || {};
  let port = config["port"] || config["source"];
  let transport_id = config["transport_id"] || "port";
  if(null == port){
    throw "connect-port requires `port` or `source`";
  }
  return connect_endpoint(node,transport_id,messageport_endpoint(port),config);
}

function connect_sharedworker(node,opts){
  let config = opts || {};
  let source = config["source"] || config["sharedworker"];
  let transport_id = config["transport_id"] || "worker";
  if(null == source){
    throw "connect-sharedworker requires `source` or `sharedworker`";
  }
  return connect_endpoint(
    node,
    transport_id,
    (null != source["create_fn"]) ? source_endpoint(source,"sharedworker") : sharedworker_endpoint(source),
    config
  );
}

function connect_worker(node,opts){
  let config = opts || {};
  let source = config["source"] || config["worker"];
  let transport_id = config["transport_id"] || "worker";
  if(null == source){
    throw "connect-worker requires `source` or `worker`";
  }
  return connect_endpoint(
    node,
    transport_id,
    (null != source["create_fn"]) ? source_endpoint(source,"webworker") : worker_endpoint(source),
    config
  );
}

function boot_self(node,opts){
  let config = opts || {};
  let target = config["target"];
  let transport_id = config["transport_id"] || "host";
  let ready = config["ready"];
  if(null == target){
    throw "boot-self requires `target`";
  }
  return main.attach_transport(node,transport_id,self_endpoint(target)).then(function (_){
    if(null == ready){
      return connection_record(node,transport_id,null);
    }
    else{
      return (main.transport_get(node,transport_id))["send_fn"](ready).then(function (_){
        return connection_record(node,transport_id,ready);
      });
    }
  });
}

function disconnect(connection){
  return main.detach_transport(connection["node"],connection["transport_id"]);
}

function blob_url(script){
  let blob = new Blob([script],{"type":"text/javascript"});
  return globalThis["URL"].createObjectURL(blob);
}

function webworker_source(script,opts){
  let worker_opts = opts || {};
  return {
    "create_fn":function (listener){
        let url = blob_url(script);
        try{
          let worker = new Worker(url,worker_opts);
          worker.addEventListener("message",function (e){
            return listener(e["data"]);
          },false);
          globalThis["URL"].revokeObjectURL(url);
          return worker;
        }
        catch(err){
          globalThis["URL"].revokeObjectURL(url);
          throw err;
        }
      }
  };
}

function sharedworker_source(script,opts){
  let worker_opts = opts || {};
  return {
    "create_fn":function (listener){
        let url = blob_url(script);
        try{
          let shared = new SharedWorker(url,worker_opts);
          let port = shared["port"];
          port.start();
          port.addEventListener("message",function (e){
            return listener(e["data"]);
          },false);
          globalThis["URL"].revokeObjectURL(url);
          return port;
        }
        catch(err){
          globalThis["URL"].revokeObjectURL(url);
          throw err;
        }
      }
  };
}

function sharedworker_url_source(url){
  return {
    "create_fn":function (listener){
        let shared = new SharedWorker(url);
        let port = shared["port"];
        port.start();
        port.addEventListener("message",function (e){
          return listener(e["data"]);
        },false);
        return port;
      }
  };
}

function node_worker_source(script,opts){
  let config = opts || {};
  let eval_flag = config["eval"];
  let eval_mode = (null == eval_flag) ? true : eval_flag;
  let {Worker} = require("worker_threads");
  return {
    "create_fn":function (listener){
        let worker = new Worker(script,eval_mode ? {"eval":true,"type":"module"} : {});
        worker.on("message",function (data){
          return listener(data);
        });
        return worker;
      }
  };
}

module.exports = {
  ["ready_eventp"]:ready_eventp,
  ["await_ready"]:await_ready,
  ["connection_record"]:connection_record,
  ["wrap_ready_endpoint"]:wrap_ready_endpoint,
  ["source_endpoint"]:source_endpoint,
  ["connect_endpoint"]:connect_endpoint,
  ["event_data"]:event_data,
  ["messageport_endpoint"]:messageport_endpoint,
  ["sharedworker_endpoint"]:sharedworker_endpoint,
  ["worker_endpoint"]:worker_endpoint,
  ["self_endpoint"]:self_endpoint,
  ["connect_port"]:connect_port,
  ["connect_sharedworker"]:connect_sharedworker,
  ["connect_worker"]:connect_worker,
  ["boot_self"]:boot_self,
  ["disconnect"]:disconnect,
  ["blob_url"]:blob_url,
  ["webworker_source"]:webworker_source,
  ["sharedworker_source"]:sharedworker_source,
  ["sharedworker_url_source"]:sharedworker_url_source,
  ["node_worker_source"]:node_worker_source
}