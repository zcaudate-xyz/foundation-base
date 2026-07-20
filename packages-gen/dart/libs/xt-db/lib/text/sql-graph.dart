import 'package:xtalk_lang/common-data.dart' as xtd;
import 'package:xtalk_db/text/base-graph.dart' as base_graph;
import 'package:xtalk_db/text/sql-util.dart' as ut;




base_query_inputs(query) {
  return base_graph.base_query_inputs(query);
}

base_format_return(input, nest_fn, column_fn) {
  if(("Map" == (input.runtimeType).toString()) || (input.runtimeType).toString().startsWith("_Map") || (input.runtimeType).toString().startsWith("LinkedMap")){
    return input["expr"] + (input.containsKey("as") ? (" AS " + input["as"]) : "");
  }
  else if((input.runtimeType).toString().startsWith("List") || (input.runtimeType).toString().startsWith("_GrowableList")){
    return Function.apply((nest_fn as Function),<dynamic>[input]);
  }
  else if("String" == (input.runtimeType).toString()){
    return Function.apply((column_fn as Function),<dynamic>[input]);
  }
  else{
    throw "Invalid input - " + (input).toString();
  }
}

select_where_pair(schema, table_name, key, clause, indent, opts, where_fn) {
  var column_fn = (null == opts["column_fn"]) ? ((x) {
    return x;
  }) : opts["column_fn"];
  var attr = schema[table_name][key];
  if(null == attr){
    throw "Attribute not found - " + table_name + " - " + key;
  }
  var arr_fn = (clause_fn, clause_arr) {
    return "(" + xtd.arr_map(clause_arr,(clause_obj) {
      return "(" + Function.apply((clause_fn as Function),<dynamic>[clause_obj]) + ")";
    }).join(" OR ") + ")";
  };
  var forward_fn = (clause_obj) {
    return key + "_id" + " IN (\n" + xt.lang.common_string.pad_left("",indent," ") + Function.apply((where_fn as Function),<dynamic>[
      schema,
      attr["ref"]["ns"],
      Function.apply((column_fn as Function),<dynamic>["id"]),
      clause_obj,
      indent,
      opts
    ]) + "\n" + xt.lang.common_string.pad_left("",indent - 2," ") + ")";
  };
  var reverse_fn = (clause_obj) {
    return "id IN (\n" + xt.lang.common_string.pad_left("",indent," ") + Function.apply((where_fn as Function),<dynamic>[
      schema,
      attr["ref"]["ns"],
      Function.apply(
          (column_fn as Function),
          <dynamic>[attr["ref"]["rkey"] + "_id"]
        ),
      clause_obj,
      indent,
      opts
    ]) + "\n" + xt.lang.common_string.pad_left("",indent - 2," ") + ")";
  };
  if("ref" == attr["type"]){
    if("forward" == attr["ref"]["type"]){
      if(("Map" == (clause.runtimeType).toString()) || (clause.runtimeType).toString().startsWith("_Map") || (clause.runtimeType).toString().startsWith("LinkedMap")){
        return Function.apply((forward_fn as Function),<dynamic>[clause]);
      }
      else if(((clause.runtimeType).toString().startsWith("List") || (clause.runtimeType).toString().startsWith("_GrowableList")) && (("Map" == (clause[0].runtimeType).toString()) || (clause[0].runtimeType).toString().startsWith("_Map") || (clause[0].runtimeType).toString().startsWith("LinkedMap"))){
        return Function.apply((arr_fn as Function),<dynamic>[forward_fn,clause]);
      }
      else{
        return ut.encode_query_segment(key + "_id",clause,column_fn,opts);
      }
    }
    else if("reverse" == attr["ref"]["type"]){
      if("String" == (clause.runtimeType).toString()){
        clause = <dynamic, dynamic>{"id":clause};
      }
      else if(((clause.runtimeType).toString().startsWith("List") || (clause.runtimeType).toString().startsWith("_GrowableList")) && ("String" == (clause[0].runtimeType).toString())){
        clause = <dynamic, dynamic>{"id":clause};
      }
      if(("Map" == (clause.runtimeType).toString()) || (clause.runtimeType).toString().startsWith("_Map") || (clause.runtimeType).toString().startsWith("LinkedMap")){
        return Function.apply((reverse_fn as Function),<dynamic>[clause]);
      }
      else if(((clause.runtimeType).toString().startsWith("List") || (clause.runtimeType).toString().startsWith("_GrowableList")) && (("Map" == (clause[0].runtimeType).toString()) || (clause[0].runtimeType).toString().startsWith("_Map") || (clause[0].runtimeType).toString().startsWith("LinkedMap"))){
        return Function.apply((arr_fn as Function),<dynamic>[reverse_fn,clause]);
      }
    }
  }
  else{
    return ut.encode_query_segment(key,clause,column_fn,opts);
  }
}

