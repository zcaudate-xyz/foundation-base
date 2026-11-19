import * as space from './base-space.mjs'

export function ensure_promise(value){
  if((value instanceof Promise) || (((null != value) && ("object" == (typeof value)) && !Array.isArray(value)) && ("function" == (typeof value["then"])))){
    return value;
  }
  else{
    return Promise.resolve().then(function (){
      return value;
    });
  }
}

export function add_pending(node,request,resolve,reject,meta){
  let {pending} = node;
  let {id} = request;
  let entry = {
    "resolve":resolve,
    "reject":reject,
    "request":request,
    "meta":meta || {}
  };
  pending[id] = entry;
  return entry;
}

export function remove_pending(node,request_id){
  let {pending} = node;
  let entry = pending[request_id];
  delete(pending[request_id]);
  return entry;
}

export function settle_pending(node,response){
  let {reply_to} = response;
  let entry = remove_pending(node,reply_to);
  if(null == entry){
    return null;
  }
  let {reject,resolve} = entry;
  if(response["status"] == "ok"){
    resolve(response["data"]);
  }
  else{
    reject(response);
  }
  return entry;
}

export function invoke_handler(node,request){
  let {action} = request;
  let entry = (node["handlers"])[action];
  if(null == entry){
    throw "handler not found - " + action;
  }
  let handler = entry["fn"];
  let current_space = space.ensure_space(node,request["space"],null);
  return ensure_promise(
    handler.apply(null,[current_space,request["args"],request,node])
  );
}

export function response_body(response){
  return ensure_promise(response).then(function (frame){
    if(frame["status"] == "ok"){
      return frame["data"];
    }
    else{
      throw frame;
    }
  });
}