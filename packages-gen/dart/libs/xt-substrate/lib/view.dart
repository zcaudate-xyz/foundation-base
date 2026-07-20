import 'package:xtalk_event/base-listener.dart' as event_common;
import 'package:xtalk_lang/common-data.dart' as xtd;
import 'package:xtalk_substrate/base-util.dart' as base_util;
import 'package:xtalk_substrate/page-core.dart' as page_core;
import 'package:xtalk_substrate/view-catalog.dart' as catalog;
import 'package:xtalk_substrate/base-space.dart' as node_space;
import 'package:xtalk_event/base-model.dart' as event_model;
import 'dart:convert';








view_spec(view_id, bindings, root) {
  return <dynamic, dynamic>{
    "version":1,
    "id":view_id,
    "bindings":bindings ?? <dynamic, dynamic>{},
    "root":root
  };
}

node(component, props, children) {
  return <dynamic, dynamic>{
    "component":component,
    "props":props ?? <dynamic, dynamic>{},
    "children":children ?? <dynamic>[]
  };
}

action(action_id, payload) {
  return <dynamic, dynamic>{"action":action_id,"payload":payload};
}

event_value(path) {
  return <dynamic, dynamic>{"\$":"event","path":path ?? <dynamic>[]};
}

json_safep(value) {
  if(null == value){
    return true;
  }
  else if("String" == (value.runtimeType).toString()){
    return true;
  }
  else if(("int" == (value.runtimeType).toString()) || ("double" == (value.runtimeType).toString()) || ("num" == (value.runtimeType).toString())){
    return true;
  }
  else if("bool" == (value.runtimeType).toString()){
    return true;
  }
  else if((value.runtimeType).toString().contains("Function") || (value.runtimeType).toString().contains("=>") || (value).toString().startsWith("Closure")){
    return false;
  }
  else if((value.runtimeType).toString().startsWith("List") || (value.runtimeType).toString().startsWith("_GrowableList")){
    var arr_51246 = value;
    for(var i51247 = 0; i51247 < arr_51246.length; ++i51247){
      var item = arr_51246[i51247];
      if(!(() {
        var dart_truthy__51240 = json_safep(item);
        return (null != dart_truthy__51240) && (false != dart_truthy__51240);
      })()){
        return false;
      }
    };
    return true;
  }
  else if(("Map" == (value.runtimeType).toString()) || (value.runtimeType).toString().startsWith("_Map") || (value.runtimeType).toString().startsWith("LinkedMap")){
    for(var entry_51268 in value.entries){
      var key = entry_51268.key;
      var item = entry_51268.value;
      if(!("String" == (key.runtimeType).toString()) || !(() {
        var dart_truthy__51241 = json_safep(item);
        return (null != dart_truthy__51241) && (false != dart_truthy__51241);
      })()){
        return false;
      }
    };
    return true;
  }
  else{
    return false;
  }
}

validate_binding(binding_id, binding) {
  if(!(("Map" == (binding.runtimeType).toString()) || (binding.runtimeType).toString().startsWith("_Map") || (binding.runtimeType).toString().startsWith("LinkedMap"))){
    throw "invalid view binding - " + binding_id;
  }
  var source = binding["source"] ?? "state";
  if(!((source == "state") || (source == "model-output") || (source == "model-input") || (source == "local"))){
    throw "invalid view binding source - " + source;
  }
  if(((source == "model-output") || (source == "model-input")) && (!("String" == (binding["group_id"].runtimeType).toString()) || !("String" == (binding["model_id"].runtimeType).toString()))){
    throw "model binding requires group_id/model_id - " + binding_id;
  }
  return true;
}

