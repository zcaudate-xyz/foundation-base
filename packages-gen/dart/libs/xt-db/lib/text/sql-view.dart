import 'package:xtalk_lang/common-tree.dart' as xtt;
import 'package:xtalk_lang/common-data.dart' as xtd;
import 'package:xtalk_db/text/base-scope.dart' as base_scope;
import 'package:xtalk_db/text/sql-util.dart' as sql_util;
import 'package:xtalk_db/text/sql-graph.dart' as sql_graph;






tree_control_array(control) {
  if((() {
    var dart_truthy__52367 = xtd.is_emptyp(control);
    return (null != dart_truthy__52367) && (false != dart_truthy__52367);
  })()){
    return <dynamic>[];
  }
  var out = <dynamic>[];
  var limit = control["limit"];
  var offset = control["offset"];
  var order_by = control["order_by"];
  var order_sort = control["order_sort"];
  if((order_by.runtimeType).toString().startsWith("List") || (order_by.runtimeType).toString().startsWith("_GrowableList")){
    out.add(sql_util.ORDER_BY(order_by));
  }
  if(null != order_sort){
    out.add(sql_util.ORDER_SORT(order_sort));
  }
  if(("int" == (limit.runtimeType).toString()) || ("double" == (limit.runtimeType).toString()) || ("num" == (limit.runtimeType).toString())){
    out.add(sql_util.LIMIT(limit));
  }
  if(("int" == (offset.runtimeType).toString()) || ("double" == (offset.runtimeType).toString()) || ("num" == (offset.runtimeType).toString())){
    out.add(sql_util.OFFSET(offset));
  }
  return out;
}

tree_base(schema, table_name, sel_query, clause, returning, opts) {
  var tarr = base_scope.merge_queries(sel_query,clause);
  var tree = xtd.arr_assign(<dynamic>[table_name],tarr);
  if((() {
    var dart_truthy__52366 = xtd.not_emptyp(returning);
    return (null != dart_truthy__52366) && (false != dart_truthy__52366);
  })()){
    tree.add(returning);
  }
  return sql_graph.select_tree(schema,tree,opts);
}

tree_count(schema, entry, clause, opts) {
  var control = entry["control"];
  var view = entry["view"];
  var query = view["query"];
  var table = view["table"];
  return tree_base(schema,table,query,clause,xtd.arr_assign(
    <dynamic>[<dynamic, dynamic>{"::":"sql/count"}],
    tree_control_array(control)
  ),opts);
}

tree_select(schema, entry, clause, opts) {
  var control = entry["control"];
  var view = entry["view"];
  var query = view["query"];
  var table = view["table"];
  return tree_base(
    schema,
    table,
    query,
    clause,
    xtd.arr_assign(<dynamic>["id"],tree_control_array(control)),
    opts
  );
}

tree_return(schema, entry, sel_query, clause, opts) {
  var view = entry["view"];
  var query = view["query"];
  var table = view["table"];
  return tree_base(schema,table,sel_query,clause,query,opts);
}

tree_combined(schema, sel_entry, ret_entry, ret_omit, clause, opts) {
  var control = sel_entry["control"];
  var sel_table = sel_entry["view"]["table"];
  var ret_table = ret_entry["view"]["table"];
  var sel_query = sel_entry["view"]["query"];
  var ret_query = ret_entry["view"]["query"];
  if(null == sel_query){
    sel_query = <dynamic, dynamic>{};
  }
  if(null == ret_query){
    ret_query = <dynamic, dynamic>{};
  }
  var ret_clause = ((null != xtd.not_emptyp(ret_omit)) && (false != xtd.not_emptyp(ret_omit))) ? <dynamic>[
    <dynamic, dynamic>{"id":<dynamic, dynamic>{"not_in":<dynamic>[ret_omit]}}
  ] : <dynamic>[];
  var combined_clause = base_scope.merge_queries(clause,ret_clause);
  return tree_base(
    schema,
    sel_table,
    sel_query,
    combined_clause,
    xtd.arr_assign(xtd.arr_clone(ret_query),tree_control_array(control)),
    opts
  );
}

query_fill_input(tree, args, input_spec, drop_first) {
  var arg_map = <dynamic, dynamic>{};
  if((null != drop_first) && (false != drop_first)){
    input_spec.removeAt(0);
  }
  if(0 == input_spec.length){
    return tree;
  }
  var arr_52368 = input_spec;
  for(var i = 0; i < arr_52368.length; ++i){
    var e = arr_52368[i];
    arg_map["{{" + e["symbol"] + "}}"] = args[i];
  };
  var out = xtt.tree_walk(tree,(x) {
    return x;
  },(x) {
    return (("String" == (x.runtimeType).toString()) && arg_map.containsKey(x)) ? arg_map[x] : x;
  });
  return out;
}

query_select(schema, entry, args, opts, as_tree) {
  var input = entry["input"];
  var itree = tree_select(schema,entry,<dynamic, dynamic>{},opts);
  var qtree = query_fill_input(itree,args,xtd.arr_clone(input),false);
  if((null != as_tree) && (false != as_tree)){
    return qtree;
  }
  else{
    return sql_graph.select_return(schema,qtree,0,opts);
  }
}

query_count(schema, entry, args, opts, as_tree) {
  var input = entry["input"];
  var itree = tree_count(schema,entry,<dynamic, dynamic>{},opts);
  var qtree = query_fill_input(itree,args,xtd.arr_clone(input),false);
  if((null != as_tree) && (false != as_tree)){
    return qtree;
  }
  else{
    return sql_graph.select_return(schema,qtree,0,opts);
  }
}

query_return(schema, entry, id, args, opts, as_tree) {
  var input = entry["input"];
  var itree = tree_return(
    schema,
    entry,
    <dynamic, dynamic>{"id":id},
    <dynamic, dynamic>{},
    opts
  );
  var qtree = query_fill_input(itree,args,xtd.arr_clone(input),true);
  if((null != as_tree) && (false != as_tree)){
    return qtree;
  }
  else{
    return sql_graph.select_return(schema,qtree,0,opts);
  }
}

query_return_bulk(schema, entry, ids, args, opts, as_tree) {
  var input = entry["input"];
  var itree = tree_return(
    schema,
    entry,
    <dynamic, dynamic>{"id":<dynamic>["in",<dynamic>[ids]]},
    <dynamic, dynamic>{},
    opts
  );
  var qtree = query_fill_input(itree,args,xtd.arr_clone(input),true);
  if((null != as_tree) && (false != as_tree)){
    return qtree;
  }
  else{
    return sql_graph.select_return(schema,qtree,0,opts);
  }
}

query_combined(schema, sel_entry, sel_args, ret_entry, ret_args, ret_omit, opts, as_tree) {
  var sel_input = sel_entry["input"];
  var ret_input = ret_entry["input"];
  var itree = tree_combined(schema,sel_entry,ret_entry,ret_omit,<dynamic>[],opts);
  var qtree = query_fill_input(
    itree,
    xtd.arr_assign(xtd.arr_clone(ret_args),sel_args),
    xtd.arr_assign(xtd.arr_clone(ret_input),sel_input),
    true
  );
  if((null != as_tree) && (false != as_tree)){
    return qtree;
  }
  else{
    return sql_graph.select_return(schema,qtree,0,opts);
  }
}