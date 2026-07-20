import 'dart:math' as math;

is_emptyp(res) {
  if(null == res){
    return true;
  }
  else if("String" == (res.runtimeType).toString()){
    return 0 == res.length;
  }
  else if((res.runtimeType).toString().startsWith("List") || (res.runtimeType).toString().startsWith("_GrowableList")){
    return 0 == res.length;
  }
  else if(("Map" == (res.runtimeType).toString()) || (res.runtimeType).toString().startsWith("_Map") || (res.runtimeType).toString().startsWith("LinkedMap")){
    for(var entry_49521 in res.entries){
      var i = entry_49521.key;
      var v = entry_49521.value;
      return false;
    };
    return true;
  }
  else{
    throw "Invalid type - " + (res).toString();
  }
}

not_emptyp(res) {
  if(null == res){
    return false;
  }
  else if("String" == (res.runtimeType).toString()){
    return 0 < res.length;
  }
  else if((res.runtimeType).toString().startsWith("List") || (res.runtimeType).toString().startsWith("_GrowableList")){
    return 0 < res.length;
  }
  else if(("Map" == (res.runtimeType).toString()) || (res.runtimeType).toString().startsWith("_Map") || (res.runtimeType).toString().startsWith("LinkedMap")){
    for(var entry_49522 in res.entries){
      var i = entry_49522.key;
      var v = entry_49522.value;
      return true;
    };
    return false;
  }
  else{
    throw "Invalid type - " + (res).toString();
  }
}

lu_create() {
  return <dynamic, dynamic>{};
}

lu_del(lu, key) {
  lu.remove(key);
  return lu;
}

lu_get(lu, key) {
  return lu[key];
}

lu_set(lu, key, value) {
  lu[key] = value;
  return lu;
}

lu_eq(x, y) {
  return x == y;
}

first(arr) {
  return arr[0];
}

second(arr) {
  return arr[1];
}

nth(arr, i) {
  return arr[i];
}

last(arr) {
  return arr[arr.length + -1];
}

second_last(arr) {
  return arr[(arr.length - 1) + -1];
}

arr_emptyp(arr) {
  if(null == arr){
    return true;
  }
  else{
    return 0 == arr.length;
  }
}

arr_not_emptyp(arr) {
  if(null == arr){
    return false;
  }
  else{
    return 0 != arr.length;
  }
}

arrayify(x) {
  if((x.runtimeType).toString().startsWith("List") || (x.runtimeType).toString().startsWith("_GrowableList")){
    return x;
  }
  if(null == x){
    return <dynamic>[];
  }
  return <dynamic>[x];
}

arr_lookup(arr) {
  var out = <dynamic, dynamic>{};
  var arr_49523 = arr;
  for(var i49524 = 0; i49524 < arr_49523.length; ++i49524){
    var k = arr_49523[i49524];
    out[k] = true;
  };
  return out;
}

arr_omit(arr, i) {
  var out = <dynamic>[];
  var arr_49545 = arr;
  for(var j = 0; j < arr_49545.length; ++j){
    var e = arr_49545[j];
    if(i != j){
      out.add(e);
    }
  };
  return out;
}

arr_reverse(arr) {
  var out = <dynamic>[];
  for(var i = arr.length; i > 0; i = (i + -1)){
    out.add(arr[i + -1]);
  };
  return out;
}

arr_zip(ks, vs) {
  var out = <dynamic, dynamic>{};
  var arr_49566 = ks;
  for(var i = 0; i < arr_49566.length; ++i){
    var k = arr_49566[i];
    out[k] = vs[i];
  };
  return out;
}

arr_clone(arr) {
  var out = <dynamic>[];
  var arr_49587 = arr;
  for(var i49588 = 0; i49588 < arr_49587.length; ++i49588){
    var e = arr_49587[i49588];
    out.add(e);
  };
  return out;
}

arr_assign(arr, other) {
  var arr_49609 = other;
  for(var i49610 = 0; i49610 < arr_49609.length; ++i49610){
    var e = arr_49609[i49610];
    arr.add(e);
  };
  return arr;
}

arr_concat(arr, other) {
  var out = <dynamic>[];
  var arr_49631 = arr;
  for(var i49632 = 0; i49632 < arr_49631.length; ++i49632){
    var e = arr_49631[i49632];
    out.add(e);
  };
  var arr_49653 = other;
  for(var i49654 = 0; i49654 < arr_49653.length; ++i49654){
    var e = arr_49653[i49654];
    out.add(e);
  };
  return out;
}

