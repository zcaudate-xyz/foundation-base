import 'package:xtalk_event/base-listener.dart' as event_common;

import 'package:xtalk_lang/common-data.dart' as xtd;

import 'package:xtalk_event/util-throttle.dart' as th;

import 'package:xtalk_substrate/page-util.dart' as page_util;

import 'package:xtalk_substrate/base-space.dart' as node_space;

import 'package:xtalk_event/base-model.dart' as event_model;

proxy_groupp(group) {
  var remote = group["remote"];
  return (null != remote) && (false != remote);
}

runtime_page(opts) {
  opts = (opts ?? <dynamic, dynamic>{});
  return <dynamic, dynamic>{
    "::":"substrate.page",
    "groups":<dynamic, dynamic>{},
    "meta":opts["meta"] ?? <dynamic, dynamic>{},
    "opts":opts
  };
}

space_get_page(node, space_id) {
  var state = node_space.get_space_state(node,space_id);
  if(("Map" == (state.runtimeType).toString()) || (state.runtimeType).toString().startsWith("_Map") || (state.runtimeType).toString().startsWith("LinkedMap")){
    return state["page"];
  }
}

space_ensure_page(node, space_id) {
  var space = node_space.ensure_space(node,space_id,null);
  var state = space["state"];
  if((null == state) || !(("Map" == (state.runtimeType).toString()) || (state.runtimeType).toString().startsWith("_Map") || (state.runtimeType).toString().startsWith("LinkedMap"))){
    state = <dynamic, dynamic>{};
    space["state"] = state;
  }
  var runtime = state["page"];
  if(!((null != runtime) && (runtime["::"] == "substrate.page"))){
    runtime = runtime_page(null);
    state["page"] = runtime;
  }
  return runtime;
}

space_set_page(node, space_id, runtime) {
  var space = node_space.ensure_space(node,space_id,null);
  var state = space["state"];
  if((null == state) || !(("Map" == (state.runtimeType).toString()) || (state.runtimeType).toString().startsWith("_Map") || (state.runtimeType).toString().startsWith("LinkedMap"))){
    state = <dynamic, dynamic>{};
    space["state"] = state;
  }
  state["page"] = runtime;
  return runtime;
}

group_get(node, space_id, group_id) {
  return xtd.get_in(
    space_ensure_page(node,space_id),
    <dynamic>["groups",group_id]
  );
}

group_ensure(node, space_id, group_id) {
  var group = group_get(node,space_id,group_id);
  if(null == group){
    throw "ERR - Group not found - " + group_id;
  }
  return group;
}

model_ensure(node, space_id, group_id, model_id) {
  var group = group_ensure(node,space_id,group_id);
  var model = (group["models"])[model_id];
  if(null == model){
    throw "ERR - Model not found - " + jsonEncode(<dynamic>[group_id,model_id]);
  }
  return <dynamic>[group,model];
}

model_get_output(node, space_id, group_id, model_id) {
  var value_41805 = model_ensure(node,space_id,group_id,model_id);
  var _group = value_41805[0];
  var model = value_41805[1];
  return xtd.get_in(model,<dynamic>["output","current"]);
}

trigger_listeners(node, space_id, path, event) {
  var view_key = jsonEncode(<dynamic>[space_id,path]);
  return event_common.trigger_keyed_listeners(
    node,
    view_key,
    xtd.obj_assign(<dynamic, dynamic>{"space_id":space_id,"path":path},event)
  );
}

model_prep(node, space_id, group_id, model_id, opts) {
  var path = <dynamic>[group_id,model_id];
  var space = node_space.ensure_space(node,space_id,null);
  var value_41806 = model_ensure(node,space_id,group_id,model_id);
  var group = value_41806[0];
  var model = value_41806[1];
  var value_41807 = event_model.pipeline_prep(model,xtd.obj_assign(
    <dynamic, dynamic>{"path":path,"node":node,"space":space,"group":group},
    opts ?? <dynamic, dynamic>{}
  ));
  var context = value_41807[0];
  var disabled = value_41807[1];
  return <dynamic>[path,context,disabled];
}

