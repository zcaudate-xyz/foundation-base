import 'package:xtalk_event/base-listener.dart' as event_common;
import 'package:xtalk_lang/common-data.dart' as xtd;
import 'package:xtalk_substrate/base-router.dart' as router;
import 'package:xtalk_substrate/base-util.dart' as base_util;
import 'package:xtalk_substrate/page-core.dart' as page_core;
import 'package:xtalk_event/base-model.dart' as event_model;
import 'dart:async';







model_serialize_input(input) {
  return <dynamic, dynamic>{"current":input["current"],"updated":input["updated"]};
}

model_serialize_output(output) {
  return <dynamic, dynamic>{
    "type":output["type"],
    "current":output["current"],
    "updated":output["updated"],
    "elapsed":output["elapsed"],
    "pending":output["pending"],
    "disabled":output["disabled"],
    "errored":output["errored"],
    "tag":output["tag"]
  };
}

model_serialize(model) {
  var input = event_model.get_input(model);
  var output = event_model.get_output(model,null);
  var remote = model["remote"];
  var sync = model["sync"];
  var out = <dynamic, dynamic>{
    "input":model_serialize_input(input),
    "output":model_serialize_output(output)
  };
  if(null != remote){
    out["remote"] = model_serialize_output(remote);
  }
  if(null != sync){
    out["sync"] = model_serialize_output(sync);
  }
  return out;
}

group_snapshot(node, space_id, group_id) {
  var group = page_core.group_get(node,space_id,group_id);
  if(null == group){
    return <dynamic, dynamic>{};
  }
  var models = group["models"];
  var out = <dynamic, dynamic>{};
  for(var entry_51235 in models.entries){
    var model_id = entry_51235.key;
    var model = entry_51235.value;
    out[model_id] = model_serialize(model);
  };
  return out;
}

model_get_output(node, space_id, group_id, model_id) {
  return page_core.model_get_output(node,space_id,group_id,model_id);
}

publish_model_output(node, space_id, path, output) {
  var target_ids = router.target_ids(node,space_id,"page.model/output");
  if(0 == target_ids.length){
    return null;
  }
  return base_util.publish(
    node,
    space_id,
    "page.model/output",
    <dynamic, dynamic>{"path":path,"output":model_serialize_output(output)},
    <dynamic, dynamic>{}
  );
}

publish_model_input(node, space_id, path, input) {
  var target_ids = router.target_ids(node,space_id,"page.model/input");
  if(0 == target_ids.length){
    return null;
  }
  return base_util.publish(
    node,
    space_id,
    "page.model/input",
    <dynamic, dynamic>{"path":path,"input":model_serialize_input(input)},
    <dynamic, dynamic>{}
  );
}

ensure_model_listeners(node, space_id, group_id, model_id, model) {
  var listeners_map = model["listeners"];
  if(null == listeners_map["@/page-proxy/output"]){
    event_model.add_listener(model,"@/page-proxy/output",(_id, data, _t, meta) {
      return publish_model_output(node,space_id,<dynamic>[group_id,model_id],data["data"]);
    },null,(event) {
      return "model.output" == event["type"];
    });
  }
  if(null == listeners_map["@/page-proxy/input"]){
    event_model.add_listener(model,"@/page-proxy/input",(_id, data, _t, meta) {
      return publish_model_input(node,space_id,<dynamic>[group_id,model_id],data["data"]);
    },null,(event) {
      return "model.input" == event["type"];
    });
  }
  return model;
}

group_handle_list(space, args, request, node) {
  var space_id = args[0];
  var runtime = page_core.space_ensure_page(node,space_id);
  var groups = runtime["groups"];
  var out = <dynamic, dynamic>{};
  for(var entry_51236 in groups.entries){
    var group_id = entry_51236.key;
    var group = entry_51236.value;
    var models = group["models"];
    var model_ids = <dynamic>[];
    for(var model_id in models.keys){
      model_ids.add(model_id);
    };
    out[group_id] = <dynamic, dynamic>{"models":model_ids};
  };
  return out;
}

