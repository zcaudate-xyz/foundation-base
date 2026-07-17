function is_emptyp(res){
  if(null == res){
    return true;
  }
  else if("string" == (typeof res)){
    return 0 == res.length;
  }
  else if(Array.isArray(res)){
    return 0 == res.length;
  }
  else if((null != res) && ("object" == (typeof res)) && !Array.isArray(res)){
    for(let [i,v] of Object.entries(res)){
      return false;
    };
    return true;
  }
  else{
    throw "Invalid type - " + String(res);
  }
}

function not_emptyp(res){
  if(null == res){
    return false;
  }
  else if("string" == (typeof res)){
    return 0 < res.length;
  }
  else if(Array.isArray(res)){
    return 0 < res.length;
  }
  else if((null != res) && ("object" == (typeof res)) && !Array.isArray(res)){
    for(let [i,v] of Object.entries(res)){
      return true;
    };
    return false;
  }
  else{
    throw "Invalid type - " + String(res);
  }
}

function lu_create(){
  return new Map();
}

function lu_del(lu,key){
  lu.delete(key);
  return lu;
}

function lu_get(lu,key){
  return lu.get(key);
}

function lu_set(lu,key,value){
  lu.set(key,value);
  return lu;
}

function lu_eq(x,y){
  return x == y;
}

function first(arr){
  return arr[0];
}

function second(arr){
  return arr[1];
}

function nth(arr,i){
  return arr[i];
}

function last(arr){
  return arr[arr.length + -1];
}

function second_last(arr){
  return arr[(arr.length - 1) + -1];
}

function arr_emptyp(arr){
  if(null == arr){
    return true;
  }
  else{
    return 0 == arr.length;
  }
}

function arr_not_emptyp(arr){
  if(null == arr){
    return false;
  }
  else{
    return 0 != arr.length;
  }
}

function arrayify(x){
  if(Array.isArray(x)){
    return x;
  }
  if(null == x){
    return [];
  }
  return [x];
}

function arr_lookup(arr){
  let out = {};
  for(let k of arr){
    out[k] = true;
  };
  return out;
}

function arr_omit(arr,i){
  let out = [];
  for(let j = 0; j < arr.length; ++j){
    let e = arr[j];
    if(i != j){
      out.push(e);
    }
  };
  return out;
}

function arr_reverse(arr){
  let out = [];
  for(let i = arr.length; i > 0; i = (i + -1)){
    out.push(arr[i + -1]);
  };
  return out;
}

function arr_zip(ks,vs){
  let out = {};
  for(let i = 0; i < ks.length; ++i){
    let k = ks[i];
    out[k] = vs[i];
  };
  return out;
}

function arr_clone(arr){
  let out = [];
  for(let e of arr){
    out.push(e);
  };
  return out;
}

function arr_assign(arr,other){
  for(let e of other){
    arr.push(e);
  };
  return arr;
}

function arr_concat(arr,other){
  let out = [];
  for(let e of arr){
    out.push(e);
  };
  for(let e of other){
    out.push(e);
  };
  return out;
}

function arr_slice(arr,start,finish){
  let out = [];
  let finish_idx = null;
  if("number" == (typeof finish)){
    finish_idx = finish;
  }
  else{
    finish_idx = arr.length;
  }
  for(let i = start; i < finish_idx; i = (i + 1)){
    out.push(arr[i]);
  };
  return out;
}

function arr_rslice(arr,start,finish){
  let out = [];
  for(let i = start; i < finish; i = (i + 1)){
    out.unshift(arr[i]);
  };
  return out;
}

function arr_tail(arr,n){
  let t = arr.length;
  return arr_rslice(arr,Math.max(t - n,0),t);
}

function arr_range(x){
  let arr = [x];
  if(Array.isArray(x)){
    arr = x;
  }
  let arrlen = arr.length;
  let start = 0;
  if(1 < arrlen){
    start = arr[0];
  }
  let finish = arr[0];
  if(1 < arrlen){
    finish = arr[1];
  }
  let step = 1;
  if(2 < arrlen){
    step = arr[2];
  }
  let out = [start];
  let i = step + start;
  if((0 < step) && (start < finish)){
    while(i < finish){
      out.push(i);
      i = (i + step);
    }
  }
  else if((0 > step) && (finish < start)){
    while(i > finish){
      out.push(i);
      i = (i + step);
    }
  }
  else{
    return [];
  }
  return out;
}