model_get_dependents(node, space_id, group_id, model_id) {
  var out = <dynamic, dynamic>{};
  var groups = (space_ensure_page(node,space_id))["groups"];
  for(var entry_41808 in groups.entries){
    var dgroup_id = entry_41808.key;
    var dgroup = entry_41808.value;
    var deps = dgroup["deps"];
    var model_lu = xtd.get_in(deps,<dynamic>[group_id,model_id]);
    if(null != model_lu){
      out[dgroup_id] = List<dynamic>.from(( model_lu ).keys);
    }
  };
  return out;
}

group_get_dependents(node, space_id, group_id) {
  var out = <dynamic, dynamic>{};
  var groups = (space_ensure_page(node,space_id))["groups"];
  for(var entry_41813 in groups.entries){
    var dgroup_id = entry_41813.key;
    var dgroup = entry_41813.value;
    var deps = dgroup["deps"];
    var group_lu = deps[group_id];
    if(null != group_lu){
      out[dgroup_id] = true;
    }
  };
  return out;
}

model_remote_call(node, space_id, group_id, model_id, args, save_output) {
  var value_41814 = model_ensure(node,space_id,group_id,model_id);
  var group = value_41814[0];
  var model = value_41814[1];
  var dispatch_fn = group["proxy_dispatch"];
  if((null != dispatch_fn) && (false != dispatch_fn)){
    return Function.apply((dispatch_fn as Function),<dynamic>[
      "proxy-call",
      node,
      space_id,
      group_id,
      <dynamic>[model_id,args,save_output]
    ]);
  }
  var value_41815 = model_prep(node,space_id,group_id,model_id,<dynamic, dynamic>{"args":args});
  var path = value_41815[0];
  var context = value_41815[1];
  var disabled = value_41815[2];
  return page_util.run_remote(context,save_output,path,null);
}

model_refresh(node, space_id, group_id, model_id, event, refresh_deps_fn) {
  var value_41816 = model_ensure(node,space_id,group_id,model_id);
  var group = value_41816[0];
  var model = value_41816[1];
  var dispatch_fn = group["proxy_dispatch"];
  if((null != dispatch_fn) && (false != dispatch_fn)){
    return Function.apply((dispatch_fn as Function),<dynamic>[
      "model-update",
      node,
      space_id,
      group_id,
      <dynamic>[model_id,event ?? <dynamic, dynamic>{}]
    ]);
  }
  var value_41817 = model_prep(
    node,
    space_id,
    group_id,
    model_id,
    <dynamic, dynamic>{"event":event}
  );
  var path = value_41817[0];
  var context = value_41817[1];
  var disabled = value_41817[2];
  return page_util.run_refresh(context,disabled,path,refresh_deps_fn);
}

model_refresh_remote(node, space_id, group_id, model_id, refresh_deps_fn) {
  var value_41818 = model_prep(node,space_id,group_id,model_id,<dynamic, dynamic>{});
  var path = value_41818[0];
  var context = value_41818[1];
  var disabled = value_41818[2];
  return page_util.run_remote(context,true,path,refresh_deps_fn);
}

model_refresh_dependents(node, space_id, group_id, model_id) {
  var dependents = model_get_dependents(node,space_id,group_id,model_id);
  for(var entry_41819 in dependents.entries){
    var dgroup_id = entry_41819.key;
    var dmodel_ids = entry_41819.value;
    var throttle = (group_ensure(node,space_id,dgroup_id))["throttle"];
    var arr_41820 = dmodel_ids;
    for(var i41821 = 0; i41821 < arr_41820.length; ++i41821){
      var dmodel_id = arr_41820[i41821];
      th.throttle_run(throttle,dmodel_id,<dynamic>[<dynamic, dynamic>{}]);
    };
  };
  return dependents;
}

