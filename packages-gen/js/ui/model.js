const event_listener = require("@xtalk/event/base-listener.js")

const xtd = require("@xtalk/lang/common-data.js")

const page_proxy = require("@xtalk/substrate/page-proxy.js")

const page_core = require("@xtalk/substrate/page-core.js")

function store_create(node,space_id,group_id,mode,opts){
  return {
    "node":node,
    "space_id":space_id,
    "group_id":group_id,
    "mode":mode || "local",
    "opts":opts || {},
    "control":null,
    "revision":0,
    "listeners":{}
  };
}

function store_version(store){
  return store["revision"];
}

function store_open(store){
  if("proxy" != store["mode"]){
    page_core.group_ensure(store["node"],store["space_id"],store["group_id"]);
    return Promise.resolve().then(function (){
      return store;
    });
  }
  return page_proxy.group_sync_proxy(
    store["node"],
    store["space_id"],
    store["group_id"],
    store["opts"]
  ).then(function (control){
    store["control"] = control;
    return store;
  });
}

function model(store,model_id){
  let [_group,current] = page_core.model_ensure(store["node"],store["space_id"],store["group_id"],model_id);
  return current;
}

function model_slot(store,model_id,slot,path,fallback){
  let current = model(store,model_id);
  let value = xtd.get_in(current,slot || []);
  if(null != path){
    value = xtd.get_in(value,path);
  }
  return (null == value) ? fallback : value;
}

function model_input(store,model_id,path,fallback){
  return model_slot(store,model_id,["input","current"],path,fallback);
}

function model_output(store,model_id,path,fallback){
  return model_slot(store,model_id,["output","current"],path,fallback);
}

function model_pendingp(store,model_id){
  return true == model_slot(store,model_id,["output","pending"],null,false);
}

function model_disabledp(store,model_id){
  return true == model_slot(store,model_id,["output","disabled"],null,false);
}

function model_error(store,model_id){
  if(!(true == model_slot(store,model_id,["output","errored"],null,false))){
    return null;
  }
  return model_slot(store,model_id,["output","current"],null,null);
}

function model_remote(store,model_id,path,fallback){
  return model_slot(store,model_id,["remote","current"],path,fallback);
}

function model_sync(store,model_id,path,fallback){
  return model_slot(store,model_id,["sync","current"],path,fallback);
}

function set_inputf(store,model_id,value,event){
  return page_core.model_set_input(
    store["node"],
    store["space_id"],
    store["group_id"],
    model_id,
    value,
    event || {}
  );
}

function patch_inputf(store,model_id,path,value,event){
  let current = model_input(store,model_id,null,{});
  if(((null != current) && ("object" == (typeof current)) && !Array.isArray(current)) && (null != current["data"]) && (1 == Object.keys(current).length)){
    let initial_data = current["data"];
    current = ((((null != initial_data) && ("object" == (typeof initial_data)) && !Array.isArray(initial_data)) && !Array.isArray(initial_data)) ? initial_data : {});
  }
  let next = JSON.parse(JSON.stringify(current || {}));
  xtd.set_in(next,path,value);
  return set_inputf(store,model_id,next,event);
}

function invokef(store,model_id,args){
  return page_core.model_remote_call(
    store["node"],
    store["space_id"],
    store["group_id"],
    model_id,
    args || [],
    true
  );
}

function refreshf(store,model_id,event){
  return page_core.model_refresh(
    store["node"],
    store["space_id"],
    store["group_id"],
    model_id,
    event || {},
    null
  );
}

function subscribef(store,subscription_id,callback){
  let node = store["node"];
  let space_id = store["space_id"];
  let group_id = store["group_id"];
  let group = page_core.group_ensure(node,space_id,group_id);
  for(let model_id of Object.keys(group["models"])){
    let key = JSON.stringify([space_id,[group_id,model_id]]);
    event_listener.add_keyed_listener(node,key,subscription_id + "/" + model_id,"ui.model",function (listener_id,data,t,meta){
      store["revision"] = (1 + store["revision"]);
      return callback(listener_id,data,t,meta);
    },{"model_id":model_id},null);
  };
  store["listeners"][subscription_id] = true;
  return subscription_id;
}

function unsubscribef(store,subscription_id){
  let node = store["node"];
  let space_id = store["space_id"];
  let group_id = store["group_id"];
  let group = page_core.group_ensure(node,space_id,group_id);
  for(let model_id of Object.keys(group["models"])){
    let key = JSON.stringify([space_id,[group_id,model_id]]);
    event_listener.remove_keyed_listener(node,key,subscription_id + "/" + model_id);
  };
  delete(store["listeners"][subscription_id]);
  return true;
}

function store_close(store){
  let listener_ids = Object.keys(store["listeners"]);
  for(let listener_id of listener_ids){
    unsubscribef(store,listener_id);
  };
  let control = store["control"];
  if((null != control) && ("function" == (typeof control["close"]))){
    return control["close"]();
  }
  return Promise.resolve().then(function (){
    return true;
  });
}

module.exports = {
  ["store_create"]:store_create,
  ["store_version"]:store_version,
  ["store_open"]:store_open,
  ["model"]:model,
  ["model_slot"]:model_slot,
  ["model_input"]:model_input,
  ["model_output"]:model_output,
  ["model_pendingp"]:model_pendingp,
  ["model_disabledp"]:model_disabledp,
  ["model_error"]:model_error,
  ["model_remote"]:model_remote,
  ["model_sync"]:model_sync,
  ["set_inputf"]:set_inputf,
  ["patch_inputf"]:patch_inputf,
  ["invokef"]:invokef,
  ["refreshf"]:refreshf,
  ["subscribef"]:subscribef,
  ["unsubscribef"]:unsubscribef,
  ["store_close"]:store_close
}