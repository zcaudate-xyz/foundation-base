import * as event_common from '../event/base-listener.mjs'

import * as xtd from '../lang/common-data.mjs'

import * as th from '../event/util-throttle.mjs'

import * as page_util from './page-util.mjs'

import * as node_space from './base-space.mjs'

import * as event_model from '../event/base-model.mjs'

export function proxy_groupp(group){
  let {remote} = group;
  return (null != remote) && (false != remote);
}

export function runtime_page(opts){
  opts = (opts || {});
  return {
    "::":"substrate.page",
    "groups":{},
    "meta":opts["meta"] || {},
    "opts":opts
  };
}

export function space_get_page(node,space_id){
  let state = node_space.get_space_state(node,space_id);
  if((null != state) && ("object" == (typeof state)) && !Array.isArray(state)){
    return state["page"];
  }
}

export function space_ensure_page(node,space_id){
  let space = node_space.ensure_space(node,space_id,null);
  let {state} = space;
  if((null == state) || !((null != state) && ("object" == (typeof state)) && !Array.isArray(state))){
    state = {};
    space["state"] = state;
  }
  let runtime = state["page"];
  if(!((null != runtime) && (runtime["::"] == "substrate.page"))){
    runtime = runtime_page(null);
    state["page"] = runtime;
  }
  return runtime;
}

export function space_set_page(node,space_id,runtime){
  let space = node_space.ensure_space(node,space_id,null);
  let {state} = space;
  if((null == state) || !((null != state) && ("object" == (typeof state)) && !Array.isArray(state))){
    state = {};
    space["state"] = state;
  }
  state["page"] = runtime;
  return runtime;
}

export function group_get(node,space_id,group_id){
  return xtd.get_in(space_ensure_page(node,space_id),["groups",group_id]);
}

export function group_ensure(node,space_id,group_id){
  let group = group_get(node,space_id,group_id);
  if(null == group){
    throw "ERR - Group not found - " + group_id;
  }
  return group;
}

export function model_ensure(node,space_id,group_id,model_id){
  let group = group_ensure(node,space_id,group_id);
  let model = (group["models"])[model_id];
  if(null == model){
    throw "ERR - Model not found - " + JSON.stringify([group_id,model_id]);
  }
  return [group,model];
}

export function model_get_output(node,space_id,group_id,model_id){
  let model_value = model_ensure(node,space_id,group_id,model_id);
  let [_group,model] = model_value;
  return xtd.get_in(model,["output","current"]);
}

export function trigger_listeners(node,space_id,path,event){
  let view_key = JSON.stringify([space_id,path]);
  return event_common.trigger_keyed_listeners(
    node,
    view_key,
    Object.assign({"space_id":space_id,"path":path},event)
  );
}

export function model_prep(node,space_id,group_id,model_id,opts){
  let path = [group_id,model_id];
  let space = node_space.ensure_space(node,space_id,null);
  let group_value = model_ensure(node,space_id,group_id,model_id);
  let [group,model] = group_value;
  let context_value = event_model.pipeline_prep(model,Object.assign(
    {"path":path,"node":node,"space":space,"group":group},
    opts || {}
  ));
  let [context,disabled] = context_value;
  return [path,context,disabled];
}

export function model_get_dependents(node,space_id,group_id,model_id){
  let out = {};
  let groups = space_ensure_page(node,space_id)["groups"];
  for(let [dgroup_id,dgroup] of Object.entries(groups)){
    let {deps} = dgroup;
    let model_lu = xtd.get_in(deps,[group_id,model_id]);
    if(null != model_lu){
      out[dgroup_id] = Object.keys(model_lu);
    }
  };
  return out;
}

export function group_get_dependents(node,space_id,group_id){
  let out = {};
  let groups = space_ensure_page(node,space_id)["groups"];
  for(let [dgroup_id,dgroup] of Object.entries(groups)){
    let {deps} = dgroup;
    let group_lu = deps[group_id];
    if(null != group_lu){
      out[dgroup_id] = true;
    }
  };
  return out;
}

export function model_remote_call(node,space_id,group_id,model_id,args,save_output){
  let group_value = model_ensure(node,space_id,group_id,model_id);
  let [group,model] = group_value;
  let dispatch_fn = group["proxy_dispatch"];
  if(dispatch_fn){
    return dispatch_fn("proxy-call",node,space_id,group_id,[model_id,args,save_output]);
  }
  let path_value = model_prep(node,space_id,group_id,model_id,{"args":args});
  let [path,context,disabled] = path_value;
  return page_util.run_remote(context,save_output,path,null);
}

export function model_refresh(node,space_id,group_id,model_id,event,refresh_deps_fn){
  let group_value = model_ensure(node,space_id,group_id,model_id);
  let [group,model] = group_value;
  let dispatch_fn = group["proxy_dispatch"];
  if(dispatch_fn){
    return dispatch_fn("model-update",node,space_id,group_id,[model_id,event || {}]);
  }
  let path_value = model_prep(node,space_id,group_id,model_id,{"event":event});
  let [path,context,disabled] = path_value;
  return page_util.run_refresh(context,disabled,path,refresh_deps_fn);
}

