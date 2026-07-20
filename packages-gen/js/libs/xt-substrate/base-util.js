const node_pubsub = require("@xtalk/substrate/base-pubsub.js")

const xtd = require("@xtalk/lang/common-data.js")

const node_request = require("@xtalk/substrate/base-request.js")

const router = require("@xtalk/substrate/base-router.js")

const frame = require("@xtalk/substrate/base-frame.js")

function transport_get(node,transport_id){
  return (node["transports"])[transport_id];
}

function transport_list(node){
  return xtd.arr_sort(Object.keys(node["transports"]),function (x){
    return x;
  },function (x,y){
    return 0 > x.localeCompare(y);
  });
}

function transport_send(node,transport_id,frame){
  let transport = transport_get(node,transport_id);
  if(null == transport){
    throw "transport not found - " + transport_id;
  }
  let send_fn = transport["send_fn"];
  if(null == send_fn){
    throw "transport missing send_fn - " + transport_id;
  }
  return node_request.ensure_promise(send_fn(frame));
}

function transport_broadcast_loop(node,ids,frame,exclude_id,index){
  if(index >= ids.length){
    return Promise.resolve().then(function (){
      return frame;
    });
  }
  let transport_id = ids[index];
  if(transport_id == exclude_id){
    return transport_broadcast_loop(node,ids,frame,exclude_id,index + 1);
  }
  else{
    return transport_send(node,transport_id,frame).then(function (_){
      return transport_broadcast_loop(node,ids,frame,exclude_id,index + 1);
    });
  }
}

function transport_request_target(node,meta){
  if(meta["local"]){
    return null;
  }
  let target = meta["transport_id"];
  if(null != target){
    return target;
  }
  let transports = transport_list(node);
  if(0 == transports.length){
    return null;
  }
  return transports[0];
}

function stream_route_loop(node,ids,frame,exclude_id,index){
  if(index >= ids.length){
    return Promise.resolve().then(function (){
      return frame;
    });
  }
  let transport_id = ids[index];
  if(transport_id == exclude_id){
    return stream_route_loop(node,ids,frame,exclude_id,index + 1);
  }
  else{
    return transport_send(node,transport_id,frame).then(function (_){
      return stream_route_loop(node,ids,frame,exclude_id,index + 1);
    });
  }
}

function pending_await(state){
  let status = state["status"];
  if(status == "resolved"){
    return Promise.resolve().then(function (){
      return state["value"];
    });
  }
  else if(status == "rejected"){
    return new Promise(function (resolve,reject){
      (function (_,reject){
        reject(state["error"]);
      })(resolve,reject);
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
      },1);
    }).then(function (_){
      return pending_await(state);
    });
  }
}

function request_context_merge(request,ctx){
  ctx = (ctx || {});
  let meta = request["meta"];
  if(null == meta){
    meta = {};
    request["meta"] = meta;
  }
  if(null != ctx["transport_id"]){
    meta["transport_id"] = ctx["transport_id"];
  }
  return request;
}

function response_ok(node,request,data,meta,ctx){
  let response = frame.response_ok_frame(request["id"],request["space"],data,meta);
  let transport_id = ctx["transport_id"];
  if(null == transport_id){
    let request_meta = request["meta"];
    if(null != request_meta){
      transport_id = request_meta["transport_id"];
    }
  }
  if(null == transport_id){
    return Promise.resolve().then(function (){
      return response;
    });
  }
  else{
    return transport_send(node,transport_id,response).then(function (_){
      return response;
    });
  }
}

function response_error(node,request,error,meta,ctx){
  let response = frame.response_error_frame(request["id"],request["space"],error,meta);
  let transport_id = ctx["transport_id"];
  if(null == transport_id){
    let request_meta = request["meta"];
    if(null != request_meta){
      transport_id = request_meta["transport_id"];
    }
  }
  if(null == transport_id){
    return Promise.resolve().then(function (){
      return response;
    });
  }
  else{
    return transport_send(node,transport_id,response).then(function (_){
      return response;
    });
  }
}

function config_normalize_space(space_id,config){
  if(null == config){
    return null;
  }
  else if(((null != config) && ("object" == (typeof config)) && !Array.isArray(config)) && ((null != config["id"]) || (null != config["state"]) || (null != config["meta"]))){
    if((null != config["id"]) && !(config["id"] == space_id)){
      throw "space id mismatch - " + space_id;
    }
    return {"state":config["state"],"meta":config["meta"] || {}};
  }
  else{
    throw "invalid space config - " + space_id;
  }
}

