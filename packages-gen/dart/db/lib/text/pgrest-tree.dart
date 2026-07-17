import 'package:xtalk_db/text/base-tree.dart' as base_tree;

import 'package:xtalk_db/text/pgrest-graph.dart' as pgrest_graph;

pgrest_query_select(schema, entry, args, opts) {
  var qtree = base_tree.plan_select(schema,entry,args,opts);
  return pgrest_graph.select_return(schema,qtree,0,opts);
}

pgrest_query_count(schema, entry, args, opts) {
  var qtree = base_tree.plan_count(schema,entry,args,opts);
  return pgrest_graph.select_return(schema,qtree,0,opts);
}

pgrest_query_return(schema, entry, id, args, opts) {
  var qtree = base_tree.plan_return(schema,entry,id,args,opts);
  return pgrest_graph.select_return(schema,qtree,0,opts);
}

pgrest_query_return_bulk(schema, entry, ids, args, opts) {
  var qtree = base_tree.plan_return_bulk(schema,entry,ids,args,opts);
  return pgrest_graph.select_return(schema,qtree,0,opts);
}

pgrest_query_combined(schema, sel_entry, sel_args, ret_entry, ret_args, ret_omit, opts) {
  var qtree = base_tree.plan_combined(schema,sel_entry,sel_args,ret_entry,ret_args,ret_omit,opts,false);
  return pgrest_graph.select_return(schema,qtree,0,opts);
}