import * as xtd from '../lang/common-data.mjs'

import * as event_model from '../event/base-model.mjs'

export function wrap_space_args(handler){
  return function (context){
    let args = context["args"] || [];
    let params = [context];
    xtd.arr_assign(params,args);
    return handler.apply(null,params);
  };
}

export function check_event(pred,signal,event,ctx){
  let check = false;
  try{
    let t = null;
    if(null == pred){
      t = true;
    }
    else if("boolean" == (typeof pred)){
      t = pred;
    }
    else if("function" == (typeof pred)){
      t = pred(signal,ctx);
    }
    else if((null != pred) && ("object" == (typeof pred)) && !Array.isArray(pred)){
      t = pred[signal];
    }
    else{
      t = (signal == pred);
    }
    if(true == t){
      check = true;
    }
    else if("function" == (typeof t)){
      check = t.apply(null,[event,ctx]);
    }
  }
  catch(err){
    check = false;
  }
  return check;
}

export function run_tail_call(context,refresh_deps_fn){
  let {acc,node,path} = context;
  let space_id = context["space"]["id"];
  let group_id = path[0];
  let model_id = path[1];
  if(acc && !acc["error"] && refresh_deps_fn){
    return Promise.resolve().then(function (){
      return refresh_deps_fn(node,space_id,group_id,model_id,refresh_deps_fn);
    }).then(function (_){
      return acc;
    });
  }
  else{
    return acc;
  }
}

export function run_remote(context,save_output,path,refresh_deps_fn){
  context["acc"]["path"] = path;
  return event_model.pipeline_run_remote(context,save_output,event_model.async_fn_promise,null,null).then(function (_){
    return run_tail_call(context,refresh_deps_fn);
  });
}

export function run_refresh(context,disabled,path,refresh_deps_fn){
  context["acc"]["path"] = path;
  return event_model.pipeline_run(context,disabled,event_model.async_fn_promise,null,null).then(function (_){
    return run_tail_call(context,refresh_deps_fn);
  });
}

export function get_group_deps(group_id,models){
  let all_deps = {};
  for(let [model_id,model_entry] of Object.entries(models)){
    let {deps} = model_entry;
    for(let path of deps || []){
      path = (Array.isArray(path) ? path : [group_id,path]);
      xtd.set_in(all_deps,[path[0],path[1],model_id],true);
    };
  };
  return all_deps;
}

export function raw_callback_id(space_id){
  return "@/raw/page/" + (space_id || "");
}

export function register_page_trigger(node,signal,trigger_fn,meta){
  let entry = {"id":signal,"fn":trigger_fn,"meta":meta || {}};
  node["triggers"][signal] = entry;
  return entry;
}

export function unregister_page_trigger(node,signal){
  let {triggers} = node;
  let prev = triggers[signal];
  delete(triggers[signal]);
  return prev;
}