const xtd = require("@xtalk/lang/common-data.js")

const base_graph = require("@xtalk/db/text/base-graph.js")

const ut = require("@xtalk/db/text/sql-util.js")

function base_query_inputs(query){
  return base_graph.base_query_inputs(query);
}

function base_format_return(input,nest_fn,column_fn){
  if((null != input) && ("object" == (typeof input)) && !Array.isArray(input)){
    return input["expr"] + ((null != input["as"]) ? (" AS " + input["as"]) : "");
  }
  else if(Array.isArray(input)){
    return nest_fn(input);
  }
  else if("string" == (typeof input)){
    return column_fn(input);
  }
  else{
    throw "Invalid input - " + String(input);
  }
}

function select_where_pair(schema,table_name,key,clause,indent,opts,where_fn){
  let column_fn = (null == opts["column_fn"]) ? (function (x){
    return x;
  }) : opts["column_fn"];
  let attr = schema[table_name][key];
  if(null == attr){
    throw "Attribute not found - " + table_name + " - " + key;
  }
  let arr_fn = function (clause_fn,clause_arr){
    return "(" + clause_arr.map(function (clause_obj){
      return "(" + clause_fn(clause_obj) + ")";
    }).join(" OR ") + ")";
  };
  let forward_fn = function (clause_obj){
    return key + "_id" + " IN (\n" + "".padStart(indent," ") + where_fn(schema,attr["ref"]["ns"],column_fn("id"),clause_obj,indent,opts) + "\n" + "".padStart(indent - 2," ") + ")";
  };
  let reverse_fn = function (clause_obj){
    return "id IN (\n" + "".padStart(indent," ") + where_fn(
      schema,
      attr["ref"]["ns"],
      column_fn(attr["ref"]["rkey"] + "_id"),
      clause_obj,
      indent,
      opts
    ) + "\n" + "".padStart(indent - 2," ") + ")";
  };
  if("ref" == attr["type"]){
    if("forward" == attr["ref"]["type"]){
      if((null != clause) && ("object" == (typeof clause)) && !Array.isArray(clause)){
        return forward_fn(clause);
      }
      else if(Array.isArray(clause) && ((null != clause[0]) && ("object" == (typeof clause[0])) && !Array.isArray(clause[0]))){
        return arr_fn(forward_fn,clause);
      }
      else{
        return ut.encode_query_segment(key + "_id",clause,column_fn,opts);
      }
    }
    else if("reverse" == attr["ref"]["type"]){
      if("string" == (typeof clause)){
        clause = {"id":clause};
      }
      else if(Array.isArray(clause) && ("string" == (typeof clause[0]))){
        clause = {"id":clause};
      }
      if((null != clause) && ("object" == (typeof clause)) && !Array.isArray(clause)){
        return reverse_fn(clause);
      }
      else if(Array.isArray(clause) && ((null != clause[0]) && ("object" == (typeof clause[0])) && !Array.isArray(clause[0]))){
        return arr_fn(reverse_fn,clause);
      }
    }
  }
  else{
    return ut.encode_query_segment(key,clause,column_fn,opts);
  }
}

function select_where(schema,table_name,return_str,where_params,indent,opts){
  let table_fn = (null == opts["table_fn"]) ? (function (x){
    return x;
  }) : opts["table_fn"];
  let column_fn = (null == opts["column_fn"]) ? (function (x){
    return x;
  }) : opts["column_fn"];
  if(!Array.isArray(where_params)){
    where_params = [where_params];
  }
  let clause_fn = function (clause){
    let sort_keys = (null == opts["sort_keys"]) ? true : opts["sort_keys"];
    let pair_fn = function (pair){
      return select_where_pair(schema,table_name,pair[0],pair[1],indent + 2,opts,select_where);
    };
    let query_pairs = Object.entries(clause);
    if(sort_keys){
      query_pairs = xtd.arr_sort(query_pairs,function (arr){
        return arr[0];
      },function (x,y){
        return 0 > x.localeCompare(y);
      });
    }
    let clause_arr = query_pairs.map(pair_fn);
    return clause_arr.join(" AND ");
  };
  let where_arr = where_params.map(clause_fn).filter(xtd.not_emptyp);
  let where_str = "";
  if(1 == where_arr.length){
    where_str = ("" + where_arr[0]);
  }
  if(1 < where_arr.length){
    where_str = ("" + where_arr.map(function (s){
      return "(" + s + ")";
    }).join(" OR "));
  }
  let out_arr = ["SELECT " + return_str," FROM " + table_fn(table_name)];
  if(0 < where_str.length){
    out_arr.push("\n" + "".padStart(indent," ") + "WHERE " + where_str);
  }
  return out_arr.join("");
}

