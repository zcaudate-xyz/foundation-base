const xtd = require("@xtalk/lang/common-data.js")

const scope = require("@xtalk/db/text/base-scope.js")

function tree_paramsp(params){
  return ((null != params) && ("object" == (typeof params)) && !Array.isArray(params)) && !Array.isArray(params) && ((null != params["where"]) || (null != params["data"]) || (null != params["links"]) || (null != params["custom"]));
}

function treep(query){
  return Array.isArray(query) && (query.length >= 2) && tree_paramsp(xtd.second(query));
}

function normalise_tree_params(params){
  let out = Object.assign({},params || {});
  if(null == out["where"]){
    out["where"] = [];
  }
  if(null == out["data"]){
    out["data"] = [];
  }
  if(null == out["links"]){
    out["links"] = [];
  }
  if(null == out["custom"]){
    out["custom"] = [];
  }
  return out;
}

function normalise_tree(query){
  if(!treep(query)){
    return query;
  }
  else{
    return [query[0],normalise_tree_params(xtd.second(query))];
  }
}

function base_query_inputs(query){
  let table_name = query[0];
  let cnt = query.length;
  if(cnt == 1){
    return [table_name,{},null];
  }
  else if(cnt == 3){
    return [table_name,query[1],xtd.nth(query,2)];
  }
  else if(Array.isArray(query[1])){
    return [table_name,{},query[1]];
  }
  else{
    return [table_name,query[1],null];
  }
}

function select_tree(schema,query,opts){
  opts = (opts || {});
  if(treep(query)){
    return normalise_tree(query);
  }
  else{
    let input = scope.get_link_standard(query);
    let table_name = input[0];
    let linked = xtd.second(input);
    let return_params = linked[linked.length + -1];
    let where_params = linked.filter(function (x){
      return ((null != x) && ("object" == (typeof x)) && !Array.isArray(x)) && xtd.not_emptyp(x);
    });
    return scope.get_tree(schema,table_name,where_params,return_params,opts);
  }
}

module.exports = {
  ["tree_paramsp"]:tree_paramsp,
  ["treep"]:treep,
  ["normalise_tree_params"]:normalise_tree_params,
  ["normalise_tree"]:normalise_tree,
  ["base_query_inputs"]:base_query_inputs,
  ["select_tree"]:select_tree
}