const event_common = require("@xtalk/event/base-listener.js")

const xtd = require("@xtalk/lang/common-data.js")

function make_box(initial){
  let initialFn = initial;
  if(!("function" == (typeof initialFn))){
    let initialData = initialFn;
    initialFn = (function (){
      return initialData;
    });
  }
  let data = initialFn();
  return {
    "::":"event.box",
    "listeners":{},
    "data":data,
    "initial":initialFn
  };
}

function check_event(event,path){
  let evpath = event["path"];
  if(path.length > evpath.length){
    return false;
  }
  for(let i = 0; i < path.length; ++i){
    let v = path[i];
    if(v != evpath[i]){
      return false;
    }
  };
  return true;
}

function add_listener(box,listener_id,path,callback,meta){
  path = event_common.arrayify_path(path);
  return event_common.add_listener(box,listener_id,"box",callback,Object.assign({"box/path":path},meta),function (event){
    return check_event(event,path);
  });
}

var remove_listener = event_common.remove_listener;

var list_listeners = event_common.list_listeners;

function get_data(box,path){
  let {data} = box;
  path = event_common.arrayify_path(path);
  return xtd.get_in(data,path);
}

function set_data_raw(box,path,value){
  let {data} = box;
  if(xtd.arr_emptyp(path)){
    box["data"] = value;
  }
  else{
    return xtd.set_in(data,path,value);
  }
}

function set_data(box,path,value){
  let {data} = box;
  path = event_common.arrayify_path(path);
  set_data_raw(box,path,value);
  return event_common.trigger_listeners(box,{"path":path,"value":value,"data":data});
}

function del_data_raw(box,path){
  path = event_common.arrayify_path(path);
  let {data} = box;
  let ppath = path.slice(0,path.length - 1);
  let parent = xtd.get_in(data,ppath);
  if(null != parent){
    let val = parent[path[path.length + -1]];
    delete(parent[path[path.length + -1]]);
    return null != val;
  }
  return false;
}

function del_data(box,path){
  path = event_common.arrayify_path(path);
  let {data} = box;
  if(del_data_raw(box,path)){
    return event_common.trigger_listeners(box,{"path":path,"value":null,"data":data});
  }
}

function reset_data(box){
  let {initial} = box;
  return set_data(box,[],initial());
}

function merge_data(box,path,value){
  path = event_common.arrayify_path(path);
  let prev = get_data(box,path);
  let merged = xtd.obj_assign(xtd.obj_clone(prev),value);
  return set_data(box,path,merged);
}

function append_data(box,path,value){
  path = event_common.arrayify_path(path);
  let arr = get_data(box,path).slice();
  arr.push(value);
  return set_data(box,path,arr);
}

module.exports = {
  ["make_box"]:make_box,
  ["check_event"]:check_event,
  ["add_listener"]:add_listener,
  ["remove_listener"]:remove_listener,
  ["list_listeners"]:list_listeners,
  ["get_data"]:get_data,
  ["set_data_raw"]:set_data_raw,
  ["set_data"]:set_data,
  ["del_data_raw"]:del_data_raw,
  ["del_data"]:del_data,
  ["reset_data"]:reset_data,
  ["merge_data"]:merge_data,
  ["append_data"]:append_data
}