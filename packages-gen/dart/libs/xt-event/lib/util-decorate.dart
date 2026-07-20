import 'package:xtalk_lang/common-data.dart' as xtd;
import 'dart:async';


incr_fn() {
  var state = <dynamic, dynamic>{"value":-1};
  var next_id_fn = () {
    var i = 1 + state["value"];
    state["value"] = i;
    return "id-" + (i).toString();
  };
  return next_id_fn;
}

plugin_timing(handle) {
  var result = <dynamic, dynamic>{"output":<dynamic, dynamic>{}};
  var on_setup = (args) {
    return result["output"] = <dynamic, dynamic>{"start":DateTime.now().millisecondsSinceEpoch};
  };
  var on_teardown = () {
    var output = result["output"];
    var t_end = DateTime.now().millisecondsSinceEpoch;
    var t_elapsed = t_end - output["start"];
    output["end"] = t_end;
    return output["elapsed"] = t_elapsed;
  };
  var on_reset = () {
    return result["output"] = <dynamic, dynamic>{};
  };
  return xtd.obj_assign(result,<dynamic, dynamic>{
    "name":"timing",
    "on_setup":on_setup,
    "on_teardown":on_teardown,
    "on_reset":on_reset
  });
}

plugin_counts(handle) {
  var result = <dynamic, dynamic>{"output":<dynamic, dynamic>{"success":0,"error":0}};
  var on_success = (ret) {
    var output = result["output"];
    return output["success"] = (1 + output["success"]);
  };
  var on_error = (ret) {
    var output = result["output"];
    return output["error"] = (1 + output["error"]);
  };
  var on_reset = () {
    return result["output"] = <dynamic, dynamic>{"success":0,"error":0};
  };
  return xtd.obj_assign(result,<dynamic, dynamic>{
    "name":"counts",
    "on_success":on_success,
    "on_error":on_error,
    "on_reset":on_reset
  });
}

to_handle_callback(cb) {
  if(null == cb){
    cb = <dynamic, dynamic>{};
  }
  return <dynamic, dynamic>{
    "on_success":cb["success"],
    "on_error":cb["error"],
    "on_teardown":cb["finally"]
  };
}

new_handle(handler, plugin_fns, opts) {
  var create_fn = opts["create_fn"];
  var delay = opts["delay"];
  var dump_fn = opts["dump_fn"];
  var id_fn = opts["id_fn"];
  var name = opts["name"];
  var wrap_fn = opts["wrap_fn"];
  if(null == id_fn){
    id_fn = Function.apply((incr_fn as Function),<dynamic>[]);
  }
  if(null == create_fn){
    create_fn = ((x) {
      return x;
    });
  }
  if(null == delay){
    delay = 0;
  }
  var handle = Function.apply((create_fn as Function),<dynamic>[
    <dynamic, dynamic>{
      "::":"handle",
      "name":name,
      "id_fn":id_fn,
      "wrap_fn":wrap_fn,
      "handler":handler,
      "delay":delay
    }
  ]);
  var plugins = xtd.arr_map(plugin_fns,(f) {
    return f(handle);
  });
  handle["plugins"] = plugins;
  return handle;
}

run_handle(handle, args, tcb) {
  var delay = handle["delay"];
  var handler = handle["handler"];
  var id_fn = handle["id_fn"];
  var plugins = handle["plugins"];
  var wrap_fn = handle["wrap_fn"];
  if(null == args){
    args = <dynamic>[];
  }
  var tcbs = xtd.arr_clone(plugins);
  if(null != tcb){
    if((tcb.runtimeType).toString().startsWith("List") || (tcb.runtimeType).toString().startsWith("_GrowableList")){
      xtd.arr_assign(tcbs,tcb);
    }
    else{
      tcbs.add(tcb);
    }
  }
  if((delay.runtimeType).toString().contains("Function") || (delay.runtimeType).toString().contains("=>") || (delay).toString().startsWith("Closure")){
    delay = delay();
  }
  var receipt = <dynamic, dynamic>{"id":Function.apply((id_fn as Function),<dynamic>[])};
  var teardown_fn = () {
    var arr_50915 = tcbs;
    for(var i50916 = 0; i50916 < arr_50915.length; ++i50916){
      var cb = arr_50915[i50916];
      var on_teardown = cb["on_teardown"];
      var name = cb["name"];
      var output = cb["output"];
      if(null != on_teardown){
        on_teardown();
      }
      if((null != name) && (null != output)){
        receipt[name] = output;
      }
    };
    return receipt;
  };
  var run_fn = () {
    var arr_50937 = tcbs;
    for(var i50938 = 0; i50938 < arr_50937.length; ++i50938){
      var cb = arr_50937[i50938];
      var on_setup = cb["on_setup"];
      if(null != on_setup){
        on_setup(args);
      }
    };
    var base_thunk = () {
      return Function.apply(handler,args);
    };
    var base_promise = (0 < ((null == delay) ? 0 : delay)) ? Future.delayed(Duration(milliseconds:  delay )).then((_) {
      return Future.sync(() {
        return Function.apply(base_thunk,<dynamic>[]);
      });
    }) : Future.sync(base_thunk);
    return ((Future.sync(() => (() async { try { return await ((Future.sync(() => ((Future.sync(() => base_promise)) as Future<dynamic>).then((value) async { return await Function.apply((ret) {
      var arr_50967 = tcbs;
      for(var i50968 = 0; i50968 < arr_50967.length; ++i50968){
        var cb = arr_50967[i50968];
        var on_success = cb["on_success"];
        if(null != on_success){
          on_success(ret);
        }
      };
      receipt["status"] = "success";
      receipt["value"] = ret;
      return receipt;
    },<dynamic>[value]); }))) as Future<dynamic>); } catch (err) { return await Future.sync(() => Function.apply((err) {
      var arr_50989 = tcbs;
      for(var i50990 = 0; i50990 < arr_50989.length; ++i50990){
        var cb = arr_50989[i50990];
        var on_error = cb["on_error"];
        if(null != on_error){
          on_error(err);
        }
      };
      receipt["status"] = "error";
      receipt["error"] = err;
      var reject_fn = () {
        throw receipt;
      };
      return Future.sync(reject_fn);
    },<dynamic>[err])); } })())) as Future<dynamic>).whenComplete(() async { await Function.apply(teardown_fn,<dynamic>[]); });
  };
  var proc = (null != wrap_fn) ? Function.apply((wrap_fn as Function),<dynamic>[run_fn,args,receipt,handle]) : Function.apply((run_fn as Function),<dynamic>[]);
  return Future.sync(() {
    return proc;
  });
}