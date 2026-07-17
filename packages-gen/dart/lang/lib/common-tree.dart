import 'package:xtalk_lang/common-data.dart' as xtd;

import 'package:xtalk_lang/common-lib.dart' as xtl;

eq_nested_loop(src, dst, eq_obj, eq_arr, cache) {
  cache = (cache ?? <dynamic, dynamic>{});
  if((("Map" == (src.runtimeType).toString()) || (src.runtimeType).toString().startsWith("_Map") || (src.runtimeType).toString().startsWith("LinkedMap")) && (("Map" == (dst.runtimeType).toString()) || (dst.runtimeType).toString().startsWith("_Map") || (dst.runtimeType).toString().startsWith("LinkedMap"))){
    if((() {
      var dart_truthy__39751 = cache[src];
      return (null != dart_truthy__39751) && (false != dart_truthy__39751);
    })() && (() {
      var dart_truthy__39752 = cache[dst];
      return (null != dart_truthy__39752) && (false != dart_truthy__39752);
    })()){
      return true;
    }
    else{
      return eq_obj(src,dst,eq_obj,eq_arr,cache);
    }
  }
  else if(((src.runtimeType).toString().startsWith("List") || (src.runtimeType).toString().startsWith("_GrowableList")) && ((dst.runtimeType).toString().startsWith("List") || (dst.runtimeType).toString().startsWith("_GrowableList"))){
    if((() {
      var dart_truthy__39753 = cache[src];
      return (null != dart_truthy__39753) && (false != dart_truthy__39753);
    })() && (() {
      var dart_truthy__39754 = cache[dst];
      return (null != dart_truthy__39754) && (false != dart_truthy__39754);
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
  var arr_39762 = ks_src;
  for(var i39763 = 0; i39763 < arr_39762.length; ++i39763){
    var k = arr_39762[i39763];
    if(!(() {
      var dart_truthy__39750 = eq_nested_loop(src[k],dst[k],eq_obj,eq_arr,cache);
      return (null != dart_truthy__39750) && (false != dart_truthy__39750);
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
  var arr_39784 = src_arr;
  for(var i = 0; i < arr_39784.length; ++i){
    var v = arr_39784[i];
    if(!(() {
      var dart_truthy__39757 = eq_nested_loop(v,dst_arr[i],eq_obj,eq_arr,cache);
      return (null != dart_truthy__39757) && (false != dart_truthy__39757);
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
    for(var entry_39805 in x.entries){
      var k = entry_39805.key;
      var v = entry_39805.value;
      out[k] = tree_walk(v,pre_fn,post_fn);
    };
    return Function.apply((post_fn as Function),<dynamic>[out]);
  }
  else if((x.runtimeType).toString().startsWith("List") || (x.runtimeType).toString().startsWith("_GrowableList")){
    var out = <dynamic>[];
    var arr_39806 = x;
    for(var i39807 = 0; i39807 < arr_39806.length; ++i39807){
      var e = arr_39806[i39807];
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
    for(var entry_39828 in obj.entries){
      var k = entry_39828.key;
      var v = entry_39828.value;
      out[k] = tree_get_data(v);
    };
    return out;
  }
  else if((obj.runtimeType).toString().startsWith("List") || (obj.runtimeType).toString().startsWith("_GrowableList")){
    var out = <dynamic>[];
    var arr_39829 = obj;
    for(var i39830 = 0; i39830 < arr_39829.length; ++i39830){
      var e = arr_39829[i39830];
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
    for(var entry_39851 in obj.entries){
      var k = entry_39851.key;
      var v = entry_39851.value;
      out[k] = tree_get_spec(v);
    };
    return out;
  }
  else if((obj.runtimeType).toString().startsWith("List") || (obj.runtimeType).toString().startsWith("_GrowableList")){
    var out = <dynamic>[];
    var arr_39852 = obj;
    for(var i39853 = 0; i39853 < arr_39852.length; ++i39853){
      var e = arr_39852[i39853];
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
  for(var entry_39874 in m.entries){
    var k = entry_39874.key;
    var v = entry_39874.value;
    if(!(() {
      var dart_truthy__39749 = eq_nested(obj[k],m[k]);
      return (null != dart_truthy__39749) && (false != dart_truthy__39749);
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
  var arr_39877 = ks;
  for(var i39878 = 0; i39878 < arr_39877.length; ++i39878){
    var k = arr_39877[i39878];
    var v = obj[k];
    var mv = m[k];
    if((("Map" == (v.runtimeType).toString()) || (v.runtimeType).toString().startsWith("_Map") || (v.runtimeType).toString().startsWith("LinkedMap")) && (("Map" == (mv.runtimeType).toString()) || (mv.runtimeType).toString().startsWith("_Map") || (mv.runtimeType).toString().startsWith("LinkedMap"))){
      var dv = tree_diff_nested(v,mv);
      if(!(() {
        var dart_truthy__39755 = xtd.obj_emptyp(dv);
        return (null != dart_truthy__39755) && (false != dart_truthy__39755);
      })()){
        out[k] = dv;
      }
    }
    else if(!(() {
      var dart_truthy__39756 = eq_nested(v,mv);
      return (null != dart_truthy__39756) && (false != dart_truthy__39756);
    })()){
      out[k] = mv;
    }
  };
  return out;
}