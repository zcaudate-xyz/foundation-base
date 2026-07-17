import 'package:xtalk_event/base-listener.dart' as event_common;

import 'package:xtalk_lang/common-data.dart' as xtd;

async_fn_basic(handler, context, callbacks) {
  var success = callbacks["success"];
  var error = callbacks["error"];
  try{
    var output = Function.apply((handler as Function),<dynamic>[context]);
    return success(output);
  }
  catch(err){
    return error(err);
  }
}

async_fn_promise(handler, context, callbacks) {
  var success = callbacks["success"];
  var error = callbacks["error"];
  try{
    var output = Function.apply((handler as Function),<dynamic>[context]);
    if((null != output) && (("Future" == (output.runtimeType).toString()) || (output.runtimeType).toString().startsWith("Future<"))){
      return (() async { try { return await ((Future.sync(() => ((Future.sync(() => output)) as Future<dynamic>).then((value) async { return await Function.apply(success,<dynamic>[value]); }))) as Future<dynamic>); } catch (err) { return await Future.sync(() => Function.apply(error,<dynamic>[err])); } })();
    }
    else{
      return Future.sync(() {
        return success(output);
      });
    }
  }
  catch(err){
    return Future.sync(() {
      return error(err);
    });
  }
}

wrap_args(handler) {
  var wrapped_fn = (context) {
    var args = context["args"];
    if(null == args){
      args = <dynamic>[];
    }
    return Function.apply(handler,args);
  };
  return wrapped_fn;
}

check_disabled(context) {
  var input = context["input"];
  if(null == input){
    return true;
  }
  if(null == input["data"]){
    return true;
  }
  if(true == input["disabled"]){
    return true;
  }
  return false;
}

parse_args(context) {
  var input = context["input"];
  return input["data"];
}

create_model(main_handler, pipeline, default_args, default_output, default_process, options) {
  var identity_fn = (x) {
    return x;
  };
  if(null == options){
    options = <dynamic, dynamic>{};
  }
  var default_args_fn = default_args;
  if(!((default_args_fn.runtimeType).toString().contains("Function") || (default_args_fn.runtimeType).toString().contains("=>") || (default_args_fn).toString().startsWith("Closure"))){
    var args_value = default_args_fn;
    default_args_fn = (() {
      return args_value;
    });
  }
  var default_output_fn = default_output;
  if(!((default_output_fn.runtimeType).toString().contains("Function") || (default_output_fn.runtimeType).toString().contains("=>") || (default_output_fn).toString().startsWith("Closure"))){
    var output_value = default_output_fn;
    default_output_fn = (() {
      return output_value;
    });
  }
  var process_fn = default_process;
  if(null == process_fn){
    process_fn = identity_fn;
  }
  var entry = <dynamic, dynamic>{
    "pipeline":xtd.obj_assign_nested(<dynamic, dynamic>{
        "main":<dynamic, dynamic>{"handler":main_handler,"wrapper":wrap_args},
        "remote":<dynamic, dynamic>{"wrapper":wrap_args},
        "sync":<dynamic, dynamic>{"wrapper":wrap_args},
        "check_args":parse_args,
        "check_disabled":check_disabled
      },pipeline),
    "options":options,
    "input":<dynamic, dynamic>{"current":null,"updated":null,"default":default_args_fn},
    "output":<dynamic, dynamic>{
        "type":"output",
        "current":null,
        "updated":null,
        "elapsed":null,
        "process":process_fn,
        "default":default_output_fn
      }
  };
  if(null != xtd.get_in(pipeline,<dynamic>["remote"])){
    entry["remote"] = <dynamic, dynamic>{
      "type":"remote",
      "current":null,
      "updated":null,
      "elapsed":null,
      "process":process_fn,
      "default":default_output_fn
    };
  }
  if(null != xtd.get_in(pipeline,<dynamic>["sync"])){
    entry["sync"] = <dynamic, dynamic>{
      "type":"sync",
      "current":null,
      "updated":null,
      "elapsed":null,
      "process":process_fn,
      "default":default_output_fn
    };
  }
  return event_common.blank_container("event.model",entry);
}

model_context(model) {
  var options = model["options"];
  var pipeline = model["pipeline"];
  var input = model["input"];
  var context = xtd.obj_assign(
    <dynamic, dynamic>{"model":model,"input":input["current"]},
    options["context"]
  );
  return context;
}

add_listener(model, listener_id, callback, meta, pred) {
  return event_common.add_listener(model,listener_id,"model",callback,meta,pred);
}

remove_listener(model, listener_id) {
  return event_common.remove_listener(model,listener_id);
}

list_listeners(model) {
  return event_common.list_listeners(model);
}

trigger_listeners(model, type_name, data) {
  return event_common.trigger_listeners(model,<dynamic, dynamic>{"type":type_name,"data":data});
}

