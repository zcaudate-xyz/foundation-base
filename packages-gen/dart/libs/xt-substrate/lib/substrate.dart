import 'package:xtalk_substrate/base-pubsub.dart' as node_pubsub;
import 'package:xtalk_event/base-listener.dart' as event_common;
import 'package:xtalk_substrate/page-proxy.dart' as page_proxy;
import 'package:xtalk_substrate/base-request.dart' as node_request;
import 'package:xtalk_substrate/base-util-handlers.dart' as util_handlers;
import 'package:xtalk_substrate/base-router.dart' as router;
import 'package:xtalk_substrate/base-frame.dart' as frame;
import 'package:xtalk_substrate/base-util.dart' as base_util;
import 'package:xtalk_substrate/page-core.dart' as page;
import 'package:xtalk_substrate/view.dart' as view;
import 'package:xtalk_substrate/base-space.dart' as node_space;
import 'dart:async';












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

nodep(obj) {
  return (("Map" == (obj.runtimeType).toString()) || (obj.runtimeType).toString().startsWith("_Map") || (obj.runtimeType).toString().startsWith("LinkedMap")) && ("substrate" == obj["::"]);
}

transportp(obj) {
  return (("Map" == (obj.runtimeType).toString()) || (obj.runtimeType).toString().startsWith("_Map") || (obj.runtimeType).toString().startsWith("LinkedMap")) && ("substrate.transport" == obj["::"]);
}

transport_create(transport_id, impl) {
  return xtd.obj_assign(
    <dynamic, dynamic>{"::":"substrate.transport","id":transport_id,"listener":null},
    impl ?? <dynamic, dynamic>{}
  );
}

get_services(node) {
  return node["services"] ?? <dynamic, dynamic>{};
}

get_service(node, service_id) {
  return (get_services(node))[service_id];
}

set_service(node, service_id, service) {
  node["services"][service_id] = service;
  return service;
}

remove_service(node, service_id) {
  var service = get_service(node,service_id);
  node["services"].remove(service_id);
  return service;
}

transport_get(node, transport_id) {
  return base_util.transport_get(node,transport_id);
}

transport_list(node) {
  return base_util.transport_list(node);
}

transport_send(node, transport_id, frame) {
  return base_util.transport_send(node,transport_id,frame);
}

list_subscriptions(node, space, signal) {
  return router.list_subscriptions(node,space,signal);
}

publish(node, space, signal, data, meta) {
  return base_util.publish(node,space,signal,data,meta);
}

list_triggers(node) {
  return base_util.list_triggers(node);
}

register_trigger(node, signal, trigger_fn, meta) {
  return base_util.register_trigger(node,signal,trigger_fn,meta);
}

get_trigger(node, signal) {
  return base_util.get_trigger(node,signal);
}

unregister_handler(node, action) {
  return base_util.unregister_handler(node,action);
}

unregister_trigger(node, signal) {
  return base_util.unregister_trigger(node,signal);
}

get_handler(node, action) {
  return base_util.get_handler(node,action);
}

list_handlers(node) {
  return base_util.list_handlers(node);
}

register_handler(node, action, handler, meta) {
  return base_util.register_handler(node,action,handler,meta);
}

request(node, space, action, args, meta) {
  return base_util.request(node,space,action,args,meta);
}

broadcast_transport(node, frame, exclude_id) {
  return base_util.transport_broadcast_loop(node,transport_list(node),frame,exclude_id,0);
}

route_stream(node, stream, exclude_id) {
  return base_util.stream_route_loop(
    node,
    router.target_ids(node,stream["space"],stream["signal"]),
    stream,
    exclude_id,
    0
  );
}

receive_request(node, request, ctx) {
  ctx = (ctx ?? <dynamic, dynamic>{});
  base_util.request_context_merge(request,ctx);
  try{
    return (() async { try { return await ((Future.sync(() => ((Future.sync(() => node_request.invoke_handler(node,request))) as Future<dynamic>).then((value) async { return await Function.apply((data) {
      return base_util.response_ok(node,request,data,null,ctx);
    },<dynamic>[value]); }))) as Future<dynamic>); } catch (err) { return await Future.sync(() => Function.apply((err) {
      return base_util.response_error(node,request,err,null,ctx);
    },<dynamic>[err])); } })();
  }
  catch(err){
    return base_util.response_error(node,request,err,null,ctx);
  }
}

receive_response(node, response) {
  node_request.settle_pending(node,response);
  return Future.sync(() {
    return response;
  });
}