model_refresh_dependents_unthrottled(node, space_id, group_id, model_id, refresh_deps_fn) {
  var dependents = model_get_dependents(node,space_id,group_id,model_id);
  var out = <dynamic>[];
  for(var entry_41842 in dependents.entries){
    var dgroup_id = entry_41842.key;
    var dmodel_ids = entry_41842.value;
    var arr_41843 = dmodel_ids;
    for(var i41844 = 0; i41844 < arr_41843.length; ++i41844){
      var dmodel_id = arr_41843[i41844];
      out.add(model_refresh(
        node,
        space_id,
        dgroup_id,
        dmodel_id,
        <dynamic, dynamic>{},
        refresh_deps_fn
      ));
    };
  };
  return Future.wait(List<Future<dynamic>>.from(( out ).map((entry) => Future.sync(() => entry))));
}

group_refresh(node, space_id, group_id, event, refresh_deps_fn) {
  var group = group_ensure(node,space_id,group_id);
  var running = <dynamic>[];
  for(var entry_41867 in group["models"].entries){
    var model_id = entry_41867.key;
    var model = entry_41867.value;
    var value_41868 = model_prep(
      node,
      space_id,
      group_id,
      model_id,
      <dynamic, dynamic>{"event":event}
    );
    var path = value_41868[0];
    var context = value_41868[1];
    var disabled = value_41868[2];
    running.add(
      page_util.run_refresh(context,disabled,path,refresh_deps_fn)
    );
  };
  return Future.wait(List<Future<dynamic>>.from(( running ).map((entry) => Future.sync(() => entry))));
}

get_unknown_deps(node, space_id, group_id, models, group_deps) {
  var out = <dynamic>[];
  for(var entry_41871 in group_deps.entries){
    var linked_group_id = entry_41871.key;
    var linked_models = entry_41871.value;
    if(group_id == linked_group_id){
      for(var linked_model_id in linked_models.keys){
        if(null == models[linked_model_id]){
          out.add(<dynamic>[linked_group_id,linked_model_id]);
        }
      };
    }
    else{
      var linked_group = group_get(node,space_id,linked_group_id);
      for(var linked_model_id in linked_models.keys){
        if((null == linked_group) || (null == (linked_group["models"])[linked_model_id])){
          out.add(<dynamic>[linked_group_id,linked_model_id]);
        }
      };
    }
  };
  return xtd.arr_sort(out,(pair) {
    return jsonEncode(pair);
  },(x, y) {
    return (x).toString().compareTo((y).toString()) < 0;
  });
}

create_throttle(node, space_id, group_id, refresh_deps_fn) {
  return th.throttle_create((model_id, event) {
    return (() async { try { return await ((Future.sync(() => model_refresh(node,space_id,group_id,model_id,event,refresh_deps_fn))) as Future<dynamic>); } catch (err) { return await Future.sync(() => Function.apply((err) {
      return err;
    },<dynamic>[err])); } })();
  },() {
    return DateTime.now().millisecondsSinceEpoch;
  });
}

create_model(node, space_id, group_id, model_id, opts) {
  var handler = opts["handler"];
  var pipeline = opts["pipeline"];
  var defaults = opts["defaults"];
  var options = opts["options"];
  var model = event_model.create_model(null,xtd.obj_assign_nested(<dynamic, dynamic>{
    "main":<dynamic, dynamic>{"handler":handler,"wrapper":page_util.wrap_space_args},
    "remote":<dynamic, dynamic>{"wrapper":page_util.wrap_space_args},
    "sync":<dynamic, dynamic>{"wrapper":page_util.wrap_space_args}
  },pipeline),defaults["args"],defaults["output"],defaults["process"],options);
  event_model.init_model(model);
  event_model.add_listener(model,"@/page",(_id, data, _t, meta) {
    var emitted = xtd.obj_assign(<dynamic, dynamic>{},data);
    emitted["meta"] = meta;
    return trigger_listeners(node,space_id,<dynamic>[group_id,model_id],emitted);
  },null,null);
  return model;
}

