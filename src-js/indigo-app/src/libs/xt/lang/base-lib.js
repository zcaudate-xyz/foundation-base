// xt.lang.base-lib/proto-create [19] 
export function proto_create(m){
  let out = {};
  for(let [k,f] of Object.entries(m)){
    if("function" == (typeof f)){
      out[k] = (function (...args){
        return f(this,...args);
      });
    }
    else{
      out[k] = f;
    }
  };
  return out;;
}

// xt.lang.base-lib/type-native [25] 
export function type_native(obj){
  if(obj == null){
    return null;
  }
  let t = typeof obj;
  if(t == "object"){
    if(Array.isArray(obj)){
      return "array";
    }
    else{
      let tn = obj["constructor"]["name"];
      if(tn == "Object"){
        return "object";
      }
      else{
        return tn;
      }
    }
  }
  else{
    return t;
  };
}

// xt.lang.base-lib/type-class [31] 
export function type_class(x){
  let ntype = type_native(x);
  if(ntype == "object"){
    return x["::"] || ntype;
  }
  else{
    return ntype;
  }
}

// xt.lang.base-lib/fn? [40] 
export function fnp(x){
  return "function" == (typeof x);
}

// xt.lang.base-lib/arr? [47] 
export function arrp(x){
  return Array.isArray(x);
}

// xt.lang.base-lib/obj? [54] 
export function objp(x){
  return (null != x) && ("object" == (typeof x)) && !Array.isArray(x);
}

// xt.lang.base-lib/id-fn [61] 
export function id_fn(x){
  return x["id"];
}

// xt.lang.base-lib/key-fn [68] 
export function key_fn(k){
  return function (x){
    return x[k];
  };
}

// xt.lang.base-lib/eq-fn [75] 
export function eq_fn(k,v){
  return function (x){
    return fnp(v) ? v(x[k]) : (v == x[k]);
  };
}

// xt.lang.base-lib/inc-fn [85] 
export function inc_fn(init){
  let i = init;
  if(null == i){
    i = -1;
  }
  let inc_fn = function (){
    i = (i + 1);
    return i;
  };
  return inc_fn;
}

// xt.lang.base-lib/identity [100] 
export function identity(x){
  return x;
}

// xt.lang.base-lib/noop [106] 
export function noop(){
  return null;
}

// xt.lang.base-lib/T [112] 
export function T(x){
  return true;
}

// xt.lang.base-lib/F [118] 
export function F(x){
  return false;
}

// xt.lang.base-lib/step-nil [129] 
export function step_nil(obj,pair){
  return null;
}

// xt.lang.base-lib/step-thrush [135] 
export function step_thrush(x,f){
  return f(x);
}

// xt.lang.base-lib/step-call [141] 
export function step_call(f,x){
  return f(x);
}

// xt.lang.base-lib/step-push [147] 
export function step_push(arr,e){
  arr.push(e);
  return arr;
}

// xt.lang.base-lib/step-set-key [154] 
export function step_set_key(obj,k,v){
  obj[k] = v;
  return obj;
}

// xt.lang.base-lib/step-set-fn [161] 
export function step_set_fn(obj,k){
  return function (v){
    return step_set_key(obj,k,v);
  };
}

// xt.lang.base-lib/step-set-pair [167] 
export function step_set_pair(obj,e){
  obj[e[0]] = e[1];
  return obj;
}

// xt.lang.base-lib/step-del-key [176] 
export function step_del_key(obj,k){
  delete(obj[k]);
  return obj;
}

// xt.lang.base-lib/starts-with? [188] 
export function starts_withp(s,match){
  return s.substring(0,match.length) == match;
}

// xt.lang.base-lib/ends-with? [197] 
export function ends_withp(s,match){
  return match == s.substring(s.length - match.length,s.length);
}

// xt.lang.base-lib/capitalize [209] 
export function capitalize(s){
  return s.substring(0,1).toUpperCase() + s.substring(1);
}

// xt.lang.base-lib/decapitalize [220] 
export function decapitalize(s){
  return s.substring(0,1).toLowerCase() + s.substring(1);
}

// xt.lang.base-lib/pad-left [231] 
export function pad_left(s,n,ch){
  let l = n - s.length;
  let out = s;
  for(let i = 0; i < l; i = (i + 1)){
    out = (ch + out);
  };
  return out;
}

// xt.lang.base-lib/pad-right [241] 
export function pad_right(s,n,ch){
  let l = n - s.length;
  let out = s;
  for(let i = 0; i < l; i = (i + 1)){
    out = (out + ch);
  };
  return out;
}

