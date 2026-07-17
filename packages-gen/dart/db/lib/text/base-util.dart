import 'package:xtalk_lang/common-data.dart' as xtd;

collect_routes(routes, type) {
  return xtd.arr_juxt(routes,(e) {
    return e["url"];
  },(e) {
    return xtd.obj_assign(<dynamic, dynamic>{"type":type},e);
  });
}

collect_views(routes) {
  var out = <dynamic, dynamic>{};
  var arr_42932 = routes;
  for(var i42933 = 0; i42933 < arr_42932.length; ++i42933){
    var route = arr_42932[i42933];
    var view = route["view"];
    var table = view["table"];
    var tag = view["tag"];
    var type = view["type"];
    var v0 = out[table];
    if(null == v0){
      v0 = <dynamic, dynamic>{};
      out[table] = v0;
    }
    var v1 = v0[type];
    if(null == v1){
      v1 = <dynamic, dynamic>{};
      v0[type] = v1;
    }
    v1[tag] = route;
  };
  return out;
}

merge_views(views, acc) {
  var merge_fn = (e, view_entry) {
    xtd.obj_assign(e["select"] ?? <dynamic, dynamic>{},view_entry["select"]);
    xtd.obj_assign(e["return"] ?? <dynamic, dynamic>{},view_entry["return"]);
    return e;
  };
  return xtd.arr_foldl(views,(out, view) {
    return xtd.obj_assign_with(out,view,merge_fn);
  },acc ?? <dynamic, dynamic>{});
}

keepf_limit(arr, pred, f, n) {
  var out = <dynamic>[];
  var i = 0;
  var arr_42954 = arr;
  for(var i42955 = 0; i42955 < arr_42954.length; ++i42955){
    var e = arr_42954[i42955];
    if(i == n){
      return out;
    }
    if((() {
      var dart_truthy__42930 = pred(e);
      return (null != dart_truthy__42930) && (false != dart_truthy__42930);
    })()){
      var interim = f(e);
      if((("int" == (interim.runtimeType).toString()) || ("double" == (interim.runtimeType).toString()) || ("num" == (interim.runtimeType).toString())) || ("String" == (interim.runtimeType).toString()) || (() {
        var dart_truthy__42931 = xtd.obj_not_emptyp(interim);
        return (null != dart_truthy__42931) && (false != dart_truthy__42931);
      })()){
        out.add(interim);
        i = (i + 1);
      }
    }
  };
  return out;
}

lu_nested(obj, key_fn) {
  if(null == obj){
    return obj;
  }
  else if(("Map" == (obj.runtimeType).toString()) || (obj.runtimeType).toString().startsWith("_Map") || (obj.runtimeType).toString().startsWith("LinkedMap")){
    return xtd.obj_map(obj,(v) {
      return lu_nested(v,key_fn);
    });
  }
  else if((obj.runtimeType).toString().startsWith("List") || (obj.runtimeType).toString().startsWith("_GrowableList")){
    return lu_nested(xtd.arr_juxt(obj,key_fn,(x) {
      return x;
    }),key_fn);
  }
  else{
    return obj;
  }
}

lu_map(arr) {
  return lu_nested(arr,(v) {
    return v["id"];
  });
}