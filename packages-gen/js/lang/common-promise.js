function promise_nativep(value){
  return ((null != value) && ("object" == (typeof value)) && !Array.isArray(value)) && ("xt.promise" == value["::"]);
}

function make_resolve_state(value){
  return {"::":"xt.promise","status":"resolved","value":value};
}

function make_rejected_state(err){
  return {"::":"xt.promise","status":"rejected","error":err};
}

function make_pending_state(is_async){
  return {
    "::":"xt.promise",
    "status":"pending",
    "is_async":is_async,
    "children":[]
  };
}

function internal_settle_action(p,status,payload,drive_fn){
  if("pending" == p["status"]){
    p["status"] = status;
    if("rejected" == status){
      p["error"] = payload;
      p["value"] = null;
    }
    else{
      p["value"] = payload;
      p["error"] = null;
    }
    let children = p["children"];
    p["children"] = [];
    for(let entry of children){
      drive_fn.apply(null,[p,entry,drive_fn]);
    };
  }
  return p;
}

function internal_link_action(promise,child,on_resolve,on_reject,drive_fn){
  let status = promise["status"];
  if("pending" == status){
    promise["children"].push({"child":child,"resolve":on_resolve,"reject":on_reject});
    return child;
  }
  else{
    return drive_fn.apply(null,[
      promise,
      {"child":child,"resolve":on_resolve,"reject":on_reject},
      drive_fn
    ]);
  }
}

function internal_adopt_action(target,value,drive_fn){
  if(promise_nativep(value)){
    let status = value["status"];
    if("pending" == status){
      return internal_link_action(value,target,null,null,drive_fn);
    }
    else if("rejected" == status){
      return internal_settle_action(target,"rejected",value["error"],drive_fn);
    }
    else{
      return internal_settle_action(target,"resolved",value["value"],drive_fn);
    }
  }
  else{
    return internal_settle_action(target,"resolved",value,drive_fn);
  }
}

function internal_drive_action(promise,entry,drive_fn){
  let status = promise["status"];
  let child = entry["child"];
  let rejectedp = "rejected" == status;
  let thunk = entry[rejectedp ? "reject" : "resolve"];
  let payload = promise[rejectedp ? "error" : "value"];
  if(null == thunk){
    return internal_settle_action(child,rejectedp ? "rejected" : "resolved",payload,drive_fn);
  }
  else{
    try{
      return internal_adopt_action(child,thunk.apply(null,[payload]),drive_fn);
    }
    catch(err){
      return internal_settle_action(child,"rejected",err,drive_fn);
    }
  }
}

function promise(thunk){
  let out = make_pending_state(null);
  try{
    out["is_async"] = new Promise(function (resolve,reject){
      setTimeout(function (){
        Promise.resolve((function (){
          try{
            return internal_adopt_action(out,thunk.apply(null,[]),internal_drive_action);
          }
          catch(err){
            return internal_settle_action(out,"rejected",err,internal_drive_action);
          }
        })()).then(resolve,reject);
      },0);
    });
    return out;
  }
  catch(err){
    return make_rejected_state(err);
  }
}

function promise_new(thunk){
  let out = make_pending_state(null);
  try{
    thunk.apply(null,[
      function (value){
          return internal_settle_action(out,"resolved",value,internal_drive_action);
        },
      function (err){
          return internal_settle_action(out,"rejected",err,internal_drive_action);
        }
    ]);
    return out;
  }
  catch(err){
    return make_rejected_state(err);
  }
}

function promise_run(value){
  if(promise_nativep(value)){
    return value;
  }
  else{
    return make_resolve_state(value);
  }
}

function promise_then(promise,thunk){
  let current = promise_run(promise);
  let child = make_pending_state(null);
  return internal_link_action(current,child,thunk,null,internal_drive_action);
}

function promise_catch(promise,thunk){
  let current = promise_run(promise);
  let child = make_pending_state(null);
  return internal_link_action(current,child,null,thunk,internal_drive_action);
}

function promise_all(promises){
  let values = (null == promises) ? [] : promises;
  let out = [];
  let chain = promise_run(null);
  for(let value of values){
    chain = promise_then(chain,function (_){
      return promise_then(promise_run(value),function (resolved){
        out.push(resolved);
        return null;
      });
    });
  };
  return promise_then(chain,function (_){
    return out;
  });
}

function with_delay(ms,thunk){
  return promise(function (){
    let start = Date.now();
    while((Date.now() - start) < ms){
      start = start;
    }
    return thunk.apply(null,[]);
  });
}

function promise_finally(promise,thunk){
  return promise_catch(promise_then(promise,function (value){
    return promise_then(promise(thunk),function (_){
      return value;
    });
  }),function (err){
    return promise_catch(promise_then(promise(thunk),function (_){
      return make_rejected_state(err);
    }),function (cleanup_err){
      return make_rejected_state(cleanup_err);
    });
  });
}

module.exports = {
  ["promise_nativep"]:promise_nativep,
  ["make_resolve_state"]:make_resolve_state,
  ["make_rejected_state"]:make_rejected_state,
  ["make_pending_state"]:make_pending_state,
  ["internal_settle_action"]:internal_settle_action,
  ["internal_link_action"]:internal_link_action,
  ["internal_adopt_action"]:internal_adopt_action,
  ["internal_drive_action"]:internal_drive_action,
  ["promise"]:promise,
  ["promise_new"]:promise_new,
  ["promise_run"]:promise_run,
  ["promise_then"]:promise_then,
  ["promise_catch"]:promise_catch,
  ["promise_all"]:promise_all,
  ["with_delay"]:with_delay,
  ["promise_finally"]:promise_finally
}