function select_return_str(schema,params,return_fn,indent,opts){
  let column_fn = (null == opts["column_fn"]) ? (function (x){
    return x;
  }) : opts["column_fn"];
  let return_count_fn = (null == opts["return_count_fn"]) ? (function (){
    return "count" + "(*)";
  }) : opts["return_count_fn"];
  let return_format_fn = (null == opts["return_format_fn"]) ? ut.default_return_format_fn : opts["return_format_fn"];
  let return_join_fn = (null == opts["return_join_fn"]) ? (function (arr){
    return arr.join(", ");
  }) : opts["return_join_fn"];
  let return_link_fn = (null == opts["return_link_fn"]) ? (function (s,link_name){
    return "(" + s + ") AS " + link_name;
  }) : opts["return_link_fn"];
  let nest_fn = function (link){
    let link_name = link[0];
    let link_tree = link[link.length + -1];
    let link_ret = return_fn(schema,link_tree,2,opts);
    return return_link_fn(link_ret,link_name);
  };
  let format_fn = function (v){
    return return_format_fn(v,nest_fn,column_fn,opts);
  };
  let data_params = params["data"];
  let link_params = params["links"];
  let custom_params = params["custom"];
  let sort_keys = (null == opts["sort_keys"]) ? true : opts["sort_keys"];
  if((1 == custom_params.length) && ("sql/count" == (custom_params[0])["::"])){
    return return_count_fn();
  }
  let return_data = data_params.map(format_fn);
  if(sort_keys){
    link_params = xtd.arr_sort(link_params,function (arr){
      return arr[0];
    },function (x,y){
      return 0 > x.localeCompare(y);
    });
  }
  let return_links = link_params.map(format_fn);
  return return_join_fn(xtd.arr_mapcat([return_data,return_links],function (x){
    return x;
  }));
}

function select_return(schema,tree,indent,opts){
  tree = base_graph.select_tree(schema,tree,opts);
  let column_fn = (null == opts["column_fn"]) ? (function (x){
    return x;
  }) : opts["column_fn"];
  let wrapper_fn = (null == opts["wrapper_fn"]) ? (function (s,indent){
    return s;
  }) : opts["wrapper_fn"];
  let format_fn = function (input){
    return ut.encode_sql(input,column_fn,opts,ut.encode_loop_fn);
  };
  let table_name = tree[0];
  let params = xtd.second(tree);
  let where_params = params["where"];
  let custom_input = params["custom"];
  let custom_params = (Array.isArray(custom_input) ? custom_input : []).filter(function (e){
    return e["::"] == "sql/keyword";
  });
  let return_str = select_return_str(schema,params,select_return,indent,opts);
  let return_base = select_where(schema,table_name,return_str,where_params,2,opts);
  let custom_str = custom_params.map(format_fn).join(" ");
  return wrapper_fn(
    xtd.not_emptyp(custom_str) ? (return_base + " " + custom_str) : return_base,
    (indent > 0) ? 2 : 0
  );
}

function select_tree(schema,query,opts){
  return base_graph.select_tree(schema,query,opts);
}

function select(schema,query,opts){
  let tree = select_tree(schema,query,opts);
  return select_return(schema,tree,0,opts);
}

module.exports = {
  ["base_query_inputs"]:base_query_inputs,
  ["base_format_return"]:base_format_return,
  ["select_where_pair"]:select_where_pair,
  ["select_where"]:select_where,
  ["select_return_str"]:select_return_str,
  ["select_return"]:select_return,
  ["select_tree"]:select_tree,
  ["select"]:select
}