group_add_attach(node, space_id, group_id, models) {
  var runtime = space_ensure_page(node,space_id);
  var groups = runtime["groups"];
  var group = groups[group_id];
  if(null == group){
    group = <dynamic, dynamic>{
      "name":group_id,
      "models":<dynamic, dynamic>{},
      "specs":<dynamic, dynamic>{},
      "throttle":create_throttle(node,space_id,group_id,model_refresh_dependents_unthrottled),
      "deps":<dynamic, dynamic>{}
    };
    groups[group_id] = group;
  }
  var group_models = group["models"];
  var group_specs = group["specs"];
  if(null == group_models){
    group_models = <dynamic, dynamic>{};
    group["models"] = group_models;
  }
  if(null == group_specs){
    group_specs = <dynamic, dynamic>{};
    group["specs"] = group_specs;
  }
  xtd.obj_assign(group_specs,models);
  for(var entry_41872 in models.entries){
    var model_id = entry_41872.key;
    var model = entry_41872.value;
    group_models[model_id] = create_model(node,space_id,group_id,model_id,model);
  };
  group["deps"] = page_util.get_group_deps(group_id,group_specs);
  return group;
}

group_add(node, space_id, group_id, models) {
  var group = group_add_attach(node,space_id,group_id,models);
  group["init"] = group_refresh(node,space_id,group_id,<dynamic, dynamic>{},null);
  return group;
}

group_remove(node, space_id, group_id) {
  var runtime = space_ensure_page(node,space_id);
  var groups = runtime["groups"];
  var dependents = group_get_dependents(node,space_id,group_id);
  if(List<dynamic>.from(( dependents ).keys).length > 0){
    throw "ERR - existing group dependents - " + jsonEncode(dependents);
  }
  var curr = groups[group_id];
  groups.remove(group_id);
  return curr;
}

model_remove(node, space_id, group_id, model_id) {
  var dependents = model_get_dependents(node,space_id,group_id,model_id);
  if(List<dynamic>.from(( dependents ).keys).length > 0){
    throw "ERR - existing model dependents - " + jsonEncode(dependents);
  }
  var group = group_get(node,space_id,group_id);
  if((null != group) && (false != group)){
    var models = group["models"];
    var curr = models[model_id];
    models.remove(model_id);
    return curr;
  }
}

group_update(node, space_id, group_id, event) {
  var group = group_ensure(node,space_id,group_id);
  var dispatch_fn = group["proxy_dispatch"];
  if((null != dispatch_fn) && (false != dispatch_fn)){
    return Function.apply(
      (dispatch_fn as Function),
      <dynamic>["group-update",node,space_id,group_id,<dynamic>[event]]
    );
  }
  var throttle = group["throttle"];
  var models = group["models"];
  var out = <dynamic>[];
  for(var model_id in models.keys){
    var entry = th.throttle_run(throttle,model_id,<dynamic>[event ?? <dynamic, dynamic>{}]);
    out.add(<dynamic>[model_id,entry["promise"]]);
  };
  return ((Future.sync(() => Future.wait(List<Future<dynamic>>.from(( xtd.arr_map(out,(arr) {
    return arr[1];
  }) ).map((entry) => Future.sync(() => entry)))))) as Future<dynamic>).then((value) async { return await Function.apply((arr) {
    return xtd.arr_zip(xtd.arr_map(out,(arr) {
      return arr[0];
    }),arr);
  },<dynamic>[value]); });
}

model_update(node, space_id, group_id, model_id, event) {
  var value_41883 = model_ensure(node,space_id,group_id,model_id);
  var group = value_41883[0];
  var model = value_41883[1];
  var dispatch_fn = group["proxy_dispatch"];
  if((null != dispatch_fn) && (false != dispatch_fn)){
    return Function.apply(
      (dispatch_fn as Function),
      <dynamic>["model-update",node,space_id,group_id,<dynamic>[model_id,event]]
    );
  }
  var throttle = group["throttle"];
  var entry = th.throttle_run(throttle,model_id,<dynamic>[event ?? <dynamic, dynamic>{}]);
  return entry["promise"];
}