function arr_intersection(arr,other){
  let lu = arr_lookup(arr);
  let out = [];
  for(let e of other){
    if(null != lu[e]){
      out.push(e);
    }
  };
  return out;
}

function arr_difference(arr,other){
  let lu = arr_lookup(arr);
  let out = [];
  for(let e of other){
    if(!(null != lu[e])){
      out.push(e);
    }
  };
  return out;
}

function arr_union(arr,other){
  let lu = {};
  for(let e of arr){
    lu[e] = e;
  };
  for(let e of other){
    lu[e] = e;
  };
  let out = [];
  for(let v of Object.values(lu)){
    out.push(v);
  };
  return out;
}

function arr_shuffle(arr){
  let tmp_val = null;
  let tmp_idx = null;
  let total = arr.length;
  for(let i = 0; i < total; i = (i + 1)){
    tmp_idx = (0 + Math.floor(Math.random() * total));
    tmp_val = arr[tmp_idx];
    arr[tmp_idx] = arr[i];
    arr[i] = tmp_val;
  };
  return arr;
}

function arr_pushl(arr,v,n){
  arr.push(v);
  if(arr.length > n){
    arr.shift();
  }
  return arr;
}

function arr_pushr(arr,v,n){
  arr.unshift(v);
  if(arr.length > n){
    arr.pop();
  }
  return arr;
}

function arr_interpose(arr,elem){
  let out = [];
  for(let e of arr){
    out.push(e);
    out.push(elem);
  };
  out.pop();
  return out;
}

function arr_random(arr){
  let idx = Math.floor(arr.length * Math.random());
  return arr[idx];
}

function arr_sample(arr,dist){
  let q = Math.random();
  for(let i = 0; i < dist.length; ++i){
    let p = dist[i];
    q = (q - p);
    if(q < 0){
      return arr[i];
    }
  };
}

function obj_emptyp(obj){
  for(let k of Object.keys(obj)){
    return false;
  };
  return true;
}

function obj_not_emptyp(obj){
  for(let k of Object.keys(obj)){
    return true;
  };
  return false;
}

function obj_first_key(obj){
  for(let k of Object.keys(obj)){
    return k;
  };
  return null;
}

function obj_first_val(obj){
  for(let v of Object.values(obj)){
    return v;
  };
  return null;
}

function obj_keys(obj){
  let out = [];
  if(null != obj){
    for(let k of Object.keys(obj)){
      out.push(k);
    };
  }
  return out;
}

function obj_vals(obj){
  let out = [];
  if(null != obj){
    for(let v of Object.values(obj)){
      out.push(v);
    };
  }
  return out;
}

function obj_pairs(obj){
  let out = [];
  if(null != obj){
    for(let [k,v] of Object.entries(obj)){
      out.push([k,v]);
    };
  }
  return out;
}

function obj_clone(obj){
  let out = {};
  if(null != obj){
    for(let [k,v] of Object.entries(obj)){
      out[k] = v;
    };
  }
  return out;
}

function obj_assign(obj,m){
  if(null == obj){
    obj = {};
  }
  if(null != m){
    for(let [k,v] of Object.entries(m)){
      obj[k] = v;
    };
  }
  return obj;
}

function obj_assign_nested(obj,m){
  if(null == obj){
    obj = {};
  }
  if(null != m){
    for(let [k,mv] of Object.entries(m)){
      let v = obj[k];
      if(((null != mv) && ("object" == (typeof mv)) && !Array.isArray(mv)) && ((null != v) && ("object" == (typeof v)) && !Array.isArray(v))){
        obj[k] = obj_assign_nested(v,mv);
      }
      else{
        obj[k] = mv;
      }
    };
  }
  return obj;
}

function obj_assign_with(obj,m,f){
  if(null != m){
    let input = {};
    if((null != m) && ("object" == (typeof m)) && !Array.isArray(m)){
      input = m;
    }
    for(let [k,mv] of Object.entries(input)){
      let merged = mv;
      if(null != obj[k]){
        merged = f(obj[k],mv);
      }
      obj[k] = merged;
    };
  }
  return obj;
}

