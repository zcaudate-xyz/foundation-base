import 'package:xtalk_lang/common-data.dart' as xtd;
import 'package:xtalk_lang/common-sort-topo.dart' as xtst;
import 'dart:async';



new_task(id, deps, args, opts) {
  return xtd.obj_assign(
    <dynamic, dynamic>{"::":"loader.task","id":id,"deps":deps,"args":args},
    opts
  );
}

task_load(task) {
  var args = task["args"];
  var assert_fn = task["assert_fn"];
  var check_fn = task["check_fn"];
  var get_fn = task["get_fn"];
  var id = task["id"];
  var load_fn = task["load_fn"];
  var load_no_check = task["load_no_check"];
  if((assert_fn.runtimeType).toString().contains("Function") || (assert_fn.runtimeType).toString().contains("=>") || (assert_fn).toString().startsWith("Closure")){
    if(!(() {
      var dart_truthy__50840 = Function.apply((assert_fn as Function),<dynamic>[]);
      return (null != dart_truthy__50840) && (false != dart_truthy__50840);
    })()){
      throw "Assertion Failed - " + id;
    }
  }
  if(true != load_no_check){
    var curr = ((get_fn.runtimeType).toString().contains("Function") || (get_fn.runtimeType).toString().contains("=>") || (get_fn).toString().startsWith("Closure")) ? Function.apply((get_fn as Function),<dynamic>[]) : null;
    var check = ((check_fn.runtimeType).toString().contains("Function") || (check_fn.runtimeType).toString().contains("=>") || (check_fn).toString().startsWith("Closure")) ? Function.apply((check_fn as Function),<dynamic>[curr]) : null;
    if(true == check){
      return Future.sync(() {
        return curr;
      });
    }
  }
  if((args.runtimeType).toString().contains("Function") || (args.runtimeType).toString().contains("=>") || (args).toString().startsWith("Closure")){
    args = args();
  }
  args = ((null == args) ? <dynamic>[] : args);
  return Future.sync(() {
    return Function.apply(load_fn,args);
  });
}

task_unload(task) {
  var check_fn = task["check_fn"];
  var get_fn = task["get_fn"];
  var unload_fn = task["unload_fn"];
  var unload_no_check = task["unload_no_check"];
  if(true != unload_no_check){
    var curr = ((get_fn.runtimeType).toString().contains("Function") || (get_fn.runtimeType).toString().contains("=>") || (get_fn).toString().startsWith("Closure")) ? Function.apply((get_fn as Function),<dynamic>[]) : null;
    var check = ((check_fn.runtimeType).toString().contains("Function") || (check_fn.runtimeType).toString().contains("=>") || (check_fn).toString().startsWith("Closure")) ? Function.apply((check_fn as Function),<dynamic>[curr]) : null;
    if(true != check){
      return Future.sync(() {
        return false;
      });
    }
  }
  return ((Future.sync(() => Future.sync(() {
    return Function.apply((unload_fn as Function),<dynamic>[]);
  }))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
    return true;
  },<dynamic>[value]); });
}

unload_tasks_loop(tasks, completed, ids, out, hook_fn) {
  if(0 == ids.length){
    return Future.sync(() {
      return out;
    });
  }
  var id = ids[0];
  var rest = ids.sublist(1 - 0,ids.length);
  if(true != completed[id]){
    return unload_tasks_loop(tasks,completed,rest,out,hook_fn);
  }
  completed.remove(id);
  var task = tasks[id];
  return ((Future.sync(() => task_unload(task))) as Future<dynamic>).then((value) async { return await Function.apply((unloaded) {
    if(null != hook_fn){
      Function.apply((hook_fn as Function),<dynamic>[id,unloaded]);
    }
    out.add(<dynamic>[id,unloaded]);
    return unload_tasks_loop(tasks,completed,rest,out,hook_fn);
  },<dynamic>[value]); });
}

new_loader_blank() {
  return <dynamic, dynamic>{
    "::":"loader",
    "completed":<dynamic, dynamic>{},
    "loading":<dynamic, dynamic>{},
    "errored":null,
    "order":<dynamic>[],
    "tasks":<dynamic, dynamic>{}
  };
}

