const event_common = require("@xtalk/event/base-listener.js")

const xtd = require("@xtalk/lang/common-data.js")

const base_util = require("@xtalk/substrate/base-util.js")

const page_core = require("@xtalk/substrate/page-core.js")

const catalog = require("@xtalk/substrate/view-catalog.js")

const node_space = require("@xtalk/substrate/base-space.js")

const event_model = require("@xtalk/event/base-model.js")

function view_spec(view_id,bindings,root){
  return {"version":1,"id":view_id,"bindings":bindings || {},"root":root};
}

function node(component,props,children){
  return {
    "component":component,
    "props":props || {},
    "children":children || []
  };
}

function action(action_id,payload){
  return {"action":action_id,"payload":payload};
}

function event_value(path){
  return {"$":"event","path":path || []};
}

function json_safep(value){
  if(null == value){
    return true;
  }
  else if("string" == (typeof value)){
    return true;
  }
  else if("number" == (typeof value)){
    return true;
  }
  else if("boolean" == (typeof value)){
    return true;
  }
  else if("function" == (typeof value)){
    return false;
  }
  else if(Array.isArray(value)){
    for(let item of value){
      if(!json_safep(item)){
        return false;
      }
    };
    return true;
  }
  else if((null != value) && ("object" == (typeof value)) && !Array.isArray(value)){
    for(let [key,item] of Object.entries(value)){
      if(!("string" == (typeof key)) || !json_safep(item)){
        return false;
      }
    };
    return true;
  }
  else{
    return false;
  }
}

function validate_binding(binding_id,binding){
  if(!((null != binding) && ("object" == (typeof binding)) && !Array.isArray(binding))){
    throw "invalid view binding - " + binding_id;
  }
  let source = binding["source"] || "state";
  if(!((source == "state") || (source == "model-output") || (source == "model-input") || (source == "local"))){
    throw "invalid view binding source - " + source;
  }
  if(((source == "model-output") || (source == "model-input")) && (!("string" == (typeof binding["group_id"])) || !("string" == (typeof binding["model_id"])))){
    throw "model binding requires group_id/model_id - " + binding_id;
  }
  return true;
}

function validate_node(value,opts){
  if(null == value){
    return true;
  }
  if(("string" == (typeof value)) || ("number" == (typeof value))){
    return true;
  }
  if(Array.isArray(value)){
    for(let child of value){
      validate_node(child,opts);
    };
    return true;
  }
  if(!((null != value) && ("object" == (typeof value)) && !Array.isArray(value))){
    throw "invalid view node";
  }
  let component_id = value["component"];
  if(!("string" == (typeof component_id))){
    throw "view node requires component";
  }
  if(catalog.has_componentp(component_id)){
    catalog.validate_props(component_id,value["props"]);
  }
  else if(catalog.platform_idp(component_id)){
    if((opts || {})["portable"]){
      throw "platform view component not portable - " + component_id;
    }
  }
  else{
    throw "unknown view component - " + component_id;
  }
  if(!json_safep(value["props"])){
    throw "view props are not serializable - " + component_id;
  }
  for(let child of value["children"] || []){
    validate_node(child,opts);
  };
  return true;
}

function validate_with(spec,opts){
  if(!((null != spec) && ("object" == (typeof spec)) && !Array.isArray(spec))){
    throw "view spec must be an object";
  }
  if(1 != spec["version"]){
    throw "unsupported view spec version - " + String(spec["version"]);
  }
  if(!("string" == (typeof spec["id"]))){
    throw "view spec requires id";
  }
  for(let [binding_id,binding] of Object.entries(spec["bindings"] || {})){
    validate_binding(binding_id,binding);
  };
  validate_node(spec["root"],opts);
  return true;
}

function validate(spec){
  return validate_with(spec,null);
}

function validate_portable(spec){
  return validate_with(spec,{"portable":true});
}

function state_container(node,space_id,view_id){
  let space = node_space.ensure_space(node,space_id,null);
  let state = space["state"];
  if((null == state) || !((null != state) && ("object" == (typeof state)) && !Array.isArray(state))){
    state = {};
    space["state"] = state;
  }
  let views = state["view"];
  if(!((null != views) && ("object" == (typeof views)) && !Array.isArray(views))){
    views = {};
    state["view"] = views;
  }
  let container = views[view_id];
  if(!((null != container) && ("object" == (typeof container)) && !Array.isArray(container))){
    container = {"values":{},"revision":0};
    views[view_id] = container;
  }
  return container;
}

function state_listener_key(space_id,view_id){
  return JSON.stringify([space_id,["view",view_id]]);
}

function model_listener_key(space_id,group_id,model_id){
  return JSON.stringify([space_id,[group_id,model_id]]);
}

