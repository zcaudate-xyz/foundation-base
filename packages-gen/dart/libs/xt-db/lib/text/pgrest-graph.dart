import 'package:xtalk_lang/common-data.dart' as xtd;
import 'package:xtalk_db/text/base-graph.dart' as base_graph;



tree_countp(custom) {
  return xtd.arr_some(custom ?? <dynamic>[],(entry) {
    return entry["::"] == "sql/count";
  });
}

pgrest_resolve_value(value) {
  if((("Map" == (value.runtimeType).toString()) || (value.runtimeType).toString().startsWith("_Map") || (value.runtimeType).toString().startsWith("LinkedMap")) && value.containsKey("::")){
    var tcls = value["::"];
    if(tcls == "sql/arg"){
      return value["name"];
    }
    else if(tcls == "sql/cast"){
      return pgrest_resolve_value((value["args"])[0]);
    }
    else if(tcls == "sql/defenum"){
      return value["name"];
    }
    else{
      return value;
    }
  }
  else{
    return value;
  }
}

value__gtquery_text(value) {
  value = pgrest_resolve_value(value);
  if(null == value){
    return "null";
  }
  else if("String" == (value.runtimeType).toString()){
    return value;
  }
  else if("bool" == (value.runtimeType).toString()){
    return ((null != value) && (false != value)) ? "true" : "false";
  }
  else if(("int" == (value.runtimeType).toString()) || ("double" == (value.runtimeType).toString()) || ("num" == (value.runtimeType).toString())){
    return (value).toString();
  }
  else{
    return (value).toString();
  }
}

normalise_in_values(value) {
  if(((value.runtimeType).toString().startsWith("List") || (value.runtimeType).toString().startsWith("_GrowableList")) && (1 == value.length) && ((value[0].runtimeType).toString().startsWith("List") || (value[0].runtimeType).toString().startsWith("_GrowableList"))){
    return value[0];
  }
  else if((value.runtimeType).toString().startsWith("List") || (value.runtimeType).toString().startsWith("_GrowableList")){
    return value;
  }
  else{
    return <dynamic>[value];
  }
}

filter_operatorp(op) {
  return (op == "eq") || (op == "neq") || (op == "gt") || (op == "gte") || (op == "lt") || (op == "lte") || (op == "like") || (op == "ilike") || (op == "is") || (op == "in");
}

compile_filter_value(op, value) {
  if(op == "in"){
    return op + ".(" + xtd.arr_map(normalise_in_values(value),value__gtquery_text).join(",") + ")";
  }
  else{
    return op + "." + value__gtquery_text(value);
  }
}

compile_filter_fragment(filter) {
  return filter["path"] + "." + compile_filter_value(filter["op"],filter["value"]);
}

compile_clause_into(prefix, clause, out) {
  var arr_52579 = xtd.arr_sort(List<dynamic>.from(( clause ?? <dynamic, dynamic>{} ).keys),(value) {
    return (value).toString();
  },(x, y) {
    return (x).toString().compareTo((y).toString()) < 0;
  });
  for(var i52580 = 0; i52580 < arr_52579.length; ++i52580){
    var key = arr_52579[i52580];
    var value = clause[key];
    var path = ((null != xtd.not_emptyp(prefix)) && (false != xtd.not_emptyp(prefix))) ? (prefix + "." + key) : key;
    if((("Map" == (value.runtimeType).toString()) || (value.runtimeType).toString().startsWith("_Map") || (value.runtimeType).toString().startsWith("LinkedMap")) && !((value.runtimeType).toString().startsWith("List") || (value.runtimeType).toString().startsWith("_GrowableList")) && (null == value["::"])){
      compile_clause_into(path,value,out);
    }
    else if(((value.runtimeType).toString().startsWith("List") || (value.runtimeType).toString().startsWith("_GrowableList")) && ("String" == (value[0].runtimeType).toString()) && (() {
      var dart_truthy__52578 = filter_operatorp(value[0]);
      return (null != dart_truthy__52578) && (false != dart_truthy__52578);
    })()){
      out.add(
        <dynamic, dynamic>{"path":path,"op":value[0],"value":value[1]}
      );
    }
    else if(((value.runtimeType).toString().startsWith("List") || (value.runtimeType).toString().startsWith("_GrowableList")) && ("String" == (value[0].runtimeType).toString())){
      throw "Unsupported filter operator - " + value[0];
    }
    else{
      out.add(<dynamic, dynamic>{"path":path,"op":"eq","value":value});
    }
  };
  return out;
}

