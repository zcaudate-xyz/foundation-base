import * as node_pubsub from './substrate/base-pubsub.mjs'

import * as event_common from './event/base-listener.mjs'

import * as page_proxy from './substrate/page-proxy.mjs'

import * as node_request from './substrate/base-request.mjs'

import * as util_handlers from './substrate/base-util-handlers.mjs'

import * as router from './substrate/base-router.mjs'

import * as frame from './substrate/base-frame.mjs'

import * as base_util from './substrate/base-util.mjs'

import * as page from './substrate/page-core.mjs'

import * as node_space from './substrate/base-space.mjs'

export var create_space = node_space.create_space;

export var get_space = node_space.get_space;

export var list_spaces = node_space.list_spaces;

export var get_space_state = node_space.get_space_state;

export var set_space_state = node_space.set_space_state;

export var update_space_state = node_space.update_space_state;

export var page_space_get = page.space_get_page;

export var page_space_ensure = page.space_ensure_page;

export var page_space_set = page.space_set_page;

export var page_group_get = page.group_get;

export var page_group_ensure = page.group_ensure;

export var page_model_ensure = page.model_ensure;

export var page_group_add_attach = page.group_add_attach;

export var page_group_add = page.group_add;

export var page_group_remove = page.group_remove;

export var page_model_remove = page.model_remove;

export var page_group_update = page.group_update;

export var page_model_update = page.model_update;

export var page_model_set_input = page.model_set_input;

export var page_group_trigger = page.group_trigger;

export var page_model_trigger = page.model_trigger;

export var page_space_trigger_all = page.space_trigger_all;

export var page_raw_callback_add = page.raw_callback_add;

export var page_raw_callback_remove = page.raw_callback_remove;

export var page_proxy_install = page_proxy.install;

export var page_proxy_list = page_proxy.group_list_proxy;

export var page_proxy_open = page_proxy.group_open_proxy;

export var page_proxy_close = page_proxy.group_close_proxy;

export var page_proxy_call = page_proxy.model_proxy_call;

export var page_proxy_sync = page_proxy.group_sync_proxy;

export function nodep(obj){
  return ((null != obj) && ("object" == (typeof obj)) && !Array.isArray(obj)) && ("substrate" == obj["::"]);
}

export function transportp(obj){
  return ((null != obj) && ("object" == (typeof obj)) && !Array.isArray(obj)) && ("substrate.transport" == obj["::"]);
}

export function transport_create(transport_id,impl){
  return Object.assign(
    {"::":"substrate.transport","id":transport_id,"listener":null},
    impl || {}
  );
}

export function get_services(node){
  return node["services"] || {};
}

export function get_service(node,service_id){
  return (get_services(node))[service_id];
}

export function set_service(node,service_id,service){
  node["services"][service_id] = service;
  return service;
}

export function remove_service(node,service_id){
  let service = get_service(node,service_id);
  delete(node["services"][service_id]);
  return service;
}

export function transport_get(node,transport_id){
  return base_util.transport_get(node,transport_id);
}

export function transport_list(node){
  return base_util.transport_list(node);
}

export function transport_send(node,transport_id,frame){
  return base_util.transport_send(node,transport_id,frame);
}

export function list_subscriptions(node,space,signal){
  return router.list_subscriptions(node,space,signal);
}

export function publish(node,space,signal,data,meta){
  return base_util.publish(node,space,signal,data,meta);
}

export function list_triggers(node){
  return base_util.list_triggers(node);
}

export function register_trigger(node,signal,trigger_fn,meta){
  return base_util.register_trigger(node,signal,trigger_fn,meta);
}

export function get_trigger(node,signal){
  return base_util.get_trigger(node,signal);
}

export function unregister_handler(node,action){
  return base_util.unregister_handler(node,action);
}

export function unregister_trigger(node,signal){
  return base_util.unregister_trigger(node,signal);
}

export function get_handler(node,action){
  return base_util.get_handler(node,action);
}

export function list_handlers(node){
  return base_util.list_handlers(node);
}

export function register_handler(node,action,handler,meta){
  return base_util.register_handler(node,action,handler,meta);
}

export function request(node,space,action,args,meta){
  return base_util.request(node,space,action,args,meta);
}

export function broadcast_transport(node,frame,exclude_id){
  return base_util.transport_broadcast_loop(node,transport_list(node),frame,exclude_id,0);
}

export function route_stream(node,stream,exclude_id){
  return base_util.stream_route_loop(
    node,
    router.target_ids(node,stream["space"],stream["signal"]),
    stream,
    exclude_id,
    0
  );
}