add_tasks(loader, tasks) {
  var prev = loader["tasks"];
  var all = xtd.arr_assign(List<dynamic>.from(( prev ).values),tasks);
  var deps = xtd.arr_map(all,(e) {
    return <dynamic>[e["id"],e["deps"]];
  });
  return xtd.obj_assign(loader,<dynamic, dynamic>{
    "order":xtst.sort_topo(deps),
    "tasks":xtd.arr_juxt(all,(e) {
        return e["id"];
      },xtd.clone_nested)
  });
}

new_loader(tasks) {
  return add_tasks(new_loader_blank(),tasks);
}

list_loading(loader) {
  return List<dynamic>.from(( loader["loading"] ).keys);
}

list_completed(loader) {
  return List<dynamic>.from(( loader["completed"] ).keys);
}

list_incomplete(loader) {
  var completed = loader["completed"];
  var order = loader["order"];
  var tasks = loader["tasks"];
  var out = <dynamic>[];
  var arr_50847 = order;
  for(var i50848 = 0; i50848 < arr_50847.length; ++i50848){
    var id = arr_50847[i50848];
    if((null != tasks[id]) && (true != completed[id])){
      out.add(id);
    }
  };
  return out;
}

list_waiting(loader) {
  var completed = loader["completed"];
  var loading = loader["loading"];
  var order = loader["order"];
  var tasks = loader["tasks"];
  var out = <dynamic>[];
  var arr_50869 = order;
  for(var i50870 = 0; i50870 < arr_50869.length; ++i50870){
    var id = arr_50869[i50870];
    var task = tasks[id];
    if((null != task) && (true != loading[id]) && (true != completed[id]) && (() {
      var dart_truthy__50839 = xtd.arr_every(task["deps"],(dep_id) {
        return true == completed[dep_id];
      });
      return (null != dart_truthy__50839) && (false != dart_truthy__50839);
    })()){
      out.add(id);
    }
  };
  return out;
}

load_tasks_single(loader, id, hook_fn, complete_fn, loop_fn) {
  var completed = loader["completed"];
  var loading = loader["loading"];
  var tasks = loader["tasks"];
  var task = tasks[id];
  loading[id] = true;
  return ((Future.sync(() => (() async { try { return await ((Future.sync(() => task_load(task))) as Future<dynamic>); } catch (err) { return await Future.sync(() => Function.apply((err) {
    loading.remove(id);
    loader["errored"] = id;
    if(null != hook_fn){
      Function.apply((hook_fn as Function),<dynamic>[id,false]);
    }
    if(null != complete_fn){
      Function.apply((complete_fn as Function),<dynamic>[err]);
    }
    throw err;
  },<dynamic>[err])); } })())) as Future<dynamic>).then((value) async { return await Function.apply((res) {
    loading.remove(id);
    completed[id] = true;
    if(null != hook_fn){
      Function.apply((hook_fn as Function),<dynamic>[id,true]);
    }
    return (null != loop_fn) ? Function.apply((loop_fn as Function),<dynamic>[loader,hook_fn,complete_fn]) : res;
  },<dynamic>[value]); });
}

load_tasks(loader, hook_fn, complete_fn) {
  var errored = loader["errored"];
  var order = loader["order"];
  if(null != errored){
    var reject_fn = () {
      throw "ERR - Task Errored - " + errored;
    };
    return Future.sync(reject_fn);
  }
  var waiting = list_waiting(loader);
  if(0 < waiting.length){
    var waiting_lu = xtd.arr_juxt(waiting,(id) {
      return id;
    },(id) {
      return true;
    });
    var next_id =     (xtd.arr_keep(order,(id) {
          if(true == waiting_lu[id]){
            return id;
          }
        }))[0];
    return load_tasks_single(loader,next_id,hook_fn,complete_fn,load_tasks);
  }
  var incomplete = list_incomplete(loader);
  if(0 == incomplete.length){
    if(null != complete_fn){
      Function.apply((complete_fn as Function),<dynamic>[true]);
    }
    return Future.sync(() {
      return loader;
    });
  }
  return Future.sync(() {
    return loader;
  });
}

unload_tasks(loader, hook_fn) {
  var completed = loader["completed"];
  var order = loader["order"];
  var tasks = loader["tasks"];
  var rorder = xtd.arr_reverse(order);
  return unload_tasks_loop(tasks,completed,rorder,<dynamic>[],hook_fn);
}