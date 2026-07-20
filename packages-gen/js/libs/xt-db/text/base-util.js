const xtd = require("@xtalk/lang/common-data.js")

function collect_routes(routes,type){
  return xtd.arr_juxt(routes,function (e){
    return e["url"];
  },function (e){
    return Object.assign({"type":type},e);
  });
}

function collect_views(routes){
  let out = {};
  for(let route of routes){
    let {view} = route;
    let {table,tag,type} = view;
    let v0 = out[table];
    if(null == v0){
      v0 = {};
      out[table] = v0;
    }
    let v1 = v0[type];
    if(null == v1){
      v1 = {};
      v0[type] = v1;
    }
    v1[tag] = route;
  };
  return out;
}

function merge_views(views,acc){
  let merge_fn = function (e,view_entry){
    Object.assign(e["select"] || {},view_entry["select"]);
    Object.assign(e["return"] || {},view_entry["return"]);
    return e;
  };
  return views.reduce(function (out,view){
    return xtd.obj_assign_with(out,view,merge_fn);
  },acc || {});
}

function keepf_limit(arr,pred,f,n){
  let out = [];
  let i = 0;
  for(let e of arr){
    if(i == n){
      return out;
    }
    if(pred(e)){
      let interim = f(e);
      if(("number" == (typeof interim)) || ("string" == (typeof interim)) || xtd.obj_not_emptyp(interim)){
        out.push(interim);
        i = (i + 1);
      }
    }
  };
  return out;
}

function lu_nested(obj,key_fn){
  if(null == obj){
    return obj;
  }
  else if((null != obj) && ("object" == (typeof obj)) && !Array.isArray(obj)){
    return xtd.obj_map(obj,function (v){
      return lu_nested(v,key_fn);
    });
  }
  else if(Array.isArray(obj)){
    return lu_nested(xtd.arr_juxt(obj,key_fn,function (x){
      return x;
    }),key_fn);
  }
  else{
    return obj;
  }
}

function lu_map(arr){
  return lu_nested(arr,function (v){
    return v["id"];
  });
}

module.exports = {
  ["collect_routes"]:collect_routes,
  ["collect_views"]:collect_views,
  ["merge_views"]:merge_views,
  ["keepf_limit"]:keepf_limit,
  ["lu_nested"]:lu_nested,
  ["lu_map"]:lu_map
}