validate_node(value, opts) {
  if(null == value){
    return true;
  }
  if(("String" == (value.runtimeType).toString()) || (("int" == (value.runtimeType).toString()) || ("double" == (value.runtimeType).toString()) || ("num" == (value.runtimeType).toString()))){
    return true;
  }
  if((value.runtimeType).toString().startsWith("List") || (value.runtimeType).toString().startsWith("_GrowableList")){
    var arr_51269 = value;
    for(var i51270 = 0; i51270 < arr_51269.length; ++i51270){
      var child = arr_51269[i51270];
      validate_node(child,opts);
    };
    return true;
  }
  if(!(("Map" == (value.runtimeType).toString()) || (value.runtimeType).toString().startsWith("_Map") || (value.runtimeType).toString().startsWith("LinkedMap"))){
    throw "invalid view node";
  }
  var component_id = value["component"];
  if(!("String" == (component_id.runtimeType).toString())){
    throw "view node requires component";
  }
  if((() {
    var dart_truthy__51242 = catalog.has_componentp(component_id);
    return (null != dart_truthy__51242) && (false != dart_truthy__51242);
  })()){
    catalog.validate_props(component_id,value["props"]);
  }
  else if((() {
    var dart_truthy__51243 = catalog.platform_idp(component_id);
    return (null != dart_truthy__51243) && (false != dart_truthy__51243);
  })()){
    if((() {
      var dart_truthy__51244 = (opts ?? <dynamic, dynamic>{})["portable"];
      return (null != dart_truthy__51244) && (false != dart_truthy__51244);
    })()){
      throw "platform view component not portable - " + component_id;
    }
  }
  else{
    throw "unknown view component - " + component_id;
  }
  if(!(() {
    var dart_truthy__51245 = json_safep(value["props"]);
    return (null != dart_truthy__51245) && (false != dart_truthy__51245);
  })()){
    throw "view props are not serializable - " + component_id;
  }
  var arr_51291 = value["children"] ?? <dynamic>[];
  for(var i51292 = 0; i51292 < arr_51291.length; ++i51292){
    var child = arr_51291[i51292];
    validate_node(child,opts);
  };
  return true;
}

validate_with(spec, opts) {
  if(!(("Map" == (spec.runtimeType).toString()) || (spec.runtimeType).toString().startsWith("_Map") || (spec.runtimeType).toString().startsWith("LinkedMap"))){
    throw "view spec must be an object";
  }
  if(1 != spec["version"]){
    throw "unsupported view spec version - " + (spec["version"]).toString();
  }
  if(!("String" == (spec["id"].runtimeType).toString())){
    throw "view spec requires id";
  }
  for(var entry_51313 in (spec["bindings"] ?? <dynamic, dynamic>{}).entries){
    var binding_id = entry_51313.key;
    var binding = entry_51313.value;
    validate_binding(binding_id,binding);
  };
  validate_node(spec["root"],opts);
  return true;
}

validate(spec) {
  return validate_with(spec,null);
}

validate_portable(spec) {
  return validate_with(spec,<dynamic, dynamic>{"portable":true});
}

state_container(node, space_id, view_id) {
  var space = node_space.ensure_space(node,space_id,null);
  var state = space["state"];
  if((null == state) || !(("Map" == (state.runtimeType).toString()) || (state.runtimeType).toString().startsWith("_Map") || (state.runtimeType).toString().startsWith("LinkedMap"))){
    state = <dynamic, dynamic>{};
    space["state"] = state;
  }
  var views = state["view"];
  if(!(("Map" == (views.runtimeType).toString()) || (views.runtimeType).toString().startsWith("_Map") || (views.runtimeType).toString().startsWith("LinkedMap"))){
    views = <dynamic, dynamic>{};
    state["view"] = views;
  }
  var container = views[view_id];
  if(!(("Map" == (container.runtimeType).toString()) || (container.runtimeType).toString().startsWith("_Map") || (container.runtimeType).toString().startsWith("LinkedMap"))){
    container = <dynamic, dynamic>{"values":<dynamic, dynamic>{},"revision":0};
    views[view_id] = container;
  }
  return container;
}

state_listener_key(space_id, view_id) {
  return jsonEncode(<dynamic>[space_id,<dynamic>["view",view_id]]);
}

model_listener_key(space_id, group_id, model_id) {
  return jsonEncode(<dynamic>[space_id,<dynamic>[group_id,model_id]]);
}

state_get(node, space_id, view_id, path, default_value) {
  var values = (state_container(node,space_id,view_id))["values"];
  var value = xtd.get_in(values,path ?? <dynamic>[]);
  return (null == value) ? default_value : value;
}

