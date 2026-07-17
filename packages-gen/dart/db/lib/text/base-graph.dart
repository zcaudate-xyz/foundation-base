import 'package:xtalk_lang/common-data.dart' as xtd;

import 'package:xtalk_db/text/base-scope.dart' as scope;

tree_paramsp(params) {
  return (("Map" == (params.runtimeType).toString()) || (params.runtimeType).toString().startsWith("_Map") || (params.runtimeType).toString().startsWith("LinkedMap")) && !((params.runtimeType).toString().startsWith("List") || (params.runtimeType).toString().startsWith("_GrowableList")) && (params.containsKey("where") || params.containsKey("data") || params.containsKey("links") || params.containsKey("custom"));
}

treep(query) {
  return (() {
    var dart_and__43007 = (() {
      var dart_and__43006 = (query.runtimeType).toString().startsWith("List") || (query.runtimeType).toString().startsWith("_GrowableList");
      return ((null != dart_and__43006) && (false != dart_and__43006)) ? (query.length >= 2) : dart_and__43006;
    })();
    return ((null != dart_and__43007) && (false != dart_and__43007)) ? tree_paramsp(xtd.second(query)) : dart_and__43007;
  })();
}

normalise_tree_params(params) {
  var out = xtd.obj_clone(params ?? <dynamic, dynamic>{});
  if(null == out["where"]){
    out["where"] = <dynamic>[];
  }
  if(null == out["data"]){
    out["data"] = <dynamic>[];
  }
  if(null == out["links"]){
    out["links"] = <dynamic>[];
  }
  if(null == out["custom"]){
    out["custom"] = <dynamic>[];
  }
  return out;
}

normalise_tree(query) {
  if(!(() {
    var dart_truthy__43008 = treep(query);
    return (null != dart_truthy__43008) && (false != dart_truthy__43008);
  })()){
    return query;
  }
  else{
    return <dynamic>[query[0],normalise_tree_params(xtd.second(query))];
  }
}

base_query_inputs(query) {
  var table_name = query[0];
  var cnt = query.length;
  if(cnt == 1){
    return <dynamic>[table_name,<dynamic, dynamic>{},null];
  }
  else if(cnt == 3){
    return <dynamic>[table_name,query[1],xtd.nth(query,2)];
  }
  else if((query[1].runtimeType).toString().startsWith("List") || (query[1].runtimeType).toString().startsWith("_GrowableList")){
    return <dynamic>[table_name,<dynamic, dynamic>{},query[1]];
  }
  else{
    return <dynamic>[table_name,query[1],null];
  }
}

select_tree(schema, query, opts) {
  opts = (opts ?? <dynamic, dynamic>{});
  if((() {
    var dart_truthy__43009 = treep(query);
    return (null != dart_truthy__43009) && (false != dart_truthy__43009);
  })()){
    return normalise_tree(query);
  }
  else{
    var input = scope.get_link_standard(query);
    var table_name = input[0];
    var linked = xtd.second(input);
    var return_params = linked[linked.length + -1];
    var where_params = xtd.arr_filter(linked,(x) {
      return (() {
        var dart_and__43010 = ("Map" == (x.runtimeType).toString()) || (x.runtimeType).toString().startsWith("_Map") || (x.runtimeType).toString().startsWith("LinkedMap");
        return ((null != dart_and__43010) && (false != dart_and__43010)) ? xtd.not_emptyp(x) : dart_and__43010;
      })();
    });
    return scope.get_tree(schema,table_name,where_params,return_params,opts);
  }
}