var PIPELINE = <dynamic, dynamic>{
  "pre":<dynamic, dynamic>{"guard":null,"handler":null},
  "main":<dynamic, dynamic>{"guard":null,"handler":null},
  "sync":<dynamic, dynamic>{"guard":null,"handler":null},
  "remote":<dynamic, dynamic>{"guard":null,"handler":null},
  "post":<dynamic, dynamic>{"guard":null,"handler":null}
};

get_input(model) {
  var input = model["input"];
  return input;
}

get_output(model, dest_key) {
  if(null == dest_key){
    dest_key = "output";
  }
  return model[dest_key];
}

get_current(model, dest_key) {
  if(null == dest_key){
    dest_key = "output";
  }
  return xtd.get_in(model,<dynamic>[dest_key,"current"]);
}

is_disabled(model) {
  var pipeline = model["pipeline"];
  var check_disabled = pipeline["check_disabled"];
  var context = model_context(model);
  return check_disabled(context);
}

is_errored(model, dest_key) {
  if(null == dest_key){
    dest_key = "output";
  }
  return true == xtd.get_in(model,<dynamic>[dest_key,"errored"]);
}

is_pending(model, dest_key) {
  if(null == dest_key){
    dest_key = "output";
  }
  return true == xtd.get_in(model,<dynamic>[dest_key,"pending"]);
}

get_time_elapsed(model, dest_key) {
  if(null == dest_key){
    dest_key = "output";
  }
  return xtd.get_in(model,<dynamic>[dest_key,"elapsed"]);
}

get_time_updated(model, dest_key) {
  if(null == dest_key){
    dest_key = "output";
  }
  return xtd.get_in(model,<dynamic>[dest_key,"updated"]);
}

get_success(model, dest_key) {
  if(null == dest_key){
    dest_key = "output";
  }
  var output = model[dest_key];
  var process = output["process"];
  if(true == output["errored"]){
    return Function.apply((process as Function),<dynamic>[output["default"]()]);
  }
  else{
    var current = output["current"];
    if(null == current){
      current = Function.apply((process as Function),<dynamic>[output["default"]()]);
    }
    return current;
  }
}

set_input(model, current) {
  var callback = model["callback"];
  var input = model["input"];
  xtd.obj_assign(input,<dynamic, dynamic>{
    "current":current,
    "updated":DateTime.now().millisecondsSinceEpoch
  });
  trigger_listeners(model,"model.input",get_input(model));
  return input;
}

set_output(model, current, errored, tag, dest_key, meta) {
  if(null == dest_key){
    dest_key = "output";
  }
  var output = model[dest_key];
  var callback = model["callback"];
  var options = model["options"];
  var accumulate = options["accumulate"];
  if((null != errored) && (false != errored)){
    output["errored"] = true;
  }
  else{
    if(output.containsKey("errored")){
      output.remove("errored");
    }
  }
  output["updated"] = DateTime.now().millisecondsSinceEpoch;
  output["tag"] = tag;
  if((null != accumulate) && (false != accumulate)){
    var prev = xtd.arrayify(output["current"]);
    var next = xtd.arr_assign(xtd.arr_clone(prev),xtd.arrayify(current));
    output["current"] = next;
  }
  else{
    output["current"] = current;
  }
  trigger_listeners(model,"model.output",output);
  return current;
}

set_output_disabled(model, value, dest_key) {
  if(null == dest_key){
    dest_key = "output";
  }
  var output = model[dest_key];
  var callback = model["callback"];
  if((null != value) && (false != value)){
    output["disabled"] = value;
  }
  else{
    if(output.containsKey("disabled")){
      output.remove("disabled");
    }
  }
  trigger_listeners(model,"model.disabled",output);
  return output;
}

set_pending(model, value, dest_key) {
  if(null == dest_key){
    dest_key = "output";
  }
  var output = model[dest_key];
  if((null != value) && (false != value)){
    output["pending"] = value;
  }
  else{
    if(output.containsKey("pending")){
      output.remove("pending");
    }
  }
  trigger_listeners(model,"model.pending",output);
  return output;
}

set_elapsed(model, value, dest_key) {
  if(null == dest_key){
    dest_key = "output";
  }
  var output = model[dest_key];
  if(("int" == (value.runtimeType).toString()) || ("double" == (value.runtimeType).toString()) || ("num" == (value.runtimeType).toString())){
    output["elapsed"] = value;
  }
  else{
    if(output.containsKey("elapsed")){
      output.remove("elapsed");
    }
  }
  trigger_listeners(model,"model.elapsed",output);
  return output;
}

init_model(model) {
  var input = model["input"];
  var options = model["options"];
  var init = options["init"];
  var data = input["default"]();
  return set_input(model,xtd.obj_assign(<dynamic, dynamic>{"data":data},init));
}

