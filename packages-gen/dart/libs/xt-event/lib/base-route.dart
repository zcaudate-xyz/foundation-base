import 'package:xtalk_event/base-listener.dart' as event_common;
import 'package:xtalk_lang/common-data.dart' as xtd;
import 'dart:convert';



interim_from_url(url) {
  var arr = ("/" + url).split("?");
  var body = arr[0];
  var search = null;
  if(1 < arr.length){
    search = arr[1];
  }
  var path = xtd.arr_filter(body.split("/"),(x) {
    return 0 < x.length;
  });
  var params = <dynamic, dynamic>{};
  if(null != search){
    var arr_50748 = search.split("&");
    for(var i50749 = 0; i50749 < arr_50748.length; ++i50749){
      var pair = arr_50748[i50749];
      if(0 < pair.length){
        var parts = pair.split("=");
        var key = parts[0];
        var val = parts[1];
        params[key] = val;
      }
    };
  }
  if((() {
    var dart_truthy__50743 = xtd.obj_emptyp(params);
    return (null != dart_truthy__50743) && (false != dart_truthy__50743);
  })()){
    return <dynamic, dynamic>{"path":path,"params":<dynamic, dynamic>{}};
  }
  else{
    return <dynamic, dynamic>{
      "path":path,
      "params":<dynamic, dynamic>{jsonEncode(path):params}
    };
  }
}

interim_to_url(interim) {
  var params = interim["params"];
  var path = interim["path"];
  var param_arr = <dynamic>[];
  var scoped_params = params[jsonEncode(path)];
  if(null == scoped_params){
    scoped_params = <dynamic, dynamic>{};
  }
  for(var entry_50770 in scoped_params.entries){
    var key = entry_50770.key;
    var val = entry_50770.value;
    if(null != val){
      param_arr.add(key + "=" + val);
    }
  };
  return path.join("/") + (((null != xtd.arr_not_emptyp(param_arr)) && (false != xtd.arr_not_emptyp(param_arr))) ? ("?" + param_arr.join("&")) : "");
}

path_to_tree(path, terminate) {
  var out = <dynamic, dynamic>{};
  var arr = <dynamic>[];
  var arr_50771 = path;
  for(var i = 0; i < arr_50771.length; ++i){
    var v = arr_50771[i];
    out[jsonEncode(arr)] = v;
    arr.add(v);
  };
  if((null != terminate) && (false != terminate)){
    out[jsonEncode(arr)] = null;
  }
  return out;
}

interim_to_tree(interim, terminate) {
  var params = interim["params"];
  var path = interim["path"];
  var tree = path_to_tree(path,terminate);
  tree["params"] = params;
  return tree;
}

path_from_tree(tree) {
  var path = <dynamic>[];
  var v = tree[jsonEncode(path)];
  while((() {
    var dart_truthy__50745 = xtd.arr_not_emptyp(v);
    return (null != dart_truthy__50745) && (false != dart_truthy__50745);
  })()){
    path.add(v);
    v = tree[jsonEncode(path)];
  }
  return path;
}

path_params_from_tree(tree, path) {
  var params = (tree["params"])[jsonEncode(path)];
  if(null == params){
    params = <dynamic, dynamic>{};
  }
  return params;
}

interim_from_tree(tree) {
  var params = tree["params"];
  var path = path_from_tree(tree);
  if(null == params){
    params = <dynamic, dynamic>{};
  }
  return <dynamic, dynamic>{"path":path,"params":params};
}

changed_params_raw(pparams, nparams) {
  if(null == pparams){
    pparams = <dynamic, dynamic>{};
  }
  if(null == nparams){
    nparams = <dynamic, dynamic>{};
  }
  var diff_fn = (m, other) {
    var out = <dynamic, dynamic>{};
    for(var entry_50792 in m.entries){
      var k = entry_50792.key;
      var v = entry_50792.value;
      if(v != other[k]){
        out[k] = true;
      }
    };
    return out;
  };
  return xtd.obj_assign(
    Function.apply((diff_fn as Function),<dynamic>[pparams,nparams]),
    Function.apply((diff_fn as Function),<dynamic>[nparams,pparams])
  );
}

