const node_pubsub = require("@xtalk/substrate/base-pubsub.js")

const event_common = require("@xtalk/event/base-listener.js")

const page_proxy = require("@xtalk/substrate/page-proxy.js")

const node_request = require("@xtalk/substrate/base-request.js")

const util_handlers = require("@xtalk/substrate/base-util-handlers.js")

const router = require("@xtalk/substrate/base-router.js")

const frame = require("@xtalk/substrate/base-frame.js")

const base_util = require("@xtalk/substrate/base-util.js")

const page = require("@xtalk/substrate/page-core.js")

const view = require("@xtalk/substrate/view.js")

const node_space = require("@xtalk/substrate/base-space.js")

var create_space = node_space.create_space;

var get_space = node_space.get_space;

var list_spaces = node_space.list_spaces;

var get_space_state = node_space.get_space_state;

var set_space_state = node_space.set_space_state;

var update_space_state = node_space.update_space_state;

var page_space_get = page.space_get_page;

var page_space_ensure = page.space_ensure_page;

var page_space_set = page.space_set_page;

var page_group_get = page.group_get;

var page_group_ensure = page.group_ensure;

var page_model_ensure = page.model_ensure;

var page_group_add_attach = page.group_add_attach;

var page_group_add = page.group_add;

var page_group_remove = page.group_remove;

var page_model_remove = page.model_remove;

var page_group_update = page.group_update;

var page_model_update = page.model_update;

var page_model_set_input = page.model_set_input;

var page_group_trigger = page.group_trigger;

var page_model_trigger = page.model_trigger;

var page_space_trigger_all = page.space_trigger_all;

var view_spec = view.view_spec;

var view_node = view.node;

var view_action = view.action;

var view_event_value = view.event_value;

var view_validate = view.validate;

var view_state_get = view.state_get;

var view_state_set = view.state_set;

var view_snapshot = view.snapshot;

var view_subscribe = view.subscribe;

var view_unsubscribe = view.unsubscribe;

var view_dispatch = view.dispatch;

var page_raw_callback_add = page.raw_callback_add;

var page_raw_callback_remove = page.raw_callback_remove;

var page_proxy_install = page_proxy.install;

var page_proxy_list = page_proxy.group_list_proxy;

var page_proxy_open = page_proxy.group_open_proxy;

var page_proxy_close = page_proxy.group_close_proxy;

var page_proxy_call = page_proxy.model_proxy_call;

var page_proxy_sync = page_proxy.group_sync_proxy;

function nodep(obj){
  return ((null != obj) && ("object" == (typeof obj)) && !Array.isArray(obj)) && ("substrate" == obj["::"]);
}

function transportp(obj){
  return ((null != obj) && ("object" == (typeof obj)) && !Array.isArray(obj)) && ("substrate.transport" == obj["::"]);
}

function transport_create(transport_id,impl){
  return Object.assign(
    {"::":"substrate.transport","id":transport_id,"listener":null},
    impl || {}
  );
}

function get_services(node){
  return node["services"] || {};
}

function get_service(node,service_id){
  return (get_services(node))[service_id];
}

function set_service(node,service_id,service){
  node["services"][service_id] = service;
  return service;
}

function remove_service(node,service_id){
  let service = get_service(node,service_id);
  delete(node["services"][service_id]);
  return service;
}

function transport_get(node,transport_id){
  return base_util.transport_get(node,transport_id);
}

function transport_list(node){
  return base_util.transport_list(node);
}

function transport_send(node,transport_id,frame){
  return base_util.transport_send(node,transport_id,frame);
}

function list_subscriptions(node,space,signal){
  return router.list_subscriptions(node,space,signal);
}

function publish(node,space,signal,data,meta){
  return base_util.publish(node,space,signal,data,meta);
}

function list_triggers(node){
  return base_util.list_triggers(node);
}

function register_trigger(node,signal,trigger_fn,meta){
  return base_util.register_trigger(node,signal,trigger_fn,meta);
}

function get_trigger(node,signal){
  return base_util.get_trigger(node,signal);
}

function unregister_handler(node,action){
  return base_util.unregister_handler(node,action);
}

function unregister_trigger(node,signal){
  return base_util.unregister_trigger(node,signal);
}

function get_handler(node,action){
  return base_util.get_handler(node,action);
}

function list_handlers(node){
  return base_util.list_handlers(node);
}

function register_handler(node,action,handler,meta){
  return base_util.register_handler(node,action,handler,meta);
}

function request(node,space,action,args,meta){
  return base_util.request(node,space,action,args,meta);
}

function broadcast_transport(node,frame,exclude_id){
  return base_util.transport_broadcast_loop(node,transport_list(node),frame,exclude_id,0);
}

function route_stream(node,stream,exclude_id){
  return base_util.stream_route_loop(
    node,
    router.target_ids(node,stream["space"],stream["signal"]),
    stream,
    exclude_id,
    0
  );
}

