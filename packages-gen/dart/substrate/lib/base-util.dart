import 'package:xtalk_substrate/base-pubsub.dart' as node_pubsub;

import 'package:xtalk_lang/common-data.dart' as xtd;

import 'package:xtalk_substrate/base-request.dart' as node_request;

import 'package:xtalk_substrate/base-router.dart' as router;

import 'package:xtalk_substrate/base-frame.dart' as frame;

transport_get(node, transport_id) {
  return (node["transports"])[transport_id];
}

transport_list(node) {
  return xtd.arr_sort(List<dynamic>.from(( node["transports"] ).keys),(x) {
    return x;
  },(x, y) {
    return (x).toString().compareTo((y).toString()) < 0;
  });
}

transport_send(node, transport_id, frame) {
  var transport = transport_get(node,transport_id);
  if(null == transport){
    throw "transport not found - " + transport_id;
  }
  var send_fn = transport["send_fn"];
  if(null == send_fn){
    throw "transport missing send_fn - " + transport_id;
  }
  return node_request.ensure_promise(Function.apply((send_fn as Function),<dynamic>[frame]));
}

transport_broadcast_loop(node, ids, frame, exclude_id, index) {
  if(index >= ids.length){
    return Future.sync(() {
      return frame;
    });
  }
  var transport_id = ids[index];
  if(transport_id == exclude_id){
    return transport_broadcast_loop(node,ids,frame,exclude_id,index + 1);
  }
  else{
    return ((Future.sync(() => transport_send(node,transport_id,frame))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
      return transport_broadcast_loop(node,ids,frame,exclude_id,index + 1);
    },<dynamic>[value]); });
  }
}

transport_request_target(node, meta) {
  if((() {
    var dart_truthy__41906 = meta["local"];
    return (null != dart_truthy__41906) && (false != dart_truthy__41906);
  })()){
    return null;
  }
  var target = meta["transport_id"];
  if(null != target){
    return target;
  }
  var transports = transport_list(node);
  if(0 == transports.length){
    return null;
  }
  return transports[0];
}

stream_route_loop(node, ids, frame, exclude_id, index) {
  if(index >= ids.length){
    return Future.sync(() {
      return frame;
    });
  }
  var transport_id = ids[index];
  if(transport_id == exclude_id){
    return stream_route_loop(node,ids,frame,exclude_id,index + 1);
  }
  else{
    return ((Future.sync(() => transport_send(node,transport_id,frame))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
      return stream_route_loop(node,ids,frame,exclude_id,index + 1);
    },<dynamic>[value]); });
  }
}

pending_await(state) {
  var status = state["status"];
  if(status == "resolved"){
    return Future.sync(() {
      return state["value"];
    });
  }
  else if(status == "rejected"){
    return Future.sync(() {
      var completer = Completer<dynamic>();
      Function.apply((_, reject) {
        return Function.apply((reject as Function),<dynamic>[state["error"]]);
      },<dynamic>[completer.complete,completer.completeError]);
      return completer.future;
    });
  }
  else{
    return ((Future.sync(() => Future.delayed(Duration(milliseconds:  1 )).then((_) {
      return Future.sync(() {
        return Function.apply(() {
          return null;
        },<dynamic>[]);
      });
    }))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
      return pending_await(state);
    },<dynamic>[value]); });
  }
}

request_context_merge(request, ctx) {
  ctx = (ctx ?? <dynamic, dynamic>{});
  var meta = request["meta"];
  if(null == meta){
    meta = <dynamic, dynamic>{};
    request["meta"] = meta;
  }
  if(null != ctx["transport_id"]){
    meta["transport_id"] = ctx["transport_id"];
  }
  return request;
}

response_ok(node, request, data, meta, ctx) {
  var response = frame.response_ok_frame(request["id"],request["space"],data,meta);
  var transport_id = ctx["transport_id"];
  if(null == transport_id){
    var request_meta = request["meta"];
    if(null != request_meta){
      transport_id = request_meta["transport_id"];
    }
  }
  if(null == transport_id){
    return Future.sync(() {
      return response;
    });
  }
  else{
    return ((Future.sync(() => transport_send(node,transport_id,response))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
      return response;
    },<dynamic>[value]); });
  }
}

response_error(node, request, error, meta, ctx) {
  var response = frame.response_error_frame(request["id"],request["space"],error,meta);
  var transport_id = ctx["transport_id"];
  if(null == transport_id){
    var request_meta = request["meta"];
    if(null != request_meta){
      transport_id = request_meta["transport_id"];
    }
  }
  if(null == transport_id){
    return Future.sync(() {
      return response;
    });
  }
  else{
    return ((Future.sync(() => transport_send(node,transport_id,response))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
      return response;
    },<dynamic>[value]); });
  }
}

config_normalize_space(space_id, config) {
  if(null == config){
    return null;
  }
  else if((("Map" == (config.runtimeType).toString()) || (config.runtimeType).toString().startsWith("_Map") || (config.runtimeType).toString().startsWith("LinkedMap")) && (config.containsKey("id") || config.containsKey("state") || config.containsKey("meta"))){
    if(config.containsKey("id") && !(config["id"] == space_id)){
      throw "space id mismatch - " + space_id;
    }
    return <dynamic, dynamic>{
      "state":config["state"],
      "meta":config["meta"] ?? <dynamic, dynamic>{}
    };
  }
  else{
    throw "invalid space config - " + space_id;
  }
}

