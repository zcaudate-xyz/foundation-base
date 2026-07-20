import 'package:xtalk_lang/common-data.dart' as xtd;
import 'package:xtalk_event/base-model.dart' as event_model;
import 'dart:async';



wrap_space_args(handler) {
  return (context) {
    var args = context["args"] ?? <dynamic>[];
    var params = <dynamic>[context];
    xtd.arr_assign(params,args);
    return Function.apply(handler,params);
  };
}

check_event(pred, signal, event, ctx) {
  var check = false;
  try{
    var t = null;
    if(null == pred){
      t = true;
    }
    else if("bool" == (pred.runtimeType).toString()){
      t = pred;
    }
    else if((pred.runtimeType).toString().contains("Function") || (pred.runtimeType).toString().contains("=>") || (pred).toString().startsWith("Closure")){
      t = pred(signal,ctx);
    }
    else if(("Map" == (pred.runtimeType).toString()) || (pred.runtimeType).toString().startsWith("_Map") || (pred.runtimeType).toString().startsWith("LinkedMap")){
      t = pred[signal];
    }
    else{
      t = (signal == pred);
    }
    if(true == t){
      check = true;
    }
    else if((t.runtimeType).toString().contains("Function") || (t.runtimeType).toString().contains("=>") || (t).toString().startsWith("Closure")){
      check = Function.apply(t,<dynamic>[event,ctx]);
    }
  }
  catch(err){
    check = false;
  }
  return check;
}

run_tail_call(context, refresh_deps_fn) {
  var acc = context["acc"];
  var path = context["path"];
  var node = context["node"];
  var space_id = (context["space"])["id"];
  var group_id = path[0];
  var model_id = path[1];
  if(((null != acc) && (false != acc)) && !(() {
    var dart_truthy__51430 = acc["error"];
    return (null != dart_truthy__51430) && (false != dart_truthy__51430);
  })() && ((null != refresh_deps_fn) && (false != refresh_deps_fn))){
    return ((Future.sync(() => Future.sync(() {
      return Function.apply(
        (refresh_deps_fn as Function),
        <dynamic>[node,space_id,group_id,model_id,refresh_deps_fn]
      );
    }))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
      return acc;
    },<dynamic>[value]); });
  }
  else{
    return acc;
  }
}

run_remote(context, save_output, path, refresh_deps_fn) {
  context["acc"]["path"] = path;
  return ((Future.sync(() => event_model.pipeline_run_remote(context,save_output,event_model.async_fn_promise,null,null))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
    return run_tail_call(context,refresh_deps_fn);
  },<dynamic>[value]); });
}

run_refresh(context, disabled, path, refresh_deps_fn) {
  context["acc"]["path"] = path;
  return ((Future.sync(() => event_model.pipeline_run(context,disabled,event_model.async_fn_promise,null,null,null))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
    return run_tail_call(context,refresh_deps_fn);
  },<dynamic>[value]); });
}

get_group_deps(group_id, models) {
  var all_deps = <dynamic, dynamic>{};
  for(var entry_51431 in models.entries){
    var model_id = entry_51431.key;
    var model_entry = entry_51431.value;
    var deps = model_entry["deps"];
    var arr_51432 = deps ?? <dynamic>[];
    for(var i51433 = 0; i51433 < arr_51432.length; ++i51433){
      var path = arr_51432[i51433];
      path = (((path.runtimeType).toString().startsWith("List") || (path.runtimeType).toString().startsWith("_GrowableList")) ? path : <dynamic>[group_id,path]);
      xtd.set_in(all_deps,<dynamic>[path[0],path[1],model_id],true);
    };
  };
  return all_deps;
}

raw_callback_id(space_id) {
  return "@/raw/page/" + (space_id ?? "");
}

register_page_trigger(node, signal, trigger_fn, meta) {
  var entry = <dynamic, dynamic>{
    "id":signal,
    "fn":trigger_fn,
    "meta":meta ?? <dynamic, dynamic>{}
  };
  node["triggers"][signal] = entry;
  return entry;
}

unregister_page_trigger(node, signal) {
  var triggers = node["triggers"];
  var prev = triggers[signal];
  triggers.remove(signal);
  return prev;
}