pipeline_prep(model, opts) {
  var pipeline = model["pipeline"];
  var check_args = pipeline["check_args"];
  var check_disabled = pipeline["check_disabled"];
  var context = xtd.obj_assign(model_context(model),opts);
  var disabled = check_disabled(context);
  var args = context["args"];
  if(null == args){
    if(!((null != disabled) && (false != disabled))){
      args = check_args(context);
    }
  }
  if(null == args){
    disabled = true;
  }
  context["args"] = xtd.arrayify(args);
  context["acc"] = <dynamic, dynamic>{"::":"model.run"};
  return <dynamic>[context,disabled];
}

pipeline_set(context, tag, acc, dest_key) {
  var model = context["model"];
  if(null == dest_key){
    dest_key = "output";
  }
  var process = xtd.get_in(model,<dynamic>[dest_key,"process"]);
  var record = acc[tag];
  var should_update = null;
  if(0 < record.length){
    should_update = record[0];
  }
  var current = null;
  if(1 < record.length){
    current = record[1];
  }
  var errored = null;
  if(2 < record.length){
    errored = record[2];
  }
  if(null == current){
    current = xtd.get_in(model,<dynamic>[dest_key,"default"])();
  }
  if((null != should_update) && (false != should_update)){
    var output = current;
    if(!((null != errored) && (false != errored))){
      output = Function.apply((process as Function),<dynamic>[current]);
    }
    set_output(model,output,errored,tag,dest_key,context["meta"]);
  }
  return acc;
}

pipeline_call(context, tag, disabled, async_fn, hook_fn, skip_guard) {
  var identity_hook = (acc, _tag) {
    return acc;
  };
  var identity_wrapper = (handler) {
    return handler;
  };
  if(null == skip_guard){
    skip_guard = <dynamic, dynamic>{};
  }
  if(null == hook_fn){
    hook_fn = identity_hook;
  }
  var acc = context["acc"];
  var args = context["args"];
  var model = context["model"];
  var pipeline = model["pipeline"];
  var stage = pipeline[tag];
  if(null == stage){
    stage = <dynamic, dynamic>{};
  }
  var guard = stage["guard"];
  var handler = stage["handler"];
  var wrapper = stage["wrapper"];
  if(null == wrapper){
    wrapper = identity_wrapper;
  }
  var error_fn = (err) {
    acc[tag] = <dynamic>[true,err,true];
    acc["error"] = true;
    return Function.apply((hook_fn as Function),<dynamic>[acc,tag]);
  };
  var skipped_fn = (res) {
    acc[tag] = <dynamic>[false];
    return Function.apply((hook_fn as Function),<dynamic>[acc,tag]);
  };
  var result_fn = (res) {
    acc[tag] = <dynamic>[true,res];
    return Function.apply((hook_fn as Function),<dynamic>[acc,tag]);
  };
  var handler_fn = null;
  var success_fn = null;
  if(!((null != disabled) && (false != disabled)) && ((handler.runtimeType).toString().contains("Function") || (handler.runtimeType).toString().contains("=>") || (handler).toString().startsWith("Closure")) && ((null == guard) || (() {
    var dart_truthy__41320 = skip_guard[tag];
    return (null != dart_truthy__41320) && (false != dart_truthy__41320);
  })() || (() {
    var dart_truthy__41321 = Function.apply((guard as Function),<dynamic>[context,acc]);
    return (null != dart_truthy__41321) && (false != dart_truthy__41321);
  })())){
    handler_fn = Function.apply((wrapper as Function),<dynamic>[handler]);
    success_fn = result_fn;
  }
  else{
    handler_fn = ((_) {
      return null;
    });
    success_fn = skipped_fn;
  }
  return Function.apply((async_fn as Function),<dynamic>[
    handler_fn,
    context,
    <dynamic, dynamic>{"success":success_fn,"error":error_fn}
  ]);
}

pipeline_run_impl(context, stages, index, async_fn, hook_fn, complete_fn, skip_guard) {
  if(index < stages.length){
    var next_hook = (acc, tag) {
      if((null != hook_fn) && (false != hook_fn)){
        Function.apply((hook_fn as Function),<dynamic>[acc,tag]);
      }
      return pipeline_run_impl(context,stages,index + 1,async_fn,hook_fn,complete_fn,skip_guard);
    };
    return pipeline_call(context,stages[index],false,async_fn,next_hook,skip_guard);
  }
  else{
    return Function.apply((complete_fn as Function),<dynamic>[context]);
  }
}