arr_slice(arr, start, finish) {
  var out = <dynamic>[];
  var finish_idx = null;
  if(("int" == (finish.runtimeType).toString()) || ("double" == (finish.runtimeType).toString()) || ("num" == (finish.runtimeType).toString())){
    finish_idx = finish;
  }
  else{
    finish_idx = arr.length;
  }
  for(var i = start; i < finish_idx; i = (i + 1)){
    out.add(arr[i]);
  };
  return out;
}

arr_rslice(arr, start, finish) {
  var out = <dynamic>[];
  for(var i = start; i < finish; i = (i + 1)){
    out.insert(0,arr[i]);
  };
  return out;
}

arr_tail(arr, n) {
  var t = arr.length;
  return arr_rslice(arr,((t - n) > 0) ? (t - n) : 0,t);
}

arr_range(x) {
  var arr = <dynamic>[x];
  if((x.runtimeType).toString().startsWith("List") || (x.runtimeType).toString().startsWith("_GrowableList")){
    arr = x;
  }
  var arrlen = arr.length;
  var start = 0;
  if(1 < arrlen){
    start = arr[0];
  }
  var finish = arr[0];
  if(1 < arrlen){
    finish = arr[1];
  }
  var step = 1;
  if(2 < arrlen){
    step = arr[2];
  }
  var out = <dynamic>[start];
  var i = step + start;
  if((0 < step) && (start < finish)){
    while(i < finish){
      out.add(i);
      i = (i + step);
    }
  }
  else if((0 > step) && (finish < start)){
    while(i > finish){
      out.add(i);
      i = (i + step);
    }
  }
  else{
    return <dynamic>[];
  }
  return out;
}

arr_intersection(arr, other) {
  var lu = arr_lookup(arr);
  var out = <dynamic>[];
  var arr_49675 = other;
  for(var i49676 = 0; i49676 < arr_49675.length; ++i49676){
    var e = arr_49675[i49676];
    if(lu.containsKey(e)){
      out.add(e);
    }
  };
  return out;
}

arr_difference(arr, other) {
  var lu = arr_lookup(arr);
  var out = <dynamic>[];
  var arr_49697 = other;
  for(var i49698 = 0; i49698 < arr_49697.length; ++i49698){
    var e = arr_49697[i49698];
    if(!lu.containsKey(e)){
      out.add(e);
    }
  };
  return out;
}

arr_union(arr, other) {
  var lu = <dynamic, dynamic>{};
  var arr_49719 = arr;
  for(var i49720 = 0; i49720 < arr_49719.length; ++i49720){
    var e = arr_49719[i49720];
    lu[e] = e;
  };
  var arr_49741 = other;
  for(var i49742 = 0; i49742 < arr_49741.length; ++i49742){
    var e = arr_49741[i49742];
    lu[e] = e;
  };
  var out = <dynamic>[];
  for(var v in lu.values){
    out.add(v);
  };
  return out;
}

arr_shuffle(arr) {
  var tmp_val = null;
  var tmp_idx = null;
  var total = arr.length;
  for(var i = 0; i < total; i = (i + 1)){
    tmp_idx = (0 + ((math.Random()).nextDouble() * total).floor());
    tmp_val = arr[tmp_idx];
    arr[tmp_idx] = arr[i];
    arr[i] = tmp_val;
  };
  return arr;
}

arr_pushl(arr, v, n) {
  arr.add(v);
  if(arr.length > n){
    arr.removeAt(0);
  }
  return arr;
}

arr_pushr(arr, v, n) {
  arr.insert(0,v);
  if(arr.length > n){
    (arr).removeLast();
  }
  return arr;
}

arr_interpose(arr, elem) {
  var out = <dynamic>[];
  var arr_49763 = arr;
  for(var i49764 = 0; i49764 < arr_49763.length; ++i49764){
    var e = arr_49763[i49764];
    out.add(e);
    out.add(elem);
  };
  (out).removeLast();
  return out;
}

arr_random(arr) {
  var idx = (arr.length * (math.Random()).nextDouble()).floor();
  return arr[idx];
}

arr_sample(arr, dist) {
  var q = (math.Random()).nextDouble();
  var arr_49785 = dist;
  for(var i = 0; i < arr_49785.length; ++i){
    var p = arr_49785[i];
    q = (q - p);
    if(q < 0){
      return arr[i];
    }
  };
}

