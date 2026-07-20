const event_common = require("@xtalk/event/base-listener.js")

const xtd = require("@xtalk/lang/common-data.js")

function interim_from_url(url){
  let arr = ("/" + url).split("?");
  let body = arr[0];
  let search = null;
  if(1 < arr.length){
    search = arr[1];
  }
  let path = body.split("/").filter(function (x){
    return 0 < x.length;
  });
  let params = {};
  if(null != search){
    for(let pair of search.split("&")){
      if(0 < pair.length){
        let parts = pair.split("=");
        let key = parts[0];
        let val = parts[1];
        params[key] = val;
      }
    };
  }
  if(xtd.obj_emptyp(params)){
    return {"path":path,"params":{}};
  }
  else{
    return {"path":path,"params":{[JSON.stringify(path)]:params}};
  }
}

function interim_to_url(interim){
  let {params,path} = interim;
  let param_arr = [];
  let scoped_params = params[JSON.stringify(path)];
  if(null == scoped_params){
    scoped_params = {};
  }
  for(let [key,val] of Object.entries(scoped_params)){
    if(null != val){
      param_arr.push(key + "=" + val);
    }
  };
  return path.join("/") + (xtd.arr_not_emptyp(param_arr) ? ("?" + param_arr.join("&")) : "");
}

function path_to_tree(path,terminate){
  let out = {};
  let arr = [];
  for(let i = 0; i < path.length; ++i){
    let v = path[i];
    out[JSON.stringify(arr)] = v;
    arr.push(v);
  };
  if(terminate){
    out[JSON.stringify(arr)] = null;
  }
  return out;
}

function interim_to_tree(interim,terminate){
  let {params,path} = interim;
  let tree = path_to_tree(path,terminate);
  tree["params"] = params;
  return tree;
}

function path_from_tree(tree){
  let path = [];
  let v = tree[JSON.stringify(path)];
  while(xtd.arr_not_emptyp(v)){
    path.push(v);
    v = tree[JSON.stringify(path)];
  }
  return path;
}

function path_params_from_tree(tree,path){
  let params = (tree["params"])[JSON.stringify(path)];
  if(null == params){
    params = {};
  }
  return params;
}

function interim_from_tree(tree){
  let {params} = tree;
  let path = path_from_tree(tree);
  if(null == params){
    params = {};
  }
  return {"path":path,"params":params};
}

function changed_params_raw(pparams,nparams){
  if(null == pparams){
    pparams = {};
  }
  if(null == nparams){
    nparams = {};
  }
  let diff_fn = function (m,other){
    let out = {};
    for(let [k,v] of Object.entries(m)){
      if(v != other[k]){
        out[k] = true;
      }
    };
    return out;
  };
  return Object.assign(diff_fn(pparams,nparams),diff_fn(nparams,pparams));
}

function changed_params(ptree,ntree,path){
  let path_arr = path;
  if(null == path_arr){
    path_arr = [];
  }
  let pparams = path_params_from_tree(ptree,path_arr);
  let nparams = path_params_from_tree(ntree,path_arr);
  return changed_params_raw(pparams,nparams);
}

function changed_path_raw(ppath,npath){
  let all = {};
  let arr = [];
  let i = 0;
  let changed = false;
  for(let v of npath){
    let pv = null;
    if(i < ppath.length){
      pv = ppath[i];
    }
    if(pv != v){
      changed = true;
    }
    if(changed){
      all[JSON.stringify(arr)] = true;
    }
    arr.push(v);
    i = (i + 1);
  };
  return all;
}

function changed_path(ptree,ntree){
  let ppath = path_from_tree(ptree);
  let npath = path_from_tree(ntree);
  return changed_path_raw(ppath,npath);
}

function get_url(route){
  let {tree} = route;
  return interim_to_url(interim_from_tree(tree));
}

function get_segment(route,path){
  let {tree} = route;
  path = event_common.arrayify_path(path);
  let pkey = JSON.stringify(path);
  return tree[pkey];
}

function get_param(route,param,path){
  let {tree} = route;
  path = ((null == path) ? path_from_tree(tree) : path);
  path = event_common.arrayify_path(path);
  return (path_params_from_tree(tree,path))[param];
}

function get_all_params(route,path){
  let {tree} = route;
  path = ((null == path) ? path_from_tree(tree) : path);
  path = event_common.arrayify_path(path);
  return path_params_from_tree(tree,path);
}

function make_route(initial){
  let input = ("function" == (typeof initial)) ? initial() : initial;
  let interim = interim_from_url(input);
  let tree = interim_to_tree(interim,false);
  return event_common.blank_container("event.route",{"tree":tree,"history":[]});
}

function add_url_listener(route,listener_id,callback,meta){
  return event_common.add_listener(route,listener_id,"route.url",callback,meta,function (event){
    return true;
  });
}

function add_path_listener(route,path,listener_id,callback,meta){
  path = event_common.arrayify_path(path);
  let pkey = JSON.stringify(path);
  return event_common.add_listener(route,listener_id,"route.path",callback,Object.assign({"route/path":path},meta),function (event){
    return (event["path"])[pkey];
  });
}

function add_param_listener(route,param,listener_id,callback,meta){
  return event_common.add_listener(route,listener_id,"route.param",callback,Object.assign({"route/param":param},meta),function (event){
    return (event["params"])[param];
  });
}