// xt.lang.base-lib/pad-lines [251] 
export function pad_lines(s,n,ch){
  let lines = s.split("\n");
  let out = "";
  for(let line of lines){
    if(0 < (out).length){
      out = (out + "\n");
    }
    out = (out + pad_left("",n," ") + line);
  };
  return out;
}

// xt.lang.base-lib/mod-pos [268] 
export function mod_pos(val,modulo){
  let out = val % modulo;
  return (out < 0) ? (out + modulo) : out;
}

// xt.lang.base-lib/mod-offset [278] 
export function mod_offset(pval,nval,modulo){
  let offset = (nval - pval) % modulo;
  if(Math.abs(offset) > (modulo / 2)){
    if(offset > 0){
      return offset - modulo;
    }
    else{
      return offset + modulo;
    }
  }
  else{
    return offset;
  }
}

// xt.lang.base-lib/gcd [294] 
export function gcd(a,b){
  return (0 == b) ? a : gcd(b,a % b);
}

// xt.lang.base-lib/lcm [302] 
export function lcm(a,b){
  return (a * b) / gcd(a,b);
}

// xt.lang.base-lib/mix [309] 
export function mix(x0,x1,v,f){
  if(null == f){
    f = identity;
  }
  return x0 + ((x1 - x0) * f(v));
}

// xt.lang.base-lib/sign [318] 
export function sign(x){
  if(x == 0){
    return 0;
  }
  else if(x < 0){
    return -1;
  }
  else{
    return 1;
  }
}

// xt.lang.base-lib/round [326] 
export function round(x){
  return Math.floor(x + 0.5);
}

// xt.lang.base-lib/clamp [332] 
export function clamp(min,max,v){
  if(v < min){
    return min;
  }
  else if(max < v){
    return max;
  }
  else{
    return v;
  }
}

// xt.lang.base-lib/bit-count [345] 
export function bit_count(x){
  let v0 = x - ((x >> 1) & 0x55555555);
  let v1 = (v0 & 0x33333333) + ((v0 >> 2) & 0x33333333);
  return (((v1 + (v1 >> 4)) & 0xF0F0F0F) * 0x1010101) >> 24;
}

// xt.lang.base-lib/sym-full [364] 
export function sym_full(ns,name){
  if(null == ns){
    return name;
  }
  else{
    return ns + "/" + name;
  }
}

// xt.lang.base-lib/sym-name [372] 
export function sym_name(sym){
  let idx = sym.indexOf("/");
  return sym.substring(idx - -1);
}

// xt.lang.base-lib/sym-ns [380] 
export function sym_ns(sym){
  let idx = sym.indexOf("/");
  if(0 < idx){
    return sym.substring(0,idx - 0);
  }
  else{
    return null;
  }
}

// xt.lang.base-lib/sym-pair [389] 
export function sym_pair(sym){
  return [sym_ns(sym),sym_name(sym)];
}

// xt.lang.base-lib/is-empty? [400] 
export function is_emptyp(res){
  if(null == res){
    return true;
  }
  else if("string" == (typeof res)){
    return 0 == res.length;
  }
  else if(arrp(res)){
    return 0 == (res).length;
  }
  else if(objp(res)){
    for(let [k,v] of Object.entries(res)){
      return false;
    };
    return true;
  }
  else{
    throw "Invalid type - " + type_native(res) + " - " + String(res);
  }
}

// xt.lang.base-lib/arr-lookup [418] 
export function arr_lookup(arr){
  let out = {};
  for(let k of arr){
    out[k] = true;
  };
  return out;
}

// xt.lang.base-lib/arr-every [427] 
export function arr_every(arr,pred){
  for(let i = 0; i < arr.length; ++i){
    let v = arr[i];
    if(!pred(v)){
      return false;
    }
  };
  return true;
}

// xt.lang.base-lib/arr-some [436] 
export function arr_some(arr,pred){
  for(let i = 0; i < arr.length; ++i){
    let v = arr[i];
    if(pred(v)){
      return true;
    }
  };
  return false;
}

// xt.lang.base-lib/arr-each [445] 
export function arr_each(arr,f){
  for(let e of arr){
    f(e);
  };
  return true;
}

// xt.lang.base-lib/arr-omit [452] 
export function arr_omit(arr,i){
  let out = [];
  for(let j = 0; j < arr.length; ++j){
    let e = arr[j];
    if(i != j){
      out.push(e);
    }
  };
  return out;
}

// xt.lang.base-lib/arr-reverse [462] 
export function arr_reverse(arr){
  let out = [];
  for(let i = arr.length; i > 0; i = (i + -1)){
    out.push(arr[i + -1]);
  };
  return out;
}

// xt.lang.base-lib/arr-find [473] 
export function arr_find(arr,pred){
  for(let i = 0; i < arr.length; ++i){
    let v = arr[i];
    if(pred(v)){
      return i - 0;
    }
  };
  return -1;
}