function obj_from_pairs(pairs){
  let out = {};
  for(let pair of pairs){
    out[pair[0]] = pair[1];
  };
  return out;
}

function obj_del(obj,ks){
  for(let k of ks){
    delete(obj[k]);
  };
  return obj;
}

function obj_del_all(obj){
  for(let k of Object.keys(obj)){
    delete(obj[k]);
  };
  return obj;
}

function obj_pick(obj,ks){
  let out = {};
  if(null == obj){
    return out;
  }
  for(let k of ks){
    let v = obj[k];
    if(null != v){
      out[k] = v;
    }
  };
  return out;
}

function obj_omit(obj,ks){
  let out = {};
  let lu = {};
  for(let k of ks){
    lu[k] = true;
  };
  for(let [k,v] of Object.entries(obj)){
    if(!(null != lu[k])){
      out[k] = v;
    }
  };
  return out;
}

function obj_transpose(obj){
  let out = {};
  if(null != obj){
    for(let [k,v] of Object.entries(obj)){
      out[v] = k;
    };
  }
  return out;
}

function obj_nest(arr,v){
  let idx = arr.length;
  let out = v;
  while(true){
    if(idx == 0){
      return out;
    }
    let nested = {};
    let k = arr[idx + -1];
    nested[k] = out;
    out = nested;
    idx = (idx - 1);
  }
}

function get_in(obj,arr){
  if(null == obj){
    return null;
  }
  else if(null == arr){
    return obj;
  }
  else if(0 == arr.length){
    return obj;
  }
  else if(1 == arr.length){
    let k = arr[0];
    if(Array.isArray(obj)){
      return ("number" == (typeof k)) ? obj[k] : null;
    }
    else if((null != obj) && ("object" == (typeof obj)) && !Array.isArray(obj)){
      return obj[k];
    }
    else{
      return null;
    }
  }
  let total = arr.length;
  let i = 0;
  let curr = obj;
  while(i < total){
    if(null == curr){
      return null;
    }
    let k = arr[i];
    if(Array.isArray(curr)){
      if("number" == (typeof k)){
        curr = curr[k];
      }
      else{
        return null;
      }
    }
    else if((null != curr) && ("object" == (typeof curr)) && !Array.isArray(curr)){
      curr = curr[k];
    }
    else{
      return null;
    }
    if(null == curr){
      return null;
    }
    else{
      i = (i + 1);
    }
  }
  return curr;
}

function set_in(obj,arr,v){
  if(null == arr){
    arr = [];
  }
  if(0 == arr.length){
    return obj;
  }
  if(!((null != obj) && ("object" == (typeof obj)) && !Array.isArray(obj))){
    let idx = arr.length;
    let out = v;
    while(true){
      if(idx == 0){
        return out;
      }
      let nested = {};
      let k = arr[idx + -1];
      nested[k] = out;
      out = nested;
      idx = (idx - 1);
    }
  }
  let k = arr[0];
  let narr = arr_slice(arr,1,null);
  let child = obj[k];
  if(0 == narr.length){
    obj[k] = v;
  }
  else{
    obj[k] = set_in(child,narr,v);
  }
  return obj;
}

function obj_intersection(obj,other){
  let out = [];
  for(let k of Object.keys(other)){
    if(null != obj[k]){
      out.push(k);
    }
  };
  return out;
}

function obj_keys_nested(m,path){
  let out = [];
  for(let [k,v] of Object.entries(m)){
    let npath = [...path];
    npath.push(k);
    if((null != v) && ("object" == (typeof v)) && !Array.isArray(v)){
      for(let e of obj_keys_nested(v,npath)){
        out.push(e);
      };
    }
    else{
      out.push([npath,v]);
    }
  };
  return out;
}

function obj_difference(obj,other){
  let out = [];
  for(let k of Object.keys(other)){
    if(!(null != obj[k])){
      out.push(k);
    }
  };
  return out;
}

function swap_key(obj,k,f,args){
  let inputs = args.slice();
  inputs.unshift(obj[k]);
  obj[k] = f.apply(null,inputs);
  return obj;
}

