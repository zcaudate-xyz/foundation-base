const event_common = require("@xtalk/event/base-listener.js")

const xtd = require("@xtalk/lang/common-data.js")

function async_fn_basic(handler,context,callbacks){
  let success = callbacks["success"];
  let error = callbacks["error"];
  try{
    let output = handler(context);
    return success(output);
  }
  catch(err){
    return error(err);
  }
}

function async_fn_promise(handler,context,callbacks){
  let success = callbacks["success"];
  let error = callbacks["error"];
  try{
    let output = handler(context);
    if(output instanceof Promise){
      return output.then(success).catch(error);
    }
    else{
      return Promise.resolve().then(function (){
        return success(output);
      });
    }
  }
  catch(err){
    return Promise.resolve().then(function (){
      return error(err);
    });
  }
}

function wrap_args(handler){
  let wrapped_fn = function (context){
    let args = context["args"];
    if(null == args){
      args = [];
    }
    return handler.apply(null,args);
  };
  return wrapped_fn;
}

function check_disabled(context){
  let {input} = context;
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

function parse_args(context){
  let {input} = context;
  return input["data"];
}

function create_model(main_handler,pipeline,default_args,default_output,default_process,options){
  let identity_fn = function (x){
    return x;
  };
  if(null == options){
    options = {};
  }
  let default_args_fn = default_args;
  if(!("function" == (typeof default_args_fn))){
    let args_value = default_args_fn;
    default_args_fn = (function (){
      return args_value;
    });
  }
  let default_output_fn = default_output;
  if(!("function" == (typeof default_output_fn))){
    let output_value = default_output_fn;
    default_output_fn = (function (){
      return output_value;
    });
  }
  let process_fn = default_process;
  if(null == process_fn){
    process_fn = identity_fn;
  }
  let entry = {
    "pipeline":xtd.obj_assign_nested({
        "main":{"handler":main_handler,"wrapper":wrap_args},
        "remote":{"wrapper":wrap_args},
        "sync":{"wrapper":wrap_args},
        "check_args":parse_args,
        "check_disabled":check_disabled
      },pipeline),
    "options":options,
    "input":{"current":null,"updated":null,"default":default_args_fn},
    "output":{
        "type":"output",
        "current":null,
        "updated":null,
        "elapsed":null,
        "process":process_fn,
        "default":default_output_fn
      }
  };
  if(null != xtd.get_in(pipeline,["remote"])){
    entry["remote"] = {
      "type":"remote",
      "current":null,
      "updated":null,
      "elapsed":null,
      "process":process_fn,
      "default":default_output_fn
    };
  }
  if(null != xtd.get_in(pipeline,["sync"])){
    entry["sync"] = {
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

function model_context(model){
  let {options,pipeline} = model;
  let {input} = model;
  let context = Object.assign({"model":model,"input":input["current"]},options["context"]);
  return context;
}

function add_listener(model,listener_id,callback,meta,pred){
  return event_common.add_listener(model,listener_id,"model",callback,meta,pred);
}

function remove_listener(model,listener_id){
  return event_common.remove_listener(model,listener_id);
}

function list_listeners(model){
  return event_common.list_listeners(model);
}

function trigger_listeners(model,type_name,data){
  return event_common.trigger_listeners(model,{"type":type_name,"data":data});
}

var PIPELINE = {
  "pre":{"guard":null,"handler":null},
  "main":{"guard":null,"handler":null},
  "sync":{"guard":null,"handler":null},
  "remote":{"guard":null,"handler":null},
  "post":{"guard":null,"handler":null}
};

function get_input(model){
  let {input} = model;
  return input;
}

function get_output(model,dest_key){
  if(null == dest_key){
    dest_key = "output";
  }
  return model[dest_key];
}

function get_current(model,dest_key){
  if(null == dest_key){
    dest_key = "output";
  }
  return xtd.get_in(model,[dest_key,"current"]);
}

function is_disabled(model){
  let {pipeline} = model;
  let {check_disabled} = pipeline;
  let context = model_context(model);
  return check_disabled(context);
}

function is_errored(model,dest_key){
  if(null == dest_key){
    dest_key = "output";
  }
  return true == xtd.get_in(model,[dest_key,"errored"]);
}

function is_pending(model,dest_key){
  if(null == dest_key){
    dest_key = "output";
  }
  return true == xtd.get_in(model,[dest_key,"pending"]);
}

function get_time_elapsed(model,dest_key){
  if(null == dest_key){
    dest_key = "output";
  }
  return xtd.get_in(model,[dest_key,"elapsed"]);
}

function get_time_updated(model,dest_key){
  if(null == dest_key){
    dest_key = "output";
  }
  return xtd.get_in(model,[dest_key,"updated"]);
}

function get_success(model,dest_key){
  if(null == dest_key){
    dest_key = "output";
  }
  let output = model[dest_key];
  let {process} = output;
  if(true == output["errored"]){
    return process(output["default"]());
  }
  else{
    let current = output["current"];
    if(null == current){
      current = process(output["default"]());
    }
    return current;
  }
}

function set_input(model,current){
  let {callback,input} = model;
  Object.assign(input,{"current":current,"updated":Date.now()});
  trigger_listeners(model,"model.input",get_input(model));
  return input;
}

function set_output(model,current,errored,tag,dest_key,meta){
  if(null == dest_key){
    dest_key = "output";
  }
  let output = model[dest_key];
  let {callback,options} = model;
  let {accumulate} = options;
  if(errored){
    output["errored"] = true;
  }
  else{
    if(null != output["errored"]){
      delete(output["errored"]);
    }
  }
  output["updated"] = Date.now();
  output["tag"] = tag;
  if(accumulate){
    let prev = xtd.arrayify(output["current"]);
    let next = xtd.arr_assign(prev.slice(),xtd.arrayify(current));
    output["current"] = next;
  }
  else{
    output["current"] = current;
  }
  trigger_listeners(model,"model.output",output);
  return current;
}

function set_output_disabled(model,value,dest_key){
  if(null == dest_key){
    dest_key = "output";
  }
  let output = model[dest_key];
  let {callback} = model;
  if(value){
    output["disabled"] = value;
  }
  else{
    if(null != output["disabled"]){
      delete(output["disabled"]);
    }
  }
  trigger_listeners(model,"model.disabled",output);
  return output;
}

function set_pending(model,value,dest_key){
  if(null == dest_key){
    dest_key = "output";
  }
  let output = model[dest_key];
  if(value){
    output["pending"] = value;
  }
  else{
    if(null != output["pending"]){
      delete(output["pending"]);
    }
  }
  trigger_listeners(model,"model.pending",output);
  return output;
}

function set_elapsed(model,value,dest_key){
  if(null == dest_key){
    dest_key = "output";
  }
  let output = model[dest_key];
  if("number" == (typeof value)){
    output["elapsed"] = value;
  }
  else{
    if(null != output["elapsed"]){
      delete(output["elapsed"]);
    }
  }
  trigger_listeners(model,"model.elapsed",output);
  return output;
}

function init_model(model){
  let {input,options} = model;
  let {init} = options;
  let data = input["default"]();
  return set_input(model,Object.assign({"data":data},init));
}

function pipeline_prep(model,opts){
  let {pipeline} = model;
  let {check_args,check_disabled} = pipeline;
  let context = Object.assign(model_context(model),opts);
  let disabled = check_disabled(context);
  let args = context["args"];
  if(null == args){
    if(!disabled){
      args = check_args(context);
    }
  }
  if(null == args){
    disabled = true;
  }
  context["args"] = xtd.arrayify(args);
  context["acc"] = {"::":"model.run"};
  return [context,disabled];
}

function pipeline_set(context,tag,acc,dest_key){
  let {model} = context;
  if(null == dest_key){
    dest_key = "output";
  }
  let process = xtd.get_in(model,[dest_key,"process"]);
  let record = acc[tag];
  let should_update = null;
  if(0 < record.length){
    should_update = record[0];
  }
  let current = null;
  if(1 < record.length){
    current = record[1];
  }
  let errored = null;
  if(2 < record.length){
    errored = record[2];
  }
  if(null == current){
    current = xtd.get_in(model,[dest_key,"default"])();
  }
  if(should_update){
    let output = current;
    if(!errored){
      output = process(current);
    }
    set_output(model,output,errored,tag,dest_key,context["meta"]);
  }
  return acc;
}

function pipeline_call(context,tag,disabled,async_fn,hook_fn,skip_guard){
  let identity_hook = function (acc,_tag){
    return acc;
  };
  let identity_wrapper = function (handler){
    return handler;
  };
  if(null == skip_guard){
    skip_guard = {};
  }
  if(null == hook_fn){
    hook_fn = identity_hook;
  }
  let {acc,args,model} = context;
  let {pipeline} = model;
  let stage = pipeline[tag];
  if(null == stage){
    stage = {};
  }
  let {guard,handler,wrapper} = stage;
  if(null == wrapper){
    wrapper = identity_wrapper;
  }
  let error_fn = function (err){
    acc[tag] = [true,err,true];
    acc["error"] = true;
    return hook_fn(acc,tag);
  };
  let skipped_fn = function (res){
    acc[tag] = [false];
    return hook_fn(acc,tag);
  };
  let result_fn = function (res){
    acc[tag] = [true,res];
    return hook_fn(acc,tag);
  };
  let handler_fn = null;
  let success_fn = null;
  if(!disabled && ("function" == (typeof handler)) && ((null == guard) || skip_guard[tag] || guard(context,acc))){
    handler_fn = wrapper(handler);
    success_fn = result_fn;
  }
  else{
    handler_fn = (function (_){
      return null;
    });
    success_fn = skipped_fn;
  }
  return async_fn(handler_fn,context,{"success":success_fn,"error":error_fn});
}

function pipeline_run_impl(context,stages,index,async_fn,hook_fn,complete_fn,skip_guard){
  if(index < stages.length){
    let next_hook = function (acc,tag){
      if(hook_fn){
        hook_fn(acc,tag);
      }
      return pipeline_run_impl(context,stages,index + 1,async_fn,hook_fn,complete_fn,skip_guard);
    };
    return pipeline_call(context,stages[index],false,async_fn,next_hook,skip_guard);
  }
  else{
    return complete_fn(context);
  }
}

function pipeline_run(context,disabled,async_fn,hook_fn,complete_fn,dest_key){
  let {acc,model} = context;
  if(null == dest_key){
    dest_key = "output";
  }
  let dest_tag = dest_key;
  if(dest_key == "output"){
    dest_tag = "main";
  }
  let output = model[dest_key];
  let started = Date.now();
  if(null != output["elapsed"]){
    delete(output["elapsed"]);
  }
  if(disabled){
    let disabled_hook = function (acc,tag){
      if(hook_fn){
        hook_fn(acc,tag);
      }
      if(complete_fn){
        complete_fn(acc);
      }
    };
    set_output_disabled(model,true,dest_key);
    return pipeline_call(context,dest_tag,true,async_fn,disabled_hook,null);
  }
  else{
    let run_hook = function (acc,tag){
      if(hook_fn){
        hook_fn(acc,tag);
      }
      if(tag == dest_tag){
        pipeline_set(context,tag,acc,dest_key);
      }
    };
    let run_complete = function (acc){
      if(complete_fn){
        complete_fn(acc);
      }
      set_elapsed(model,Date.now() - started,dest_key);
      set_pending(model,false,dest_key);
    };
    if(output["disabled"]){
      set_output_disabled(model,false,dest_key);
    }
    set_pending(model,true,dest_key);
    return pipeline_run_impl(
      context,
      ["pre",dest_tag,"post"],
      0,
      async_fn,
      run_hook,
      run_complete,
      null
    );
  }
}

function pipeline_run_force(context,save_output,async_fn,hook_fn,complete_fn,dest_key){
  let {acc,model} = context;
  let started = Date.now();
  let force_hook = function (acc,tag){
    if(hook_fn){
      hook_fn(acc,tag);
    }
    if(tag == dest_key){
      pipeline_set(context,tag,acc,dest_key);
      if(save_output){
        pipeline_set(context,tag,acc,"output");
      }
    }
  };
  let force_complete = function (acc){
    if(complete_fn){
      complete_fn(acc);
    }
    set_elapsed(model,Date.now() - started,dest_key);
    set_pending(model,false,dest_key);
  };
  set_pending(model,true,dest_key);
  return pipeline_run_impl(
    context,
    ["pre",dest_key,"post"],
    0,
    async_fn,
    force_hook,
    force_complete,
    null
  );
}

function pipeline_run_remote(context,save_output,async_fn,hook_fn,complete_fn){
  return pipeline_run_force(context,save_output,async_fn,hook_fn,complete_fn,"remote");
}

function pipeline_run_sync(context,save_output,async_fn,hook_fn,complete_fn){
  return pipeline_run_force(context,save_output,async_fn,hook_fn,complete_fn,"sync");
}

function get_with_lookup(results,opts){
  if(null == opts){
    opts = {};
  }
  let {key_fn,sort_fn,val_fn} = opts;
  if(null != sort_fn){
    results = sort_fn(results);
  }
  if(null == key_fn){
    key_fn = (function (e){
      return e["id"];
    });
  }
  if(null == val_fn){
    val_fn = (function (x){
      return x;
    });
  }
  if(null == results){
    results = [];
  }
  return {
    "results":results,
    "lookup":xtd.arr_juxt(results,key_fn,val_fn)
  };
}

function sorted_lookup(key){
  let sort_key = key;
  if(null == sort_key){
    sort_key = "name";
  }
  return function (results){
    return get_with_lookup(results,{
      "sort_fn":function (arr){
            return xtd.arr_sort(arr,function (e){
              return e[sort_key];
            },function (x,y){
              return 0 > x.localeCompare(y);
            });
          }
    });
  };
}

function group_by_lookup(key){
  return function (results){
    return {
      "results":results,
      "lookup":xtd.arr_group_by(results,function (e){
            return e[key];
          },function (x){
            return x;
          })
    };
  };
}

module.exports = {
  ["async_fn_basic"]:async_fn_basic,
  ["async_fn_promise"]:async_fn_promise,
  ["wrap_args"]:wrap_args,
  ["check_disabled"]:check_disabled,
  ["parse_args"]:parse_args,
  ["create_model"]:create_model,
  ["model_context"]:model_context,
  ["add_listener"]:add_listener,
  ["remove_listener"]:remove_listener,
  ["list_listeners"]:list_listeners,
  ["trigger_listeners"]:trigger_listeners,
  ["PIPELINE"]:PIPELINE,
  ["get_input"]:get_input,
  ["get_output"]:get_output,
  ["get_current"]:get_current,
  ["is_disabled"]:is_disabled,
  ["is_errored"]:is_errored,
  ["is_pending"]:is_pending,
  ["get_time_elapsed"]:get_time_elapsed,
  ["get_time_updated"]:get_time_updated,
  ["get_success"]:get_success,
  ["set_input"]:set_input,
  ["set_output"]:set_output,
  ["set_output_disabled"]:set_output_disabled,
  ["set_pending"]:set_pending,
  ["set_elapsed"]:set_elapsed,
  ["init_model"]:init_model,
  ["pipeline_prep"]:pipeline_prep,
  ["pipeline_set"]:pipeline_set,
  ["pipeline_call"]:pipeline_call,
  ["pipeline_run_impl"]:pipeline_run_impl,
  ["pipeline_run"]:pipeline_run,
  ["pipeline_run_force"]:pipeline_run_force,
  ["pipeline_run_remote"]:pipeline_run_remote,
  ["pipeline_run_sync"]:pipeline_run_sync,
  ["get_with_lookup"]:get_with_lookup,
  ["sorted_lookup"]:sorted_lookup,
  ["group_by_lookup"]:group_by_lookup
}