group_handle_open(space, args, request, node) {
  var payload = args[0];
  var space_id = payload["space"];
  var group_id = payload["group"];
  var transport_id = xtd.get_in(request,<dynamic>["meta","transport_id"]);
  var group = page_core.group_get(node,space_id,group_id);
  if(null == group){
    return <dynamic, dynamic>{"error":"group not found","space":space_id,"group":group_id};
  }
  var models = group["models"];
  for(var entry_51237 in models.entries){
    var model_id = entry_51237.key;
    var model = entry_51237.value;
    ensure_model_listeners(node,space_id,group_id,model_id,model);
  };
  if(null != transport_id){
    router.add_subscription(
      node,
      transport_id,
      space_id,
      "page.model/output",
      null,
      <dynamic, dynamic>{}
    );
    router.add_subscription(
      node,
      transport_id,
      space_id,
      "page.model/input",
      null,
      <dynamic, dynamic>{}
    );
  }
  var init = group["init"];
  if(null != init){
    return ((Future.sync(() => init)) as Future<dynamic>).then((value) async { return await Function.apply((_) {
      return <dynamic, dynamic>{
        "space":space_id,
        "group":group_id,
        "models":group_snapshot(node,space_id,group_id)
      };
    },<dynamic>[value]); });
  }
  else{
    return <dynamic, dynamic>{
      "space":space_id,
      "group":group_id,
      "models":group_snapshot(node,space_id,group_id)
    };
  }
}

group_handle_close(space, args, request, node) {
  var payload = args[0];
  var space_id = payload["space"];
  var group_id = payload["group"];
  var transport_id = xtd.get_in(request,<dynamic>["meta","transport_id"]);
  if(null != transport_id){
    router.remove_subscription(node,transport_id,space_id,"page.model/output");
    router.remove_subscription(node,transport_id,space_id,"page.model/input");
  }
  return <dynamic, dynamic>{"status":"closed","space":space_id,"group":group_id};
}

group_handle_update(space, args, request, node) {
  var payload = args[0];
  return ((Future.sync(() => page_core.group_update(
    node,
    payload["space"],
    payload["group"],
    payload["event"] ?? <dynamic, dynamic>{}
  ))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
    return <dynamic, dynamic>{"status":"ok"};
  },<dynamic>[value]); });
}

model_handle_update(space, args, request, node) {
  var payload = args[0];
  return page_core.model_update(
    node,
    payload["space"],
    payload["group"],
    payload["model"],
    payload["event"] ?? <dynamic, dynamic>{}
  );
}

model_handle_set_input(space, args, request, node) {
  var payload = args[0];
  return ((Future.sync(() => page_core.model_set_input(
    node,
    payload["space"],
    payload["group"],
    payload["model"],
    payload["current"],
    payload["event"] ?? <dynamic, dynamic>{}
  ))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
    return <dynamic, dynamic>{"status":"ok"};
  },<dynamic>[value]); });
}

model_handle_trigger(space, args, request, node) {
  var payload = args[0];
  var out = page_core.model_trigger(
    node,
    payload["space"],
    payload["group"],
    payload["model"],
    payload["signal"],
    payload["event"] ?? <dynamic, dynamic>{}
  );
  return <dynamic, dynamic>{"status":"ok","triggered":null != out};
}

group_handle_trigger(space, args, request, node) {
  var payload = args[0];
  var out = page_core.group_trigger(
    node,
    payload["space"],
    payload["group"],
    payload["signal"],
    payload["event"] ?? <dynamic, dynamic>{}
  );
  return <dynamic, dynamic>{"status":"ok","models":out};
}

model_handle_proxy_call(space, args, request, node) {
  var payload = args[0];
  var space_id = payload["space"];
  var group_id = payload["group"];
  var model_id = payload["model"];
  return (() async { try { return await ((Future.sync(() => ((Future.sync(() => page_core.model_remote_call(
    node,
    space_id,
    group_id,
    model_id,
    payload["args"] ?? <dynamic>[],
    payload["save_output"]
  ))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
    var value_51238 = page_core.model_ensure(node,space_id,group_id,model_id);
    var _group = value_51238[0];
    var model = value_51238[1];
    return <dynamic, dynamic>{
      "status":"ok",
      "output":model_serialize_output(model["output"])
    };
  },<dynamic>[value]); }))) as Future<dynamic>); } catch (err) { return await Future.sync(() => Function.apply((err) {
    return <dynamic, dynamic>{
      "status":"error",
      "message":((err is Map) && ("xt.exception" == ((err as Map)["__type__"]))) ? ((err as Map)["message"]) : null,
      "stack":err["stack"],
      "data":((err is Map) && ("xt.exception" == ((err as Map)["__type__"]))) ? ((err as Map)["data"]) : null
    };
  },<dynamic>[err])); } })();
}

