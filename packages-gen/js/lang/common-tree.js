const xtd = require("@xtalk/lang/common-data.js")

const xtl = require("@xtalk/lang/common-lib.js")

function eq_nested_loop(src,dst,eq_obj,eq_arr,cache){
  cache = (cache || new Map());
  if(((null != src) && ("object" == (typeof src)) && !Array.isArray(src)) && ((null != dst) && ("object" == (typeof dst)) && !Array.isArray(dst))){
    if(cache.get(src) && cache.get(dst)){
      return true;
    }
    else{
      return eq_obj(src,dst,eq_obj,eq_arr,cache);
    }
  }
  else if(Array.isArray(src) && Array.isArray(dst)){
    if(cache.get(src) && cache.get(dst)){
      return true;
    }
    else{
      return eq_arr(src,dst,eq_obj,eq_arr,cache);
    }
  }
  else{
    return src == dst;
  }
}

function eq_shallow_raw(src,dst,eq_obj,eq_arr,cache){
  return src == dst;
}

function eq_shallow(obj,m){
  return eq_nested_loop(obj,m,eq_shallow_raw,eq_shallow_raw,null);
}

function eq_nested_obj(src,dst,eq_obj,eq_arr,cache){
  cache.set(src,src);
  cache.set(dst,dst);
  let ks_src = Object.keys(src);
  let ks_dst = Object.keys(dst);
  if(ks_src.length != ks_dst.length){
    return false;
  }
  for(let k of ks_src){
    if(!eq_nested_loop(src[k],dst[k],eq_obj,eq_arr,cache)){
      return false;
    }
  };
  return true;
}

function eq_nested_arr(src_arr,dst_arr,eq_obj,eq_arr,cache){
  cache.set(src_arr,src_arr);
  cache.set(dst_arr,dst_arr);
  if(src_arr.length != dst_arr.length){
    return false;
  }
  for(let i = 0; i < src_arr.length; ++i){
    let v = src_arr[i];
    if(!eq_nested_loop(v,dst_arr[i],eq_obj,eq_arr,cache)){
      return false;
    }
  };
  return true;
}

function eq_nested(obj,m){
  return eq_nested_loop(obj,m,eq_nested_obj,eq_nested_arr,null);
}

function tree_walk(x,pre_fn,post_fn){
  x = pre_fn(x);
  if(null == x){
    return post_fn(x);
  }
  else if((null != x) && ("object" == (typeof x)) && !Array.isArray(x)){
    let out = {};
    for(let [k,v] of Object.entries(x)){
      out[k] = tree_walk(v,pre_fn,post_fn);
    };
    return post_fn(out);
  }
  else if(Array.isArray(x)){
    let out = [];
    for(let e of x){
      out.push(tree_walk(e,pre_fn,post_fn));
    };
    return post_fn(out);
  }
  else{
    return post_fn(x);
  }
}

function tree_get_data(obj){
  if(null == obj){
    return obj;
  }
  else if((null != obj) && ("object" == (typeof obj)) && !Array.isArray(obj)){
    let out = {};
    for(let [k,v] of Object.entries(obj)){
      out[k] = tree_get_data(v);
    };
    return out;
  }
  else if(Array.isArray(obj)){
    let out = [];
    for(let e of obj){
      out.push(tree_get_data(e));
    };
    return out;
  }
  else if(("string" == (typeof obj)) || ("number" == (typeof obj)) || ("boolean" == (typeof obj))){
    return obj;
  }
  else{
    return "<" + xtl.type_native(obj) + ">";
  }
}

function tree_get_spec(obj){
  if((null != obj) && ("object" == (typeof obj)) && !Array.isArray(obj)){
    let out = {};
    for(let [k,v] of Object.entries(obj)){
      out[k] = tree_get_spec(v);
    };
    return out;
  }
  else if(Array.isArray(obj)){
    let out = [];
    for(let e of obj){
      out.push(tree_get_spec(e));
    };
    return out;
  }
  else{
    return xtl.type_native(obj);
  }
}

function tree_diff(obj,m){
  if(null == m){
    return {};
  }
  if(null == obj){
    return m;
  }
  let out = {};
  for(let [k,v] of Object.entries(m)){
    if(!eq_nested(obj[k],m[k])){
      out[k] = v;
    }
  };
  return out;
}

function tree_diff_nested(obj,m){
  if(null == m){
    return {};
  }
  if(null == obj){
    return m;
  }
  let out = {};
  let ks = Object.keys(m);
  for(let k of ks){
    let v = obj[k];
    let mv = m[k];
    if(((null != v) && ("object" == (typeof v)) && !Array.isArray(v)) && ((null != mv) && ("object" == (typeof mv)) && !Array.isArray(mv))){
      let dv = tree_diff_nested(v,mv);
      if(!xtd.obj_emptyp(dv)){
        out[k] = dv;
      }
    }
    else if(!eq_nested(v,mv)){
      out[k] = mv;
    }
  };
  return out;
}

module.exports = {
  ["eq_nested_loop"]:eq_nested_loop,
  ["eq_shallow_raw"]:eq_shallow_raw,
  ["eq_shallow"]:eq_shallow,
  ["eq_nested_obj"]:eq_nested_obj,
  ["eq_nested_arr"]:eq_nested_arr,
  ["eq_nested"]:eq_nested,
  ["tree_walk"]:tree_walk,
  ["tree_get_data"]:tree_get_data,
  ["tree_get_spec"]:tree_get_spec,
  ["tree_diff"]:tree_diff,
  ["tree_diff_nested"]:tree_diff_nested
}