function add_full_listener(route,path,param,listener_id,callback,meta){
  path = event_common.arrayify_path(path);
  let pkey = JSON.stringify(path);
  return event_common.add_listener(route,listener_id,"route.full",callback,Object.assign({"route/path":path,"route/param":param},meta),function (event){
    return (true == (event["path"])[pkey]) && (null != (event["params"])[param]);
  });
}

function remove_listener(route,listener_id){
  return event_common.remove_listener(route,listener_id);
}

function list_listeners(route){
  return event_common.list_listeners(route);
}

function set_url(route,url,terminate){
  let {listeners,tree} = route;
  let ninterim = interim_from_url(url);
  let ninterim_params = ninterim["params"];
  let all_params = tree["params"];
  let ppath = path_from_tree(tree);
  let npath = ninterim["path"];
  let pkey = JSON.stringify(npath);
  let pparams = all_params[pkey];
  let nparams = ninterim_params[pkey];
  if(null == nparams){
    nparams = {};
  }
  let dpath = changed_path_raw(ppath,npath);
  let dparams = changed_params_raw(pparams,nparams);
  Object.assign(tree,path_to_tree(npath,terminate));
  if(xtd.obj_emptyp(nparams)){
    if(null != all_params[pkey]){
      delete(all_params[pkey]);
    }
  }
  else{
    all_params[pkey] = nparams;
  }
  let {history} = route;
  xtd.arr_pushl(history,url,50);
  return event_common.trigger_listeners(route,{"type":"route.url","params":dparams,"path":dpath});
}

function set_path(route,path,params){
  let {tree} = route;
  let all_params = tree["params"];
  let ppath = path_from_tree(tree);
  let npath = path;
  if(null == npath){
    npath = ppath;
  }
  npath = xtd.arrayify(npath);
  let pkey = JSON.stringify(npath);
  let pparams = all_params[pkey];
  let nparams = params;
  if(null == nparams){
    nparams = pparams;
  }
  if(null == nparams){
    nparams = {};
  }
  let dpath = changed_path_raw(ppath,npath);
  let dparams = changed_params_raw(pparams,nparams);
  Object.assign(tree,path_to_tree(npath,true));
  if(xtd.obj_emptyp(nparams)){
    if(null != all_params[pkey]){
      delete(all_params[pkey]);
    }
  }
  else{
    all_params[pkey] = nparams;
  }
  let {history} = route;
  xtd.arr_pushl(history,get_url(route),50);
  return event_common.trigger_listeners(route,{"type":"route.path","params":dparams,"path":dpath});
}

function set_segment(route,path,value){
  let {tree} = route;
  path = xtd.arrayify(path);
  let pkey = JSON.stringify(path);
  let pvalue = tree[pkey];
  tree[pkey] = value;
  let {history} = route;
  xtd.arr_pushl(history,get_url(route),50);
  return event_common.trigger_listeners(route,{"type":"route.path","params":{},"path":{[pkey]:true}});
}

function set_param(route,param,value,path){
  let {tree} = route;
  if(null == path){
    path = path_from_tree(tree);
  }
  path = xtd.arrayify(path);
  let pkey = JSON.stringify(path);
  let all_params = tree["params"];
  let pparams = all_params[pkey];
  if(null == pparams){
    pparams = {};
  }
  let pvalue = pparams[param];
  if(pvalue != value){
    if(null == value){
      delete(pparams[param]);
    }
    else{
      pparams[param] = value;
    }
    if(xtd.obj_emptyp(pparams)){
      if(null != all_params[pkey]){
        delete(all_params[pkey]);
      }
    }
    else{
      all_params[pkey] = pparams;
    }
    let {history} = route;
    xtd.arr_pushl(history,get_url(route),50);
    return event_common.trigger_listeners(
      route,
      {"type":"route.params","params":{[param]:true},"path":{}}
    );
  }
  else{
    return [];
  }
}

function reset_route(route,url){
  route["history"] = [];
  let route_url = (null == url) ? "" : url;
  route["tree"] = interim_to_tree(interim_from_url(route_url),true);
  set_url(route,route_url,true);
}

module.exports = {
  ["interim_from_url"]:interim_from_url,
  ["interim_to_url"]:interim_to_url,
  ["path_to_tree"]:path_to_tree,
  ["interim_to_tree"]:interim_to_tree,
  ["path_from_tree"]:path_from_tree,
  ["path_params_from_tree"]:path_params_from_tree,
  ["interim_from_tree"]:interim_from_tree,
  ["changed_params_raw"]:changed_params_raw,
  ["changed_params"]:changed_params,
  ["changed_path_raw"]:changed_path_raw,
  ["changed_path"]:changed_path,
  ["get_url"]:get_url,
  ["get_segment"]:get_segment,
  ["get_param"]:get_param,
  ["get_all_params"]:get_all_params,
  ["make_route"]:make_route,
  ["add_url_listener"]:add_url_listener,
  ["add_path_listener"]:add_path_listener,
  ["add_param_listener"]:add_param_listener,
  ["add_full_listener"]:add_full_listener,
  ["remove_listener"]:remove_listener,
  ["list_listeners"]:list_listeners,
  ["set_url"]:set_url,
  ["set_path"]:set_path,
  ["set_segment"]:set_segment,
  ["set_param"]:set_param,
  ["reset_route"]:reset_route
}