model_set_input(node, space_id, group_id, model_id, current, event) {
  var value_41884 = model_ensure(node,space_id,group_id,model_id);
  var group = value_41884[0];
  var model = value_41884[1];
  var dispatch_fn = group["proxy_dispatch"];
  if((null != dispatch_fn) && (false != dispatch_fn)){
    return Function.apply((dispatch_fn as Function),<dynamic>[
      "model-set-input",
      node,
      space_id,
      group_id,
      <dynamic>[model_id,current,event]
    ]);
  }
  event_model.set_input(model,current);
  return model_update(node,space_id,group_id,model_id,event ?? <dynamic, dynamic>{});
}

group_trigger_raw(node, space_id, group, signal, event) {
  var models = group["models"];
  var out = <dynamic>[];
  for(var entry_41885 in models.entries){
    var model_id = entry_41885.key;
    var model = entry_41885.value;
    var options = model["options"];
    var trigger = options["trigger"];
    var check = page_util.check_event(
      trigger,
      signal,
      event,
      <dynamic, dynamic>{"model":model,"group":group,"node":node,"space_id":space_id}
    );
    if((null != check) && (false != check)){
      th.throttle_run(group["throttle"],model_id,<dynamic>[event]);
      out.add(model_id);
    }
  };
  return xtd.arr_sort(out,(model_id) {
    return model_id;
  },(x, y) {
    return (x).toString().compareTo((y).toString()) < 0;
  });
}

group_trigger(node, space_id, group_id, signal, event) {
  var group = group_ensure(node,space_id,group_id);
  var dispatch_fn = group["proxy_dispatch"];
  if((null != dispatch_fn) && (false != dispatch_fn)){
    return Function.apply(
      (dispatch_fn as Function),
      <dynamic>["trigger-group",node,space_id,group_id,<dynamic>[signal,event]]
    );
  }
  return group_trigger_raw(node,space_id,group,signal,event);
}

model_trigger(node, space_id, group_id, model_id, signal, event) {
  var value_41886 = model_ensure(node,space_id,group_id,model_id);
  var group = value_41886[0];
  var model = value_41886[1];
  var dispatch_fn = group["proxy_dispatch"];
  if((null != dispatch_fn) && (false != dispatch_fn)){
    return Function.apply((dispatch_fn as Function),<dynamic>[
      "trigger-model",
      node,
      space_id,
      group_id,
      <dynamic>[model_id,signal,event]
    ]);
  }
  var options = model["options"];
  var trigger = options["trigger"];
  if((() {
    var dart_truthy__41804 = page_util.check_event(
      trigger,
      signal,
      event,
      <dynamic, dynamic>{"model":model,"group":group,"node":node,"space_id":space_id}
    );
    return (null != dart_truthy__41804) && (false != dart_truthy__41804);
  })()){
    var entry = th.throttle_run(group["throttle"],model_id,<dynamic>[event]);
    return entry["promise"];
  }
  return null;
}

space_trigger_all(node, space_id, signal, event) {
  var groups = (space_ensure_page(node,space_id))["groups"];
  var out = <dynamic, dynamic>{};
  for(var entry_41887 in groups.entries){
    var group_id = entry_41887.key;
    var group = entry_41887.value;
    var dispatch_fn = group["proxy_dispatch"];
    out[group_id] = (((null != dispatch_fn) && (false != dispatch_fn)) ? Function.apply(
      (dispatch_fn as Function),
      <dynamic>["trigger-group",node,space_id,group_id,<dynamic>[signal,event]]
    ) : group_trigger_raw(node,space_id,group,signal,event));
  };
  return out;
}

raw_callback_add(node, space_id) {
  var trigger_id = page_util.raw_callback_id(space_id);
  return page_util.register_page_trigger(node,trigger_id,(_space, frame, local_node) {
    return space_trigger_all(local_node,space_id,frame["signal"],frame);
  },<dynamic, dynamic>{"space_id":space_id});
}

raw_callback_remove(node, space_id) {
  return page_util.unregister_page_trigger(node,page_util.raw_callback_id(space_id));
}