// xt.lang.base-lib/arr-zip [482] 
export function arr_zip(ks,vs){
  let out = {};
  for(let i = 0; i < ks.length; ++i){
    let k = ks[i];
    out[k] = vs[i];
  };
  return out;
}

// xt.lang.base-lib/arr-map [491] 
export function arr_map(arr,f){
  let out = [];
  for(let e of arr){
    out.push(f(e));
  };
  return out;
}

// xt.lang.base-lib/arr-clone [500] 
export function arr_clone(arr){
  let out = [];
  for(let e of arr){
    out.push(e);
  };
  return out;
}

// xt.lang.base-lib/arr-append [509] 
export function arr_append(arr,other){
  for(let e of other){
    arr.push(e);
  };
  return arr;
}

// xt.lang.base-lib/arr-slice [517] 
export function arr_slice(arr,start,finish){
  let out = [];
  for(let i = start; i < (finish || (arr).length); i = (i + 1)){
    out.push(arr[i]);
  };
  return out;
}

// xt.lang.base-lib/arr-rslice [527] 
export function arr_rslice(arr,start,finish){
  let out = [];
  for(let i = start; i < finish; i = (i + 1)){
    out.unshift(arr[i]);
  };
  return out;
}

// xt.lang.base-lib/arr-tail [536] 
export function arr_tail(arr,n){
  let t = (arr).length;
  return arr_rslice(arr,Math.max(t - n,0),t);
}

