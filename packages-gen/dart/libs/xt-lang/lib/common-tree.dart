import 'package:xtalk_lang/common-data.dart' as xtd;
import 'package:xtalk_lang/common-lib.dart' as xtl;



eq_nested_loop(src, dst, eq_obj, eq_arr, cache) {
  cache = (cache ?? <dynamic, dynamic>{});
  if((("Map" == (src.runtimeType).toString()) || (src.runtimeType).toString().startsWith("_Map") || (src.runtimeType).toString().startsWith("LinkedMap")) && (("Map" == (dst.runtimeType).toString()) || (dst.runtimeType).toString().startsWith("_Map") || (dst.runtimeType).toString().startsWith("LinkedMap"))){
    if((() {
      var dart_truthy__49297 = cache[src];
      return (null != dart_truthy__49297) && (false != dart_truthy__49297);
    })() && (() {
      var dart_truthy__49298 = cache[dst];
      return (null != dart_truthy__49298) && (false != dart_truthy__49298);
    })()){
      return true;
    }
    else{
      return eq_obj(src,dst,eq_obj,eq_arr,cache);
    }
  }
  else if(((src.runtimeType).toString().startsWith("List") || (src.runtimeType).toString().startsWith("_GrowableList")) && ((dst.runtimeType).toString().startsWith("List") || (dst.runtimeType).toString().startsWith("_GrowableList"))){
    if((() {
      var dart_truthy__49299 = cache[src];
      return (null != dart_truthy__49299) && (false != dart_truthy__49299);
    })() && (() {
      var dart_truthy__49300 = cache[dst];
      return (null != dart_truthy__49300) && (false != dart_truthy__49300);
    })()){
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

eq_shallow_raw(src, dst, eq_obj, eq_arr, cache) {
  return src == dst;
}

eq_shallow(obj, m) {
  return eq_nested_loop(obj,m,eq_shallow_raw,eq_shallow_raw,null);
}

eq_nested_obj(src, dst, eq_obj, eq_arr, cache) {
  cache[src] = src;
  cache[dst] = dst;
  var ks_src = List<dynamic>.from(( src ).keys);
  var ks_dst = List<dynamic>.from(( dst ).keys);
  if(ks_src.length != ks_dst.length){
    return false;
  }
  var arr_49308 = ks_src;
  for(var i49309 = 0; i49309 < arr_49308.length; ++i49309){
    var k = arr_49308[i49309];
    if(!(() {
      var dart_truthy__49296 = eq_nested_loop(src[k],dst[k],eq_obj,eq_arr,cache);
      return (null != dart_truthy__49296) && (false != dart_truthy__49296);
    })()){
      return false;
    }
  };
  return true;
}

eq_nested_arr(src_arr, dst_arr, eq_obj, eq_arr, cache) {
  cache[src_arr] = src_arr;
  cache[dst_arr] = dst_arr;
  if(src_arr.length != dst_arr.length){
    return false;
  }
  var arr_49330 = src_arr;
  for(var i = 0; i < arr_49330.length; ++i){
    var v = arr_49330[i];
    if(!(() {
      var dart_truthy__49303 = eq_nested_loop(v,dst_arr[i],eq_obj,eq_arr,cache);
      return (null != dart_truthy__49303) && (false != dart_truthy__49303);
    })()){
      return false;
    }
  };
  return true;
}

eq_nested(obj, m) {
  return eq_nested_loop(obj,m,eq_nested_obj,eq_nested_arr,null);
}

tree_walk(x, pre_fn, post_fn) {
  x = Function.apply((pre_fn as Function),<dynamic>[x]);
  if(null == x){
    return Function.apply((post_fn as Function),<dynamic>[x]);
  }
  else if(("Map" == (x.runtimeType).toString()) || (x.runtimeType).toString().startsWith("_Map") || (x.runtimeType).toString().startsWith("LinkedMap")){
    var out = <dynamic, dynamic>{};
    for(var entry_49351 in x.entries){
      var k = entry_49351.key;
      var v = entry_49351.value;
      out[k] = tree_walk(v,pre_fn,post_fn);
    };
    return Function.apply((post_fn as Function),<dynamic>[out]);
  }
  else if((x.runtimeType).toString().startsWith("List") || (x.runtimeType).toString().startsWith("_GrowableList")){
    var out = <dynamic>[];
    var arr_49352 = x;
    for(var i49353 = 0; i49353 < arr_49352.length; ++i49353){
      var e = arr_49352[i49353];
      out.add(tree_walk(e,pre_fn,post_fn));
    };
    return Function.apply((post_fn as Function),<dynamic>[out]);
  }
  else{
    return Function.apply((post_fn as Function),<dynamic>[x]);
  }
}

tree_get_data(obj) {
  if(null == obj){
    return obj;
  }
  else if(("Map" == (obj.runtimeType).toString()) || (obj.runtimeType).toString().startsWith("_Map") || (obj.runtimeType).toString().startsWith("LinkedMap")){
    var out = <dynamic, dynamic>{};
    for(var entry_49374 in obj.entries){
      var k = entry_49374.key;
      var v = entry_49374.value;
      out[k] = tree_get_data(v);
    };
    return out;
  }
  else if((obj.runtimeType).toString().startsWith("List") || (obj.runtimeType).toString().startsWith("_GrowableList")){
    var out = <dynamic>[];
    var arr_49375 = obj;
    for(var i49376 = 0; i49376 < arr_49375.length; ++i49376){
      var e = arr_49375[i49376];
      out.add(tree_get_data(e));
    };
    return out;
  }
  else if(("String" == (obj.runtimeType).toString()) || (("int" == (obj.runtimeType).toString()) || ("double" == (obj.runtimeType).toString()) || ("num" == (obj.runtimeType).toString())) || ("bool" == (obj.runtimeType).toString())){
    return obj;
  }
  else{
    return "<" + xtl.type_native(obj) + ">";
  }
}

tree_get_spec(obj) {
  if(("Map" == (obj.runtimeType).toString()) || (obj.runtimeType).toString().startsWith("_Map") || (obj.runtimeType).toString().startsWith("LinkedMap")){
    var out = <dynamic, dynamic>{};
    for(var entry_49397 in obj.entries){
      var k = entry_49397.key;
      var v = entry_49397.value;
      out[k] = tree_get_spec(v);
    };
    return out;
  }
  else if((obj.runtimeType).toString().startsWith("List") || (obj.runtimeType).toString().startsWith("_GrowableList")){
    var out = <dynamic>[];
    var arr_49398 = obj;
    for(var i49399 = 0; i49399 < arr_49398.length; ++i49399){
      var e = arr_49398[i49399];
      out.add(tree_get_spec(e));
    };
    return out;
  }
  else{
    return xtl.type_native(obj);
  }
}

tree_diff(obj, m) {
  if(null == m){
    return <dynamic, dynamic>{};
  }
  if(null == obj){
    return m;
  }
  var out = <dynamic, dynamic>{};
  for(var entry_49420 in m.entries){
    var k = entry_49420.key;
    var v = entry_49420.value;
    if(!(() {
      var dart_truthy__49295 = eq_nested(obj[k],m[k]);
      return (null != dart_truthy__49295) && (false != dart_truthy__49295);
    })()){
      out[k] = v;
    }
  };
  return out;
}

tree_diff_nested(obj, m) {
  if(null == m){
    return <dynamic, dynamic>{};
  }
  if(null == obj){
    return m;
  }
  var out = <dynamic, dynamic>{};
  var ks = List<dynamic>.from(( m ).keys);
  var arr_49423 = ks;
  for(var i49424 = 0; i49424 < arr_49423.length; ++i49424){
    var k = arr_49423[i49424];
    var v = obj[k];
    var mv = m[k];
    if((("Map" == (v.runtimeType).toString()) || (v.runtimeType).toString().startsWith("_Map") || (v.runtimeType).toString().startsWith("LinkedMap")) && (("Map" == (mv.runtimeType).toString()) || (mv.runtimeType).toString().startsWith("_Map") || (mv.runtimeType).toString().startsWith("LinkedMap"))){
      var dv = tree_diff_nested(v,mv);
      if(!(() {
        var dart_truthy__49301 = xtd.obj_emptyp(dv);
        return (null != dart_truthy__49301) && (false != dart_truthy__49301);
      })()){
        out[k] = dv;
      }
    }
    else if(!(() {
      var dart_truthy__49302 = eq_nested(v,mv);
      return (null != dart_truthy__49302) && (false != dart_truthy__49302);
    })()){
      out[k] = mv;
    }
  };
  return out;
}