const space = require("@xtalk/substrate/base-space.js")

function ensure_promise(value){
  if((value instanceof Promise) || (((null != value) && ("object" == (typeof value)) && !Array.isArray(value)) && ("function" == (typeof value["then"])))){
    return value;
  }
  else{
    return Promise.resolve().then(function (){
      return value;
    });
  }
}

function add_pending(node,request,resolve,reject,meta){
  let pending = node["pending"];
  let id = request["id"];
  let entry = {
    "resolve":resolve,
    "reject":reject,
    "request":request,
    "meta":meta || {}
  };
  pending[id] = entry;
  return entry;
}

function remove_pending(node,request_id){
  let pending = node["pending"];
  let entry = pending[request_id];
  delete(pending[request_id]);
  return entry;
}

function settle_pending(node,response){
  let reply_to = response["reply_to"];
  let entry = remove_pending(node,reply_to);
  if(null == entry){
    return null;
  }
  let resolve = entry["resolve"];
  let reject = entry["reject"];
  if(response["status"] == "ok"){
    resolve(response["data"]);
  }
  else{
    reject(response);
  }
  return entry;
}

function invoke_handler(node,request){
  let action = request["action"];
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

function response_body(response){
  return ensure_promise(response).then(function (frame){
    if(frame["status"] == "ok"){
      return frame["data"];
    }
    else{
      throw frame;
    }
  });
}

module.exports = {
  ["ensure_promise"]:ensure_promise,
  ["add_pending"]:add_pending,
  ["remove_pending"]:remove_pending,
  ["settle_pending"]:settle_pending,
  ["invoke_handler"]:invoke_handler,
  ["response_body"]:response_body
}