obj_emptyp(obj) {
  for(var k in obj.keys){
    return false;
  };
  return true;
}

obj_not_emptyp(obj) {
  for(var k in obj.keys){
    return true;
  };
  return false;
}

obj_first_key(obj) {
  for(var k in obj.keys){
    return k;
  };
  return null;
}

obj_first_val(obj) {
  for(var v in obj.values){
    return v;
  };
  return null;
}

obj_keys(obj) {
  var out = <dynamic>[];
  if(null != obj){
    for(var k in obj.keys){
      out.add(k);
    };
  }
  return out;
}

obj_vals(obj) {
  var out = <dynamic>[];
  if(null != obj){
    for(var v in obj.values){
      out.add(v);
    };
  }
  return out;
}

obj_pairs(obj) {
  var out = <dynamic>[];
  if(null != obj){
    for(var entry_49806 in obj.entries){
      var k = entry_49806.key;
      var v = entry_49806.value;
      out.add(<dynamic>[k,v]);
    };
  }
  return out;
}

obj_clone(obj) {
  var out = <dynamic, dynamic>{};
  if(null != obj){
    for(var entry_49807 in obj.entries){
      var k = entry_49807.key;
      var v = entry_49807.value;
      out[k] = v;
    };
  }
  return out;
}

obj_assign(obj, m) {
  if(null == obj){
    obj = <dynamic, dynamic>{};
  }
  if(null != m){
    for(var entry_49808 in m.entries){
      var k = entry_49808.key;
      var v = entry_49808.value;
      obj[k] = v;
    };
  }
  return obj;
}

obj_assign_nested(obj, m) {
  if(null == obj){
    obj = <dynamic, dynamic>{};
  }
  if(null != m){
    for(var entry_49809 in m.entries){
      var k = entry_49809.key;
      var mv = entry_49809.value;
      var v = obj[k];
      if((("Map" == (mv.runtimeType).toString()) || (mv.runtimeType).toString().startsWith("_Map") || (mv.runtimeType).toString().startsWith("LinkedMap")) && (("Map" == (v.runtimeType).toString()) || (v.runtimeType).toString().startsWith("_Map") || (v.runtimeType).toString().startsWith("LinkedMap"))){
        obj[k] = obj_assign_nested(v,mv);
      }
      else{
        obj[k] = mv;
      }
    };
  }
  return obj;
}

obj_assign_with(obj, m, f) {
  if(null != m){
    var input = <dynamic, dynamic>{};
    if(("Map" == (m.runtimeType).toString()) || (m.runtimeType).toString().startsWith("_Map") || (m.runtimeType).toString().startsWith("LinkedMap")){
      input = m;
    }
    for(var entry_49810 in input.entries){
      var k = entry_49810.key;
      var mv = entry_49810.value;
      var merged = mv;
      if(obj.containsKey(k)){
        merged = f(obj[k],mv);
      }
      obj[k] = merged;
    };
  }
  return obj;
}

obj_from_pairs(pairs) {
  var out = <dynamic, dynamic>{};
  var arr_49811 = pairs;
  for(var i49812 = 0; i49812 < arr_49811.length; ++i49812){
    var pair = arr_49811[i49812];
    out[pair[0]] = pair[1];
  };
  return out;
}

obj_del(obj, ks) {
  var arr_49833 = ks;
  for(var i49834 = 0; i49834 < arr_49833.length; ++i49834){
    var k = arr_49833[i49834];
    obj.remove(k);
  };
  return obj;
}

obj_del_all(obj) {
  var arr_49855 = List<dynamic>.from(( obj ).keys);
  for(var i49856 = 0; i49856 < arr_49855.length; ++i49856){
    var k = arr_49855[i49856];
    obj.remove(k);
  };
  return obj;
}

obj_pick(obj, ks) {
  var out = <dynamic, dynamic>{};
  if(null == obj){
    return out;
  }
  var arr_49879 = ks;
  for(var i49880 = 0; i49880 < arr_49879.length; ++i49880){
    var k = arr_49879[i49880];
    var v = obj[k];
    if(null != v){
      out[k] = v;
    }
  };
  return out;
}