pipeline_run(context, disabled, async_fn, hook_fn, complete_fn, dest_key) {
  var acc = context["acc"];
  var model = context["model"];
  if(null == dest_key){
    dest_key = "output";
  }
  var dest_tag = dest_key;
  if(dest_key == "output"){
    dest_tag = "main";
  }
  var output = model[dest_key];
  var started = DateTime.now().millisecondsSinceEpoch;
  if(output.containsKey("elapsed")){
    output.remove("elapsed");
  }
  if((null != disabled) && (false != disabled)){
    var disabled_hook = (acc, tag) {
      if((null != hook_fn) && (false != hook_fn)){
        Function.apply((hook_fn as Function),<dynamic>[acc,tag]);
      }
      if((null != complete_fn) && (false != complete_fn)){
        Function.apply((complete_fn as Function),<dynamic>[acc]);
      }
    };
    set_output_disabled(model,true,dest_key);
    return pipeline_call(context,dest_tag,true,async_fn,disabled_hook,null);
  }
  else{
    var run_hook = (acc, tag) {
      if((null != hook_fn) && (false != hook_fn)){
        Function.apply((hook_fn as Function),<dynamic>[acc,tag]);
      }
      if(tag == dest_tag){
        pipeline_set(context,tag,acc,dest_key);
      }
    };
    var run_complete = (acc) {
      if((null != complete_fn) && (false != complete_fn)){
        Function.apply((complete_fn as Function),<dynamic>[acc]);
      }
      set_elapsed(
        model,
        DateTime.now().millisecondsSinceEpoch - started,
        dest_key
      );
      return set_pending(model,false,dest_key);
    };
    if((() {
      var dart_truthy__41319 = output["disabled"];
      return (null != dart_truthy__41319) && (false != dart_truthy__41319);
    })()){
      set_output_disabled(model,false,dest_key);
    }
    set_pending(model,true,dest_key);
    return pipeline_run_impl(
      context,
      <dynamic>["pre",dest_tag,"post"],
      0,
      async_fn,
      run_hook,
      run_complete,
      null
    );
  }
}

pipeline_run_force(context, save_output, async_fn, hook_fn, complete_fn, dest_key) {
  var acc = context["acc"];
  var model = context["model"];
  var started = DateTime.now().millisecondsSinceEpoch;
  var force_hook = (acc, tag) {
    if((null != hook_fn) && (false != hook_fn)){
      Function.apply((hook_fn as Function),<dynamic>[acc,tag]);
    }
    if(tag == dest_key){
      pipeline_set(context,tag,acc,dest_key);
      if((null != save_output) && (false != save_output)){
        pipeline_set(context,tag,acc,"output");
      }
    }
  };
  var force_complete = (acc) {
    if((null != complete_fn) && (false != complete_fn)){
      Function.apply((complete_fn as Function),<dynamic>[acc]);
    }
    set_elapsed(
      model,
      DateTime.now().millisecondsSinceEpoch - started,
      dest_key
    );
    return set_pending(model,false,dest_key);
  };
  set_pending(model,true,dest_key);
  return pipeline_run_impl(
    context,
    <dynamic>["pre",dest_key,"post"],
    0,
    async_fn,
    force_hook,
    force_complete,
    null
  );
}

pipeline_run_remote(context, save_output, async_fn, hook_fn, complete_fn) {
  return pipeline_run_force(context,save_output,async_fn,hook_fn,complete_fn,"remote");
}

pipeline_run_sync(context, save_output, async_fn, hook_fn, complete_fn) {
  return pipeline_run_force(context,save_output,async_fn,hook_fn,complete_fn,"sync");
}

get_with_lookup(results, opts) {
  if(null == opts){
    opts = <dynamic, dynamic>{};
  }
  var key_fn = opts["key_fn"];
  var sort_fn = opts["sort_fn"];
  var val_fn = opts["val_fn"];
  if(null != sort_fn){
    results = Function.apply((sort_fn as Function),<dynamic>[results]);
  }
  if(null == key_fn){
    key_fn = ((e) {
      return e["id"];
    });
  }
  if(null == val_fn){
    val_fn = ((x) {
      return x;
    });
  }
  if(null == results){
    results = <dynamic>[];
  }
  return <dynamic, dynamic>{
    "results":results,
    "lookup":xtd.arr_juxt(results,key_fn,val_fn)
  };
}

sorted_lookup(key) {
  var sort_key = key;
  if(null == sort_key){
    sort_key = "name";
  }
  return (results) {
    return get_with_lookup(results,<dynamic, dynamic>{
      "sort_fn":(arr) {
            return xtd.arr_sort(arr,(e) {
              return e[sort_key];
            },(x, y) {
              return (x).toString().compareTo((y).toString()) < 0;
            });
          }
    });
  };
}

group_by_lookup(key) {
  return (results) {
    return <dynamic, dynamic>{
      "results":results,
      "lookup":xtd.arr_group_by(results,(e) {
            return e[key];
          },(x) {
            return x;
          })
    };
  };
}