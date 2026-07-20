const React = require("react")

const backend = require("@xtalk/ui/react/view/backend.js")

const r = require("@xtalk/ui/react.js")

const polyfill = require("@xtalk/ui/react/view/polyfill.js")

const view = require("@xtalk/substrate/view.js")

function runtime_create(node,spec,render_fn,opts){
  opts = (opts || {});
  let registry = backend.registry(opts["overrides"],opts["polyfills"] || polyfill.registry());
  let local = {};
  for(let [binding_id,binding] of Object.entries(spec["bindings"] || {})){
    if("local" == binding["source"]){
      local[binding_id] = binding["initial"];
    }
  };
  let runtime = {
    "listener_id":opts["listener_id"] || ("react/" + spec["id"] + "/" + String(Date.now())),
    "render_fn":render_fn,
    "spec":spec,
    "space_id":opts["space_id"],
    "backend":"react",
    "registry":registry,
    "invalidate":null,
    "local":local,
    "node":node
  };
  runtime["dispatch"] = (function (action_desc,event){
    return view.dispatch(node,runtime["space_id"],action_desc,event,null);
  });
  return runtime;
}

function snapshot(runtime){
  let out = view.snapshot(runtime["node"],runtime["spec"]);
  Object.assign(out,runtime["local"]);
  return out;
}

function local_set(runtime,binding_id,value){
  runtime["local"][binding_id] = value;
  let invalidate = runtime["invalidate"];
  if("function" == (typeof invalidate)){
    invalidate();
  }
  return value;
}

function resolve_node(runtime,node,seen){
  if((null == node) || ("string" == (typeof node)) || ("number" == (typeof node))){
    return node;
  }
  if(Array.isArray(node)){
    return node.map(function (child){
      return resolve_node(runtime,child,{});
    });
  }
  let component_id = node["component"];
  let registry = runtime["registry"];
  let override = (registry["overrides"])[component_id];
  let native = backend.native_entry(registry,component_id);
  if(override || native){
    return node;
  }
  if(null != seen[component_id]){
    throw "view polyfill cycle [react] - " + component_id;
  }
  let lowering = (registry["polyfills"])[component_id];
  if(!("function" == (typeof lowering))){
    throw "view implementation missing [react] - " + component_id;
  }
  seen[component_id] = true;
  return resolve_node(runtime,lowering(node),seen);
}

function render_node(runtime,node){
  if(null == node){
    return null;
  }
  if("string" == (typeof node)){
    return node;
  }
  if("number" == (typeof node)){
    return String(node);
  }
  if(Array.isArray(node)){
    return node.map(function (child){
      return render_node(runtime,child);
    });
  }
  if(true == (node["props"] || {})["hidden"]){
    return null;
  }
  node = resolve_node(runtime,node,{});
  let component_id = node["component"];
  let registry = runtime["registry"];
  let props = node["props"] || {};
  let children = render_node(runtime,node["children"] || []);
  let override = (registry["overrides"])[component_id];
  if("function" == (typeof override)){
    return override(runtime,props,children);
  }
  let entry = backend.native_entry(registry,component_id);
  return backend.render_native(runtime,entry,props,children);
}

function render(runtime,snapshot){
  let concrete = runtime["render_fn"](snapshot);
  view.validate(view.view_spec(
    (runtime["spec"])["id"],
    (runtime["spec"])["bindings"],
    concrete
  ));
  return render_node(runtime,concrete);
}

function View({node,options,render_fn,spec}){
  let refresh = r.useRefresh();
  let runtime_ref = React.useRef(runtime_create(node,spec,render_fn,options));
  let subscription_ref = React.useRef(null);
  runtime_ref.current["invalidate"] = refresh;
  React.useEffect(function (){
    let subscription = view.subscribe(node,spec,(runtime_ref.current)["listener_id"],function (_snapshot,_revision,_event){
      return refresh();
    });
    subscription_ref.current = subscription;
    return function (){
      let current = subscription_ref.current;
      if(current){
        view.unsubscribe(current);
      }
    };
  },[]);
  let runtime = runtime_ref.current;
  return render(runtime,snapshot(runtime));
}

module.exports = {
  ["runtime_create"]:runtime_create,
  ["snapshot"]:snapshot,
  ["local_set"]:local_set,
  ["resolve_node"]:resolve_node,
  ["render_node"]:render_node,
  ["render"]:render,
  ["View"]:View
}