select_where(schema, table_name, return_str, where_params, indent, opts) {
  var table_fn = (null == opts["table_fn"]) ? ((x) {
    return x;
  }) : opts["table_fn"];
  var column_fn = (null == opts["column_fn"]) ? ((x) {
    return x;
  }) : opts["column_fn"];
  if(!((where_params.runtimeType).toString().startsWith("List") || (where_params.runtimeType).toString().startsWith("_GrowableList"))){
    where_params = <dynamic>[where_params];
  }
  var clause_fn = (clause) {
    var sort_keys = (null == opts["sort_keys"]) ? true : opts["sort_keys"];
    var pair_fn = (pair) {
      return select_where_pair(schema,table_name,pair[0],pair[1],indent + 2,opts,select_where);
    };
    var query_pairs = List<List<dynamic>>.from(( clause ).entries.map((entry) => [entry.key, entry.value]));
    if((null != sort_keys) && (false != sort_keys)){
      query_pairs = xtd.arr_sort(query_pairs,(arr) {
        return arr[0];
      },(x, y) {
        return (x).toString().compareTo((y).toString()) < 0;
      });
    }
    var clause_arr = xtd.arr_map(query_pairs,pair_fn);
    return clause_arr.join(" AND ");
  };
  var where_arr = xtd.arr_filter(xtd.arr_map(where_params,clause_fn),xtd.not_emptyp);
  var where_str = "";
  if(1 == where_arr.length){
    where_str = ("" + where_arr[0]);
  }
  if(1 < where_arr.length){
    where_str = ("" + xtd.arr_map(where_arr,(s) {
      return "(" + s + ")";
    }).join(" OR "));
  }
  var out_arr = <dynamic>[
    "SELECT " + return_str,
    " FROM " + Function.apply((table_fn as Function),<dynamic>[table_name])
  ];
  if(0 < where_str.length){
    out_arr.add(
      "\n" + xt.lang.common_string.pad_left("",indent," ") + "WHERE " + where_str
    );
  }
  return out_arr.join("");
}

select_return_str(schema, params, return_fn, indent, opts) {
  var column_fn = (null == opts["column_fn"]) ? ((x) {
    return x;
  }) : opts["column_fn"];
  var return_count_fn = (null == opts["return_count_fn"]) ? (() {
    return "count" + "(*)";
  }) : opts["return_count_fn"];
  var return_format_fn = (null == opts["return_format_fn"]) ? ut.default_return_format_fn : opts["return_format_fn"];
  var return_join_fn = (null == opts["return_join_fn"]) ? ((arr) {
    return arr.join(", ");
  }) : opts["return_join_fn"];
  var return_link_fn = (null == opts["return_link_fn"]) ? ((s, link_name) {
    return "(" + s + ") AS " + link_name;
  }) : opts["return_link_fn"];
  var nest_fn = (link) {
    var link_name = link[0];
    var link_tree = link[link.length + -1];
    var link_ret = Function.apply((return_fn as Function),<dynamic>[schema,link_tree,2,opts]);
    return Function.apply((return_link_fn as Function),<dynamic>[link_ret,link_name]);
  };
  var format_fn = (v) {
    return Function.apply(
      (return_format_fn as Function),
      <dynamic>[v,nest_fn,column_fn,opts]
    );
  };
  var data_params = params["data"];
  var link_params = params["links"];
  var custom_params = params["custom"];
  var sort_keys = (null == opts["sort_keys"]) ? true : opts["sort_keys"];
  if((1 == custom_params.length) && ("sql/count" == (custom_params[0])["::"])){
    return Function.apply((return_count_fn as Function),<dynamic>[]);
  }
  var return_data = xtd.arr_map(data_params,format_fn);
  if((null != sort_keys) && (false != sort_keys)){
    link_params = xtd.arr_sort(link_params,(arr) {
      return arr[0];
    },(x, y) {
      return (x).toString().compareTo((y).toString()) < 0;
    });
  }
  var return_links = xtd.arr_map(link_params,format_fn);
  return Function.apply((return_join_fn as Function),<dynamic>[
    xtd.arr_mapcat(<dynamic>[return_data,return_links],(x) {
      return x;
    })
  ]);
}

select_return(schema, tree, indent, opts) {
  tree = base_graph.select_tree(schema,tree,opts);
  var column_fn = (null == opts["column_fn"]) ? ((x) {
    return x;
  }) : opts["column_fn"];
  var wrapper_fn = (null == opts["wrapper_fn"]) ? ((s, indent) {
    return s;
  }) : opts["wrapper_fn"];
  var format_fn = (input) {
    return ut.encode_sql(input,column_fn,opts,ut.encode_loop_fn);
  };
  var table_name = tree[0];
  var params = xtd.second(tree);
  var where_params = params["where"];
  var custom_input = params["custom"];
  var custom_params = xtd.arr_filter(((custom_input.runtimeType).toString().startsWith("List") || (custom_input.runtimeType).toString().startsWith("_GrowableList")) ? custom_input : <dynamic>[],(e) {
    return e["::"] == "sql/keyword";
  });
  var return_str = select_return_str(schema,params,select_return,indent,opts);
  var return_base = select_where(schema,table_name,return_str,where_params,2,opts);
  var custom_str = xtd.arr_map(custom_params,format_fn).join(" ");
  return Function.apply((wrapper_fn as Function),<dynamic>[
    ((null != xtd.not_emptyp(custom_str)) && (false != xtd.not_emptyp(custom_str))) ? (return_base + " " + custom_str) : return_base,
    (indent > 0) ? 2 : 0
  ]);
}

select_tree(schema, query, opts) {
  return base_graph.select_tree(schema,query,opts);
}

select(schema, query, opts) {
  var tree = select_tree(schema,query,opts);
  return select_return(schema,tree,0,opts);
}