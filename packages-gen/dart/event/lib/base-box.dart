import 'package:xtalk_event/base-listener.dart' as event_common;

import 'package:xtalk_lang/common-data.dart' as xtd;

make_box(initial) {
  var initialFn = initial;
  if(!((initialFn.runtimeType).toString().contains("Function") || (initialFn.runtimeType).toString().contains("=>") || (initialFn).toString().startsWith("Closure"))){
    var initialData = initialFn;
    initialFn = (() {
      return initialData;
    });
  }
  var data = initialFn();
  return <dynamic, dynamic>{
    "::":"event.box",
    "listeners":<dynamic, dynamic>{},
    "data":data,
    "initial":initialFn
  };
}

check_event(event, path) {
  var evpath = event["path"];
  if(path.length > evpath.length){
    return false;
  }
  var arr_41344 = path;
  for(var i = 0; i < arr_41344.length; ++i){
    var v = arr_41344[i];
    if(v != evpath[i]){
      return false;
    }
  };
  return true;
}

add_listener(box, listener_id, path, callback, meta) {
  path = event_common.arrayify_path(path);
  return event_common.add_listener(box,listener_id,"box",callback,xtd.obj_assign(<dynamic, dynamic>{"box/path":path},meta),(event) {
    return check_event(event,path);
  });
}

var remove_listener = event_common.remove_listener;

var list_listeners = event_common.list_listeners;

get_data(box, path) {
  var data = box["data"];
  path = event_common.arrayify_path(path);
  return xtd.get_in(data,path);
}

set_data_raw(box, path, value) {
  var data = box["data"];
  if((() {
    var dart_truthy__41343 = xtd.arr_emptyp(path);
    return (null != dart_truthy__41343) && (false != dart_truthy__41343);
  })()){
    box["data"] = value;
  }
  else{
    return xtd.set_in(data,path,value);
  }
}

set_data(box, path, value) {
  var data = box["data"];
  path = event_common.arrayify_path(path);
  set_data_raw(box,path,value);
  return event_common.trigger_listeners(
    box,
    <dynamic, dynamic>{"path":path,"value":value,"data":data}
  );
}

del_data_raw(box, path) {
  path = event_common.arrayify_path(path);
  var data = box["data"];
  var ppath = path.sublist(0 - 0,path.length - 1);
  var parent = xtd.get_in(data,ppath);
  if(null != parent){
    var val = parent[path[path.length + -1]];
    parent.remove(path[path.length + -1]);
    return null != val;
  }
  return false;
}

del_data(box, path) {
  path = event_common.arrayify_path(path);
  var data = box["data"];
  if((() {
    var dart_truthy__41342 = del_data_raw(box,path);
    return (null != dart_truthy__41342) && (false != dart_truthy__41342);
  })()){
    return event_common.trigger_listeners(box,<dynamic, dynamic>{"path":path,"value":null,"data":data});
  }
}

reset_data(box) {
  var initial = box["initial"];
  return set_data(box,<dynamic>[],initial());
}

merge_data(box, path, value) {
  path = event_common.arrayify_path(path);
  var prev = get_data(box,path);
  var merged = xtd.obj_assign(xtd.obj_clone(prev),value);
  return set_data(box,path,merged);
}

append_data(box, path, value) {
  path = event_common.arrayify_path(path);
  var arr = xtd.arr_clone(get_data(box,path));
  arr.add(value);
  return set_data(box,path,arr);
}