config_normalize_handler(action, config) {
  if((config.runtimeType).toString().contains("Function") || (config.runtimeType).toString().contains("=>") || (config).toString().startsWith("Closure")){
    return <dynamic, dynamic>{"fn":config,"meta":<dynamic, dynamic>{}};
  }
  else if((("Map" == (config.runtimeType).toString()) || (config.runtimeType).toString().startsWith("_Map") || (config.runtimeType).toString().startsWith("LinkedMap")) && ((config["fn"].runtimeType).toString().contains("Function") || (config["fn"].runtimeType).toString().contains("=>") || (config["fn"]).toString().startsWith("Closure"))){
    if(config.containsKey("id") && !(config["id"] == action)){
      throw "handler id mismatch - " + action;
    }
    return <dynamic, dynamic>{
      "fn":config["fn"],
      "meta":config["meta"] ?? <dynamic, dynamic>{}
    };
  }
  else{
    throw "invalid handler config - " + action;
  }
}

config_normalize_trigger(signal, config) {
  if((config.runtimeType).toString().contains("Function") || (config.runtimeType).toString().contains("=>") || (config).toString().startsWith("Closure")){
    return <dynamic, dynamic>{"fn":config,"meta":<dynamic, dynamic>{}};
  }
  else if((("Map" == (config.runtimeType).toString()) || (config.runtimeType).toString().startsWith("_Map") || (config.runtimeType).toString().startsWith("LinkedMap")) && ((config["fn"].runtimeType).toString().contains("Function") || (config["fn"].runtimeType).toString().contains("=>") || (config["fn"]).toString().startsWith("Closure"))){
    if(config.containsKey("id") && !(config["id"] == signal)){
      throw "trigger id mismatch - " + signal;
    }
    return <dynamic, dynamic>{
      "fn":config["fn"],
      "meta":config["meta"] ?? <dynamic, dynamic>{}
    };
  }
  else{
    throw "invalid trigger config - " + signal;
  }
}

node_base_opts(opts) {
  var base = xtd.obj_clone(opts ?? <dynamic, dynamic>{});
  base.remove("spaces");
  base.remove("handlers");
  base.remove("triggers");
  return base;
}

register_handler(node, action, handler, meta) {
  var entry = <dynamic, dynamic>{"id":action,"fn":handler,"meta":meta ?? <dynamic, dynamic>{}};
  node["handlers"][action] = entry;
  return entry;
}

unregister_handler(node, action) {
  var handlers = node["handlers"];
  var prev = handlers[action];
  handlers.remove(action);
  return prev;
}

get_handler(node, action) {
  return (node["handlers"])[action];
}

list_handlers(node) {
  return xtd.arr_sort(List<dynamic>.from(( node["handlers"] ).keys),(x) {
    return x;
  },(x, y) {
    return (x).toString().compareTo((y).toString()) < 0;
  });
}

register_trigger(node, signal, trigger_fn, meta) {
  var entry = <dynamic, dynamic>{
    "id":signal,
    "fn":trigger_fn,
    "meta":meta ?? <dynamic, dynamic>{}
  };
  node["triggers"][signal] = entry;
  return entry;
}

unregister_trigger(node, signal) {
  var triggers = node["triggers"];
  var prev = triggers[signal];
  triggers.remove(signal);
  return prev;
}

get_trigger(node, signal) {
  return (node["triggers"])[signal];
}

list_triggers(node) {
  return xtd.arr_sort(List<dynamic>.from(( node["triggers"] ).keys),(x) {
    return x;
  },(x, y) {
    return (x).toString().compareTo((y).toString()) < 0;
  });
}

request(node, space, action, args, meta) {
  meta = (meta ?? <dynamic, dynamic>{});
  var request_frame = frame.request_frame(space,action,args,meta);
  var target = transport_request_target(node,meta);
  if(null == target){
    return node_request.invoke_handler(node,request_frame);
  }
  else{
    return Future.sync(() {
      var completer = Completer<dynamic>();
      Function.apply((resolve, reject) {
        node_request.add_pending(
          node,
          request_frame,
          resolve,
          reject,
          <dynamic, dynamic>{"transport_id":target}
        );
        try{
          return (() async { try { return await ((Future.sync(() => transport_send(node,target,request_frame))) as Future<dynamic>); } catch (err) { return await Future.sync(() => Function.apply((err) {
            node_request.remove_pending(node,request_frame["id"]);
            return Function.apply((reject as Function),<dynamic>[err]);
          },<dynamic>[err])); } })();
        }
        catch(err){
          node_request.remove_pending(node,request_frame["id"]);
          return Function.apply((reject as Function),<dynamic>[err]);
        }
      },<dynamic>[completer.complete,completer.completeError]);
      return completer.future;
    });
  }
}

publish(node, space, signal, data, meta) {
  meta = (meta ?? <dynamic, dynamic>{});
  var stream = frame.stream_frame(space,signal,data,meta,meta["cause"]);
  return ((Future.sync(() => node_pubsub.receive_publish(node,stream))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
    return stream_route_loop(
      node,
      router.target_ids(node,stream["space"],stream["signal"]),
      stream,
      meta["transport_id"],
      0
    );
  },<dynamic>[value]); });
}