function to_flat(obj){
  let out = [];
  if((null != obj) && ("object" == (typeof obj)) && !Array.isArray(obj)){
    for(let [k,v] of Object.entries(obj)){
      out.push(k);
      out.push(v);
    };
  }
  else if(Array.isArray(obj)){
    for(let e of obj){
      out.push(e[0]);
      out.push(e[1]);
    };
  }
  return out;
}

function set_pair_step(out,k,v){
  out[k] = v;
  return out;
}

function from_flat(arr,f,init){
  let out = init;
  let k = null;
  for(let i = 0; i < arr.length; ++i){
    let e = arr[i];
    if(0 == (i % 2)){
      k = e;
    }
    else{
      out = f(out,k,e);
    }
  };
  return out;
}

function arr_every(arr,pred){
  for(let i = 0; i < arr.length; ++i){
    let v = arr[i];
    if(!pred(v)){
      return false;
    }
  };
  return true;
}

function arr_some(arr,pred){
  for(let i = 0; i < arr.length; ++i){
    let v = arr[i];
    if(pred(v)){
      return true;
    }
  };
  return false;
}

function arr_each(arr,f){
  for(let e of arr){
    f(e);
  };
  return true;
}

function arr_find(arr,pred){
  for(let i = 0; i < arr.length; ++i){
    let v = arr[i];
    if(pred(v)){
      return i - 0;
    }
  };
  return -1;
}

function arr_map(arr,f){
  let out = [];
  for(let e of arr){
    out.push(f(e));
  };
  return out;
}

function arr_mapcat(arr,f){
  let out = [];
  for(let e of arr){
    let res = f(e);
    if(null != res){
      for(let v of res){
        out.push(v);
      };
    }
  };
  return out;
}

function arr_partition(arr,n){
  let out = [];
  let i = 0;
  let sarr = [];
  for(let e of arr){
    if(i == n){
      out.push(sarr);
      i = 0;
      sarr = [];
    }
    sarr.push(e);
    i = (i + 1);
  };
  if(0 < sarr.length){
    out.push(sarr);
  }
  return out;
}

function arr_filter(arr,pred){
  let out = [];
  for(let e of arr){
    if(pred(e)){
      out.push(e);
    }
  };
  return out;
}

function arr_keep(arr,f){
  let out = [];
  for(let e of arr){
    let v = f(e);
    if(null != v){
      out.push(v);
    }
  };
  return out;
}

function arr_keepf(arr,pred,f){
  let out = [];
  for(let e of arr){
    if(pred(e)){
      out.push(f(e));
    }
  };
  return out;
}

function arr_juxt(arr,key_fn,val_fn){
  let out = {};
  if(null != arr){
    for(let e of arr){
      out[key_fn(e)] = val_fn(e);
    };
  }
  return out;
}

function arr_foldl(arr,f,init){
  let out = init;
  for(let e of arr){
    out = f(out,e);
  };
  return out;
}

function arr_foldr(arr,f,init){
  let out = init;
  for(let i = arr.length; i > 0; i = (i + -1)){
    out = f(out,arr[i + -1]);
  };
  return out;
}

function arr_pipel(arr,e){
  return arr.reduce(function (x,f){
    return f(x);
  },e);
}

function arr_piper(arr,e){
  return arr.reduceRight(function (x,f){
    return f(x);
  },e);
}

function arr_group_by(arr,key_fn,view_fn){
  let out = {};
  if(null != arr){
    for(let e of arr){
      let g = key_fn(e);
      let garr = (null == out[g]) ? [] : out[g];
      out[g] = [];
      garr.push(view_fn(e));
      out[g] = garr;
    };
  }
  return out;
}

function arr_repeat(x,n){
  let out = [];
  for(let i = 0; i < (n - 0); i = (i + 1)){
    let item = x;
    if("function" == (typeof x)){
      item = x();
    }
    out.push(item);
  };
  return out;
}

function arr_normalise(arr){
  let total = arr.reduce(function (x,y){
    return x + y;
  },0);
  return arr.map(function (x){
    return x / total;
  });
}