changed_params(ptree, ntree, path) {
  var path_arr = path;
  if(null == path_arr){
    path_arr = <dynamic>[];
  }
  var pparams = path_params_from_tree(ptree,path_arr);
  var nparams = path_params_from_tree(ntree,path_arr);
  return changed_params_raw(pparams,nparams);
}

changed_path_raw(ppath, npath) {
  var all = <dynamic, dynamic>{};
  var arr = <dynamic>[];
  var i = 0;
  var changed = false;
  var arr_50793 = npath;
  for(var i50794 = 0; i50794 < arr_50793.length; ++i50794){
    var v = arr_50793[i50794];
    var pv = null;
    if(i < ppath.length){
      pv = ppath[i];
    }
    if(pv != v){
      changed = true;
    }
    if((null != changed) && (false != changed)){
      all[jsonEncode(arr)] = true;
    }
    arr.add(v);
    i = (i + 1);
  };
  return all;
}

changed_path(ptree, ntree) {
  var ppath = path_from_tree(ptree);
  var npath = path_from_tree(ntree);
  return changed_path_raw(ppath,npath);
}

get_url(route) {
  var tree = route["tree"];
  return interim_to_url(interim_from_tree(tree));
}

get_segment(route, path) {
  var tree = route["tree"];
  path = event_common.arrayify_path(path);
  var pkey = jsonEncode(path);
  return tree[pkey];
}

get_param(route, param, path) {
  var tree = route["tree"];
  path = ((null == path) ? path_from_tree(tree) : path);
  path = event_common.arrayify_path(path);
  return (path_params_from_tree(tree,path))[param];
}

get_all_params(route, path) {
  var tree = route["tree"];
  path = ((null == path) ? path_from_tree(tree) : path);
  path = event_common.arrayify_path(path);
  return path_params_from_tree(tree,path);
}

make_route(initial) {
  var input = ((initial.runtimeType).toString().contains("Function") || (initial.runtimeType).toString().contains("=>") || (initial).toString().startsWith("Closure")) ? initial() : initial;
  var interim = interim_from_url(input);
  var tree = interim_to_tree(interim,false);
  return event_common.blank_container(
    "event.route",
    <dynamic, dynamic>{"tree":tree,"history":<dynamic>[]}
  );
}

add_url_listener(route, listener_id, callback, meta) {
  return event_common.add_listener(route,listener_id,"route.url",callback,meta,(event) {
    return true;
  });
}

add_path_listener(route, path, listener_id, callback, meta) {
  path = event_common.arrayify_path(path);
  var pkey = jsonEncode(path);
  return event_common.add_listener(route,listener_id,"route.path",callback,xtd.obj_assign(<dynamic, dynamic>{"route/path":path},meta),(event) {
    return (event["path"])[pkey];
  });
}

add_param_listener(route, param, listener_id, callback, meta) {
  return event_common.add_listener(route,listener_id,"route.param",callback,xtd.obj_assign(<dynamic, dynamic>{"route/param":param},meta),(event) {
    return (event["params"])[param];
  });
}

add_full_listener(route, path, param, listener_id, callback, meta) {
  path = event_common.arrayify_path(path);
  var pkey = jsonEncode(path);
  return event_common.add_listener(route,listener_id,"route.full",callback,xtd.obj_assign(
    <dynamic, dynamic>{"route/path":path,"route/param":param},
    meta
  ),(event) {
    return (true == (event["path"])[pkey]) && (null != (event["params"])[param]);
  });
}

remove_listener(route, listener_id) {
  return event_common.remove_listener(route,listener_id);
}

list_listeners(route) {
  return event_common.list_listeners(route);
}