compile_or_clause(clause) {
  var fragments = xtd.arr_map(
    compile_clause_into("",clause,<dynamic>[]),
    compile_filter_fragment
  );
  if(0 == fragments.length){
    return "";
  }
  else if(1 == fragments.length){
    return fragments[0];
  }
  else{
    return "and(" + fragments.join(",") + ")";
  }
}

forward_ref_columnp(schema, table_name, key) {
  var table = schema[table_name];
  var col = ((null != table) && (false != table)) ? table[key] : table;
  return (("Map" == (col.runtimeType).toString()) || (col.runtimeType).toString().startsWith("_Map") || (col.runtimeType).toString().startsWith("LinkedMap")) && ("ref" == col["type"]) && !("reverse" == col["ref"]["type"]);
}

flatten_forward_ref_clause(schema, table_name, clause) {
  var out = <dynamic, dynamic>{};
  for(var entry_52603 in clause.entries){
    var k = entry_52603.key;
    var v = entry_52603.value;
    if((() {
      var dart_truthy__52576 = forward_ref_columnp(schema,table_name,k);
      return (null != dart_truthy__52576) && (false != dart_truthy__52576);
    })() && (("Map" == (v.runtimeType).toString()) || (v.runtimeType).toString().startsWith("_Map") || (v.runtimeType).toString().startsWith("LinkedMap")) && v.containsKey("id")){
      out[k + "_id"] = v["id"];
    }
    else if(("Map" == (v.runtimeType).toString()) || (v.runtimeType).toString().startsWith("_Map") || (v.runtimeType).toString().startsWith("LinkedMap")){
      out[k] = flatten_forward_ref_clause(schema,table_name,v);
    }
    else{
      out[k] = v;
    }
  };
  return out;
}

flatten_forward_ref_filters(schema, table_name, where) {
  where = (((where.runtimeType).toString().startsWith("List") || (where.runtimeType).toString().startsWith("_GrowableList")) ? where : ((("Map" == (where.runtimeType).toString()) || (where.runtimeType).toString().startsWith("_Map") || (where.runtimeType).toString().startsWith("LinkedMap")) ? <dynamic>[where] : <dynamic>[]));
  return xtd.arr_map(where,(clause) {
    return flatten_forward_ref_clause(schema,table_name,clause);
  });
}

compile_where_params(where) {
  where = (((where.runtimeType).toString().startsWith("List") || (where.runtimeType).toString().startsWith("_GrowableList")) ? where : ((("Map" == (where.runtimeType).toString()) || (where.runtimeType).toString().startsWith("_Map") || (where.runtimeType).toString().startsWith("LinkedMap")) ? <dynamic>[where] : <dynamic>[]));
  if(0 == where.length){
    return <dynamic>[];
  }
  if(1 == where.length){
    return xtd.arr_map(compile_clause_into("",where[0],<dynamic>[]),(filter) {
      return filter["path"] + "=" + compile_filter_value(filter["op"],filter["value"]);
    });
  }
  var clauses = xtd.arr_filter(xtd.arr_map(where,compile_or_clause),xtd.not_emptyp);
  return <dynamic>["or=(" + clauses.join(",") + ")"];
}

compile_tree_select_item(item, select_params_fn) {
  if("String" == (item.runtimeType).toString()){
    return item;
  }
  else if(((item.runtimeType).toString().startsWith("List") || (item.runtimeType).toString().startsWith("_GrowableList")) && (item.length >= 3) && ("String" == (item[0].runtimeType).toString())){
    return item[0] + ":" + (xtd.nth(item,2))[0] + "(" + Function.apply(
      (select_params_fn as Function),
      <dynamic>[xtd.second(xtd.nth(item,2))]
    ) + ")";
  }
  else{
    return (pgrest_resolve_value(item)).toString();
  }
}

