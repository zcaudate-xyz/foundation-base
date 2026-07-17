const xtd = require("@xtalk/lang/common-data.js")

function blank_container(type_name,opts){
  let container = Object.assign({"::":type_name,"listeners":{}},opts);
  return container;
}

function make_container(initial,type_name,opts){
  let initialFn = initial;
  if(!("function" == (typeof initialFn))){
    initialFn = (function (){
      return initial;
    });
  }
  let data = initialFn();
  let container = Object.assign(
    {"::":type_name,"data":data,"initial":initialFn,"listeners":{}},
    opts
  );
  return container;
}

function make_listener_entry(listener_id,listener_type,callback,meta,pred){
  return {
    "callback":callback,
    "pred":pred,
    "meta":Object.assign(
        {"listener/id":listener_id,"listener/type":listener_type},
        meta
      )
  };
}

function listener_entryp(entry){
  return (null != entry) && ("function" == (typeof entry["callback"]));
}

function arrayify_path(x){
  if(Array.isArray(x)){
    return x;
  }
  if((null == x) || (((null != x) && ("object" == (typeof x)) && !Array.isArray(x)) && xtd.is_emptyp(x))){
    return [];
  }
  return [x];
}

function callback_data(event){
  if(!((null != event) && ("object" == (typeof event)) && !Array.isArray(event))){
    return event;
  }
  let out = Object.assign({},event);
  if(null != out["meta"]){
    delete(out["meta"]);
  }
  return out;
}

function callback_time(event){
  if(!((null != event) && ("object" == (typeof event)) && !Array.isArray(event))){
    return null;
  }
  if(null != event["time"]){
    return event["time"];
  }
  if(null != event["t"]){
    return event["t"];
  }
  return null;
}

function clear_listeners(container){
  let {listeners} = container;
  let cleared = {};
  let kept = {};
  for(let [id,entry] of Object.entries(listeners)){
    if(listener_entryp(entry)){
      cleared[id] = entry;
    }
    else{
      kept[id] = entry;
    }
  };
  container["listeners"] = kept;
  return cleared;
}

function add_listener(container,listener_id,listener_type,callback,meta,pred){
  let {listeners} = container;
  let entry = make_listener_entry(listener_id,listener_type,callback,meta,pred);
  listeners[listener_id] = entry;
  return entry;
}

function remove_listener(container,listener_id){
  let {listeners} = container;
  let entry = listeners[listener_id];
  if(!listener_entryp(entry)){
    return null;
  }
  delete(listeners[listener_id]);
  return entry;
}

function list_listeners(container){
  let {listeners} = container;
  let out = [];
  for(let [id,entry] of Object.entries(listeners)){
    if(listener_entryp(entry)){
      out.push(id);
    }
  };
  return out;
}

function list_listener_types(container){
  let {listeners} = container;
  let out = {};
  for(let [id,listener_entry] of Object.entries(listeners)){
    if(listener_entryp(listener_entry)){
      let {meta} = listener_entry;
      let t = meta["listener/type"];
      let arr = out[t];
      if(null == arr){
        arr = [];
        out[t] = arr;
      }
      arr.push(id);
    }
  };
  return out;
}

function trigger_entry(entry,event){
  let {callback,meta,pred} = entry;
  if((null == pred) || pred(event)){
    let nmeta = Object.assign(event["meta"] || {},meta);
    let listener_id = meta["listener/id"];
    return callback(listener_id,callback_data(event),callback_time(event),nmeta);
  }
}

function trigger_listeners(container,event){
  if(null == event){
    event = {};
  }
  let {listeners} = container;
  let triggered = [];
  for(let [id,entry] of Object.entries(listeners)){
    if(listener_entryp(entry)){
      trigger_entry(entry,event);
      triggered.push(id);
    }
  };
  return triggered;
}

function add_keyed_listener(container,key,listener_id,listener_type,callback,meta,pred){
  let {listeners} = container;
  let entry = make_listener_entry(listener_id,listener_type,callback,meta,pred);
  let group = listeners[key];
  if(null == group){
    group = {};
    listeners[key] = group;
  }
  group[listener_id] = entry;
  return entry;
}

function remove_keyed_listener(container,key,listener_id){
  let {listeners} = container;
  let group = listeners[key];
  if((null == group) || listener_entryp(group)){
    return null;
  }
  let entry = group[listener_id];
  delete(group[listener_id]);
  if(xtd.obj_emptyp(group)){
    delete(listeners[key]);
  }
  return entry;
}

function list_keyed_listeners(container,key){
  let {listeners} = container;
  let group = listeners[key];
  if((null == group) || listener_entryp(group)){
    return [];
  }
  return Object.keys(group);
}

function all_keyed_listeners(container){
  let {listeners} = container;
  let out = {};
  for(let [key,group] of Object.entries(listeners)){
    if(!listener_entryp(group)){
      out[key] = list_keyed_listeners(container,key);
    }
  };
  return out;
}

function trigger_keyed_listeners(container,key,event){
  if(null == event){
    event = {};
  }
  let {listeners} = container;
  let group = listeners[key];
  let triggered = [];
  if((null != group) && !listener_entryp(group)){
    for(let [id,entry] of Object.entries(group)){
      trigger_entry(entry,event);
      triggered.push(id);
    };
  }
  return triggered;
}

module.exports = {
  ["blank_container"]:blank_container,
  ["make_container"]:make_container,
  ["make_listener_entry"]:make_listener_entry,
  ["listener_entryp"]:listener_entryp,
  ["arrayify_path"]:arrayify_path,
  ["callback_data"]:callback_data,
  ["callback_time"]:callback_time,
  ["clear_listeners"]:clear_listeners,
  ["add_listener"]:add_listener,
  ["remove_listener"]:remove_listener,
  ["list_listeners"]:list_listeners,
  ["list_listener_types"]:list_listener_types,
  ["trigger_entry"]:trigger_entry,
  ["trigger_listeners"]:trigger_listeners,
  ["add_keyed_listener"]:add_keyed_listener,
  ["remove_keyed_listener"]:remove_keyed_listener,
  ["list_keyed_listeners"]:list_keyed_listeners,
  ["all_keyed_listeners"]:all_keyed_listeners,
  ["trigger_keyed_listeners"]:trigger_keyed_listeners
}