install_handlers(node) {
  base_util.register_handler(node,"@page/group-list",group_handle_list,null);
  base_util.register_handler(node,"@page/group-open",group_handle_open,null);
  base_util.register_handler(node,"@page/group-close",group_handle_close,null);
  base_util.register_handler(node,"@page/group-update",group_handle_update,null);
  base_util.register_handler(node,"@page/model-update",model_handle_update,null);
  base_util.register_handler(node,"@page/model-set-input",model_handle_set_input,null);
  base_util.register_handler(node,"@page/model-trigger",model_handle_trigger,null);
  base_util.register_handler(node,"@page/model-proxy-call",model_handle_proxy_call,null);
  base_util.register_handler(node,"@page/group-trigger",group_handle_trigger,null);
  return node;
}

model_create_proxy(node, space_id, group_id, model_id, snapshot) {
  var identity_fn = (x) {
    return x;
  };
  var nil_fn = () {
    return null;
  };
  var input_snapshot = snapshot["input"];
  var output_snapshot = snapshot["output"];
  var model = event_common.blank_container("event.model",<dynamic, dynamic>{
    "pipeline":<dynamic, dynamic>{},
    "options":<dynamic, dynamic>{},
    "input":<dynamic, dynamic>{
        "current":input_snapshot["current"],
        "updated":input_snapshot["updated"],
        "default":nil_fn
      },
    "output":<dynamic, dynamic>{
        "elapsed":output_snapshot["elapsed"],
        "errored":output_snapshot["errored"],
        "process":identity_fn,
        "tag":output_snapshot["tag"],
        "current":output_snapshot["current"],
        "type":output_snapshot["type"],
        "updated":output_snapshot["updated"],
        "disabled":output_snapshot["disabled"],
        "pending":output_snapshot["pending"],
        "default":nil_fn
      }
  });
  var remote_snapshot = snapshot["remote"];
  if(null != remote_snapshot){
    model["remote"] = <dynamic, dynamic>{
      "elapsed":remote_snapshot["elapsed"],
      "errored":remote_snapshot["errored"],
      "process":identity_fn,
      "tag":remote_snapshot["tag"],
      "current":remote_snapshot["current"],
      "type":remote_snapshot["type"],
      "updated":remote_snapshot["updated"],
      "disabled":remote_snapshot["disabled"],
      "pending":remote_snapshot["pending"],
      "default":nil_fn
    };
  }
  var sync_snapshot = snapshot["sync"];
  if(null != sync_snapshot){
    model["sync"] = <dynamic, dynamic>{
      "elapsed":sync_snapshot["elapsed"],
      "errored":sync_snapshot["errored"],
      "process":identity_fn,
      "tag":sync_snapshot["tag"],
      "current":sync_snapshot["current"],
      "type":sync_snapshot["type"],
      "updated":sync_snapshot["updated"],
      "disabled":sync_snapshot["disabled"],
      "pending":sync_snapshot["pending"],
      "default":nil_fn
    };
  }
  event_model.add_listener(model,"@/page",(_id, data, _t, meta) {
    var emitted = xtd.obj_assign(<dynamic, dynamic>{},data);
    emitted["meta"] = meta;
    return page_core.trigger_listeners(node,space_id,<dynamic>[group_id,model_id],emitted);
  },null,null);
  return model;
}

