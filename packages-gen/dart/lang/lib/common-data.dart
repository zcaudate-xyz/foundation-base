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
    for(var entry_40298 in res.entries){
      var i = entry_40298.key;
      var v = entry_40298.value;
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
    for(var entry_40299 in res.entries){
      var i = entry_40299.key;
      var v = entry_40299.value;
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
  var arr_40300 = arr;
  for(var i40301 = 0; i40301 < arr_40300.length; ++i40301){
    var k = arr_40300[i40301];
    out[k] = true;
  };
  return out;
}

arr_omit(arr, i) {
  var out = <dynamic>[];
  var arr_40322 = arr;
  for(var j = 0; j < arr_40322.length; ++j){
    var e = arr_40322[j];
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
  var arr_40343 = ks;
  for(var i = 0; i < arr_40343.length; ++i){
    var k = arr_40343[i];
    out[k] = vs[i];
  };
  return out;
}

arr_clone(arr) {
  var out = <dynamic>[];
  var arr_40364 = arr;
  for(var i40365 = 0; i40365 < arr_40364.length; ++i40365){
    var e = arr_40364[i40365];
    out.add(e);
  };
  return out;
}

arr_assign(arr, other) {
  var arr_40386 = other;
  for(var i40387 = 0; i40387 < arr_40386.length; ++i40387){
    var e = arr_40386[i40387];
    arr.add(e);
  };
  return arr;
}

arr_concat(arr, other) {
  var out = <dynamic>[];
  var arr_40408 = arr;
  for(var i40409 = 0; i40409 < arr_40408.length; ++i40409){
    var e = arr_40408[i40409];
    out.add(e);
  };
  var arr_40430 = other;
  for(var i40431 = 0; i40431 < arr_40430.length; ++i40431){
    var e = arr_40430[i40431];
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
  return arr_rslice(arr,math.max(t - n,0),t);
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
  var arr_40452 = other;
  for(var i40453 = 0; i40453 < arr_40452.length; ++i40453){
    var e = arr_40452[i40453];
    if(lu.containsKey(e)){
      out.add(e);
    }
  };
  return out;
}

arr_difference(arr, other) {
  var lu = arr_lookup(arr);
  var out = <dynamic>[];
  var arr_40474 = other;
  for(var i40475 = 0; i40475 < arr_40474.length; ++i40475){
    var e = arr_40474[i40475];
    if(!lu.containsKey(e)){
      out.add(e);
    }
  };
  return out;
}

arr_union(arr, other) {
  var lu = <dynamic, dynamic>{};
  var arr_40496 = arr;
  for(var i40497 = 0; i40497 < arr_40496.length; ++i40497){
    var e = arr_40496[i40497];
    lu[e] = e;
  };
  var arr_40518 = other;
  for(var i40519 = 0; i40519 < arr_40518.length; ++i40519){
    var e = arr_40518[i40519];
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
  var arr_40540 = arr;
  for(var i40541 = 0; i40541 < arr_40540.length; ++i40541){
    var e = arr_40540[i40541];
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
  var arr_40562 = dist;
  for(var i = 0; i < arr_40562.length; ++i){
    var p = arr_40562[i];
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
    for(var entry_40583 in obj.entries){
      var k = entry_40583.key;
      var v = entry_40583.value;
      out.add(<dynamic>[k,v]);
    };
  }
  return out;
}

obj_clone(obj) {
  var out = <dynamic, dynamic>{};
  if(null != obj){
    for(var entry_40584 in obj.entries){
      var k = entry_40584.key;
      var v = entry_40584.value;
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
    for(var entry_40585 in m.entries){
      var k = entry_40585.key;
      var v = entry_40585.value;
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
    for(var entry_40586 in m.entries){
      var k = entry_40586.key;
      var mv = entry_40586.value;
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
    for(var entry_40587 in input.entries){
      var k = entry_40587.key;
      var mv = entry_40587.value;
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
  var arr_40588 = pairs;
  for(var i40589 = 0; i40589 < arr_40588.length; ++i40589){
    var pair = arr_40588[i40589];
    out[pair[0]] = pair[1];
  };
  return out;
}

obj_del(obj, ks) {
  var arr_40610 = ks;
  for(var i40611 = 0; i40611 < arr_40610.length; ++i40611){
    var k = arr_40610[i40611];
    obj.remove(k);
  };
  return obj;
}

obj_del_all(obj) {
  var arr_40632 = List<dynamic>.from(( obj ).keys);
  for(var i40633 = 0; i40633 < arr_40632.length; ++i40633){
    var k = arr_40632[i40633];
    obj.remove(k);
  };
  return obj;
}

obj_pick(obj, ks) {
  var out = <dynamic, dynamic>{};
  if(null == obj){
    return out;
  }
  var arr_40656 = ks;
  for(var i40657 = 0; i40657 < arr_40656.length; ++i40657){
    var k = arr_40656[i40657];
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
  var arr_40678 = ks;
  for(var i40679 = 0; i40679 < arr_40678.length; ++i40679){
    var k = arr_40678[i40679];
    lu[k] = true;
  };
  for(var entry_40700 in obj.entries){
    var k = entry_40700.key;
    var v = entry_40700.value;
    if(!lu.containsKey(k)){
      out[k] = v;
    }
  };
  return out;
}

obj_transpose(obj) {
  var out = <dynamic, dynamic>{};
  if(null != obj){
    for(var entry_40701 in obj.entries){
      var k = entry_40701.key;
      var v = entry_40701.value;
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
  for(var entry_40702 in m.entries){
    var k = entry_40702.key;
    var v = entry_40702.value;
    var npath = <dynamic>[...path];
    npath.add(k);
    if(("Map" == (v.runtimeType).toString()) || (v.runtimeType).toString().startsWith("_Map") || (v.runtimeType).toString().startsWith("LinkedMap")){
      var arr_40703 = obj_keys_nested(v,npath);
      for(var i40704 = 0; i40704 < arr_40703.length; ++i40704){
        var e = arr_40703[i40704];
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
    for(var entry_40725 in obj.entries){
      var k = entry_40725.key;
      var v = entry_40725.value;
      out.add(k);
      out.add(v);
    };
  }
  else if((obj.runtimeType).toString().startsWith("List") || (obj.runtimeType).toString().startsWith("_GrowableList")){
    var arr_40726 = obj;
    for(var i40727 = 0; i40727 < arr_40726.length; ++i40727){
      var e = arr_40726[i40727];
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
  var arr_40748 = arr;
  for(var i = 0; i < arr_40748.length; ++i){
    var e = arr_40748[i];
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
  var arr_40769 = arr;
  for(var i = 0; i < arr_40769.length; ++i){
    var v = arr_40769[i];
    if(!(() {
      var dart_truthy__40290 = pred(v);
      return (null != dart_truthy__40290) && (false != dart_truthy__40290);
    })()){
      return false;
    }
  };
  return true;
}

arr_some(arr, pred) {
  var arr_40790 = arr;
  for(var i = 0; i < arr_40790.length; ++i){
    var v = arr_40790[i];
    if((() {
      var dart_truthy__40292 = pred(v);
      return (null != dart_truthy__40292) && (false != dart_truthy__40292);
    })()){
      return true;
    }
  };
  return false;
}

arr_each(arr, f) {
  var arr_40811 = arr;
  for(var i40812 = 0; i40812 < arr_40811.length; ++i40812){
    var e = arr_40811[i40812];
    f(e);
  };
  return true;
}

arr_find(arr, pred) {
  var arr_40833 = arr;
  for(var i = 0; i < arr_40833.length; ++i){
    var v = arr_40833[i];
    if((() {
      var dart_truthy__40296 = pred(v);
      return (null != dart_truthy__40296) && (false != dart_truthy__40296);
    })()){
      return i - 0;
    }
  };
  return -1;
}

arr_map(arr, f) {
  var out = <dynamic>[];
  var arr_40854 = arr;
  for(var i40855 = 0; i40855 < arr_40854.length; ++i40855){
    var e = arr_40854[i40855];
    out.add(f(e));
  };
  return out;
}

arr_mapcat(arr, f) {
  var out = <dynamic>[];
  var arr_40876 = arr;
  for(var i40877 = 0; i40877 < arr_40876.length; ++i40877){
    var e = arr_40876[i40877];
    var res = f(e);
    if(null != res){
      var arr_40898 = res;
      for(var i40899 = 0; i40899 < arr_40898.length; ++i40899){
        var v = arr_40898[i40899];
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
  var arr_40920 = arr;
  for(var i40921 = 0; i40921 < arr_40920.length; ++i40921){
    var e = arr_40920[i40921];
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
  var arr_40942 = arr;
  for(var i40943 = 0; i40943 < arr_40942.length; ++i40943){
    var e = arr_40942[i40943];
    if((() {
      var dart_truthy__40291 = pred(e);
      return (null != dart_truthy__40291) && (false != dart_truthy__40291);
    })()){
      out.add(e);
    }
  };
  return out;
}

arr_keep(arr, f) {
  var out = <dynamic>[];
  var arr_40964 = arr;
  for(var i40965 = 0; i40965 < arr_40964.length; ++i40965){
    var e = arr_40964[i40965];
    var v = f(e);
    if(null != v){
      out.add(v);
    }
  };
  return out;
}

arr_keepf(arr, pred, f) {
  var out = <dynamic>[];
  var arr_40986 = arr;
  for(var i40987 = 0; i40987 < arr_40986.length; ++i40987){
    var e = arr_40986[i40987];
    if((() {
      var dart_truthy__40297 = pred(e);
      return (null != dart_truthy__40297) && (false != dart_truthy__40297);
    })()){
      out.add(f(e));
    }
  };
  return out;
}

arr_juxt(arr, key_fn, val_fn) {
  var out = <dynamic, dynamic>{};
  if(null != arr){
    var arr_41008 = arr;
    for(var i41009 = 0; i41009 < arr_41008.length; ++i41009){
      var e = arr_41008[i41009];
      out[Function.apply((key_fn as Function),<dynamic>[e])] = Function.apply((val_fn as Function),<dynamic>[e]);
    };
  }
  return out;
}

arr_foldl(arr, f, init) {
  var out = init;
  var arr_41030 = arr;
  for(var i41031 = 0; i41031 < arr_41030.length; ++i41031){
    var e = arr_41030[i41031];
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
    var arr_41052 = arr;
    for(var i41053 = 0; i41053 < arr_41052.length; ++i41053){
      var e = arr_41052[i41053];
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
        var dart_truthy__40293 = Function.apply((comp_fn as Function),<dynamic>[
          Function.apply((key_fn as Function),<dynamic>[right]),
          Function.apply((key_fn as Function),<dynamic>[left])
        ]);
        return (null != dart_truthy__40293) && (false != dart_truthy__40293);
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
      var dart_truthy__40295 = Function.apply((comp_fn as Function),<dynamic>[aitem,bitem]);
      return (null != dart_truthy__40295) && (false != dart_truthy__40295);
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
    for(var entry_41074 in obj.entries){
      var k = entry_41074.key;
      var v = entry_41074.value;
      out[k] = f(v);
    };
  }
  return out;
}

obj_filter(obj, pred) {
  var out = <dynamic, dynamic>{};
  if(null != obj){
    for(var entry_41075 in obj.entries){
      var k = entry_41075.key;
      var v = entry_41075.value;
      if((() {
        var dart_truthy__40289 = pred(v);
        return (null != dart_truthy__40289) && (false != dart_truthy__40289);
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
    for(var entry_41076 in obj.entries){
      var k = entry_41076.key;
      var e = entry_41076.value;
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
    for(var entry_41077 in obj.entries){
      var k = entry_41077.key;
      var e = entry_41077.value;
      if((() {
        var dart_truthy__40294 = pred(e);
        return (null != dart_truthy__40294) && (false != dart_truthy__40294);
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
    for(var entry_41078 in x.entries){
      var k = entry_41078.key;
      var v = entry_41078.value;
      out[k] = clone_nested_loop(v,lu);
    };
    return out;
  }
  else if((x.runtimeType).toString().startsWith("List") || (x.runtimeType).toString().startsWith("_GrowableList")){
    var out = <dynamic>[];
    lu[x] = out;
    var arr_41079 = x;
    for(var i41080 = 0; i41080 < arr_41079.length; ++i41080){
      var e = arr_41079[i41080];
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