function arr_sort(arr,key_fn,comp_fn){
  let tmp = null;
  let total = arr.length;
  for(let i = 0; i < (total - 1); i = (i + 1)){
    for(let j = i + 1; j < total; j = (j + 1)){
      let left = arr[i];
      let right = arr[j];
      if(comp_fn(key_fn(right),key_fn(left))){
        tmp = left;
        arr[i] = right;
        arr[j] = tmp;
      }
    };
  };
  return arr;
}

function arr_sorted_merge(arr,brr,comp_fn){
  arr = (arr || []);
  brr = (brr || []);
  let alen = arr.length;
  let blen = brr.length;
  let i = 0;
  let j = 0;
  let k = 0;
  let out = [];
  while((i < alen) && (j < blen)){
    let aitem = arr[i];
    let bitem = brr[j];
    if(comp_fn(aitem,bitem)){
      i = (i + 1);
      out.push(aitem);
    }
    else{
      j = (j + 1);
      out.push(bitem);
    }
  }
  while(i < alen){
    let aitem = arr[i];
    i = (i + 1);
    out.push(aitem);
  }
  while(j < blen){
    let bitem = brr[j];
    j = (j + 1);
    out.push(bitem);
  }
  return out;
}

function obj_map(obj,f){
  let out = {};
  if(null != obj){
    for(let [k,v] of Object.entries(obj)){
      out[k] = f(v);
    };
  }
  return out;
}

function obj_filter(obj,pred){
  let out = {};
  if(null != obj){
    for(let [k,v] of Object.entries(obj)){
      if(pred(v)){
        out[k] = v;
      }
    };
  }
  return out;
}

function obj_keep(obj,f){
  let out = {};
  if(null != obj){
    for(let [k,e] of Object.entries(obj)){
      let v = f(e);
      if(null != v){
        out[k] = v;
      }
    };
  }
  return out;
}

function obj_keepf(obj,pred,f){
  let out = {};
  if(null != obj){
    for(let [k,e] of Object.entries(obj)){
      if(pred(e)){
        out[k] = f(e);
      }
    };
  }
  return out;
}

function clone_shallow(x){
  if(null == x){
    return x;
  }
  else if((null != x) && ("object" == (typeof x)) && !Array.isArray(x)){
    return x.slice();
  }
  else if(Array.isArray(x)){
    return Object.assign({},x);
  }
  else{
    return x;
  }
}

function clone_nested_loop(x,lu){
  if(null == x){
    return x;
  }
  let cached = lu.get(x);
  if(null != cached){
    return cached;
  }
  else if((null != x) && ("object" == (typeof x)) && !Array.isArray(x)){
    let out = {};
    lu.set(x,out);
    for(let [k,v] of Object.entries(x)){
      out[k] = clone_nested_loop(v,lu);
    };
    return out;
  }
  else if(Array.isArray(x)){
    let out = [];
    lu.set(x,out);
    for(let e of x){
      out.push(clone_nested_loop(e,lu));
    };
    return out;
  }
  else{
    return x;
  }
}

function clone_nested(x){
  if(!(((null != x) && ("object" == (typeof x)) && !Array.isArray(x)) || Array.isArray(x))){
    return x;
  }
  else{
    return clone_nested_loop(x,new Map());
  }
}

function memoize_key_step(f,key,cache){
  let value = f(key);
  cache[key] = value;
  return value;
}

function memoize_key(f){
  let cache = {};
  let cache_fn = function (key){
    return memoize_key_step(f,key,cache);
  };
  return function (key){
    return cache[key] || cache_fn(key);
  };
}

function id_fn(x){
  return x["id"];
}

function key_fn(k){
  return function (x){
    return x[k];
  };
}

function template_entry(obj,template,props){
  if("function" == (typeof template)){
    return template(obj,props);
  }
  else if(null == template){
    return obj;
  }
  else if(Array.isArray(template)){
    return get_in(obj,template);
  }
  else{
    return template;
  }
}

function template_fn(template){
  return function (obj,props){
    return template_entry(obj,template,props);
  };
}