proxy_dispatch_op(node, transport_id, op, space_id, group_id, args) {
  if(op == "group-update"){
    var event = xtd.nth(args,0);
    return base_util.request(node,space_id,"@page/group-update",<dynamic>[
      <dynamic, dynamic>{"space":space_id,"group":group_id,"event":event}
    ],<dynamic, dynamic>{"transport_id":transport_id});
  }
  else if(op == "model-update"){
    var model_id = xtd.nth(args,0);
    var event = xtd.nth(args,1);
    return base_util.request(node,space_id,"@page/model-update",<dynamic>[
      <dynamic, dynamic>{
          "space":space_id,
          "group":group_id,
          "model":model_id,
          "event":event
        }
    ],<dynamic, dynamic>{"transport_id":transport_id});
  }
  else if(op == "model-set-input"){
    var model_id = xtd.nth(args,0);
    var current = xtd.nth(args,1);
    var event = xtd.nth(args,2);
    return base_util.request(node,space_id,"@page/model-set-input",<dynamic>[
      <dynamic, dynamic>{
          "space":space_id,
          "group":group_id,
          "model":model_id,
          "current":current,
          "event":event
        }
    ],<dynamic, dynamic>{"transport_id":transport_id});
  }
  else if(op == "trigger-model"){
    var model_id = xtd.nth(args,0);
    var signal = xtd.nth(args,1);
    var event = xtd.nth(args,2);
    return base_util.request(node,space_id,"@page/model-trigger",<dynamic>[
      <dynamic, dynamic>{
          "space":space_id,
          "group":group_id,
          "model":model_id,
          "signal":signal,
          "event":event
        }
    ],<dynamic, dynamic>{"transport_id":transport_id});
  }
  else if(op == "trigger-group"){
    var signal = xtd.nth(args,0);
    var event = xtd.nth(args,1);
    return base_util.request(node,space_id,"@page/group-trigger",<dynamic>[
      <dynamic, dynamic>{
          "space":space_id,
          "group":group_id,
          "signal":signal,
          "event":event
        }
    ],<dynamic, dynamic>{"transport_id":transport_id});
  }
  else if(op == "proxy-call"){
    var model_id = xtd.nth(args,0);
    var call_args = xtd.nth(args,1);
    var save_output = xtd.nth(args,2);
    return base_util.request(node,space_id,"@page/model-proxy-call",<dynamic>[
      <dynamic, dynamic>{
          "space":space_id,
          "group":group_id,
          "model":model_id,
          "args":call_args,
          "save_output":save_output
        }
    ],<dynamic, dynamic>{"transport_id":transport_id});
  }
  else{
    return null;
  }
}

proxy_dispatcher(op, node, space_id, group_id, args) {
  var group = page_core.group_get(node,space_id,group_id);
  var dispatch_fn = group["proxy_dispatch"];
  return Function.apply(
    (dispatch_fn as Function),
    <dynamic>[op,node,space_id,group_id,args]
  );
}

group_create_proxy(node, space_id, group_id, snapshot, remote_spec) {
  var runtime = page_core.space_ensure_page(node,space_id);
  var groups = runtime["groups"];
  var group_models = <dynamic, dynamic>{};
  for(var entry_51239 in snapshot.entries){
    var model_id = entry_51239.key;
    var model_snapshot = entry_51239.value;
    group_models[model_id] = model_create_proxy(node,space_id,group_id,model_id,model_snapshot);
  };
  var transport_id = remote_spec["transport_id"];
  var dispatch_fn = (op, node, space_id, group_id, args) {
    return proxy_dispatch_op(node,transport_id,op,space_id,group_id,args);
  };
  var group = <dynamic, dynamic>{
    "name":group_id,
    "models":group_models,
    "remote":remote_spec,
    "proxy_dispatch":dispatch_fn,
    "deps":<dynamic, dynamic>{},
    "throttle":null
  };
  groups[group_id] = group;
  return group;
}

model_apply_output(space, stream, node) {
  var data = stream["data"];
  var space_id = stream["space"];
  var path = data["path"];
  var group_id = path[0];
  var model_id = path[1];
  var output = data["output"];
  var group = page_core.group_get(node,space_id,group_id);
  if((null == group) || !(() {
    var dart_truthy__51234 = page_core.proxy_groupp(group);
    return (null != dart_truthy__51234) && (false != dart_truthy__51234);
  })()){
    return null;
  }
  var model = xtd.get_in(group,<dynamic>["models",model_id]);
  if(null == model){
    return null;
  }
  xtd.obj_assign(model["output"],output);
  return event_model.trigger_listeners(model,"model.output",model["output"]);
}

