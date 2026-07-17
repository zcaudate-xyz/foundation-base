const xtd = require("@xtalk/lang/common-data.js")

function throttle_create(handler,now_fn){
  return {
    "now_fn":(null == now_fn) ? (function (){
        return Date.now();
      }) : now_fn,
    "handler":handler,
    "active":{},
    "queued":{}
  };
}

function throttle_run_async(throttle,id,args){
  let {active,handler,queued} = throttle;
  let key = String(id);
  args = xtd.arrayify(args);
  let inputs = [id];
  for(let arg of args){
    inputs.push(arg);
  };
  let base_promise = Promise.resolve().then(function (){
    return handler.apply(null,inputs);
  });
  return base_promise.finally(function (){
    delete(active[key]);
    let qentry = queued[key];
    if(null != qentry){
      active[key] = qentry;
      delete(queued[key]);
      throttle_run_async(throttle,id,qentry["args"]);
    }
  });
}

function throttle_run(throttle,id,args){
  let {active,now_fn,queued} = throttle;
  let key = String(id);
  args = xtd.arrayify(args);
  let qentry = queued[key];
  if(null != qentry){
    return qentry;
  }
  let aentry = active[key];
  if(null != aentry){
    qentry = {
      "promise":aentry["promise"],
      "started":now_fn(),
      "args":aentry["args"]
    };
    queued[key] = qentry;
    return qentry;
  }
  let promise = throttle_run_async(throttle,id,args);
  aentry = {"promise":promise,"started":now_fn(),"args":args};
  active[key] = aentry;
  return aentry;
}

function throttle_waiting(throttle){
  let {active,queued} = throttle;
  return xtd.arr_union(Object.keys(active),Object.keys(queued));
}

function throttle_active(throttle){
  let {active} = throttle;
  return Object.keys(active);
}

function throttle_queued(throttle){
  let {queued} = throttle;
  return Object.keys(queued);
}

module.exports = {
  ["throttle_create"]:throttle_create,
  ["throttle_run_async"]:throttle_run_async,
  ["throttle_run"]:throttle_run,
  ["throttle_waiting"]:throttle_waiting,
  ["throttle_active"]:throttle_active,
  ["throttle_queued"]:throttle_queued
}