obj_omit(obj, ks) {
  var out = <dynamic, dynamic>{};
  var lu = <dynamic, dynamic>{};
  var arr_49901 = ks;
  for(var i49902 = 0; i49902 < arr_49901.length; ++i49902){
    var k = arr_49901[i49902];
    lu[k] = true;
  };
  for(var entry_49923 in obj.entries){
    var k = entry_49923.key;
    var v = entry_49923.value;
    if(!lu.containsKey(k)){
      out[k] = v;
    }
  };
  return out;
}

obj_transpose(obj) {
  var out = <dynamic, dynamic>{};
  if(null != obj){
    for(var entry_49924 in obj.entries){
      var k = entry_49924.key;
      var v = entry_49924.value;
      out[v] = k;
    };
  }
  return out;
}

obj_nest(arr, v) {
  var idx = arr.length;
  var out = v;
  while(true){
    if(idx == 0){
      return out;
    }
    var nested = <dynamic, dynamic>{};
    var k = arr[idx + -1];
    nested[k] = out;
    out = nested;
    idx = (idx - 1);
  }
}

get_in(obj, arr) {
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
    var k = arr[0];
    if((obj.runtimeType).toString().startsWith("List") || (obj.runtimeType).toString().startsWith("_GrowableList")){
      return (("int" == (k.runtimeType).toString()) || ("double" == (k.runtimeType).toString()) || ("num" == (k.runtimeType).toString())) ? obj[k] : null;
    }
    else if(("Map" == (obj.runtimeType).toString()) || (obj.runtimeType).toString().startsWith("_Map") || (obj.runtimeType).toString().startsWith("LinkedMap")){
      return obj[k];
    }
    else{
      return null;
    }
  }
  var total = arr.length;
  var i = 0;
  var curr = obj;
  while(i < total){
    if(null == curr){
      return null;
    }
    var k = arr[i];
    if((curr.runtimeType).toString().startsWith("List") || (curr.runtimeType).toString().startsWith("_GrowableList")){
      if(("int" == (k.runtimeType).toString()) || ("double" == (k.runtimeType).toString()) || ("num" == (k.runtimeType).toString())){
        curr = curr[k];
      }
      else{
        return null;
      }
    }
    else if(("Map" == (curr.runtimeType).toString()) || (curr.runtimeType).toString().startsWith("_Map") || (curr.runtimeType).toString().startsWith("LinkedMap")){
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

set_in(obj, arr, v) {
  if(null == arr){
    arr = <dynamic>[];
  }
  if(0 == arr.length){
    return obj;
  }
  if(!(("Map" == (obj.runtimeType).toString()) || (obj.runtimeType).toString().startsWith("_Map") || (obj.runtimeType).toString().startsWith("LinkedMap"))){
    var idx = arr.length;
    var out = v;
    while(true){
      if(idx == 0){
        return out;
      }
      var nested = <dynamic, dynamic>{};
      var k = arr[idx + -1];
      nested[k] = out;
      out = nested;
      idx = (idx - 1);
    }
  }
  var k = arr[0];
  var narr = arr_slice(arr,1,null);
  var child = obj[k];
  if(0 == narr.length){
    obj[k] = v;
  }
  else{
    obj[k] = set_in(child,narr,v);
  }
  return obj;
}

obj_intersection(obj, other) {
  var out = <dynamic>[];
  for(var k in other.keys){
    if(obj.containsKey(k)){
      out.add(k);
    }
  };
  return out;
}

obj_keys_nested(m, path) {
  var out = <dynamic>[];
  for(var entry_49925 in m.entries){
    var k = entry_49925.key;
    var v = entry_49925.value;
    var npath = <dynamic>[...path];
    npath.add(k);
    if(("Map" == (v.runtimeType).toString()) || (v.runtimeType).toString().startsWith("_Map") || (v.runtimeType).toString().startsWith("LinkedMap")){
      var arr_49926 = obj_keys_nested(v,npath);
      for(var i49927 = 0; i49927 < arr_49926.length; ++i49927){
        var e = arr_49926[i49927];
        out.add(e);
      };
    }
    else{
      out.add(<dynamic>[npath,v]);
    }
  };
  return out;
}

obj_difference(obj, other) {
  var out = <dynamic>[];
  for(var k in other.keys){
    if(!obj.containsKey(k)){
      out.add(k);
    }
  };
  return out;
}

swap_key(obj, k, f, args) {
  var inputs = arr_clone(args);
  inputs.insert(0,obj[k]);
  obj[k] = Function.apply(f,inputs);
  return obj;
}

to_flat(obj) {
  var out = <dynamic>[];
  if(("Map" == (obj.runtimeType).toString()) || (obj.runtimeType).toString().startsWith("_Map") || (obj.runtimeType).toString().startsWith("LinkedMap")){
    for(var entry_49948 in obj.entries){
      var k = entry_49948.key;
      var v = entry_49948.value;
      out.add(k);
      out.add(v);
    };
  }
  else if((obj.runtimeType).toString().startsWith("List") || (obj.runtimeType).toString().startsWith("_GrowableList")){
    var arr_49949 = obj;
    for(var i49950 = 0; i49950 < arr_49949.length; ++i49950){
      var e = arr_49949[i49950];
      out.add(e[0]);
      out.add(e[1]);
    };
  }
  return out;
}

set_pair_step(out, k, v) {
  out[k] = v;
  return out;
}

from_flat(arr, f, init) {
  var out = init;
  var k = null;
  var arr_49971 = arr;
  for(var i = 0; i < arr_49971.length; ++i){
    var e = arr_49971[i];
    if(0 == (i % 2)){
      k = e;
    }
    else{
      out = f(out,k,e);
    }
  };
  return out;
}

arr_every(arr, pred) {
  var arr_49992 = arr;
  for(var i = 0; i < arr_49992.length; ++i){
    var v = arr_49992[i];
    if(!(() {
      var dart_truthy__49513 = pred(v);
      return (null != dart_truthy__49513) && (false != dart_truthy__49513);
    })()){
      return false;
    }
  };
  return true;
}

arr_some(arr, pred) {
  var arr_50013 = arr;
  for(var i = 0; i < arr_50013.length; ++i){
    var v = arr_50013[i];
    if((() {
      var dart_truthy__49515 = pred(v);
      return (null != dart_truthy__49515) && (false != dart_truthy__49515);
    })()){
      return true;
    }
  };
  return false;
}

arr_each(arr, f) {
  var arr_50034 = arr;
  for(var i50035 = 0; i50035 < arr_50034.length; ++i50035){
    var e = arr_50034[i50035];
    f(e);
  };
  return true;
}

arr_find(arr, pred) {
  var arr_50056 = arr;
  for(var i = 0; i < arr_50056.length; ++i){
    var v = arr_50056[i];
    if((() {
      var dart_truthy__49519 = pred(v);
      return (null != dart_truthy__49519) && (false != dart_truthy__49519);
    })()){
      return i - 0;
    }
  };
  return -1;
}

arr_map(arr, f) {
  var out = <dynamic>[];
  var arr_50077 = arr;
  for(var i50078 = 0; i50078 < arr_50077.length; ++i50078){
    var e = arr_50077[i50078];
    out.add(f(e));
  };
  return out;
}

arr_mapcat(arr, f) {
  var out = <dynamic>[];
  var arr_50099 = arr;
  for(var i50100 = 0; i50100 < arr_50099.length; ++i50100){
    var e = arr_50099[i50100];
    var res = f(e);
    if(null != res){
      var arr_50121 = res;
      for(var i50122 = 0; i50122 < arr_50121.length; ++i50122){
        var v = arr_50121[i50122];
        out.add(v);
      };
    }
  };
  return out;
}

arr_partition(arr, n) {
  var out = <dynamic>[];
  var i = 0;
  var sarr = <dynamic>[];
  var arr_50143 = arr;
  for(var i50144 = 0; i50144 < arr_50143.length; ++i50144){
    var e = arr_50143[i50144];
    if(i == n){
      out.add(sarr);
      i = 0;
      sarr = <dynamic>[];
    }
    sarr.add(e);
    i = (i + 1);
  };
  if(0 < sarr.length){
    out.add(sarr);
  }
  return out;
}

arr_filter(arr, pred) {
  var out = <dynamic>[];
  var arr_50165 = arr;
  for(var i50166 = 0; i50166 < arr_50165.length; ++i50166){
    var e = arr_50165[i50166];
    if((() {
      var dart_truthy__49514 = pred(e);
      return (null != dart_truthy__49514) && (false != dart_truthy__49514);
    })()){
      out.add(e);
    }
  };
  return out;
}

arr_keep(arr, f) {
  var out = <dynamic>[];
  var arr_50187 = arr;
  for(var i50188 = 0; i50188 < arr_50187.length; ++i50188){
    var e = arr_50187[i50188];
    var v = f(e);
    if(null != v){
      out.add(v);
    }
  };
  return out;
}

arr_keepf(arr, pred, f) {
  var out = <dynamic>[];
  var arr_50209 = arr;
  for(var i50210 = 0; i50210 < arr_50209.length; ++i50210){
    var e = arr_50209[i50210];
    if((() {
      var dart_truthy__49520 = pred(e);
      return (null != dart_truthy__49520) && (false != dart_truthy__49520);
    })()){
      out.add(f(e));
    }
  };
  return out;
}

arr_juxt(arr, key_fn, val_fn) {
  var out = <dynamic, dynamic>{};
  if(null != arr){
    var arr_50231 = arr;
    for(var i50232 = 0; i50232 < arr_50231.length; ++i50232){
      var e = arr_50231[i50232];
      out[Function.apply((key_fn as Function),<dynamic>[e])] = Function.apply((val_fn as Function),<dynamic>[e]);
    };
  }
  return out;
}

arr_foldl(arr, f, init) {
  var out = init;
  var arr_50253 = arr;
  for(var i50254 = 0; i50254 < arr_50253.length; ++i50254){
    var e = arr_50253[i50254];
    out = f(out,e);
  };
  return out;
}

arr_foldr(arr, f, init) {
  var out = init;
  for(var i = arr.length; i > 0; i = (i + -1)){
    out = f(out,arr[i + -1]);
  };
  return out;
}

arr_pipel(arr, e) {
  return arr_foldl(arr,(x, f) {
    return f(x);
  },e);
}

arr_piper(arr, e) {
  return arr_foldr(arr,(x, f) {
    return f(x);
  },e);
}

arr_group_by(arr, key_fn, view_fn) {
  var out = <dynamic, dynamic>{};
  if(null != arr){
    var arr_50275 = arr;
    for(var i50276 = 0; i50276 < arr_50275.length; ++i50276){
      var e = arr_50275[i50276];
      var g = Function.apply((key_fn as Function),<dynamic>[e]);
      var garr = (null == out[g]) ? <dynamic>[] : out[g];
      out[g] = <dynamic>[];
      garr.add(Function.apply((view_fn as Function),<dynamic>[e]));
      out[g] = garr;
    };
  }
  return out;
}

arr_repeat(x, n) {
  var out = <dynamic>[];
  for(var i = 0; i < (n - 0); i = (i + 1)){
    var item = x;
    if((x.runtimeType).toString().contains("Function") || (x.runtimeType).toString().contains("=>") || (x).toString().startsWith("Closure")){
      item = x();
    }
    out.add(item);
  };
  return out;
}

arr_normalise(arr) {
  var total = arr_foldl(arr,(x, y) {
    return x + y;
  },0);
  return arr_map(arr,(x) {
    return x / total;
  });
}

arr_sort(arr, key_fn, comp_fn) {
  var tmp = null;
  var total = arr.length;
  for(var i = 0; i < (total - 1); i = (i + 1)){
    for(var j = i + 1; j < total; j = (j + 1)){
      var left = arr[i];
      var right = arr[j];
      if((() {
        var dart_truthy__49516 = Function.apply((comp_fn as Function),<dynamic>[
          Function.apply((key_fn as Function),<dynamic>[right]),
          Function.apply((key_fn as Function),<dynamic>[left])
        ]);
        return (null != dart_truthy__49516) && (false != dart_truthy__49516);
      })()){
        tmp = left;
        arr[i] = right;
        arr[j] = tmp;
      }
    };
  };
  return arr;
}

arr_sorted_merge(arr, brr, comp_fn) {
  arr = (arr ?? <dynamic>[]);
  brr = (brr ?? <dynamic>[]);
  var alen = arr.length;
  var blen = brr.length;
  var i = 0;
  var j = 0;
  var k = 0;
  var out = <dynamic>[];
  while((i < alen) && (j < blen)){
    var aitem = arr[i];
    var bitem = brr[j];
    if((() {
      var dart_truthy__49518 = Function.apply((comp_fn as Function),<dynamic>[aitem,bitem]);
      return (null != dart_truthy__49518) && (false != dart_truthy__49518);
    })()){
      i = (i + 1);
      out.add(aitem);
    }
    else{
      j = (j + 1);
      out.add(bitem);
    }
  }
  while(i < alen){
    var aitem = arr[i];
    i = (i + 1);
    out.add(aitem);
  }
  while(j < blen){
    var bitem = brr[j];
    j = (j + 1);
    out.add(bitem);
  }
  return out;
}

obj_map(obj, f) {
  var out = <dynamic, dynamic>{};
  if(null != obj){
    for(var entry_50297 in obj.entries){
      var k = entry_50297.key;
      var v = entry_50297.value;
      out[k] = f(v);
    };
  }
  return out;
}

obj_filter(obj, pred) {
  var out = <dynamic, dynamic>{};
  if(null != obj){
    for(var entry_50298 in obj.entries){
      var k = entry_50298.key;
      var v = entry_50298.value;
      if((() {
        var dart_truthy__49512 = pred(v);
        return (null != dart_truthy__49512) && (false != dart_truthy__49512);
      })()){
        out[k] = v;
      }
    };
  }
  return out;
}

obj_keep(obj, f) {
  var out = <dynamic, dynamic>{};
  if(null != obj){
    for(var entry_50299 in obj.entries){
      var k = entry_50299.key;
      var e = entry_50299.value;
      var v = f(e);
      if(null != v){
        out[k] = v;
      }
    };
  }
  return out;
}

obj_keepf(obj, pred, f) {
  var out = <dynamic, dynamic>{};
  if(null != obj){
    for(var entry_50300 in obj.entries){
      var k = entry_50300.key;
      var e = entry_50300.value;
      if((() {
        var dart_truthy__49517 = pred(e);
        return (null != dart_truthy__49517) && (false != dart_truthy__49517);
      })()){
        out[k] = f(e);
      }
    };
  }
  return out;
}

clone_shallow(x) {
  if(null == x){
    return x;
  }
  else if(("Map" == (x.runtimeType).toString()) || (x.runtimeType).toString().startsWith("_Map") || (x.runtimeType).toString().startsWith("LinkedMap")){
    return arr_clone(x);
  }
  else if((x.runtimeType).toString().startsWith("List") || (x.runtimeType).toString().startsWith("_GrowableList")){
    return obj_clone(x);
  }
  else{
    return x;
  }
}

clone_nested_loop(x, lu) {
  if(null == x){
    return x;
  }
  var cached = lu[x];
  if(null != cached){
    return cached;
  }
  else if(("Map" == (x.runtimeType).toString()) || (x.runtimeType).toString().startsWith("_Map") || (x.runtimeType).toString().startsWith("LinkedMap")){
    var out = <dynamic, dynamic>{};
    lu[x] = out;
    for(var entry_50301 in x.entries){
      var k = entry_50301.key;
      var v = entry_50301.value;
      out[k] = clone_nested_loop(v,lu);
    };
    return out;
  }
  else if((x.runtimeType).toString().startsWith("List") || (x.runtimeType).toString().startsWith("_GrowableList")){
    var out = <dynamic>[];
    lu[x] = out;
    var arr_50302 = x;
    for(var i50303 = 0; i50303 < arr_50302.length; ++i50303){
      var e = arr_50302[i50303];
      out.add(clone_nested_loop(e,lu));
    };
    return out;
  }
  else{
    return x;
  }
}

clone_nested(x) {
  if(!((("Map" == (x.runtimeType).toString()) || (x.runtimeType).toString().startsWith("_Map") || (x.runtimeType).toString().startsWith("LinkedMap")) || ((x.runtimeType).toString().startsWith("List") || (x.runtimeType).toString().startsWith("_GrowableList")))){
    return x;
  }
  else{
    return clone_nested_loop(x,<dynamic, dynamic>{});
  }
}

memoize_key_step(f, key, cache) {
  var value = f(key);
  cache[key] = value;
  return value;
}

memoize_key(f) {
  var cache = <dynamic, dynamic>{};
  var cache_fn = (key) {
    return memoize_key_step(f,key,cache);
  };
  return (key) {
    return cache[key] ?? Function.apply((cache_fn as Function),<dynamic>[key]);
  };
}

id_fn(x) {
  return x["id"];
}

key_fn(k) {
  return (x) {
    return x[k];
  };
}

template_entry(obj, template, props) {
  if((template.runtimeType).toString().contains("Function") || (template.runtimeType).toString().contains("=>") || (template).toString().startsWith("Closure")){
    return template(obj,props);
  }
  else if(null == template){
    return obj;
  }
  else if((template.runtimeType).toString().startsWith("List") || (template.runtimeType).toString().startsWith("_GrowableList")){
    return get_in(obj,template);
  }
  else{
    return template;
  }
}

template_fn(template) {
  return (obj, props) {
    return template_entry(obj,template,props);
  };
}