function state_get(node,space_id,view_id,path,default_value){
  let values = (state_container(node,space_id,view_id))["values"];
  let value = xtd.get_in(values,path || []);
  return (null == value) ? default_value : value;
}

function state_set(node,space_id,view_id,path,value){
  let container = state_container(node,space_id,view_id);
  let values = container["values"];
  if(0 == (path || []).length){
    values = value;
    container["values"] = values;
  }
  if((path || []).length > 0){
    xtd.set_in(values,path,value);
  }
  let revision = 1 + (container["revision"] || 0);
  container["revision"] = revision;
  event_common.trigger_keyed_listeners(node,state_listener_key(space_id,view_id),{
    "space_id":space_id,
    "view_id":view_id,
    "revision":revision,
    "path":path || [],
    "value":value
  });
  return value;
}

function binding_read(node,view_id,binding){
  let source = binding["source"] || "state";
  let space_id = binding["space_id"];
  let path = binding["path"] || [];
  if(source == "local"){
    return binding["initial"];
  }
  else if(source == "state"){
    return state_get(node,space_id,view_id,path,binding["default"]);
  }
  else if(source == "model-output"){
    return xtd.get_in(
      page_core.model_get_output(node,space_id,binding["group_id"],binding["model_id"]),
      path
    );
  }
  else if(source == "model-input"){
    let [_group,model] = page_core.model_ensure(node,space_id,binding["group_id"],binding["model_id"]);
    return xtd.get_in((event_model.get_input(model))["current"],path);
  }
  else{
    throw "unsupported binding source - " + source;
  }
}

function snapshot(node,spec){
  let out = {};
  let view_id = spec["id"];
  for(let [binding_id,binding] of Object.entries(spec["bindings"] || {})){
    out[binding_id] = binding_read(node,view_id,binding);
  };
  return out;
}

function subscription_notify(subscription,event){
  let revision = 1 + subscription["revision"];
  subscription["revision"] = revision;
  let next = snapshot(subscription["node"],subscription["spec"]);
  subscription["snapshot"] = next;
  subscription["callback"](next,revision,event);
  return next;
}

function subscribe(node,spec,listener_id,callback){
  validate(spec);
  let subscription = {
    "id":listener_id,
    "node":node,
    "spec":spec,
    "callback":callback,
    "revision":0,
    "snapshot":snapshot(node,spec),
    "keys":[]
  };
  let seen = {};
  let view_id = spec["id"];
  for(let binding of Object.values(spec["bindings"] || {})){
    let source = binding["source"] || "state";
    if(source != "local"){
      let space_id = binding["space_id"];
      let key = (source == "state") ? state_listener_key(space_id,view_id) : model_listener_key(space_id,binding["group_id"],binding["model_id"]);
      if(!(null != seen[key])){
        seen[key] = true;
        subscription["keys"].push(key);
        event_common.add_keyed_listener(node,key,listener_id,"view",function (_id,event,_time,_meta){
          return subscription_notify(subscription,event);
        },{"view_id":view_id},null);
      }
    }
  };
  return subscription;
}

function unsubscribe(subscription){
  let node = subscription["node"];
  let listener_id = subscription["id"];
  for(let key of subscription["keys"]){
    event_common.remove_keyed_listener(node,key,listener_id);
  };
  subscription["keys"] = [];
  return true;
}

function dispatch(node,space_id,action_desc,event,meta){
  let action_id = action_desc["action"];
  if(!("string" == (typeof action_id))){
    throw "view action requires a substrate handler id";
  }
  let payload = action_desc["payload"];
  if(((null != payload) && ("object" == (typeof payload)) && !Array.isArray(payload)) && ("event" == payload["$"])){
    payload = xtd.get_in(event,payload["path"] || []);
  }
  return base_util.request(node,space_id,action_id,[payload],meta || {});
}

module.exports = {
  ["view_spec"]:view_spec,
  ["node"]:node,
  ["action"]:action,
  ["event_value"]:event_value,
  ["json_safep"]:json_safep,
  ["validate_binding"]:validate_binding,
  ["validate_node"]:validate_node,
  ["validate_with"]:validate_with,
  ["validate"]:validate,
  ["validate_portable"]:validate_portable,
  ["state_container"]:state_container,
  ["state_listener_key"]:state_listener_key,
  ["model_listener_key"]:model_listener_key,
  ["state_get"]:state_get,
  ["state_set"]:state_set,
  ["binding_read"]:binding_read,
  ["snapshot"]:snapshot,
  ["subscription_notify"]:subscription_notify,
  ["subscribe"]:subscribe,
  ["unsubscribe"]:unsubscribe,
  ["dispatch"]:dispatch
}