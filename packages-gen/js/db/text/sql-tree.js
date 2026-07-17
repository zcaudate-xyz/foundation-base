const base_tree = require("@xtalk/db/text/base-tree.js")

const sql_graph = require("@xtalk/db/text/sql-graph.js")

function sql_query_select(schema,entry,args,opts){
  let qtree = base_tree.plan_select(schema,entry,args,opts);
  return sql_graph.select_return(schema,qtree,0,opts);
}

function sql_query_count(schema,entry,args,opts){
  let qtree = base_tree.plan_count(schema,entry,args,opts);
  return sql_graph.select_return(schema,qtree,0,opts);
}

function sql_query_return(schema,entry,id,args,opts){
  let qtree = base_tree.plan_return(schema,entry,id,args,opts);
  return sql_graph.select_return(schema,qtree,0,opts);
}

function sql_query_return_bulk(schema,entry,ids,args,opts){
  let qtree = base_tree.plan_return_bulk(schema,entry,ids,args,opts);
  return sql_graph.select_return(schema,qtree,0,opts);
}

function sql_query_combined(schema,sel_entry,sel_args,ret_entry,ret_args,ret_omit,opts){
  let qtree = base_tree.plan_combined(schema,sel_entry,sel_args,ret_entry,ret_args,ret_omit,opts,false);
  return sql_graph.select_return(schema,qtree,0,opts);
}

module.exports = {
  ["sql_query_select"]:sql_query_select,
  ["sql_query_count"]:sql_query_count,
  ["sql_query_return"]:sql_query_return,
  ["sql_query_return_bulk"]:sql_query_return_bulk,
  ["sql_query_combined"]:sql_query_combined
}