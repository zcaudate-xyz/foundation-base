const event_common = require("@xtalk/event/base-listener.js")

const xtd = require("@xtalk/lang/common-data.js")

const router = require("@xtalk/substrate/base-router.js")

const base_util = require("@xtalk/substrate/base-util.js")

const page_core = require("@xtalk/substrate/page-core.js")

const event_model = require("@xtalk/event/base-model.js")

function model_serialize_input(input){
  return {"current":input["current"],"updated":input["updated"]};
}

function model_serialize_output(output){
  return {
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

function model_serialize(model){
  let input = event_model.get_input(model);
  let output = event_model.get_output(model,null);
  let remote = model["remote"];
  let sync = model["sync"];
  let out = {
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

function group_snapshot(node,space_id,group_id){
  let group = page_core.group_get(node,space_id,group_id);
  if(null == group){
    return {};
  }
  let models = group["models"];
  let out = {};
  for(let [model_id,model] of Object.entries(models)){
    out[model_id] = model_serialize(model);
  };
  return out;
}

function model_get_output(node,space_id,group_id,model_id){
  return page_core.model_get_output(node,space_id,group_id,model_id);
}

function publish_model_output(node,space_id,path,output){
  let target_ids = router.target_ids(node,space_id,"page.model/output");
  if(0 == target_ids.length){
    return null;
  }
  return base_util.publish(
    node,
    space_id,
    "page.model/output",
    {"path":path,"output":model_serialize_output(output)},
    {}
  );
}

function publish_model_input(node,space_id,path,input){
  let target_ids = router.target_ids(node,space_id,"page.model/input");
  if(0 == target_ids.length){
    return null;
  }
  return base_util.publish(
    node,
    space_id,
    "page.model/input",
    {"path":path,"input":model_serialize_input(input)},
    {}
  );
}

function ensure_model_listeners(node,space_id,group_id,model_id,model){
  let listeners_map = model["listeners"];
  if(null == listeners_map["@/page-proxy/output"]){
    event_model.add_listener(model,"@/page-proxy/output",function (_id,data,_t,meta){
      return publish_model_output(node,space_id,[group_id,model_id],data["data"]);
    },null,function (event){
      return "model.output" == event["type"];
    });
  }
  if(null == listeners_map["@/page-proxy/input"]){
    event_model.add_listener(model,"@/page-proxy/input",function (_id,data,_t,meta){
      return publish_model_input(node,space_id,[group_id,model_id],data["data"]);
    },null,function (event){
      return "model.input" == event["type"];
    });
  }
  return model;
}

function group_handle_list(space,args,request,node){
  let space_id = args[0];
  let runtime = page_core.space_ensure_page(node,space_id);
  let groups = runtime["groups"];
  let out = {};
  for(let [group_id,group] of Object.entries(groups)){
    let models = group["models"];
    let model_ids = [];
    for(let model_id of Object.keys(models)){
      model_ids.push(model_id);
    };
    out[group_id] = {"models":model_ids};
  };
  return out;
}

function group_handle_open(space,args,request,node){
  let payload = args[0];
  let space_id = payload["space"];
  let group_id = payload["group"];
  let transport_id = xtd.get_in(request,["meta","transport_id"]);
  let group = page_core.group_get(node,space_id,group_id);
  if(null == group){
    return {"error":"group not found","space":space_id,"group":group_id};
  }
  let models = group["models"];
  for(let [model_id,model] of Object.entries(models)){
    ensure_model_listeners(node,space_id,group_id,model_id,model);
  };
  if(null != transport_id){
    router.add_subscription(node,transport_id,space_id,"page.model/output",null,{});
    router.add_subscription(node,transport_id,space_id,"page.model/input",null,{});
  }
  let init = group["init"];
  if(null != init){
    return init.then(function (_){
      return {
        "space":space_id,
        "group":group_id,
        "models":group_snapshot(node,space_id,group_id)
      };
    });
  }
  else{
    return {
      "space":space_id,
      "group":group_id,
      "models":group_snapshot(node,space_id,group_id)
    };
  }
}

function group_handle_close(space,args,request,node){
  let payload = args[0];
  let space_id = payload["space"];
  let group_id = payload["group"];
  let transport_id = xtd.get_in(request,["meta","transport_id"]);
  if(null != transport_id){
    router.remove_subscription(node,transport_id,space_id,"page.model/output");
    router.remove_subscription(node,transport_id,space_id,"page.model/input");
  }
  return {"status":"closed","space":space_id,"group":group_id};
}

function group_handle_update(space,args,request,node){
  let payload = args[0];
  return page_core.group_update(node,payload["space"],payload["group"],payload["event"] || {}).then(function (_){
    return {"status":"ok"};
  });
}

function model_handle_update(space,args,request,node){
  let payload = args[0];
  return page_core.model_update(
    node,
    payload["space"],
    payload["group"],
    payload["model"],
    payload["event"] || {}
  );
}

function model_handle_set_input(space,args,request,node){
  let payload = args[0];
  return page_core.model_set_input(
    node,
    payload["space"],
    payload["group"],
    payload["model"],
    payload["current"],
    payload["event"] || {}
  ).then(function (_){
    return {"status":"ok"};
  });
}

function model_handle_trigger(space,args,request,node){
  let payload = args[0];
  let out = page_core.model_trigger(
    node,
    payload["space"],
    payload["group"],
    payload["model"],
    payload["signal"],
    payload["event"] || {}
  );
  return {"status":"ok","triggered":null != out};
}

function group_handle_trigger(space,args,request,node){
  let payload = args[0];
  let out = page_core.group_trigger(
    node,
    payload["space"],
    payload["group"],
    payload["signal"],
    payload["event"] || {}
  );
  return {"status":"ok","models":out};
}

function model_handle_proxy_call(space,args,request,node){
  let payload = args[0];
  let space_id = payload["space"];
  let group_id = payload["group"];
  let model_id = payload["model"];
  return page_core.model_remote_call(
    node,
    space_id,
    group_id,
    model_id,
    payload["args"] || [],
    payload["save_output"]
  ).then(function (_){
    let [_group,model] = page_core.model_ensure(node,space_id,group_id,model_id);
    return {
      "status":"ok",
      "output":model_serialize_output(model["output"])
    };
  }).catch(function (err){
    return {
      "status":"error",
      "message":(err instanceof Error) ? err["message"] : null,
      "stack":err["stack"],
      "data":(err instanceof Error) ? err["data"] : null
    };
  });
}

function install_handlers(node){
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

function model_create_proxy(node,space_id,group_id,model_id,snapshot){
  let identity_fn = function (x){
    return x;
  };
  let nil_fn = function (){
    return null;
  };
  let input_snapshot = snapshot["input"];
  let output_snapshot = snapshot["output"];
  let model = event_common.blank_container("event.model",{
    "pipeline":{},
    "options":{},
    "input":{
        "current":input_snapshot["current"],
        "updated":input_snapshot["updated"],
        "default":nil_fn
      },
    "output":{
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
  let remote_snapshot = snapshot["remote"];
  if(null != remote_snapshot){
    model["remote"] = {
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
  let sync_snapshot = snapshot["sync"];
  if(null != sync_snapshot){
    model["sync"] = {
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
  event_model.add_listener(model,"@/page",function (_id,data,_t,meta){
    let emitted = Object.assign({},data);
    emitted["meta"] = meta;
    return page_core.trigger_listeners(node,space_id,[group_id,model_id],emitted);
  },null,null);
  return model;
}

function proxy_dispatch_op(node,transport_id,op,space_id,group_id,args){
  if(op == "group-update"){
    let event = xtd.nth(args,0);
    return base_util.request(
      node,
      space_id,
      "@page/group-update",
      [{"space":space_id,"group":group_id,"event":event}],
      {"transport_id":transport_id}
    );
  }
  else if(op == "model-update"){
    let model_id = xtd.nth(args,0);
    let event = xtd.nth(args,1);
    return base_util.request(node,space_id,"@page/model-update",[
      {
          "space":space_id,
          "group":group_id,
          "model":model_id,
          "event":event
        }
    ],{"transport_id":transport_id});
  }
  else if(op == "model-set-input"){
    let model_id = xtd.nth(args,0);
    let current = xtd.nth(args,1);
    let event = xtd.nth(args,2);
    return base_util.request(node,space_id,"@page/model-set-input",[
      {
          "space":space_id,
          "group":group_id,
          "model":model_id,
          "current":current,
          "event":event
        }
    ],{"transport_id":transport_id});
  }
  else if(op == "trigger-model"){
    let model_id = xtd.nth(args,0);
    let signal = xtd.nth(args,1);
    let event = xtd.nth(args,2);
    return base_util.request(node,space_id,"@page/model-trigger",[
      {
          "space":space_id,
          "group":group_id,
          "model":model_id,
          "signal":signal,
          "event":event
        }
    ],{"transport_id":transport_id});
  }
  else if(op == "trigger-group"){
    let signal = xtd.nth(args,0);
    let event = xtd.nth(args,1);
    return base_util.request(node,space_id,"@page/group-trigger",[
      {
          "space":space_id,
          "group":group_id,
          "signal":signal,
          "event":event
        }
    ],{"transport_id":transport_id});
  }
  else if(op == "proxy-call"){
    let model_id = xtd.nth(args,0);
    let call_args = xtd.nth(args,1);
    let save_output = xtd.nth(args,2);
    return base_util.request(node,space_id,"@page/model-proxy-call",[
      {
          "space":space_id,
          "group":group_id,
          "model":model_id,
          "args":call_args,
          "save_output":save_output
        }
    ],{"transport_id":transport_id});
  }
  else{
    return null;
  }
}

function proxy_dispatcher(op,node,space_id,group_id,args){
  let group = page_core.group_get(node,space_id,group_id);
  let dispatch_fn = group["proxy_dispatch"];
  return dispatch_fn(op,node,space_id,group_id,args);
}

function group_create_proxy(node,space_id,group_id,snapshot,remote_spec){
  let runtime = page_core.space_ensure_page(node,space_id);
  let groups = runtime["groups"];
  let group_models = {};
  for(let [model_id,model_snapshot] of Object.entries(snapshot)){
    group_models[model_id] = model_create_proxy(node,space_id,group_id,model_id,model_snapshot);
  };
  let transport_id = remote_spec["transport_id"];
  let dispatch_fn = function (op,node,space_id,group_id,args){
    return proxy_dispatch_op(node,transport_id,op,space_id,group_id,args);
  };
  let group = {
    "name":group_id,
    "models":group_models,
    "remote":remote_spec,
    "proxy_dispatch":dispatch_fn,
    "deps":{},
    "throttle":null
  };
  groups[group_id] = group;
  return group;
}

function model_apply_output(space,stream,node){
  let data = stream["data"];
  let space_id = stream["space"];
  let path = data["path"];
  let group_id = path[0];
  let model_id = path[1];
  let output = data["output"];
  let group = page_core.group_get(node,space_id,group_id);
  if((null == group) || !page_core.proxy_groupp(group)){
    return null;
  }
  let model = xtd.get_in(group,["models",model_id]);
  if(null == model){
    return null;
  }
  Object.assign(model["output"],output);
  return event_model.trigger_listeners(model,"model.output",model["output"]);
}

function model_apply_input(space,stream,node){
  let data = stream["data"];
  let space_id = stream["space"];
  let path = data["path"];
  let group_id = path[0];
  let model_id = path[1];
  let input = data["input"];
  let group = page_core.group_get(node,space_id,group_id);
  if((null == group) || !page_core.proxy_groupp(group)){
    return null;
  }
  let model = xtd.get_in(group,["models",model_id]);
  if(null == model){
    return null;
  }
  Object.assign(model["input"],input);
  return event_model.trigger_listeners(model,"model.input",model["input"]);
}

function install_triggers(node){
  base_util.register_trigger(node,"page.model/output",model_apply_output,null);
  base_util.register_trigger(node,"page.model/input",model_apply_input,null);
  return node;
}

function install(node){
  install_handlers(node);
  install_triggers(node);
  return node;
}

function group_list_proxy(node,space_id,opts){
  let transport_id = opts["transport_id"];
  return base_util.request(
    node,
    space_id,
    "@page/group-list",
    [space_id],
    {"transport_id":transport_id}
  );
}

function group_open_proxy(node,space_id,group_id,opts){
  let transport_id = opts["transport_id"];
  let existing_group = page_core.group_get(node,space_id,group_id);
  let remote_spec = (existing_group && existing_group["remote"]) || opts;
  return base_util.request(
    node,
    space_id,
    "@page/group-open",
    [{"space":space_id,"group":group_id}],
    {"transport_id":transport_id}
  ).then(function (response){
    let error = response["error"];
    if(null != error){
      throw "ERR - " + error;
    }
    let snapshot = response["models"];
    group_create_proxy(node,space_id,group_id,snapshot,remote_spec);
    return page_core.group_get(node,space_id,group_id);
  });
}

function group_close_proxy(node,space_id,group_id,opts){
  let transport_id = opts["transport_id"];
  return base_util.request(
    node,
    space_id,
    "@page/group-close",
    [{"space":space_id,"group":group_id}],
    {"transport_id":transport_id}
  ).then(function (_){
    let runtime = page_core.space_ensure_page(node,space_id);
    let groups = runtime["groups"];
    delete(groups[group_id]);
    return null;
  });
}

function model_proxy_call(node,space_id,group_id,model_id,args,save_output,opts){
  let group = page_core.group_get(node,space_id,group_id);
  let dispatch_fn = group["proxy_dispatch"];
  return dispatch_fn("proxy-call",node,space_id,group_id,[model_id,args,save_output]).then(function (response){
    let output = response["output"];
    if(null != output){
      model_apply_output(space_id,{
        "space":space_id,
        "data":{"path":[group_id,model_id],"output":output}
      },node);
    }
    return response;
  });
}

function group_sync_proxy(node,space_id,group_id,opts){
  install(node);
  let transport_id = opts["transport_id"];
  return group_open_proxy(node,space_id,group_id,opts).then(function (group){
    return {
      "space":space_id,
      "group":group_id,
      "group-obj":group,
      "transport_id":transport_id,
      "close":function (){
            return group_close_proxy(node,space_id,group_id,opts);
          }
    };
  });
}

module.exports = {
  ["model_serialize_input"]:model_serialize_input,
  ["model_serialize_output"]:model_serialize_output,
  ["model_serialize"]:model_serialize,
  ["group_snapshot"]:group_snapshot,
  ["model_get_output"]:model_get_output,
  ["publish_model_output"]:publish_model_output,
  ["publish_model_input"]:publish_model_input,
  ["ensure_model_listeners"]:ensure_model_listeners,
  ["group_handle_list"]:group_handle_list,
  ["group_handle_open"]:group_handle_open,
  ["group_handle_close"]:group_handle_close,
  ["group_handle_update"]:group_handle_update,
  ["model_handle_update"]:model_handle_update,
  ["model_handle_set_input"]:model_handle_set_input,
  ["model_handle_trigger"]:model_handle_trigger,
  ["group_handle_trigger"]:group_handle_trigger,
  ["model_handle_proxy_call"]:model_handle_proxy_call,
  ["install_handlers"]:install_handlers,
  ["model_create_proxy"]:model_create_proxy,
  ["proxy_dispatch_op"]:proxy_dispatch_op,
  ["proxy_dispatcher"]:proxy_dispatcher,
  ["group_create_proxy"]:group_create_proxy,
  ["model_apply_output"]:model_apply_output,
  ["model_apply_input"]:model_apply_input,
  ["install_triggers"]:install_triggers,
  ["install"]:install,
  ["group_list_proxy"]:group_list_proxy,
  ["group_open_proxy"]:group_open_proxy,
  ["group_close_proxy"]:group_close_proxy,
  ["model_proxy_call"]:model_proxy_call,
  ["group_sync_proxy"]:group_sync_proxy
}