// xt.lang.base-lib/arr-mapcat [543] 
export function arr_mapcat(arr,f){
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

// xt.lang.base-lib/arr-partition [555] 
export function arr_partition(arr,n){
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

// xt.lang.base-lib/arr-filter [573] 
export function arr_filter(arr,pred){
  let out = [];
  for(let e of arr){
    if(pred(e)){
      out.push(e);
    }
  };
  return out;
}

// xt.lang.base-lib/arr-keep [583] 
export function arr_keep(arr,f){
  let out = [];
  for(let e of arr){
    let v = f(e);
    if(null != v){
      out.push(v);
    }
  };
  return out;
}

// xt.lang.base-lib/arr-keepf [594] 
export function arr_keepf(arr,pred,f){
  let out = [];
  for(let e of arr){
    if(pred(e)){
      out.push(f(e));
    }
  };
  return out;
}

// xt.lang.base-lib/arr-juxt [604] 
export function arr_juxt(arr,key_fn,val_fn){
  let out = {};
  if(null != arr){
    for(let e of arr){
      out[key_fn(e)] = val_fn(e);
    };
  }
  return out;
}

// xt.lang.base-lib/arr-foldl [615] 
export function arr_foldl(arr,f,init){
  let out = init;
  for(let e of arr){
    out = f(out,e);
  };
  return out;
}

// xt.lang.base-lib/arr-foldr [624] 
export function arr_foldr(arr,f,init){
  let out = init;
  for(let i = arr.length; i > 0; i = (i + -1)){
    out = f(out,arr[i + -1]);
  };
  return out;
}

// xt.lang.base-lib/arr-pipel [635] 
export function arr_pipel(arr,e){
  return arr_foldl(arr,step_thrush,e);
}

// xt.lang.base-lib/arr-piper [641] 
export function arr_piper(arr,e){
  return arr_foldr(arr,step_thrush,e);
}

// xt.lang.base-lib/arr-group-by [647] 
export function arr_group_by(arr,key_fn,view_fn){
  let out = {};
  if(null != arr){
    for(let e of arr){
      let g = key_fn(e);
      let garr = out[g] || [];
      out[g] = [];
      garr.push(view_fn(e));
      out[g] = garr;
    };
  }
  return out;
}

// xt.lang.base-lib/arr-range [661] 
export function arr_range(x){
  let arr = Array.isArray(x) ? x : [x];
  let arrlen = arr.length;
  let start = (1 < arrlen) ? arr[0] : 0;
  let finish = (1 < arrlen) ? arr[1] : arr[0];
  let step = (2 < arrlen) ? arr[2] : 1;
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

// xt.lang.base-lib/arr-intersection [687] 
export function arr_intersection(arr,other){
  let lu = {};
  for(let k of arr){
    lu[k] = true;
  };
  let out = [];
  for(let e of other){
    if(lu[e] != null){
      out.push(e);
    }
  };
  return out;
}

// xt.lang.base-lib/arr-difference [698] 
export function arr_difference(arr,other){
  let lu = {};
  for(let k of arr){
    lu[k] = true;
  };
  let out = [];
  for(let e of other){
    if(!(lu[e] != null)){
      out.push(e);
    }
  };
  return out;
}

// xt.lang.base-lib/arr-union [709] 
export function arr_union(arr,other){
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

// xt.lang.base-lib/arr-sort [724] 
export function arr_sort(arr,key_fn,comp_fn){
  let out = Array.from(arr);
  out.sort(function (a,b){
    return comp_fn(key_fn(a),key_fn(b)) ? -1 : 1;
  });
  return out;
}

// xt.lang.base-lib/arr-sorted-merge [732] 
export function arr_sorted_merge(arr,brr,comp_fn){
  arr = (arr || []);
  brr = (brr || []);
  let alen = (arr).length;
  let blen = (brr).length;
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

// xt.lang.base-lib/arr-shuffle [767] 
export function arr_shuffle(arr){
  let tmp_val = null;
  let tmp_idx = null;
  let total = (arr).length;
  for(let i = 0; i < total; i = (i + 1)){
    tmp_idx = (0 + Math.floor(Math.random() * total));
    tmp_val = arr[tmp_idx];
    arr[tmp_idx] = arr[i];
    arr[i] = tmp_val;
  };
  return arr;
}

// xt.lang.base-lib/arr-pushl [781] 
export function arr_pushl(arr,v,n){
  arr.push(v);
  if(arr.length > n){
    arr.shift();
  }
  return arr;
}

// xt.lang.base-lib/arr-pushr [790] 
export function arr_pushr(arr,v,n){
  arr.unshift(v);
  if(arr.length > n){
    arr.pop();
  }
  return arr;
}

// xt.lang.base-lib/arr-join [799] 
export function arr_join(arr,s){
  return arr.join(s);
}

// xt.lang.base-lib/arr-interpose [805] 
export function arr_interpose(arr,elem){
  let out = [];
  for(let e of arr){
    out.push(e);
    out.push(elem);
  };
  out.pop();
  return out;
}

// xt.lang.base-lib/arr-repeat [816] 
export function arr_repeat(x,n){
  let out = [];
  for(let i = 0; i < (n - 0); i = (i + 1)){
    out.push(("function" == (typeof x)) ? x() : x);
  };
  return out;
}

// xt.lang.base-lib/arr-random [827] 
export function arr_random(arr){
  let idx = Math.floor(arr.length * Math.random());
  return arr[idx];
}

// xt.lang.base-lib/arr-normalise [834] 
export function arr_normalise(arr){
  let total = arr_foldl(arr,function (a,b){
    return a + b;
  },0);
  return arr_map(arr,function (x){
    return x / total;
  });
}

// xt.lang.base-lib/arr-sample [841] 
export function arr_sample(arr,dist){
  let q = Math.random();
  for(let i = 0; i < dist.length; ++i){
    let p = dist[i];
    q = (q - p);
    if(q < 0){
      return arr[i];
    }
  };
}

// xt.lang.base-lib/arrayify [851] 
export function arrayify(x){
  return Array.isArray(x) ? x : ((null == x) ? [] : [x]);
}

// xt.lang.base-lib/obj-empty? [868] 
export function obj_emptyp(obj){
  for(let k of Object.keys(obj)){
    return false;
  };
  return true;
}

// xt.lang.base-lib/obj-not-empty? [876] 
export function obj_not_emptyp(obj){
  for(let k of Object.keys(obj)){
    return true;
  };
  return false;
}

// xt.lang.base-lib/obj-first-key [884] 
export function obj_first_key(obj){
  for(let k of Object.keys(obj)){
    return k;
  };
  return null;
}

// xt.lang.base-lib/obj-first-val [892] 
export function obj_first_val(obj){
  for(let v of Object.values(obj)){
    return v;
  };
  return null;
}

// xt.lang.base-lib/obj-keys [900] 
export function obj_keys(obj){
  let out = [];
  if(null != obj){
    for(let k of Object.keys(obj)){
      out.push(k);
    };
  }
  return out;
}

// xt.lang.base-lib/obj-vals [910] 
export function obj_vals(obj){
  let out = [];
  if(null != obj){
    for(let v of Object.values(obj)){
      out.push(v);
    };
  }
  return out;
}

// xt.lang.base-lib/obj-pairs [920] 
export function obj_pairs(obj){
  let out = [];
  if(null != obj){
    for(let [k,v] of Object.entries(obj)){
      out.push([k,v]);
    };
  }
  return out;
}

// xt.lang.base-lib/obj-clone [930] 
export function obj_clone(obj){
  let out = {};
  if(null != obj){
    for(let [k,v] of Object.entries(obj)){
      out[k] = v;
    };
  }
  return out;
}

// xt.lang.base-lib/obj-assign [940] 
export function obj_assign(obj,m){
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

// xt.lang.base-lib/obj-assign-nested [951] 
export function obj_assign_nested(obj,m){
  if(null == obj){
    obj = {};
  }
  if(null != m){
    for(let [k,mv] of Object.entries(m)){
      let v = obj[k];
      if(objp(mv) && objp(v)){
        obj[k] = obj_assign_nested(v,mv);
      }
      else{
        obj[k] = mv;
      }
    };
  }
  return obj;
}

// xt.lang.base-lib/obj-assign-with [968] 
export function obj_assign_with(obj,m,f){
  if(null != m){
    let input = m || {};
    for(let [k,mv] of Object.entries(input)){
      obj[k] = ((obj[k] != null) ? f(obj[k],mv) : mv);
    };
  }
  return obj;
}

// xt.lang.base-lib/obj-from-pairs [982] 
export function obj_from_pairs(pairs){
  let out = {};
  for(let pair of pairs){
    out[pair[0]] = pair[1];
  };
  return out;
}

// xt.lang.base-lib/obj-del [993] 
export function obj_del(obj,ks){
  for(let k of ks){
    delete(obj[k]);
  };
  return obj;
}

// xt.lang.base-lib/obj-del-all [1001] 
export function obj_del_all(obj){
  for(let k of obj_keys(obj)){
    delete(obj[k]);
  };
  return obj;
}

// xt.lang.base-lib/obj-pick [1010] 
export function obj_pick(obj,ks){
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

// xt.lang.base-lib/obj-omit [1023] 
export function obj_omit(obj,ks){
  let out = {};
  let lu = {};
  for(let k of ks){
    lu[k] = true;
  };
  for(let [k,v] of Object.entries(obj)){
    if(!(lu[k] != null)){
      out[k] = v;
    }
  };
  return out;
}

// xt.lang.base-lib/obj-transpose [1036] 
export function obj_transpose(obj){
  let out = {};
  if(null != obj){
    for(let [k,v] of Object.entries(obj)){
      out[v] = k;
    };
  }
  return out;
}

// xt.lang.base-lib/obj-nest [1046] 
export function obj_nest(arr,v){
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

// xt.lang.base-lib/obj-map [1061] 
export function obj_map(obj,f){
  let out = {};
  if(null != obj){
    for(let [k,v] of Object.entries(obj)){
      out[k] = f(v);
    };
  }
  return out;
}

// xt.lang.base-lib/obj-filter [1071] 
export function obj_filter(obj,pred){
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

// xt.lang.base-lib/obj-keep [1082] 
export function obj_keep(obj,f){
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

// xt.lang.base-lib/obj-keepf [1094] 
export function obj_keepf(obj,pred,f){
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

// xt.lang.base-lib/obj-intersection [1105] 
export function obj_intersection(obj,other){
  let out = [];
  for(let k of Object.keys(other)){
    if(obj[k] != null){
      out.push(k);
    }
  };
  return out;
}

// xt.lang.base-lib/obj-difference [1115] 
export function obj_difference(obj,other){
  let out = [];
  for(let k of Object.keys(other)){
    if(!(obj[k] != null)){
      out.push(k);
    }
  };
  return out;
}

// xt.lang.base-lib/obj-keys-nested [1125] 
export function obj_keys_nested(m,path){
  let out = [];
  for(let [k,v] of Object.entries(m)){
    let npath = [...path];
    npath.push(k);
    if(objp(v)){
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

// xt.lang.base-lib/to-flat [1146] 
export function to_flat(obj){
  let out = [];
  if(objp(obj)){
    for(let [k,v] of Object.entries(obj)){
      out.push(k);
      out.push(v);
    };
  }
  else if(arrp(obj)){
    for(let e of obj){
      out.push(e[0]);
      out.push(e[1]);
    };
  }
  return out;
}

// xt.lang.base-lib/from-flat [1162] 
export function from_flat(arr,f,init){
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

// xt.lang.base-lib/get-in [1174] 
export function get_in(obj,arr){
  if(null == obj){
    return null;
  }
  else if(0 == arr.length){
    return obj;
  }
  else if(1 == arr.length){
    return obj[arr[0]];
  }
  else{
    let total = arr.length;
    let i = 0;
    let curr = obj;
    while(i < total){
      let k = arr[i];
      curr = curr[k];
      if(null == curr){
        return null;
      }
      else{
        i = (i + 1);
      }
    }
    return curr;
  }
}

// xt.lang.base-lib/path-fn [1199] 
export function path_fn(path){
  return function (x){
    return get_in(x,path);
  };
}

// xt.lang.base-lib/set-in [1206] 
export function set_in(obj,arr,v){
  if(0 == (arr || []).length){
    return obj;
  }
  else if(!objp(obj)){
    return obj_nest(arr,v);
  }
  else{
    let k = arr[0];
    let narr = Array.from(arr);
    narr.shift();
    let child = obj[k];
    if(0 == narr.length){
      obj[k] = v;
    }
    else{
      obj[k] = set_in(child,narr,v);
    }
    return obj;
  }
}

// xt.lang.base-lib/memoize-key [1226] 
export function memoize_key(f){
  let cache = {};
  let cache_fn = function (key){
    let res = f(key);
    cache[key] = res;
    return res;
  };
  return function (key){
    return cache[key] || cache_fn(key);
  };
}

// xt.lang.base-lib/not-empty? [1239] 
export function not_emptyp(res){
  if(null == res){
    return false;
  }
  else if("string" == (typeof res)){
    return 0 < res.length;
  }
  else if(arrp(res)){
    return 0 < (res).length;
  }
  else if(objp(res)){
    for(let [i,v] of Object.entries(res)){
      return true;
    };
    return false;
  }
  else{
    throw "Invalid type - " + type_native(res) + " - " + String(res);
  }
}

// xt.lang.base-lib/eq-nested-loop [1261] 
export function eq_nested_loop(src,dst,eq_obj,eq_arr,cache){
  if(objp(src) && objp(dst)){
    if(cache && cache.get(src) && cache.get(dst)){
      return true;
    }
    else{
      return eq_obj(src,dst,eq_obj,eq_arr,cache || new WeakMap());
    }
  }
  else if(arrp(src) && arrp(dst)){
    if(cache && cache.get(src) && cache.get(dst)){
      return true;
    }
    else{
      return eq_arr(src,dst,eq_obj,eq_arr,cache || new WeakMap());
    }
  }
  else{
    return src == dst;
  }
}

// xt.lang.base-lib/eq-nested-obj [1282] 
export function eq_nested_obj(src,dst,eq_obj,eq_arr,cache){
  cache.set(src,src);
  cache.set(dst,dst);
  let ks_src = obj_keys(src);
  let ks_dst = obj_keys(dst);
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

// xt.lang.base-lib/eq-nested-arr [1301] 
export function eq_nested_arr(src_arr,dst_arr,eq_obj,eq_arr,cache){
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

// xt.lang.base-lib/eq-nested [1318] 
export function eq_nested(obj,m){
  return eq_nested_loop(obj,m,eq_nested_obj,eq_nested_arr,null);
}

// xt.lang.base-lib/eq-shallow [1328] 
export function eq_shallow(obj,m){
  return eq_nested_loop(obj,m,function (a,b){
    return a == b;
  },function (a,b){
    return a == b;
  },null);
}

// xt.lang.base-lib/obj-diff [1338] 
export function obj_diff(obj,m){
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

// xt.lang.base-lib/obj-diff-nested [1351] 
export function obj_diff_nested(obj,m){
  if(null == m){
    return {};
  }
  if(null == obj){
    return m;
  }
  let out = {};
  let ks = obj_keys(m);
  for(let k of ks){
    let v = obj[k];
    let mv = m[k];
    if(objp(v) && objp(mv)){
      let dv = obj_diff_nested(v,mv);
      if(obj_not_emptyp(dv)){
        out[k] = dv;
      }
    }
    else if(!eq_nested(v,mv)){
      out[k] = mv;
    }
  };
  return out;
}

// xt.lang.base-lib/sort [1370] 
export function sort(arr){
  return arr_sort(arr,identity,function (a,b){
    return a < b;
  });
}

// xt.lang.base-lib/objify [1376] 
export function objify(v){
  if("string" == (typeof v)){
    return JSON.parse(v);
  }
  else{
    return v;
  }
}

// xt.lang.base-lib/template-entry [1385] 
export function template_entry(obj,template,props){
  if(fnp(template)){
    return template(obj,props);
  }
  else if(null == template){
    return obj;
  }
  else if(arrp(template)){
    return get_in(obj,template);
  }
  else{
    return template;
  }
}

// xt.lang.base-lib/template-fn [1401] 
export function template_fn(template){
  return function (obj,props){
    return template_entry(obj,template,props);
  };
}

// xt.lang.base-lib/template-multi [1407] 
export function template_multi(arr){
  let template_fn = function (entry,props){
    for(let template of arr){
      let out = template_entry(entry,template,props);
      if(null != out){
        return out;
      }
    };
  };
  return template_fn;
}

// xt.lang.base-lib/sort-by [1419] 
export function sort_by(arr,inputs){
  let keys = arr_map(inputs,function (e){
    return arrp(e) ? e[0] : e;
  });
  let inverts = arr_map(inputs,function (e){
    return arrp(e) ? e[1] : false;
  });
  let get_fn = function (e,key){
    if(fnp(key)){
      return key(e);
    }
    else{
      return e[key];
    }
  };
  let key_fn = function (e){
    return arr_map(keys,function (key){
      return get_fn(e,key);
    });
  };
  let comp_fn = function (a0,a1){
    for(let i = 0; i < a0.length; ++i){
      let v0 = a0[i];
      let v1 = a1[i];
      let invert = inverts[i];
      if(v0 != v1){
        if(invert){
          if("number" == (typeof v0)){
            return v1 < v0;
          }
          else{
            return 0 > String(v1).localeCompare(String(v0));
          }
        }
        else{
          if("number" == (typeof v0)){
            return v0 < v1;
          }
          else{
            return 0 > String(v0).localeCompare(String(v1));
          }
        }
      }
    };
    return false;
  };
  return arr_sort(arr,key_fn,comp_fn);
}

// xt.lang.base-lib/sort-edges-build [1458] 
export function sort_edges_build(nodes,edge){
  let n_from = edge[0];
  let n_to = edge[1];
  if(!(nodes[n_from] != null)){
    nodes[n_from] = {"id":n_from,"links":[]};
  }
  if(!(nodes[n_to] != null)){
    nodes[n_to] = {"id":n_to,"links":[]};
  }
  let links = nodes[n_from]["links"];
  links.push(n_to);
}

// xt.lang.base-lib/sort-edges-visit [1473] 
export function sort_edges_visit(nodes,visited,sorted,id,ancestors){
  if(visited[id]){
    return;
  }
  let node = nodes[id];
  if(!node){
    throw "Not available: " + id;
  }
  ancestors = (ancestors || []);
  ancestors.push(id);
  visited[id] = true;
  let input = node["links"];
  for(let aid of input){
    sort_edges_visit(nodes,visited,sorted,aid,Array.from(ancestors));
  };
  sorted.unshift(id);
}

// xt.lang.base-lib/sort-edges [1490] 
export function sort_edges(edges){
  let nodes = {};
  let sorted = [];
  let visited = {};
  for(let e of edges){
    sort_edges_build(nodes,e);
  };
  for(let id of Object.keys(nodes)){
    sort_edges_visit(nodes,visited,sorted,id,null);
  };
  return sorted;
}

// xt.lang.base-lib/sort-topo [1503] 
export function sort_topo(input){
  let edges = [];
  for(let link of input){
    let root = link[0];
    let deps = link[1];
    for(let d of deps){
      edges.push([root,d]);
    };
  };
  return sort_edges(edges).slice().reverse();
}

// xt.lang.base-lib/clone-shallow [1516] 
export function clone_shallow(obj){
  if(null == obj){
    return obj;
  }
  else if(objp(obj)){
    return obj_clone(obj);
  }
  else if(arrp(obj)){
    return arr_clone(obj);
  }
  else{
    return obj;
  }
}

// xt.lang.base-lib/clone-nested-loop [1525] 
export function clone_nested_loop(obj,cache){
  if(null == obj){
    return obj;
  }
  let cached_output = cache.get(obj);
  if(cached_output){
    return cached_output;
  }
  else if(objp(obj)){
    let out = {};
    cache.set(obj,out);
    for(let [k,v] of Object.entries(obj)){
      out[k] = clone_nested_loop(v,cache);
    };
    return out;
  }
  else if(arrp(obj)){
    let out = [];
    cache.set(obj,out);
    for(let e of obj){
      out.push(clone_nested_loop(e,cache));
    };
    return out;
  }
  else{
    return obj;
  }
}

// xt.lang.base-lib/clone-nested [1552] 
export function clone_nested(obj){
  if(!(objp(obj) || arrp(obj))){
    return obj;
  }
  else{
    return clone_nested_loop(obj,new WeakMap());
  }
}

// xt.lang.base-lib/wrap-callback [1563] 
export function wrap_callback(callbacks,key){
  callbacks = (callbacks || {});
  let result_fn = function (result){
    let f = callbacks[key];
    if(null != f){
      return f(result);
    }
    else{
      return result;
    }
  };
  return result_fn;
}

// xt.lang.base-lib/walk [1576] 
export function walk(obj,pre_fn,post_fn){
  obj = pre_fn(obj);
  if(null == obj){
    return post_fn(obj);
  }
  else if(objp(obj)){
    let out = {};
    for(let [k,v] of Object.entries(obj)){
      out[k] = walk(v,pre_fn,post_fn);
    };
    return post_fn(out);
  }
  else if(arrp(obj)){
    let out = [];
    for(let e of obj){
      out.push(walk(e,pre_fn,post_fn));
    };
    return post_fn(out);
  }
  else{
    return post_fn(obj);
  }
}

// xt.lang.base-lib/get-data [1599] 
export function get_data(obj){
  let data_fn = function (obj){
    if(("string" == (typeof obj)) || ("number" == (typeof obj)) || ("boolean" == (typeof obj)) || ((null != obj) && ("object" == (typeof obj)) && !Array.isArray(obj)) || Array.isArray(obj) || (null == obj)){
      return obj;
    }
    else{
      return "<" + type_native(obj) + ">";
    }
  };
  return walk(obj,identity,data_fn);
}

// xt.lang.base-lib/get-spec [1615] 
export function get_spec(obj){
  let spec_fn = function (obj){
    if(!(objp(obj) || arrp(obj))){
      return type_native(obj);
    }
    else{
      return obj;
    }
  };
  return walk(obj,identity,spec_fn);
}

// xt.lang.base-lib/split-long [1631] 
export function split_long(s,lineLen){
  if(is_emptyp(s)){
    return "";
  }
  lineLen = (lineLen || 50);
  let total = (s).length;
  let lines = Math.ceil(total / lineLen);
  let out = [];
  for(let i = 0; i < lines; i = (i + 1)){
    let line = s.substring(i * lineLen,(i + 1) * lineLen);
    if(0 < line.length){
      out.push(line);
    }
  };
  return out;
}

// xt.lang.base-lib/str-rand [1649] 
export function str_rand(n){
  let choices = [
    "A",
    "B",
    "C",
    "D",
    "E",
    "F",
    "G",
    "H",
    "I",
    "J",
    "K",
    "L",
    "M",
    "N",
    "O",
    "P",
    "Q",
    "R",
    "S",
    "T",
    "U",
    "V",
    "W",
    "X",
    "Y",
    "Z",
    "a",
    "b",
    "c",
    "d",
    "e",
    "f",
    "g",
    "h",
    "i",
    "j",
    "k",
    "l",
    "m",
    "n",
    "o",
    "p",
    "q",
    "r",
    "s",
    "t",
    "u",
    "v",
    "w",
    "x",
    "y",
    "z",
    "0",
    "1",
    "2",
    "3",
    "4",
    "5",
    "6",
    "7",
    "8"
  ];
  let out = "";
  for(let i = 0; i < n; i = (i + 1)){
    out = (out + arr_random(choices));
  };
  return out;
}

// xt.lang.base-lib/proto-spec [1663] 
export function proto_spec(spec_arr){
  let acc_fn = function (acc,e){
    let [spec_i,spec_map] = e;
    for(let key of spec_i){
      if(null == spec_map[key]){
        throw "NOT VALID." + JSON.stringify({"required":key,"actual":obj_keys(spec_map)});
      }
    };
    return obj_assign(acc,spec_map);
  };
  return arr_foldl(spec_arr,acc_fn,{});
}

// xt.lang.base-lib/with-delay [1685] 
export function with_delay(thunk,ms){
  setTimeout(function (){
    new Promise(function (resolve,reject){
      resolve(thunk());
    });
  },ms);
}

// xt.lang.base-lib/trace-log [1728] 
export function trace_log(){
  if(!(null == globalThis["TRACE"])){
    return globalThis["TRACE"];
  }
  else{
    globalThis["TRACE"] = [];
    return globalThis["TRACE"];
  }
}

// xt.lang.base-lib/trace-log-clear [1737] 
export function trace_log_clear(){
  globalThis["TRACE"] = [];
  return globalThis["TRACE"];
}

// xt.lang.base-lib/trace-log-add [1744] 
export function trace_log_add(data,tag,opts){
  let log = trace_log();
  let m = obj_assign({"tag":tag,"data":data,"time":Date.now()},opts);
  log.push(m);
  return log.length;
}

// xt.lang.base-lib/trace-filter [1757] 
export function trace_filter(tag){
  return arr_filter(trace_log(),function (e){
    return tag == e["tag"];
  });
}

// xt.lang.base-lib/trace-last-entry [1763] 
export function trace_last_entry(tag){
  let log = trace_log();
  if(null == tag){
    return log[log.length + -1];
  }
  else{
    let tagged = trace_filter(tag);
    return tagged[tagged.length + -1];
  }
}

// xt.lang.base-lib/trace-data [1773] 
export function trace_data(tag){
  return arr_map(trace_log(),function (e){
    return e["data"];
  });
}

// xt.lang.base-lib/trace-last [1779] 
export function trace_last(tag){
  return (trace_last_entry(tag))["data"];
}

// xt.lang.base-lib/trace-run [1795] 
export function trace_run(f){
  trace_log_clear();
  f();
  return trace_log();
}