export function model_refresh_remote(node,space_id,group_id,model_id,refresh_deps_fn){
  let path_value = model_prep(node,space_id,group_id,model_id,{});
  let [path,context,disabled] = path_value;
  return page_util.run_remote(context,true,path,refresh_deps_fn);
}

export function model_refresh_dependents(node,space_id,group_id,model_id){
  let dependents = model_get_dependents(node,space_id,group_id,model_id);
  for(let [dgroup_id,dmodel_ids] of Object.entries(dependents)){
    let throttle = group_ensure(node,space_id,dgroup_id)["throttle"];
    for(let dmodel_id of dmodel_ids){
      th.throttle_run(throttle,dmodel_id,[{}]);
    };
  };
  return dependents;
}

export function model_refresh_dependents_unthrottled(node,space_id,group_id,model_id,refresh_deps_fn){
  let dependents = model_get_dependents(node,space_id,group_id,model_id);
  let out = [];
  for(let [dgroup_id,dmodel_ids] of Object.entries(dependents)){
    for(let dmodel_id of dmodel_ids){
      out.push(
        model_refresh(node,space_id,dgroup_id,dmodel_id,{},refresh_deps_fn)
      );
    };
  };
  return Promise.all(out);
}

export function group_refresh(node,space_id,group_id,event,refresh_deps_fn){
  let group = group_ensure(node,space_id,group_id);
  let running = [];
  for(let [model_id,model] of Object.entries(group["models"])){
    let path_value = model_prep(node,space_id,group_id,model_id,{"event":event});
    let [path,context,disabled] = path_value;
    running.push(
      page_util.run_refresh(context,disabled,path,refresh_deps_fn)
    );
  };
  return Promise.all(running);
}

export function get_unknown_deps(node,space_id,group_id,models,group_deps){
  let out = [];
  for(let [linked_group_id,linked_models] of Object.entries(group_deps)){
    if(group_id == linked_group_id){
      for(let linked_model_id of Object.keys(linked_models)){
        if(null == models[linked_model_id]){
          out.push([linked_group_id,linked_model_id]);
        }
      };
    }
    else{
      let linked_group = group_get(node,space_id,linked_group_id);
      for(let linked_model_id of Object.keys(linked_models)){
        if((null == linked_group) || (null == (linked_group["models"])[linked_model_id])){
          out.push([linked_group_id,linked_model_id]);
        }
      };
    }
  };
  return xtd.arr_sort(out,function (pair){
    return JSON.stringify(pair);
  },function (x,y){
    return 0 > x.localeCompare(y);
  });
}

export function create_throttle(node,space_id,group_id,refresh_deps_fn){
  return th.throttle_create(function (model_id,event){
    return model_refresh(node,space_id,group_id,model_id,event,refresh_deps_fn).catch(function (err){
      return err;
    });
  },function (){
    return Date.now();
  });
}

export function create_model(node,space_id,group_id,model_id,opts){
  let {defaults,handler,options,pipeline} = opts;
  let model = event_model.create_model(null,xtd.obj_assign_nested({
    "main":{"handler":handler,"wrapper":page_util.wrap_space_args},
    "remote":{"wrapper":page_util.wrap_space_args},
    "sync":{"wrapper":page_util.wrap_space_args}
  },pipeline),defaults["args"],defaults["output"],defaults["process"],options);
  event_model.init_model(model);
  event_model.add_listener(model,"@/page",function (_id,data,_t,meta){
    let emitted = Object.assign({},data);
    emitted["meta"] = meta;
    return trigger_listeners(node,space_id,[group_id,model_id],emitted);
  },null,null);
  return model;
}

export function group_add_attach(node,space_id,group_id,models){
  let runtime = space_ensure_page(node,space_id);
  let {groups} = runtime;
  let group = groups[group_id];
  if(null == group){
    group = {
      "name":group_id,
      "models":{},
      "specs":{},
      "throttle":create_throttle(node,space_id,group_id,model_refresh_dependents_unthrottled),
      "deps":{}
    };
    groups[group_id] = group;
  }
  let group_models = group["models"];
  let group_specs = group["specs"];
  if(null == group_models){
    group_models = {};
    group["models"] = group_models;
  }
  if(null == group_specs){
    group_specs = {};
    group["specs"] = group_specs;
  }
  xtd.obj_assign(group_specs,models);
  for(let [model_id,model] of Object.entries(models)){
    group_models[model_id] = create_model(node,space_id,group_id,model_id,model);
  };
  group["deps"] = page_util.get_group_deps(group_id,group_specs);
  return group;
}

export function group_add(node,space_id,group_id,models){
  let group = group_add_attach(node,space_id,group_id,models);
  group["init"] = group_refresh(node,space_id,group_id,{},null);
  return group;
}

