const xtt = require("@xtalk/lang/common-tree.js")

const xtd = require("@xtalk/lang/common-data.js")

const base_scope = require("@xtalk/db/text/base-scope.js")

const sql_util = require("@xtalk/db/text/sql-util.js")

const sql_graph = require("@xtalk/db/text/sql-graph.js")

function tree_control_array(control){
  if(xtd.is_emptyp(control)){
    return [];
  }
  let out = [];
  let {limit,offset,order_by,order_sort} = control;
  if(Array.isArray(order_by)){
    out.push(sql_util.ORDER_BY(order_by));
  }
  if(null != order_sort){
    out.push(sql_util.ORDER_SORT(order_sort));
  }
  if("number" == (typeof limit)){
    out.push(sql_util.LIMIT(limit));
  }
  if("number" == (typeof offset)){
    out.push(sql_util.OFFSET(offset));
  }
  return out;
}

function tree_base(schema,table_name,sel_query,clause,returning,opts){
  let tarr = base_scope.merge_queries(sel_query,clause);
  let tree = xtd.arr_assign([table_name],tarr);
  if(xtd.not_emptyp(returning)){
    tree.push(returning);
  }
  return sql_graph.select_tree(schema,tree,opts);
}

function tree_count(schema,entry,clause,opts){
  let {control,view} = entry;
  let {query,table} = view;
  return tree_base(
    schema,
    table,
    query,
    clause,
    xtd.arr_assign([{"::":"sql/count"}],tree_control_array(control)),
    opts
  );
}

function tree_select(schema,entry,clause,opts){
  let {control,view} = entry;
  let {query,table} = view;
  return tree_base(
    schema,
    table,
    query,
    clause,
    xtd.arr_assign(["id"],tree_control_array(control)),
    opts
  );
}

function tree_return(schema,entry,sel_query,clause,opts){
  let {view} = entry;
  let {query,table} = view;
  return tree_base(schema,table,sel_query,clause,query,opts);
}

function tree_combined(schema,sel_entry,ret_entry,ret_omit,clause,opts){
  let {control} = sel_entry;
  let sel_table = sel_entry["view"]["table"];
  let ret_table = ret_entry["view"]["table"];
  let sel_query = sel_entry["view"]["query"];
  let ret_query = ret_entry["view"]["query"];
  if(null == sel_query){
    sel_query = {};
  }
  if(null == ret_query){
    ret_query = {};
  }
  let ret_clause = xtd.not_emptyp(ret_omit) ? [{"id":{"not_in":[ret_omit]}}] : [];
  let combined_clause = base_scope.merge_queries(clause,ret_clause);
  return tree_base(
    schema,
    sel_table,
    sel_query,
    combined_clause,
    xtd.arr_assign(ret_query.slice(),tree_control_array(control)),
    opts
  );
}

function query_fill_input(tree,args,input_spec,drop_first){
  let arg_map = {};
  if(drop_first){
    input_spec.shift();
  }
  if(0 == input_spec.length){
    return tree;
  }
  for(let i = 0; i < input_spec.length; ++i){
    let e = input_spec[i];
    arg_map["{{" + e["symbol"] + "}}"] = args[i];
  };
  let out = xtt.tree_walk(tree,function (x){
    return x;
  },function (x){
    return (("string" == (typeof x)) && (null != arg_map[x])) ? arg_map[x] : x;
  });
  return out;
}

function query_select(schema,entry,args,opts,as_tree){
  let {input} = entry;
  let itree = tree_select(schema,entry,{},opts);
  let qtree = query_fill_input(itree,args,input.slice(),false);
  if(as_tree){
    return qtree;
  }
  else{
    return sql_graph.select_return(schema,qtree,0,opts);
  }
}

function query_count(schema,entry,args,opts,as_tree){
  let {input} = entry;
  let itree = tree_count(schema,entry,{},opts);
  let qtree = query_fill_input(itree,args,input.slice(),false);
  if(as_tree){
    return qtree;
  }
  else{
    return sql_graph.select_return(schema,qtree,0,opts);
  }
}

function query_return(schema,entry,id,args,opts,as_tree){
  let {input} = entry;
  let itree = tree_return(schema,entry,{"id":id},{},opts);
  let qtree = query_fill_input(itree,args,input.slice(),true);
  if(as_tree){
    return qtree;
  }
  else{
    return sql_graph.select_return(schema,qtree,0,opts);
  }
}

function query_return_bulk(schema,entry,ids,args,opts,as_tree){
  let {input} = entry;
  let itree = tree_return(schema,entry,{"id":["in",[ids]]},{},opts);
  let qtree = query_fill_input(itree,args,input.slice(),true);
  if(as_tree){
    return qtree;
  }
  else{
    return sql_graph.select_return(schema,qtree,0,opts);
  }
}

function query_combined(schema,sel_entry,sel_args,ret_entry,ret_args,ret_omit,opts,as_tree){
  let sel_input = sel_entry["input"];
  let ret_input = ret_entry["input"];
  let itree = tree_combined(schema,sel_entry,ret_entry,ret_omit,[],opts);
  let qtree = query_fill_input(
    itree,
    xtd.arr_assign(ret_args.slice(),sel_args),
    xtd.arr_assign(ret_input.slice(),sel_input),
    true
  );
  if(as_tree){
    return qtree;
  }
  else{
    return sql_graph.select_return(schema,qtree,0,opts);
  }
}

module.exports = {
  ["tree_control_array"]:tree_control_array,
  ["tree_base"]:tree_base,
  ["tree_count"]:tree_count,
  ["tree_select"]:tree_select,
  ["tree_return"]:tree_return,
  ["tree_combined"]:tree_combined,
  ["query_fill_input"]:query_fill_input,
  ["query_select"]:query_select,
  ["query_count"]:query_count,
  ["query_return"]:query_return,
  ["query_return_bulk"]:query_return_bulk,
  ["query_combined"]:query_combined
}