function config_normalize_handler(action,config){
  if("function" == (typeof config)){
    return {"fn":config,"meta":{}};
  }
  else if(((null != config) && ("object" == (typeof config)) && !Array.isArray(config)) && ("function" == (typeof config["fn"]))){
    if((null != config["id"]) && !(config["id"] == action)){
      throw "handler id mismatch - " + action;
    }
    return {"fn":config["fn"],"meta":config["meta"] || {}};
  }
  else{
    throw "invalid handler config - " + action;
  }
}

function config_normalize_trigger(signal,config){
  if("function" == (typeof config)){
    return {"fn":config,"meta":{}};
  }
  else if(((null != config) && ("object" == (typeof config)) && !Array.isArray(config)) && ("function" == (typeof config["fn"]))){
    if((null != config["id"]) && !(config["id"] == signal)){
      throw "trigger id mismatch - " + signal;
    }
    return {"fn":config["fn"],"meta":config["meta"] || {}};
  }
  else{
    throw "invalid trigger config - " + signal;
  }
}

function node_base_opts(opts){
  let base = Object.assign({},opts || {});
  delete(base["spaces"]);
  delete(base["handlers"]);
  delete(base["triggers"]);
  return base;
}

function register_handler(node,action,handler,meta){
  let entry = {"id":action,"fn":handler,"meta":meta || {}};
  node["handlers"][action] = entry;
  return entry;
}

function unregister_handler(node,action){
  let handlers = node["handlers"];
  let prev = handlers[action];
  delete(handlers[action]);
  return prev;
}

function get_handler(node,action){
  return (node["handlers"])[action];
}

function list_handlers(node){
  return xtd.arr_sort(Object.keys(node["handlers"]),function (x){
    return x;
  },function (x,y){
    return 0 > x.localeCompare(y);
  });
}

function register_trigger(node,signal,trigger_fn,meta){
  let entry = {"id":signal,"fn":trigger_fn,"meta":meta || {}};
  node["triggers"][signal] = entry;
  return entry;
}

function unregister_trigger(node,signal){
  let triggers = node["triggers"];
  let prev = triggers[signal];
  delete(triggers[signal]);
  return prev;
}

function get_trigger(node,signal){
  return (node["triggers"])[signal];
}

function list_triggers(node){
  return xtd.arr_sort(Object.keys(node["triggers"]),function (x){
    return x;
  },function (x,y){
    return 0 > x.localeCompare(y);
  });
}

function request(node,space,action,args,meta){
  meta = (meta || {});
  let request_frame = frame.request_frame(space,action,args,meta);
  let target = transport_request_target(node,meta);
  if(null == target){
    return node_request.invoke_handler(node,request_frame);
  }
  else{
    return new Promise(function (resolve,reject){
      (function (resolve,reject){
        node_request.add_pending(node,request_frame,resolve,reject,{"transport_id":target});
        try{
          return transport_send(node,target,request_frame).catch(function (err){
            node_request.remove_pending(node,request_frame["id"]);
            return reject(err);
          });
        }
        catch(err){
          node_request.remove_pending(node,request_frame["id"]);
          return reject(err);
        }
      })(resolve,reject);
    });
  }
}

function publish(node,space,signal,data,meta){
  meta = (meta || {});
  let stream = frame.stream_frame(space,signal,data,meta,meta["cause"]);
  return node_pubsub.receive_publish(node,stream).then(function (_){
    return stream_route_loop(
      node,
      router.target_ids(node,stream["space"],stream["signal"]),
      stream,
      meta["transport_id"],
      0
    );
  });
}

module.exports = {
  ["transport_get"]:transport_get,
  ["transport_list"]:transport_list,
  ["transport_send"]:transport_send,
  ["transport_broadcast_loop"]:transport_broadcast_loop,
  ["transport_request_target"]:transport_request_target,
  ["stream_route_loop"]:stream_route_loop,
  ["pending_await"]:pending_await,
  ["request_context_merge"]:request_context_merge,
  ["response_ok"]:response_ok,
  ["response_error"]:response_error,
  ["config_normalize_space"]:config_normalize_space,
  ["config_normalize_handler"]:config_normalize_handler,
  ["config_normalize_trigger"]:config_normalize_trigger,
  ["node_base_opts"]:node_base_opts,
  ["register_handler"]:register_handler,
  ["unregister_handler"]:unregister_handler,
  ["get_handler"]:get_handler,
  ["list_handlers"]:list_handlers,
  ["register_trigger"]:register_trigger,
  ["unregister_trigger"]:unregister_trigger,
  ["get_trigger"]:get_trigger,
  ["list_triggers"]:list_triggers,
  ["request"]:request,
  ["publish"]:publish
}