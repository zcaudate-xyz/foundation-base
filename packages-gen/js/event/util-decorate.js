const xtd = require("@xtalk/lang/common-data.js")

function incr_fn(){
  let state = {"value":-1};
  let next_id_fn = function (){
    let i = 1 + state["value"];
    state["value"] = i;
    return "id-" + String(i);
  };
  return next_id_fn;
}

function plugin_timing(handle){
  let result = {"output":{}};
  let on_setup = function (args){
    result["output"] = {"start":Date.now()};
  };
  let on_teardown = function (){
    let output = result["output"];
    let t_end = Date.now();
    let t_elapsed = t_end - output["start"];
    output["end"] = t_end;
    output["elapsed"] = t_elapsed;
  };
  let on_reset = function (){
    result["output"] = {};
  };
  return Object.assign(result,{
    "name":"timing",
    "on_setup":on_setup,
    "on_teardown":on_teardown,
    "on_reset":on_reset
  });
}

function plugin_counts(handle){
  let result = {"output":{"success":0,"error":0}};
  let on_success = function (ret){
    let {output} = result;
    output["success"] = (1 + output["success"]);
  };
  let on_error = function (ret){
    let {output} = result;
    output["error"] = (1 + output["error"]);
  };
  let on_reset = function (){
    result["output"] = {"success":0,"error":0};
  };
  return Object.assign(result,{
    "name":"counts",
    "on_success":on_success,
    "on_error":on_error,
    "on_reset":on_reset
  });
}

function to_handle_callback(cb){
  if(null == cb){
    cb = {};
  }
  return {
    "on_success":cb["success"],
    "on_error":cb["error"],
    "on_teardown":cb["finally"]
  };
}

function new_handle(handler,plugin_fns,opts){
  let {create_fn,delay,dump_fn,id_fn,name,wrap_fn} = opts;
  if(null == id_fn){
    id_fn = incr_fn();
  }
  if(null == create_fn){
    create_fn = (function (x){
      return x;
    });
  }
  if(null == delay){
    delay = 0;
  }
  let handle = create_fn({
    "::":"handle",
    "name":name,
    "id_fn":id_fn,
    "wrap_fn":wrap_fn,
    "handler":handler,
    "delay":delay
  });
  let plugins = plugin_fns.map(function (f){
    return f(handle);
  });
  handle["plugins"] = plugins;
  return handle;
}

function run_handle(handle,args,tcb){
  let {delay,handler,id_fn,plugins,wrap_fn} = handle;
  if(null == args){
    args = [];
  }
  let tcbs = plugins.slice();
  if(null != tcb){
    if(Array.isArray(tcb)){
      xtd.arr_assign(tcbs,tcb);
    }
    else{
      tcbs.push(tcb);
    }
  }
  if("function" == (typeof delay)){
    delay = delay();
  }
  let receipt = {"id":id_fn()};
  let teardown_fn = function (){
    for(let cb of tcbs){
      let on_teardown = cb["on_teardown"];
      let name = cb["name"];
      let output = cb["output"];
      if(null != on_teardown){
        on_teardown();
      }
      if((null != name) && (null != output)){
        receipt[name] = output;
      }
    };
    return receipt;
  };
  let run_fn = function (){
    for(let cb of tcbs){
      let on_setup = cb["on_setup"];
      if(null != on_setup){
        on_setup(args);
      }
    };
    let base_thunk = function (){
      return handler.apply(null,args);
    };
    let base_promise = (0 < ((null == delay) ? 0 : delay)) ? new Promise(function (resolve,reject){
      setTimeout(function (){
        new Promise(function (inner_resolve){
          inner_resolve(base_thunk());
        }).then(function (value){
          resolve(value);
        }).catch(function (err){
          reject(err);
        });
      },delay);
    }) : Promise.resolve().then(base_thunk);
    return base_promise.then(function (ret){
      for(let cb of tcbs){
        let on_success = cb["on_success"];
        if(null != on_success){
          on_success(ret);
        }
      };
      receipt["status"] = "success";
      receipt["value"] = ret;
      return receipt;
    }).catch(function (err){
      for(let cb of tcbs){
        let on_error = cb["on_error"];
        if(null != on_error){
          on_error(err);
        }
      };
      receipt["status"] = "error";
      receipt["error"] = err;
      let reject_fn = function (){
        throw receipt;
      };
      return Promise.resolve().then(reject_fn);
    }).finally(teardown_fn);
  };
  let proc = (null != wrap_fn) ? wrap_fn(run_fn,args,receipt,handle) : run_fn();
  return Promise.resolve().then(function (){
    return proc;
  });
}

module.exports = {
  ["incr_fn"]:incr_fn,
  ["plugin_timing"]:plugin_timing,
  ["plugin_counts"]:plugin_counts,
  ["to_handle_callback"]:to_handle_callback,
  ["new_handle"]:new_handle,
  ["run_handle"]:run_handle
}