const base_tree = require("@xtalk/db/text/base-tree.js")

const pgrest_graph = require("@xtalk/db/text/pgrest-graph.js")

function pgrest_query_select(schema,entry,args,opts){
  let qtree = base_tree.plan_select(schema,entry,args,opts);
  return pgrest_graph.select_return(schema,qtree,0,opts);
}

function pgrest_query_count(schema,entry,args,opts){
  let qtree = base_tree.plan_count(schema,entry,args,opts);
  return pgrest_graph.select_return(schema,qtree,0,opts);
}

function pgrest_query_return(schema,entry,id,args,opts){
  let qtree = base_tree.plan_return(schema,entry,id,args,opts);
  return pgrest_graph.select_return(schema,qtree,0,opts);
}

function pgrest_query_return_bulk(schema,entry,ids,args,opts){
  let qtree = base_tree.plan_return_bulk(schema,entry,ids,args,opts);
  return pgrest_graph.select_return(schema,qtree,0,opts);
}

function pgrest_query_combined(schema,sel_entry,sel_args,ret_entry,ret_args,ret_omit,opts){
  let qtree = base_tree.plan_combined(schema,sel_entry,sel_args,ret_entry,ret_args,ret_omit,opts,false);
  return pgrest_graph.select_return(schema,qtree,0,opts);
}

module.exports = {
  ["pgrest_query_select"]:pgrest_query_select,
  ["pgrest_query_count"]:pgrest_query_count,
  ["pgrest_query_return"]:pgrest_query_return,
  ["pgrest_query_return_bulk"]:pgrest_query_return_bulk,
  ["pgrest_query_combined"]:pgrest_query_combined
}