set_url(route, url, terminate) {
  var listeners = route["listeners"];
  var tree = route["tree"];
  var ninterim = interim_from_url(url);
  var ninterim_params = ninterim["params"];
  var all_params = tree["params"];
  var ppath = path_from_tree(tree);
  var npath = ninterim["path"];
  var pkey = jsonEncode(npath);
  var pparams = all_params[pkey];
  var nparams = ninterim_params[pkey];
  if(null == nparams){
    nparams = <dynamic, dynamic>{};
  }
  var dpath = changed_path_raw(ppath,npath);
  var dparams = changed_params_raw(pparams,nparams);
  xtd.obj_assign(tree,path_to_tree(npath,terminate));
  if((() {
    var dart_truthy__50744 = xtd.obj_emptyp(nparams);
    return (null != dart_truthy__50744) && (false != dart_truthy__50744);
  })()){
    if(all_params.containsKey(pkey)){
      all_params.remove(pkey);
    }
  }
  else{
    all_params[pkey] = nparams;
  }
  var history = route["history"];
  xtd.arr_pushl(history,url,50);
  return event_common.trigger_listeners(
    route,
    <dynamic, dynamic>{"type":"route.url","params":dparams,"path":dpath}
  );
}

set_path(route, path, params) {
  var tree = route["tree"];
  var all_params = tree["params"];
  var ppath = path_from_tree(tree);
  var npath = path;
  if(null == npath){
    npath = ppath;
  }
  npath = xtd.arrayify(npath);
  var pkey = jsonEncode(npath);
  var pparams = all_params[pkey];
  var nparams = params;
  if(null == nparams){
    nparams = pparams;
  }
  if(null == nparams){
    nparams = <dynamic, dynamic>{};
  }
  var dpath = changed_path_raw(ppath,npath);
  var dparams = changed_params_raw(pparams,nparams);
  xtd.obj_assign(tree,path_to_tree(npath,true));
  if((() {
    var dart_truthy__50747 = xtd.obj_emptyp(nparams);
    return (null != dart_truthy__50747) && (false != dart_truthy__50747);
  })()){
    if(all_params.containsKey(pkey)){
      all_params.remove(pkey);
    }
  }
  else{
    all_params[pkey] = nparams;
  }
  var history = route["history"];
  xtd.arr_pushl(history,get_url(route),50);
  return event_common.trigger_listeners(
    route,
    <dynamic, dynamic>{"type":"route.path","params":dparams,"path":dpath}
  );
}

set_segment(route, path, value) {
  var tree = route["tree"];
  path = xtd.arrayify(path);
  var pkey = jsonEncode(path);
  var pvalue = tree[pkey];
  tree[pkey] = value;
  var history = route["history"];
  xtd.arr_pushl(history,get_url(route),50);
  return event_common.trigger_listeners(route,<dynamic, dynamic>{
    "type":"route.path",
    "params":<dynamic, dynamic>{},
    "path":<dynamic, dynamic>{pkey:true}
  });
}

set_param(route, param, value, path) {
  var tree = route["tree"];
  if(null == path){
    path = path_from_tree(tree);
  }
  path = xtd.arrayify(path);
  var pkey = jsonEncode(path);
  var all_params = tree["params"];
  var pparams = all_params[pkey];
  if(null == pparams){
    pparams = <dynamic, dynamic>{};
  }
  var pvalue = pparams[param];
  if(pvalue != value){
    if(null == value){
      pparams.remove(param);
    }
    else{
      pparams[param] = value;
    }
    if((() {
      var dart_truthy__50746 = xtd.obj_emptyp(pparams);
      return (null != dart_truthy__50746) && (false != dart_truthy__50746);
    })()){
      if(all_params.containsKey(pkey)){
        all_params.remove(pkey);
      }
    }
    else{
      all_params[pkey] = pparams;
    }
    var history = route["history"];
    xtd.arr_pushl(history,get_url(route),50);
    return event_common.trigger_listeners(route,<dynamic, dynamic>{
      "type":"route.params",
      "params":<dynamic, dynamic>{param:true},
      "path":<dynamic, dynamic>{}
    });
  }
  else{
    return <dynamic>[];
  }
}

reset_route(route, url) {
  route["history"] = <dynamic>[];
  var route_url = (null == url) ? "" : url;
  route["tree"] = interim_to_tree(interim_from_url(route_url),true);
  return set_url(route,route_url,true);
}