function receive_request(node,request,ctx){
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

function receive_response(node,response){
  node_request.settle_pending(node,response);
  return Promise.resolve().then(function (){
    return response;
  });
}

function subscribe(node,space,signal,subscription_id,meta){
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

function unsubscribe(node,space,signal,subscription_id,meta){
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

function receive_publish(node,stream,ctx){
  ctx = (ctx || {});
  return node_pubsub.receive_publish(node,stream).then(function (_){
    return route_stream(node,stream,ctx["transport_id"]);
  });
}

function receive_frame(node,event,ctx){
  let kind = event["kind"];
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

function attach_transport(node,transport_id,transport){
  transport = (transportp(transport) ? transport : transport_create(transport_id,transport));
  node["transports"][transport_id] = transport;
  router.register_connection(node,transport_id,{"meta":transport["meta"]});
  let start_fn = transport["start_fn"];
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

function detach_transport(node,transport_id){
  let transports = node["transports"];
  let transport = transports[transport_id];
  if(null == transport){
    return Promise.resolve().then(function (){
      return null;
    });
  }
  delete(transports[transport_id]);
  router.unregister_connection(node,transport_id);
  let stop_fn = transport["stop_fn"];
  if(null == stop_fn){
    return Promise.resolve().then(function (){
      return transport;
    });
  }
  return node_request.ensure_promise(stop_fn(transport["listener"])).then(function (_){
    return transport;
  });
}

function node_configure(node,opts){
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

function node_create(opts){
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

module.exports = {
  ["create_space"]:create_space,
  ["get_space"]:get_space,
  ["list_spaces"]:list_spaces,
  ["get_space_state"]:get_space_state,
  ["set_space_state"]:set_space_state,
  ["update_space_state"]:update_space_state,
  ["page_space_get"]:page_space_get,
  ["page_space_ensure"]:page_space_ensure,
  ["page_space_set"]:page_space_set,
  ["page_group_get"]:page_group_get,
  ["page_group_ensure"]:page_group_ensure,
  ["page_model_ensure"]:page_model_ensure,
  ["page_group_add_attach"]:page_group_add_attach,
  ["page_group_add"]:page_group_add,
  ["page_group_remove"]:page_group_remove,
  ["page_model_remove"]:page_model_remove,
  ["page_group_update"]:page_group_update,
  ["page_model_update"]:page_model_update,
  ["page_model_set_input"]:page_model_set_input,
  ["page_group_trigger"]:page_group_trigger,
  ["page_model_trigger"]:page_model_trigger,
  ["page_space_trigger_all"]:page_space_trigger_all,
  ["view_spec"]:view_spec,
  ["view_node"]:view_node,
  ["view_action"]:view_action,
  ["view_event_value"]:view_event_value,
  ["view_validate"]:view_validate,
  ["view_state_get"]:view_state_get,
  ["view_state_set"]:view_state_set,
  ["view_snapshot"]:view_snapshot,
  ["view_subscribe"]:view_subscribe,
  ["view_unsubscribe"]:view_unsubscribe,
  ["view_dispatch"]:view_dispatch,
  ["page_raw_callback_add"]:page_raw_callback_add,
  ["page_raw_callback_remove"]:page_raw_callback_remove,
  ["page_proxy_install"]:page_proxy_install,
  ["page_proxy_list"]:page_proxy_list,
  ["page_proxy_open"]:page_proxy_open,
  ["page_proxy_close"]:page_proxy_close,
  ["page_proxy_call"]:page_proxy_call,
  ["page_proxy_sync"]:page_proxy_sync,
  ["nodep"]:nodep,
  ["transportp"]:transportp,
  ["transport_create"]:transport_create,
  ["get_services"]:get_services,
  ["get_service"]:get_service,
  ["set_service"]:set_service,
  ["remove_service"]:remove_service,
  ["transport_get"]:transport_get,
  ["transport_list"]:transport_list,
  ["transport_send"]:transport_send,
  ["list_subscriptions"]:list_subscriptions,
  ["publish"]:publish,
  ["list_triggers"]:list_triggers,
  ["register_trigger"]:register_trigger,
  ["get_trigger"]:get_trigger,
  ["unregister_handler"]:unregister_handler,
  ["unregister_trigger"]:unregister_trigger,
  ["get_handler"]:get_handler,
  ["list_handlers"]:list_handlers,
  ["register_handler"]:register_handler,
  ["request"]:request,
  ["broadcast_transport"]:broadcast_transport,
  ["route_stream"]:route_stream,
  ["receive_request"]:receive_request,
  ["receive_response"]:receive_response,
  ["subscribe"]:subscribe,
  ["unsubscribe"]:unsubscribe,
  ["receive_publish"]:receive_publish,
  ["receive_frame"]:receive_frame,
  ["attach_transport"]:attach_transport,
  ["detach_transport"]:detach_transport,
  ["node_configure"]:node_configure,
  ["node_create"]:node_create
}