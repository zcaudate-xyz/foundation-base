import 'package:xtalk_lang/common-data.dart' as xtd;

blank_container(type_name, opts) {
  var container = xtd.obj_assign(
    <dynamic, dynamic>{"::":type_name,"listeners":<dynamic, dynamic>{}},
    opts
  );
  return container;
}

make_container(initial, type_name, opts) {
  var initialFn = initial;
  if(!((initialFn.runtimeType).toString().contains("Function") || (initialFn.runtimeType).toString().contains("=>") || (initialFn).toString().startsWith("Closure"))){
    initialFn = (() {
      return initial;
    });
  }
  var data = initialFn();
  var container = xtd.obj_assign(<dynamic, dynamic>{
    "::":type_name,
    "data":data,
    "initial":initialFn,
    "listeners":<dynamic, dynamic>{}
  },opts);
  return container;
}

make_listener_entry(listener_id, listener_type, callback, meta, pred) {
  return <dynamic, dynamic>{
    "callback":callback,
    "pred":pred,
    "meta":xtd.obj_assign(
        <dynamic, dynamic>{"listener/id":listener_id,"listener/type":listener_type},
        meta
      )
  };
}

listener_entryp(entry) {
  return (null != entry) && ((entry["callback"].runtimeType).toString().contains("Function") || (entry["callback"].runtimeType).toString().contains("=>") || (entry["callback"]).toString().startsWith("Closure"));
}

arrayify_path(x) {
  if((x.runtimeType).toString().startsWith("List") || (x.runtimeType).toString().startsWith("_GrowableList")){
    return x;
  }
  if((null == x) || ((("Map" == (x.runtimeType).toString()) || (x.runtimeType).toString().startsWith("_Map") || (x.runtimeType).toString().startsWith("LinkedMap")) && (() {
    var dart_truthy__41322 = xtd.is_emptyp(x);
    return (null != dart_truthy__41322) && (false != dart_truthy__41322);
  })())){
    return <dynamic>[];
  }
  return <dynamic>[x];
}

callback_data(event) {
  if(!(("Map" == (event.runtimeType).toString()) || (event.runtimeType).toString().startsWith("_Map") || (event.runtimeType).toString().startsWith("LinkedMap"))){
    return event;
  }
  var out = xtd.obj_clone(event);
  if(out.containsKey("meta")){
    out.remove("meta");
  }
  return out;
}

callback_time(event) {
  if(!(("Map" == (event.runtimeType).toString()) || (event.runtimeType).toString().startsWith("_Map") || (event.runtimeType).toString().startsWith("LinkedMap"))){
    return null;
  }
  if(event.containsKey("time")){
    return event["time"];
  }
  if(event.containsKey("t")){
    return event["t"];
  }
  return null;
}

clear_listeners(container) {
  var listeners = container["listeners"];
  var cleared = <dynamic, dynamic>{};
  var kept = <dynamic, dynamic>{};
  for(var entry_41334 in listeners.entries){
    var id = entry_41334.key;
    var entry = entry_41334.value;
    if((() {
      var dart_truthy__41325 = listener_entryp(entry);
      return (null != dart_truthy__41325) && (false != dart_truthy__41325);
    })()){
      cleared[id] = entry;
    }
    else{
      kept[id] = entry;
    }
  };
  container["listeners"] = kept;
  return cleared;
}

add_listener(container, listener_id, listener_type, callback, meta, pred) {
  var listeners = container["listeners"];
  var entry = make_listener_entry(listener_id,listener_type,callback,meta,pred);
  listeners[listener_id] = entry;
  return entry;
}

remove_listener(container, listener_id) {
  var listeners = container["listeners"];
  var entry = listeners[listener_id];
  if(!(() {
    var dart_truthy__41323 = listener_entryp(entry);
    return (null != dart_truthy__41323) && (false != dart_truthy__41323);
  })()){
    return null;
  }
  listeners.remove(listener_id);
  return entry;
}

list_listeners(container) {
  var listeners = container["listeners"];
  var out = <dynamic>[];
  for(var entry_41335 in listeners.entries){
    var id = entry_41335.key;
    var entry = entry_41335.value;
    if((() {
      var dart_truthy__41324 = listener_entryp(entry);
      return (null != dart_truthy__41324) && (false != dart_truthy__41324);
    })()){
      out.add(id);
    }
  };
  return out;
}