state_set(node, space_id, view_id, path, value) {
  var container = state_container(node,space_id,view_id);
  var values = container["values"];
  if(0 == (path ?? <dynamic>[]).length){
    values = value;
    container["values"] = values;
  }
  if((path ?? <dynamic>[]).length > 0){
    xtd.set_in(values,path,value);
  }
  var revision = 1 + (container["revision"] ?? 0);
  container["revision"] = revision;
  event_common.trigger_keyed_listeners(node,state_listener_key(space_id,view_id),<dynamic, dynamic>{
    "space_id":space_id,
    "view_id":view_id,
    "revision":revision,
    "path":path ?? <dynamic>[],
    "value":value
  });
  return value;
}

binding_read(node, view_id, binding) {
  var source = binding["source"] ?? "state";
  var space_id = binding["space_id"];
  var path = binding["path"] ?? <dynamic>[];
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
    var value_51314 = page_core.model_ensure(node,space_id,binding["group_id"],binding["model_id"]);
    var _group = value_51314[0];
    var model = value_51314[1];
    return xtd.get_in((event_model.get_input(model))["current"],path);
  }
  else{
    throw "unsupported binding source - " + source;
  }
}

snapshot(node, spec) {
  var out = <dynamic, dynamic>{};
  var view_id = spec["id"];
  for(var entry_51315 in (spec["bindings"] ?? <dynamic, dynamic>{}).entries){
    var binding_id = entry_51315.key;
    var binding = entry_51315.value;
    out[binding_id] = binding_read(node,view_id,binding);
  };
  return out;
}

subscription_notify(subscription, event) {
  var revision = 1 + subscription["revision"];
  subscription["revision"] = revision;
  var next = snapshot(subscription["node"],subscription["spec"]);
  subscription["snapshot"] = next;
  subscription["callback"](next,revision,event);
  return next;
}

subscribe(node, spec, listener_id, callback) {
  validate(spec);
  var subscription = <dynamic, dynamic>{
    "id":listener_id,
    "node":node,
    "spec":spec,
    "callback":callback,
    "revision":0,
    "snapshot":snapshot(node,spec),
    "keys":<dynamic>[]
  };
  var seen = <dynamic, dynamic>{};
  var view_id = spec["id"];
  for(var binding in (spec["bindings"] ?? <dynamic, dynamic>{}).values){
    var source = binding["source"] ?? "state";
    if(source != "local"){
      var space_id = binding["space_id"];
      var key = (source == "state") ? state_listener_key(space_id,view_id) : model_listener_key(space_id,binding["group_id"],binding["model_id"]);
      if(!seen.containsKey(key)){
        seen[key] = true;
        subscription["keys"].add(key);
        event_common.add_keyed_listener(node,key,listener_id,"view",(_id, event, _time, _meta) {
          return subscription_notify(subscription,event);
        },<dynamic, dynamic>{"view_id":view_id},null);
      }
    }
  };
  return subscription;
}

unsubscribe(subscription) {
  var node = subscription["node"];
  var listener_id = subscription["id"];
  var arr_51316 = subscription["keys"];
  for(var i51317 = 0; i51317 < arr_51316.length; ++i51317){
    var key = arr_51316[i51317];
    event_common.remove_keyed_listener(node,key,listener_id);
  };
  subscription["keys"] = <dynamic>[];
  return true;
}

dispatch(node, space_id, action_desc, event, meta) {
  var action_id = action_desc["action"];
  if(!("String" == (action_id.runtimeType).toString())){
    throw "view action requires a substrate handler id";
  }
  var payload = action_desc["payload"];
  if((("Map" == (payload.runtimeType).toString()) || (payload.runtimeType).toString().startsWith("_Map") || (payload.runtimeType).toString().startsWith("LinkedMap")) && ("event" == payload["\$"])){
    payload = xtd.get_in(event,payload["path"] ?? <dynamic>[]);
  }
  return base_util.request(
    node,
    space_id,
    action_id,
    <dynamic>[payload],
    meta ?? <dynamic, dynamic>{}
  );
}