module.exports = {
  ["is_emptyp"]:is_emptyp,
  ["not_emptyp"]:not_emptyp,
  ["lu_create"]:lu_create,
  ["lu_del"]:lu_del,
  ["lu_get"]:lu_get,
  ["lu_set"]:lu_set,
  ["lu_eq"]:lu_eq,
  ["first"]:first,
  ["second"]:second,
  ["nth"]:nth,
  ["last"]:last,
  ["second_last"]:second_last,
  ["arr_emptyp"]:arr_emptyp,
  ["arr_not_emptyp"]:arr_not_emptyp,
  ["arrayify"]:arrayify,
  ["arr_lookup"]:arr_lookup,
  ["arr_omit"]:arr_omit,
  ["arr_reverse"]:arr_reverse,
  ["arr_zip"]:arr_zip,
  ["arr_clone"]:arr_clone,
  ["arr_assign"]:arr_assign,
  ["arr_concat"]:arr_concat,
  ["arr_slice"]:arr_slice,
  ["arr_rslice"]:arr_rslice,
  ["arr_tail"]:arr_tail,
  ["arr_range"]:arr_range,
  ["arr_intersection"]:arr_intersection,
  ["arr_difference"]:arr_difference,
  ["arr_union"]:arr_union,
  ["arr_shuffle"]:arr_shuffle,
  ["arr_pushl"]:arr_pushl,
  ["arr_pushr"]:arr_pushr,
  ["arr_interpose"]:arr_interpose,
  ["arr_random"]:arr_random,
  ["arr_sample"]:arr_sample,
  ["obj_emptyp"]:obj_emptyp,
  ["obj_not_emptyp"]:obj_not_emptyp,
  ["obj_first_key"]:obj_first_key,
  ["obj_first_val"]:obj_first_val,
  ["obj_keys"]:obj_keys,
  ["obj_vals"]:obj_vals,
  ["obj_pairs"]:obj_pairs,
  ["obj_clone"]:obj_clone,
  ["obj_assign"]:obj_assign,
  ["obj_assign_nested"]:obj_assign_nested,
  ["obj_assign_with"]:obj_assign_with,
  ["obj_from_pairs"]:obj_from_pairs,
  ["obj_del"]:obj_del,
  ["obj_del_all"]:obj_del_all,
  ["obj_pick"]:obj_pick,
  ["obj_omit"]:obj_omit,
  ["obj_transpose"]:obj_transpose,
  ["obj_nest"]:obj_nest,
  ["get_in"]:get_in,
  ["set_in"]:set_in,
  ["obj_intersection"]:obj_intersection,
  ["obj_keys_nested"]:obj_keys_nested,
  ["obj_difference"]:obj_difference,
  ["swap_key"]:swap_key,
  ["to_flat"]:to_flat,
  ["set_pair_step"]:set_pair_step,
  ["from_flat"]:from_flat,
  ["arr_every"]:arr_every,
  ["arr_some"]:arr_some,
  ["arr_each"]:arr_each,
  ["arr_find"]:arr_find,
  ["arr_map"]:arr_map,
  ["arr_mapcat"]:arr_mapcat,
  ["arr_partition"]:arr_partition,
  ["arr_filter"]:arr_filter,
  ["arr_keep"]:arr_keep,
  ["arr_keepf"]:arr_keepf,
  ["arr_juxt"]:arr_juxt,
  ["arr_foldl"]:arr_foldl,
  ["arr_foldr"]:arr_foldr,
  ["arr_pipel"]:arr_pipel,
  ["arr_piper"]:arr_piper,
  ["arr_group_by"]:arr_group_by,
  ["arr_repeat"]:arr_repeat,
  ["arr_normalise"]:arr_normalise,
  ["arr_sort"]:arr_sort,
  ["arr_sorted_merge"]:arr_sorted_merge,
  ["obj_map"]:obj_map,
  ["obj_filter"]:obj_filter,
  ["obj_keep"]:obj_keep,
  ["obj_keepf"]:obj_keepf,
  ["clone_shallow"]:clone_shallow,
  ["clone_nested_loop"]:clone_nested_loop,
  ["clone_nested"]:clone_nested,
  ["memoize_key_step"]:memoize_key_step,
  ["memoize_key"]:memoize_key,
  ["id_fn"]:id_fn,
  ["key_fn"]:key_fn,
  ["template_entry"]:template_entry,
  ["template_fn"]:template_fn
}