list_listener_types(container) {
  var listeners = container["listeners"];
  var out = <dynamic, dynamic>{};
  for(var entry_41336 in listeners.entries){
    var id = entry_41336.key;
    var listener_entry = entry_41336.value;
    if((() {
      var dart_truthy__41330 = listener_entryp(listener_entry);
      return (null != dart_truthy__41330) && (false != dart_truthy__41330);
    })()){
      var meta = listener_entry["meta"];
      var t = meta["listener/type"];
      var arr = out[t];
      if(null == arr){
        arr = <dynamic>[];
        out[t] = arr;
      }
      arr.add(id);
    }
  };
  return out;
}

trigger_entry(entry, event) {
  var callback = entry["callback"];
  var meta = entry["meta"];
  var pred = entry["pred"];
  if((null == pred) || (() {
    var dart_truthy__41333 = pred(event);
    return (null != dart_truthy__41333) && (false != dart_truthy__41333);
  })()){
    var nmeta = xtd.obj_assign(event["meta"] ?? <dynamic, dynamic>{},meta);
    var listener_id = meta["listener/id"];
    return callback(listener_id,callback_data(event),callback_time(event),nmeta);
  }
}

trigger_listeners(container, event) {
  if(null == event){
    event = <dynamic, dynamic>{};
  }
  var listeners = container["listeners"];
  var triggered = <dynamic>[];
  for(var entry_41337 in listeners.entries){
    var id = entry_41337.key;
    var entry = entry_41337.value;
    if((() {
      var dart_truthy__41328 = listener_entryp(entry);
      return (null != dart_truthy__41328) && (false != dart_truthy__41328);
    })()){
      trigger_entry(entry,event);
      triggered.add(id);
    }
  };
  return triggered;
}

add_keyed_listener(container, key, listener_id, listener_type, callback, meta, pred) {
  var listeners = container["listeners"];
  var entry = make_listener_entry(listener_id,listener_type,callback,meta,pred);
  var group = listeners[key];
  if(null == group){
    group = <dynamic, dynamic>{};
    listeners[key] = group;
  }
  group[listener_id] = entry;
  return entry;
}

remove_keyed_listener(container, key, listener_id) {
  var listeners = container["listeners"];
  var group = listeners[key];
  if((null == group) || (() {
    var dart_truthy__41331 = listener_entryp(group);
    return (null != dart_truthy__41331) && (false != dart_truthy__41331);
  })()){
    return null;
  }
  var entry = group[listener_id];
  group.remove(listener_id);
  if((() {
    var dart_truthy__41332 = xtd.obj_emptyp(group);
    return (null != dart_truthy__41332) && (false != dart_truthy__41332);
  })()){
    listeners.remove(key);
  }
  return entry;
}

list_keyed_listeners(container, key) {
  var listeners = container["listeners"];
  var group = listeners[key];
  if((null == group) || (() {
    var dart_truthy__41329 = listener_entryp(group);
    return (null != dart_truthy__41329) && (false != dart_truthy__41329);
  })()){
    return <dynamic>[];
  }
  return List<dynamic>.from(( group ).keys);
}

all_keyed_listeners(container) {
  var listeners = container["listeners"];
  var out = <dynamic, dynamic>{};
  for(var entry_41340 in listeners.entries){
    var key = entry_41340.key;
    var group = entry_41340.value;
    if(!(() {
      var dart_truthy__41326 = listener_entryp(group);
      return (null != dart_truthy__41326) && (false != dart_truthy__41326);
    })()){
      out[key] = list_keyed_listeners(container,key);
    }
  };
  return out;
}

trigger_keyed_listeners(container, key, event) {
  if(null == event){
    event = <dynamic, dynamic>{};
  }
  var listeners = container["listeners"];
  var group = listeners[key];
  var triggered = <dynamic>[];
  if((null != group) && !(() {
    var dart_truthy__41327 = listener_entryp(group);
    return (null != dart_truthy__41327) && (false != dart_truthy__41327);
  })()){
    for(var entry_41341 in group.entries){
      var id = entry_41341.key;
      var entry = entry_41341.value;
      trigger_entry(entry,event);
      triggered.add(id);
    };
  }
  return triggered;
}