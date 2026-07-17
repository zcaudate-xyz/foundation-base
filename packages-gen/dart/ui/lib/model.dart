import 'package:xtalk_event/base-listener.dart' as event_listener;

import 'package:xtalk_lang/common-data.dart' as xtd;

import 'package:xtalk_substrate/page-proxy.dart' as page_proxy;

import 'package:xtalk_substrate/page-core.dart' as page_core;

store_create(node, space_id, group_id, mode, opts) {
  return <dynamic, dynamic>{
    "node":node,
    "space_id":space_id,
    "group_id":group_id,
    "mode":mode ?? "local",
    "opts":opts ?? <dynamic, dynamic>{},
    "control":null,
    "revision":0,
    "listeners":<dynamic, dynamic>{}
  };
}

store_version(store) {
  return store["revision"];
}

store_open(store) {
  if("proxy" != store["mode"]){
    page_core.group_ensure(store["node"],store["space_id"],store["group_id"]);
    return Future.sync(() {
      return store;
    });
  }
  return ((Future.sync(() => page_proxy.group_sync_proxy(
    store["node"],
    store["space_id"],
    store["group_id"],
    store["opts"]
  ))) as Future<dynamic>).then((value) async { return await Function.apply((control) {
    store["control"] = control;
    return store;
  },<dynamic>[value]); });
}

model(store, model_id) {
  var value_43150 = page_core.model_ensure(store["node"],store["space_id"],store["group_id"],model_id);
  var _group = value_43150[0];
  var current = value_43150[1];
  return current;
}

model_slot(store, model_id, slot, path, fallback) {
  var current = model(store,model_id);
  var value = xtd.get_in(current,slot ?? <dynamic>[]);
  if(null != path){
    value = xtd.get_in(value,path);
  }
  return (null == value) ? fallback : value;
}

model_input(store, model_id, path, fallback) {
  return model_slot(store,model_id,<dynamic>["input","current"],path,fallback);
}

model_output(store, model_id, path, fallback) {
  return model_slot(store,model_id,<dynamic>["output","current"],path,fallback);
}

model_pendingp(store, model_id) {
  return true == model_slot(store,model_id,<dynamic>["output","pending"],null,false);
}

model_disabledp(store, model_id) {
  return true == model_slot(store,model_id,<dynamic>["output","disabled"],null,false);
}

model_error(store, model_id) {
  if(!(true == model_slot(store,model_id,<dynamic>["output","errored"],null,false))){
    return null;
  }
  return model_slot(store,model_id,<dynamic>["output","current"],null,null);
}

model_remote(store, model_id, path, fallback) {
  return model_slot(store,model_id,<dynamic>["remote","current"],path,fallback);
}

model_sync(store, model_id, path, fallback) {
  return model_slot(store,model_id,<dynamic>["sync","current"],path,fallback);
}

set_inputf(store, model_id, value, event) {
  return page_core.model_set_input(
    store["node"],
    store["space_id"],
    store["group_id"],
    model_id,
    value,
    event ?? <dynamic, dynamic>{}
  );
}

patch_inputf(store, model_id, path, value, event) {
  var current = model_input(store,model_id,null,<dynamic, dynamic>{});
  if((("Map" == (current.runtimeType).toString()) || (current.runtimeType).toString().startsWith("_Map") || (current.runtimeType).toString().startsWith("LinkedMap")) && current.containsKey("data") && (1 == List<dynamic>.from(( current ).keys).length)){
    var initial_data = current["data"];
    current = (((("Map" == (initial_data.runtimeType).toString()) || (initial_data.runtimeType).toString().startsWith("_Map") || (initial_data.runtimeType).toString().startsWith("LinkedMap")) && !((initial_data.runtimeType).toString().startsWith("List") || (initial_data.runtimeType).toString().startsWith("_GrowableList"))) ? initial_data : <dynamic, dynamic>{});
  }
  var next = jsonDecode(jsonEncode(current ?? <dynamic, dynamic>{}));
  xtd.set_in(next,path,value);
  return set_inputf(store,model_id,next,event);
}

invokef(store, model_id, args) {
  return page_core.model_remote_call(
    store["node"],
    store["space_id"],
    store["group_id"],
    model_id,
    args ?? <dynamic>[],
    true
  );
}

refreshf(store, model_id, event) {
  return page_core.model_refresh(
    store["node"],
    store["space_id"],
    store["group_id"],
    model_id,
    event ?? <dynamic, dynamic>{},
    null
  );
}

subscribef(store, subscription_id, callback) {
  var node = store["node"];
  var space_id = store["space_id"];
  var group_id = store["group_id"];
  var group = page_core.group_ensure(node,space_id,group_id);
  for(var model_id in group["models"].keys){
    var key = jsonEncode(<dynamic>[space_id,<dynamic>[group_id,model_id]]);
    event_listener.add_keyed_listener(node,key,subscription_id + "/" + model_id,"ui.model",(listener_id, data, t, meta) {
      store["revision"] = (1 + store["revision"]);
      return callback(listener_id,data,t,meta);
    },<dynamic, dynamic>{"model_id":model_id},null);
  };
  store["listeners"][subscription_id] = true;
  return subscription_id;
}

unsubscribef(store, subscription_id) {
  var node = store["node"];
  var space_id = store["space_id"];
  var group_id = store["group_id"];
  var group = page_core.group_ensure(node,space_id,group_id);
  for(var model_id in group["models"].keys){
    var key = jsonEncode(<dynamic>[space_id,<dynamic>[group_id,model_id]]);
    event_listener.remove_keyed_listener(node,key,subscription_id + "/" + model_id);
  };
  store["listeners"].remove(subscription_id);
  return true;
}

store_close(store) {
  var listener_ids = List<dynamic>.from(( store["listeners"] ).keys);
  var arr_43157 = listener_ids;
  for(var i43158 = 0; i43158 < arr_43157.length; ++i43158){
    var listener_id = arr_43157[i43158];
    unsubscribef(store,listener_id);
  };
  var control = store["control"];
  if((null != control) && ((control["close"].runtimeType).toString().contains("Function") || (control["close"].runtimeType).toString().contains("=>") || (control["close"]).toString().startsWith("Closure"))){
    return control["close"]();
  }
  return Future.sync(() {
    return true;
  });
}