subscribe(node, space, signal, subscription_id, meta) {
  meta = (meta ?? <dynamic, dynamic>{});
  var event = router.subscribe_frame(space,signal,subscription_id,meta);
  var target = base_util.transport_request_target(node,meta);
  if(null == target){
    return Future.sync(() {
      return event;
    });
  }
  else{
    return ((Future.sync(() => transport_send(node,target,event))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
      return event;
    },<dynamic>[value]); });
  }
}

unsubscribe(node, space, signal, subscription_id, meta) {
  meta = (meta ?? <dynamic, dynamic>{});
  var event = router.unsubscribe_frame(space,signal,subscription_id,meta);
  var target = base_util.transport_request_target(node,meta);
  if(null == target){
    return Future.sync(() {
      return event;
    });
  }
  else{
    return ((Future.sync(() => transport_send(node,target,event))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
      return event;
    },<dynamic>[value]); });
  }
}

receive_publish(node, stream, ctx) {
  ctx = (ctx ?? <dynamic, dynamic>{});
  return ((Future.sync(() => node_pubsub.receive_publish(node,stream))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
    return route_stream(node,stream,ctx["transport_id"]);
  },<dynamic>[value]); });
}

receive_frame(node, event, ctx) {
  var kind = event["kind"];
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
    return Future.sync(() {
      return event;
    });
  }
}

attach_transport(node, transport_id, transport) {
  transport = (((null != transportp(transport)) && (false != transportp(transport))) ? transport : transport_create(transport_id,transport));
  node["transports"][transport_id] = transport;
  router.register_connection(
    node,
    transport_id,
    <dynamic, dynamic>{"meta":transport["meta"]}
  );
  var start_fn = transport["start_fn"];
  if(null == start_fn){
    return Future.sync(() {
      return transport;
    });
  }
  return ((Future.sync(() => node_request.ensure_promise(Function.apply((start_fn as Function),<dynamic>[
    (event, ctx) {
      ctx = (ctx ?? <dynamic, dynamic>{});
      if(null == ctx["transport_id"]){
        ctx["transport_id"] = transport_id;
      }
      return receive_frame(node,event,ctx);
    }
  ])))) as Future<dynamic>).then((value) async { return await Function.apply((listener) {
    transport["listener"] = listener;
    return transport;
  },<dynamic>[value]); });
}

detach_transport(node, transport_id) {
  var transports = node["transports"];
  var transport = transports[transport_id];
  if(null == transport){
    return Future.sync(() {
      return null;
    });
  }
  transports.remove(transport_id);
  router.unregister_connection(node,transport_id);
  var stop_fn = transport["stop_fn"];
  if(null == stop_fn){
    return Future.sync(() {
      return transport;
    });
  }
  return ((Future.sync(() => node_request.ensure_promise(
    Function.apply((stop_fn as Function),<dynamic>[transport["listener"]])
  ))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
    return transport;
  },<dynamic>[value]); });
}

node_configure(node, opts) {
  opts = (opts ?? <dynamic, dynamic>{});
  util_handlers.install_util_handlers(node);
  for(var entry_51505 in (opts["spaces"] ?? <dynamic, dynamic>{}).entries){
    var space_id = entry_51505.key;
    var config = entry_51505.value;
    node_space.create_space(
      node,
      space_id,
      base_util.config_normalize_space(space_id,config)
    );
  };
  for(var entry_51506 in (opts["handlers"] ?? <dynamic, dynamic>{}).entries){
    var action = entry_51506.key;
    var config = entry_51506.value;
    var entry = base_util.config_normalize_handler(action,config);
    register_handler(node,action,entry["fn"],entry["meta"]);
  };
  for(var entry_51507 in (opts["triggers"] ?? <dynamic, dynamic>{}).entries){
    var signal = entry_51507.key;
    var config = entry_51507.value;
    var entry = base_util.config_normalize_trigger(signal,config);
    register_trigger(node,signal,entry["fn"],entry["meta"]);
  };
  return node;
}

node_create(opts) {
  opts = (opts ?? <dynamic, dynamic>{});
  var node = event_common.blank_container("substrate",xtd.obj_assign(<dynamic, dynamic>{
    "meta":opts["meta"] ?? <dynamic, dynamic>{},
    "router":<dynamic, dynamic>{
        "connections":<dynamic, dynamic>{},
        "subscriptions":<dynamic, dynamic>{}
      },
    "pending":<dynamic, dynamic>{},
    "handlers":<dynamic, dynamic>{},
    "triggers":<dynamic, dynamic>{},
    "spaces":<dynamic, dynamic>{},
    "id":opts["id"] ?? frame.rand_id("node-",6),
    "transports":<dynamic, dynamic>{},
    "services":<dynamic, dynamic>{}
  },base_util.node_base_opts(opts)));
  node_configure(node,opts);
  return node;
}