export function receive_request(node,request,ctx){
  ctx = (ctx || {});
  base_util.request_context_merge(request,ctx);
  try{
    return node_request.invoke_handler(node,request).then(function (data){
      return base_util.response_ok(node,request,data,null,ctx);
    }).catch(function (err){
      return base_util.response_error(node,request,err,null,ctx);
    });
  }
  catch(err){
    return base_util.response_error(node,request,err,null,ctx);
  }
}

export function receive_response(node,response){
  node_request.settle_pending(node,response);
  return Promise.resolve().then(function (){
    return response;
  });
}

export function subscribe(node,space,signal,subscription_id,meta){
  meta = (meta || {});
  let event = router.subscribe_frame(space,signal,subscription_id,meta);
  let target = base_util.transport_request_target(node,meta);
  if(null == target){
    return Promise.resolve().then(function (){
      return event;
    });
  }
  else{
    return transport_send(node,target,event).then(function (_){
      return event;
    });
  }
}

export function unsubscribe(node,space,signal,subscription_id,meta){
  meta = (meta || {});
  let event = router.unsubscribe_frame(space,signal,subscription_id,meta);
  let target = base_util.transport_request_target(node,meta);
  if(null == target){
    return Promise.resolve().then(function (){
      return event;
    });
  }
  else{
    return transport_send(node,target,event).then(function (_){
      return event;
    });
  }
}

export function receive_publish(node,stream,ctx){
  ctx = (ctx || {});
  return node_pubsub.receive_publish(node,stream).then(function (_){
    return route_stream(node,stream,ctx["transport_id"]);
  });
}

export function receive_frame(node,event,ctx){
  let {kind} = event;
  if(kind == "request"){
    return receive_request(node,event,ctx);
  }
  else if(kind == "response"){
    return receive_response(node,event);
  }
  else if(kind == "stream"){
    return receive_publish(node,event,ctx);
  }
  else if(kind == "subscribe"){
    return router.receive_subscribe(node,event,ctx);
  }
  else if(kind == "unsubscribe"){
    return router.receive_unsubscribe(node,event,ctx);
  }
  else{
    return Promise.resolve().then(function (){
      return event;
    });
  }
}

export function attach_transport(node,transport_id,transport){
  transport = (transportp(transport) ? transport : transport_create(transport_id,transport));
  node["transports"][transport_id] = transport;
  router.register_connection(node,transport_id,{"meta":transport["meta"]});
  let {start_fn} = transport;
  if(null == start_fn){
    return Promise.resolve().then(function (){
      return transport;
    });
  }
  return node_request.ensure_promise(start_fn(function (event,ctx){
    ctx = (ctx || {});
    if(null == ctx["transport_id"]){
      ctx["transport_id"] = transport_id;
    }
    return receive_frame(node,event,ctx);
  })).then(function (listener){
    transport["listener"] = listener;
    return transport;
  });
}

export function detach_transport(node,transport_id){
  let {transports} = node;
  let transport = transports[transport_id];
  if(null == transport){
    return Promise.resolve().then(function (){
      return null;
    });
  }
  delete(transports[transport_id]);
  router.unregister_connection(node,transport_id);
  let {stop_fn} = transport;
  if(null == stop_fn){
    return Promise.resolve().then(function (){
      return transport;
    });
  }
  return node_request.ensure_promise(stop_fn(transport["listener"])).then(function (_){
    return transport;
  });
}

export function node_configure(node,opts){
  opts = (opts || {});
  util_handlers.install_util_handlers(node);
  for(let [space_id,config] of Object.entries(opts["spaces"] || {})){
    node_space.create_space(
      node,
      space_id,
      base_util.config_normalize_space(space_id,config)
    );
  };
  for(let [action,config] of Object.entries(opts["handlers"] || {})){
    let entry = base_util.config_normalize_handler(action,config);
    register_handler(node,action,entry["fn"],entry["meta"]);
  };
  for(let [signal,config] of Object.entries(opts["triggers"] || {})){
    let entry = base_util.config_normalize_trigger(signal,config);
    register_trigger(node,signal,entry["fn"],entry["meta"]);
  };
  return node;
}

export function node_create(opts){
  opts = (opts || {});
  let node = event_common.blank_container("substrate",Object.assign({
    "meta":opts["meta"] || {},
    "router":{"connections":{},"subscriptions":{}},
    "pending":{},
    "handlers":{},
    "triggers":{},
    "spaces":{},
    "id":opts["id"] || frame.rand_id("node-",6),
    "transports":{},
    "services":{}
  },base_util.node_base_opts(opts)));
  node_configure(node,opts);
  return node;
}