model_apply_input(space, stream, node) {
  var data = stream["data"];
  var space_id = stream["space"];
  var path = data["path"];
  var group_id = path[0];
  var model_id = path[1];
  var input = data["input"];
  var group = page_core.group_get(node,space_id,group_id);
  if((null == group) || !(() {
    var dart_truthy__51233 = page_core.proxy_groupp(group);
    return (null != dart_truthy__51233) && (false != dart_truthy__51233);
  })()){
    return null;
  }
  var model = xtd.get_in(group,<dynamic>["models",model_id]);
  if(null == model){
    return null;
  }
  xtd.obj_assign(model["input"],input);
  return event_model.trigger_listeners(model,"model.input",model["input"]);
}

install_triggers(node) {
  base_util.register_trigger(node,"page.model/output",model_apply_output,null);
  base_util.register_trigger(node,"page.model/input",model_apply_input,null);
  return node;
}

install(node) {
  install_handlers(node);
  install_triggers(node);
  return node;
}

group_list_proxy(node, space_id, opts) {
  var transport_id = opts["transport_id"];
  return base_util.request(
    node,
    space_id,
    "@page/group-list",
    <dynamic>[space_id],
    <dynamic, dynamic>{"transport_id":transport_id}
  );
}

group_open_proxy(node, space_id, group_id, opts) {
  var transport_id = opts["transport_id"];
  var existing_group = page_core.group_get(node,space_id,group_id);
  var remote_spec = (((null != existing_group) && (false != existing_group)) ? existing_group["remote"] : existing_group) ?? opts;
  return ((Future.sync(() => base_util.request(
    node,
    space_id,
    "@page/group-open",
    <dynamic>[<dynamic, dynamic>{"space":space_id,"group":group_id}],
    <dynamic, dynamic>{"transport_id":transport_id}
  ))) as Future<dynamic>).then((value) async { return await Function.apply((response) {
    var error = response["error"];
    if(null != error){
      throw "ERR - " + error;
    }
    var snapshot = response["models"];
    group_create_proxy(node,space_id,group_id,snapshot,remote_spec);
    return page_core.group_get(node,space_id,group_id);
  },<dynamic>[value]); });
}

group_close_proxy(node, space_id, group_id, opts) {
  var transport_id = opts["transport_id"];
  return ((Future.sync(() => base_util.request(
    node,
    space_id,
    "@page/group-close",
    <dynamic>[<dynamic, dynamic>{"space":space_id,"group":group_id}],
    <dynamic, dynamic>{"transport_id":transport_id}
  ))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
    var runtime = page_core.space_ensure_page(node,space_id);
    var groups = runtime["groups"];
    groups.remove(group_id);
    return null;
  },<dynamic>[value]); });
}

model_proxy_call(node, space_id, group_id, model_id, args, save_output, opts) {
  var group = page_core.group_get(node,space_id,group_id);
  var dispatch_fn = group["proxy_dispatch"];
  return ((Future.sync(() => Function.apply((dispatch_fn as Function),<dynamic>[
    "proxy-call",
    node,
    space_id,
    group_id,
    <dynamic>[model_id,args,save_output]
  ]))) as Future<dynamic>).then((value) async { return await Function.apply((response) {
    var output = response["output"];
    if(null != output){
      model_apply_output(space_id,<dynamic, dynamic>{
        "space":space_id,
        "data":<dynamic, dynamic>{"path":<dynamic>[group_id,model_id],"output":output}
      },node);
    }
    return response;
  },<dynamic>[value]); });
}

group_sync_proxy(node, space_id, group_id, opts) {
  install(node);
  var transport_id = opts["transport_id"];
  return ((Future.sync(() => group_open_proxy(node,space_id,group_id,opts))) as Future<dynamic>).then((value) async { return await Function.apply((group) {
    return <dynamic, dynamic>{
      "space":space_id,
      "group":group_id,
      "group-obj":group,
      "transport_id":transport_id,
      "close":() {
            return group_close_proxy(node,space_id,group_id,opts);
          }
    };
  },<dynamic>[value]); });
}