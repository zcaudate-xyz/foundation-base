import 'dart:async';

promise_nativep(value) {
  return (("Map" == (value.runtimeType).toString()) || (value.runtimeType).toString().startsWith("_Map") || (value.runtimeType).toString().startsWith("LinkedMap")) && ("xt.promise" == value["::"]);
}

make_resolve_state(value) {
  return <dynamic, dynamic>{"::":"xt.promise","status":"resolved","value":value};
}

make_rejected_state(err) {
  return <dynamic, dynamic>{"::":"xt.promise","status":"rejected","error":err};
}

make_pending_state(is_async) {
  return <dynamic, dynamic>{
    "::":"xt.promise",
    "status":"pending",
    "is_async":is_async,
    "children":<dynamic>[]
  };
}

internal_settle_action(p, status, payload, drive_fn) {
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
    var children = p["children"];
    p["children"] = <dynamic>[];
    var arr_49468 = children;
    for(var i49469 = 0; i49469 < arr_49468.length; ++i49469){
      var entry = arr_49468[i49469];
      Function.apply(drive_fn,<dynamic>[p,entry,drive_fn]);
    };
  }
  return p;
}

internal_link_action(promise, child, on_resolve, on_reject, drive_fn) {
  var status = promise["status"];
  if("pending" == status){
    promise["children"].add(
      <dynamic, dynamic>{"child":child,"resolve":on_resolve,"reject":on_reject}
    );
    return child;
  }
  else{
    return Function.apply(drive_fn,<dynamic>[
      promise,
      <dynamic, dynamic>{"child":child,"resolve":on_resolve,"reject":on_reject},
      drive_fn
    ]);
  }
}

internal_adopt_action(target, value, drive_fn) {
  if((() {
    var dart_truthy__49467 = promise_nativep(value);
    return (null != dart_truthy__49467) && (false != dart_truthy__49467);
  })()){
    var status = value["status"];
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

internal_drive_action(promise, entry, drive_fn) {
  var status = promise["status"];
  var child = entry["child"];
  var rejectedp = "rejected" == status;
  var thunk = entry[((null != rejectedp) && (false != rejectedp)) ? "reject" : "resolve"];
  var payload = promise[((null != rejectedp) && (false != rejectedp)) ? "error" : "value"];
  if(null == thunk){
    return internal_settle_action(
      child,
      ((null != rejectedp) && (false != rejectedp)) ? "rejected" : "resolved",
      payload,
      drive_fn
    );
  }
  else{
    try{
      return internal_adopt_action(child,Function.apply(thunk,<dynamic>[payload]),drive_fn);
    }
    catch(err){
      return internal_settle_action(child,"rejected",err,drive_fn);
    }
  }
}

promise(thunk) {
  var out = make_pending_state(null);
  try{
    out["is_async"] = Future.sync(() {
      try{
        return internal_adopt_action(out,Function.apply(thunk,<dynamic>[]),internal_drive_action);
      }
      catch(err){
        return internal_settle_action(out,"rejected",err,internal_drive_action);
      }
    });
    return out;
  }
  catch(err){
    return make_rejected_state(err);
  }
}

promise_new(thunk) {
  var out = make_pending_state(null);
  try{
    Function.apply(thunk,<dynamic>[
      (value) {
          return internal_settle_action(out,"resolved",value,internal_drive_action);
        },
      (err) {
          return internal_settle_action(out,"rejected",err,internal_drive_action);
        }
    ]);
    return out;
  }
  catch(err){
    return make_rejected_state(err);
  }
}

promise_run(value) {
  if((() {
    var dart_truthy__49466 = promise_nativep(value);
    return (null != dart_truthy__49466) && (false != dart_truthy__49466);
  })()){
    return value;
  }
  else{
    return make_resolve_state(value);
  }
}

promise_then(promise, thunk) {
  var current = promise_run(promise);
  var child = make_pending_state(null);
  return internal_link_action(current,child,thunk,null,internal_drive_action);
}

promise_catch(promise, thunk) {
  var current = promise_run(promise);
  var child = make_pending_state(null);
  return internal_link_action(current,child,null,thunk,internal_drive_action);
}

promise_all(promises) {
  var values = (null == promises) ? <dynamic>[] : promises;
  var out = <dynamic>[];
  var chain = promise_run(null);
  var arr_49490 = values;
  for(var i49491 = 0; i49491 < arr_49490.length; ++i49491){
    var value = arr_49490[i49491];
    chain = promise_then(chain,(_) {
      return promise_then(promise_run(value),(resolved) {
        out.add(resolved);
        return null;
      });
    });
  };
  return promise_then(chain,(_) {
    return out;
  });
}

with_delay(ms, thunk) {
  return promise(() {
    var start = DateTime.now().millisecondsSinceEpoch;
    while((DateTime.now().millisecondsSinceEpoch - start) < ms){
      start = start;
    }
    return Function.apply(thunk,<dynamic>[]);
  });
}

promise_finally(promise, thunk) {
  return promise_catch(promise_then(promise,(value) {
    return promise_then(promise(thunk),(_) {
      return value;
    });
  }),(err) {
    return promise_catch(promise_then(promise(thunk),(_) {
      return make_rejected_state(err);
    }),(cleanup_err) {
      return make_rejected_state(cleanup_err);
    });
  });
}