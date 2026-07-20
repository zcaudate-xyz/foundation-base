import 'package:xtalk_lang/common-data.dart' as xtd;
import 'xtalk_ui/ui/view/polyfill.dart' as polyfill;
import 'xtalk_ui/ui/view/backend.dart' as backend;
import 'package:xtalk_substrate/view.dart' as view;





runtime_create(node, spec, render_fn, opts) {
  opts = (opts ?? <dynamic, dynamic>{});
  var registry = backend.registry(opts["overrides"],opts["polyfills"] ?? polyfill.registry());
  var local = <dynamic, dynamic>{};
  for(var entry_53183 in (spec["bindings"] ?? <dynamic, dynamic>{}).entries){
    var binding_id = entry_53183.key;
    var binding = entry_53183.value;
    if("local" == binding["source"]){
      local[binding_id] = binding["initial"];
    }
  };
  var runtime = <dynamic, dynamic>{
    "listener_id":opts["listener_id"] ?? ("wind/" + spec["id"] + "/" + (DateTime.now().millisecondsSinceEpoch).toString()),
    "render_fn":render_fn,
    "spec":spec,
    "space_id":opts["space_id"],
    "backend":"wind",
    "registry":registry,
    "subscription":null,
    "invalidate":null,
    "local":local,
    "local_revision":0,
    "node":node
  };
  runtime["dispatch"] = ((action_desc, event) {
    return view.dispatch(node,runtime["space_id"],action_desc,event,null);
  });
  return runtime;
}

snapshot(runtime) {
  var out = view.snapshot(runtime["node"],runtime["spec"]);
  xtd.obj_assign(out,runtime["local"]);
  return out;
}

local_set(runtime, binding_id, value) {
  runtime["local"][binding_id] = value;
  var revision = 1 + runtime["local_revision"];
  runtime["local_revision"] = revision;
  var invalidate = runtime["invalidate"];
  if((invalidate.runtimeType).toString().contains("Function") || (invalidate.runtimeType).toString().contains("=>") || (invalidate).toString().startsWith("Closure")){
    invalidate(
      snapshot(runtime),
      revision,
      <dynamic, dynamic>{"source":"local","binding_id":binding_id}
    );
  }
  return value;
}

resolve_node(runtime, node, seen) {
  if((null == node) || ("String" == (node.runtimeType).toString()) || (("int" == (node.runtimeType).toString()) || ("double" == (node.runtimeType).toString()) || ("num" == (node.runtimeType).toString()))){
    return node;
  }
  if((node.runtimeType).toString().startsWith("List") || (node.runtimeType).toString().startsWith("_GrowableList")){
    return xtd.arr_map(node,(child) {
      return resolve_node(runtime,child,<dynamic, dynamic>{});
    });
  }
  var component_id = node["component"];
  var registry = runtime["registry"];
  var override = (registry["overrides"])[component_id];
  var native = backend.native_entry(registry,component_id);
  if(((null != override) && (false != override)) || ((null != native) && (false != native))){
    return node;
  }
  if(seen.containsKey(component_id)){
    throw "view polyfill cycle [wind] - " + component_id;
  }
  var lowering = (registry["polyfills"])[component_id];
  if(!((lowering.runtimeType).toString().contains("Function") || (lowering.runtimeType).toString().contains("=>") || (lowering).toString().startsWith("Closure"))){
    throw "view implementation missing [wind] - " + component_id;
  }
  seen[component_id] = true;
  return resolve_node(runtime,lowering(node),seen);
}

prepare_node(runtime, node, state) {
  if(null == node){
    return null;
  }
  if(("String" == (node.runtimeType).toString()) || (("int" == (node.runtimeType).toString()) || ("double" == (node.runtimeType).toString()) || ("num" == (node.runtimeType).toString()))){
    return <dynamic, dynamic>{
      "type":"WText",
      "props":<dynamic, dynamic>{"text":(node).toString()},
      "children":<dynamic>[]
    };
  }
  if((node.runtimeType).toString().startsWith("List") || (node.runtimeType).toString().startsWith("_GrowableList")){
    return xtd.arr_map(node,(child) {
      return prepare_node(runtime,child,state);
    });
  }
  if(true == (node["props"] ?? <dynamic, dynamic>{})["hidden"]){
    return <dynamic, dynamic>{
      "type":"WText",
      "props":<dynamic, dynamic>{"text":""},
      "children":<dynamic>[]
    };
  }
  node = resolve_node(runtime,node,<dynamic, dynamic>{});
  var component_id = node["component"];
  var registry = runtime["registry"];
  var children = prepare_node(runtime,node["children"] ?? <dynamic>[],state);
  var override = (registry["overrides"])[component_id];
  if((override.runtimeType).toString().contains("Function") || (override.runtimeType).toString().contains("=>") || (override).toString().startsWith("Closure")){
    return override(runtime,node,children,state);
  }
  var entry = backend.native_entry(registry,component_id);
  return backend.prepare_native(
    runtime,
    component_id,
    entry,
    node["props"] ?? <dynamic, dynamic>{},
    children,
    state
  );
}

prepare(runtime) {
  var snapshot = snapshot(runtime);
  var concrete = runtime["render_fn"](snapshot);
  view.validate(view.view_spec(
    (runtime["spec"])["id"],
    (runtime["spec"])["bindings"],
    concrete
  ));
  var state = <dynamic, dynamic>{"next":0,"actions":<dynamic, dynamic>{}};
  var json = prepare_node(runtime,concrete,state);
  return <dynamic, dynamic>{"json":json,"actions":state["actions"]};
}

open(runtime, notify) {
  var spec = runtime["spec"];
  runtime["invalidate"] = notify;
  var subscription = view.subscribe(runtime["node"],spec,runtime["listener_id"],(snapshot, revision, event) {
    return notify(snapshot,revision,event);
  });
  runtime["subscription"] = subscription;
  return runtime;
}

close(runtime) {
  var subscription = runtime["subscription"];
  if((null != subscription) && (false != subscription)){
    view.unsubscribe(subscription);
    runtime["subscription"] = null;
  }
  runtime["invalidate"] = null;
  return true;
}