compile_tree_select_params(params) {
  var custom = params["custom"];
  var data = params["data"];
  var links = params["links"];
  if((() {
    var dart_truthy__52577 = tree_countp(custom);
    return (null != dart_truthy__52577) && (false != dart_truthy__52577);
  })()){
    return "count";
  }
  var out = <dynamic>[];
  xtd.arr_assign(out,xtd.arr_map(data ?? <dynamic>[],(item) {
    return pgrest_resolve_value(item);
  }));
  xtd.arr_assign(out,xtd.arr_map(links ?? <dynamic>[],(item) {
    return compile_tree_select_item(item,compile_tree_select_params);
  }));
  return (out.length > 0) ? out.join(",") : "*";
}

compile_control_params(custom) {
  var order_cols = null;
  var order_sort = null;
  var limit = null;
  var offset = null;
  var arr_52604 = custom ?? <dynamic>[];
  for(var i52605 = 0; i52605 < arr_52604.length; ++i52605){
    var entry = arr_52604[i52605];
    if(entry["::"] == "sql/keyword"){
      var name = entry["name"];
      if(name == "ORDER BY"){
        var tuple = (entry["args"] ?? <dynamic>[])[0];
        order_cols = xtd.arr_map(tuple["args"] ?? <dynamic>[],(arg) {
          return arg["name"];
        });
      }
      else if((name == "ASC") || (name == "DESC")){
        order_sort = (name).toLowerCase();
      }
      else if(name == "LIMIT"){
        limit = ((entry["args"] ?? <dynamic>[])[0])["name"];
      }
      else if(name == "OFFSET"){
        offset = ((entry["args"] ?? <dynamic>[])[0])["name"];
      }
    }
  };
  var out = <dynamic>[];
  if((order_cols.runtimeType).toString().startsWith("List") || (order_cols.runtimeType).toString().startsWith("_GrowableList")){
    out.add("order=" + xtd.arr_map(order_cols,(col) {
      return (null != order_sort) ? (col + "." + order_sort) : col;
    }).join(","));
  }
  if(null != limit){
    out.add("limit=" + value__gtquery_text(limit));
  }
  if(null != offset){
    out.add("offset=" + value__gtquery_text(offset));
  }
  return out;
}

compile_query_string(params) {
  return (params ?? <dynamic>[]).join("&");
}

compile_url(path, params) {
  var query = compile_query_string(params);
  return ((null != xtd.not_emptyp(query)) && (false != xtd.not_emptyp(query))) ? (path + "?" + query) : path;
}

select_return(schema, tree, indent, opts) {
  tree = base_graph.select_tree(schema,tree,opts);
  var table_name = tree[0];
  var params = xtd.second(tree);
  var where = flatten_forward_ref_filters(schema,table_name,params["where"] ?? <dynamic>[]);
  params["where"] = where;
  var custom = params["custom"] ?? <dynamic>[];
  var select = compile_tree_select_params(params);
  var request_params = <dynamic>["select=" + select];
  request_params = xtd.arr_concat(request_params,compile_where_params(where));
  request_params = xtd.arr_concat(request_params,compile_control_params(custom));
  var path = "/rest/v1/" + table_name;
  var query = compile_query_string(request_params);
  var url = compile_url(path,request_params);
  return <dynamic, dynamic>{
    "table":table_name,
    "url":url,
    "params":request_params,
    "method":"GET",
    "query":query,
    "path":path,
    "filters":where,
    "select":select,
    "type":"query",
    "headers":<dynamic, dynamic>{}
  };
}

select_tree(schema, query, opts) {
  return base_graph.select_tree(schema,query,opts);
}

select(schema, query, opts) {
  return select_return(schema,query,0,opts);
}