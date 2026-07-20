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
    var dart_truthy__50693 = xtd.is_emptyp(x);
    return (null != dart_truthy__50693) && (false != dart_truthy__50693);
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
  for(var entry_50705 in listeners.entries){
    var id = entry_50705.key;
    var entry = entry_50705.value;
    if((() {
      var dart_truthy__50696 = listener_entryp(entry);
      return (null != dart_truthy__50696) && (false != dart_truthy__50696);
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
    var dart_truthy__50694 = listener_entryp(entry);
    return (null != dart_truthy__50694) && (false != dart_truthy__50694);
  })()){
    return null;
  }
  listeners.remove(listener_id);
  return entry;
}

list_listeners(container) {
  var listeners = container["listeners"];
  var out = <dynamic>[];
  for(var entry_50706 in listeners.entries){
    var id = entry_50706.key;
    var entry = entry_50706.value;
    if((() {
      var dart_truthy__50695 = listener_entryp(entry);
      return (null != dart_truthy__50695) && (false != dart_truthy__50695);
    })()){
      out.add(id);
    }
  };
  return out;
}

list_listener_types(container) {
  var listeners = container["listeners"];
  var out = <dynamic, dynamic>{};
  for(var entry_50707 in listeners.entries){
    var id = entry_50707.key;
    var listener_entry = entry_50707.value;
    if((() {
      var dart_truthy__50701 = listener_entryp(listener_entry);
      return (null != dart_truthy__50701) && (false != dart_truthy__50701);
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
    var dart_truthy__50704 = pred(event);
    return (null != dart_truthy__50704) && (false != dart_truthy__50704);
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
  for(var entry_50708 in listeners.entries){
    var id = entry_50708.key;
    var entry = entry_50708.value;
    if((() {
      var dart_truthy__50699 = listener_entryp(entry);
      return (null != dart_truthy__50699) && (false != dart_truthy__50699);
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
    var dart_truthy__50702 = listener_entryp(group);
    return (null != dart_truthy__50702) && (false != dart_truthy__50702);
  })()){
    return null;
  }
  var entry = group[listener_id];
  group.remove(listener_id);
  if((() {
    var dart_truthy__50703 = xtd.obj_emptyp(group);
    return (null != dart_truthy__50703) && (false != dart_truthy__50703);
  })()){
    listeners.remove(key);
  }
  return entry;
}

list_keyed_listeners(container, key) {
  var listeners = container["listeners"];
  var group = listeners[key];
  if((null == group) || (() {
    var dart_truthy__50700 = listener_entryp(group);
    return (null != dart_truthy__50700) && (false != dart_truthy__50700);
  })()){
    return <dynamic>[];
  }
  return List<dynamic>.from(( group ).keys);
}

all_keyed_listeners(container) {
  var listeners = container["listeners"];
  var out = <dynamic, dynamic>{};
  for(var entry_50711 in listeners.entries){
    var key = entry_50711.key;
    var group = entry_50711.value;
    if(!(() {
      var dart_truthy__50697 = listener_entryp(group);
      return (null != dart_truthy__50697) && (false != dart_truthy__50697);
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
    var dart_truthy__50698 = listener_entryp(group);
    return (null != dart_truthy__50698) && (false != dart_truthy__50698);
  })()){
    for(var entry_50712 in group.entries){
      var id = entry_50712.key;
      var entry = entry_50712.value;
      trigger_entry(entry,event);
      triggered.add(id);
    };
  }
  return triggered;
}