export function group_remove(node,space_id,group_id){
  let runtime = space_ensure_page(node,space_id);
  let {groups} = runtime;
  let dependents = group_get_dependents(node,space_id,group_id);
  if(Object.keys(dependents).length > 0){
    throw "ERR - existing group dependents - " + JSON.stringify(dependents);
  }
  let curr = groups[group_id];
  delete(groups[group_id]);
  return curr;
}

export function model_remove(node,space_id,group_id,model_id){
  let dependents = model_get_dependents(node,space_id,group_id,model_id);
  if(Object.keys(dependents).length > 0){
    throw "ERR - existing model dependents - " + JSON.stringify(dependents);
  }
  let group = group_get(node,space_id,group_id);
  if(group){
    let {models} = group;
    let curr = models[model_id];
    delete(models[model_id]);
    return curr;
  }
}

export function group_update(node,space_id,group_id,event){
  let group = group_ensure(node,space_id,group_id);
  let dispatch_fn = group["proxy_dispatch"];
  if(dispatch_fn){
    return dispatch_fn("group-update",node,space_id,group_id,[event]);
  }
  let {models,throttle} = group;
  let out = [];
  for(let model_id of Object.keys(models)){
    let entry = th.throttle_run(throttle,model_id,[event || {}]);
    out.push([model_id,entry["promise"]]);
  };
  return Promise.all(out.map(function (arr){
    return arr[1];
  })).then(function (arr){
    return xtd.arr_zip(out.map(function (arr){
      return arr[0];
    }),arr);
  });
}

export function model_update(node,space_id,group_id,model_id,event){
  let group_value = model_ensure(node,space_id,group_id,model_id);
  let [group,model] = group_value;
  let dispatch_fn = group["proxy_dispatch"];
  if(dispatch_fn){
    return dispatch_fn("model-update",node,space_id,group_id,[model_id,event]);
  }
  let {throttle} = group;
  let entry = th.throttle_run(throttle,model_id,[event || {}]);
  return entry["promise"];
}

export function model_set_input(node,space_id,group_id,model_id,current,event){
  let group_value = model_ensure(node,space_id,group_id,model_id);
  let [group,model] = group_value;
  let dispatch_fn = group["proxy_dispatch"];
  if(dispatch_fn){
    return dispatch_fn(
      "model-set-input",
      node,
      space_id,
      group_id,
      [model_id,current,event]
    );
  }
  event_model.set_input(model,current);
  return model_update(node,space_id,group_id,model_id,event || {});
}

export function group_trigger_raw(node,space_id,group,signal,event){
  let {models} = group;
  let out = [];
  for(let [model_id,model] of Object.entries(models)){
    let {options} = model;
    let {trigger} = options;
    let check = page_util.check_event(
      trigger,
      signal,
      event,
      {"model":model,"group":group,"node":node,"space_id":space_id}
    );
    if(check){
      th.throttle_run(group["throttle"],model_id,[event]);
      out.push(model_id);
    }
  };
  return xtd.arr_sort(out,function (model_id){
    return model_id;
  },function (x,y){
    return 0 > x.localeCompare(y);
  });
}

export function group_trigger(node,space_id,group_id,signal,event){
  let group = group_ensure(node,space_id,group_id);
  let dispatch_fn = group["proxy_dispatch"];
  if(dispatch_fn){
    return dispatch_fn("trigger-group",node,space_id,group_id,[signal,event]);
  }
  return group_trigger_raw(node,space_id,group,signal,event);
}

export function model_trigger(node,space_id,group_id,model_id,signal,event){
  let group_value = model_ensure(node,space_id,group_id,model_id);
  let [group,model] = group_value;
  let dispatch_fn = group["proxy_dispatch"];
  if(dispatch_fn){
    return dispatch_fn("trigger-model",node,space_id,group_id,[model_id,signal,event]);
  }
  let {options} = model;
  let {trigger} = options;
  if(page_util.check_event(
    trigger,
    signal,
    event,
    {"model":model,"group":group,"node":node,"space_id":space_id}
  )){
    let entry = th.throttle_run(group["throttle"],model_id,[event]);
    return entry["promise"];
  }
  return null;
}

export function space_trigger_all(node,space_id,signal,event){
  let groups = space_ensure_page(node,space_id)["groups"];
  let out = {};
  for(let [group_id,group] of Object.entries(groups)){
    let dispatch_fn = group["proxy_dispatch"];
    out[group_id] = (dispatch_fn ? dispatch_fn("trigger-group",node,space_id,group_id,[signal,event]) : group_trigger_raw(node,space_id,group,signal,event));
  };
  return out;
}

export function raw_callback_add(node,space_id){
  let trigger_id = page_util.raw_callback_id(space_id);
  return page_util.register_page_trigger(node,trigger_id,function (_space,frame,local_node){
    return space_trigger_all(local_node,space_id,frame["signal"],frame);
  },{"space_id":space_id});
}

export function raw_callback_remove(node,space_id){
  return page_util.unregister_page_trigger(node,page_util.raw_callback_id(space_id));
}