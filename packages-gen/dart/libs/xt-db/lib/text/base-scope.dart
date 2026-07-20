import 'package:xtalk_lang/common-tree.dart' as xtt;
import 'package:xtalk_lang/common-data.dart' as xtd;
import 'package:xtalk_lang/common-string.dart' as xts;




var Scopes = <dynamic, dynamic>{
  "*/min":<dynamic, dynamic>{"-/id":true,"-/key":true},
  "*/info":<dynamic, dynamic>{"-/info":true,"-/id":true,"-/key":true},
  "*/data":<dynamic, dynamic>{"-/info":true,"-/id":true,"-/data":true,"-/key":true},
  "*/default":<dynamic, dynamic>{
    "-/info":true,
    "-/id":true,
    "-/ref":true,
    "-/data":true,
    "-/key":true
  },
  "*/detail":<dynamic, dynamic>{
    "-/detail":true,
    "-/info":true,
    "-/id":true,
    "-/data":true,
    "-/key":true
  },
  "*/standard":<dynamic, dynamic>{
    "-/detail":true,
    "-/info":true,
    "-/id":true,
    "-/ref":true,
    "-/data":true,
    "-/key":true
  },
  "*/all":<dynamic, dynamic>{
    "-/detail":true,
    "-/info":true,
    "-/id":true,
    "-/ref":true,
    "-/data":true,
    "-/system":true,
    "-/key":true
  },
  "*/everything":<dynamic, dynamic>{
    "-/detail":true,
    "-/info":true,
    "-/hidden":true,
    "-/id":true,
    "-/ref":true,
    "-/data":true,
    "-/system":true,
    "-/key":true
  }
};

merge_queries(q_0, q_1) {
  var arr_0 = xtd.arr_filter(xtd.arrayify(q_0),xtd.not_emptyp);
  var arr_1 = xtd.arr_filter(xtd.arrayify(q_1),xtd.not_emptyp);
  if((() {
    var dart_truthy__52502 = xtd.arr_emptyp(arr_0);
    return (null != dart_truthy__52502) && (false != dart_truthy__52502);
  })()){
    return arr_1;
  }
  if((() {
    var dart_truthy__52503 = xtd.arr_emptyp(arr_1);
    return (null != dart_truthy__52503) && (false != dart_truthy__52503);
  })()){
    return arr_0;
  }
  var out = <dynamic>[];
  var arr_52505 = arr_0;
  for(var i52506 = 0; i52506 < arr_52505.length; ++i52506){
    var e_0 = arr_52505[i52506];
    var arr_52527 = arr_1;
    for(var i52528 = 0; i52528 < arr_52527.length; ++i52528){
      var e_1 = arr_52527[i52528];
      out.add(xtd.obj_assign_nested(xtt.tree_walk(e_0,(x) {
        return x;
      },(x) {
        return x;
      }),xtt.tree_walk(e_1,(x) {
        return x;
      },(x) {
        return x;
      })));
    };
  };
  return out;
}

filter_scope(ks) {
  var mscopes = xtd.arr_filter(ks,(s) {
    return "-" == xts.sym_ns(s);
  });
  var ascopes = xtd.arr_filter(ks,(s) {
    return "*" == xts.sym_ns(s);
  });
  return xtd.arr_foldl(xtd.arr_map(ascopes,(s) {
    return Scopes[s];
  }),(obj, other) {
    return xtd.obj_assign(obj,other);
  },xtd.arr_lookup(mscopes));
}

filter_plain_key(s) {
  if(null == xts.sym_ns(s)){
    return ((null != (() {
      var dart_and__52504 = s.length >= 3;
      return ((null != dart_and__52504) && (false != dart_and__52504)) ? xts.ends_withp(s,"_id") : dart_and__52504;
    })()) && (false != (() {
      var dart_and__52504 = s.length >= 3;
      return ((null != dart_and__52504) && (false != dart_and__52504)) ? xts.ends_withp(s,"_id") : dart_and__52504;
    })())) ? s.substring(0 - 0,s.length - 3) : s;
  }
}

filter_plain(ks) {
  return xtd.arr_lookup(xtd.arr_keep(ks,filter_plain_key));
}

get_data_columns(schema, table_key, ks) {
  var str_ks = xtd.arr_filter(ks,(value) {
    return "String" == (value.runtimeType).toString();
  });
  var scopes = filter_scope(str_ks);
  var plains = filter_plain(str_ks);
  var cols = schema[table_key];
  if(null == cols){
    throw "ERR - Table not in Schema - " + table_key;
  }
  var scoped = xtd.arr_filter(List<dynamic>.from(( cols ).values),(e) {
    return (e.containsKey("scope") && (true == scopes["-/" + e["scope"]])) || plains.containsKey(e["ident"]);
  });
  return xtd.arr_sort(scoped,(e) {
    return e["order"];
  },(a, b) {
    return a < b;
  });
}

get_link_standard(link) {
  var ltag = link[0];
  var llen = link.length;
  if(1 == llen){
    return <dynamic>[ltag,<dynamic>[<dynamic, dynamic>{},<dynamic>["*/data"]]];
  }
  var lmap = xtd.arr_filter(link,(value) {
    return ("Map" == (value.runtimeType).toString()) || (value.runtimeType).toString().startsWith("_Map") || (value.runtimeType).toString().startsWith("LinkedMap");
  });
  var larr = xtd.arr_filter(link,(value) {
    return (value.runtimeType).toString().startsWith("List") || (value.runtimeType).toString().startsWith("_GrowableList");
  });
  if(0 == larr.length){
    larr = <dynamic>[<dynamic>["*/data"]];
  }
  if(0 == lmap.length){
    lmap = <dynamic>[<dynamic, dynamic>{}];
  }
  var lout = <dynamic>[];
  xtd.arr_assign(lout,lmap);
  xtd.arr_assign(lout,larr);
  return <dynamic>[ltag,lout];
}

get_query_tables(schema, table_key, query, acc) {
  acc = ((("Map" == (acc.runtimeType).toString()) || (acc.runtimeType).toString().startsWith("_Map") || (acc.runtimeType).toString().startsWith("LinkedMap")) ? acc : <dynamic, dynamic>{});
  var table = schema[table_key];
  if((null != table) && (false != table)){
    acc[table_key] = true;
    for(var entry_52551 in query.entries){
      var k = entry_52551.key;
      var v = entry_52551.value;
      var e = table[k];
      if("ref" == e["type"]){
        var link_key = e["ref"]["ns"];
        if(("Map" == (v.runtimeType).toString()) || (v.runtimeType).toString().startsWith("_Map") || (v.runtimeType).toString().startsWith("LinkedMap")){
          get_query_tables(schema,link_key,v,acc);
        }
        else{
          acc[link_key] = true;
        }
      }
    };
  }
  return acc;
}

get_link_columns(schema, table_key, ks) {
  var link_arr = xtd.arr_filter(ks,(value) {
    return (value.runtimeType).toString().startsWith("List") || (value.runtimeType).toString().startsWith("_GrowableList");
  });
  var linked = xtd.obj_from_pairs(xtd.arr_map(link_arr,get_link_standard));
  var cols = schema[table_key];
  return xtd.arr_keepf(List<dynamic>.from(( cols ).values),(col) {
    return linked.containsKey(col["ident"]);
  },(col) {
    return <dynamic>[col,linked[col["ident"]]];
  });
}

get_linked_tables_loop(schema, table_key, returning, acc) {
  var linked = get_link_columns(
    schema,
    table_key,
    ((returning.runtimeType).toString().startsWith("List") || (returning.runtimeType).toString().startsWith("_GrowableList")) ? returning : <dynamic>[]
  );
  acc[table_key] = true;
  var arr_52554 = linked;
  for(var i52555 = 0; i52555 < arr_52554.length; ++i52555){
    var arr = arr_52554[i52555];
    var attr = arr[0];
    var link_query = xtd.second(arr);
    var link_returning = xtd.second(link_query);
    get_linked_tables_loop(schema,attr["ref"]["ns"],link_returning,acc);
  };
  return acc;
}

get_linked_tables(schema, table_key, returning) {
  return get_linked_tables_loop(schema,table_key,returning,<dynamic, dynamic>{});
}

as_where_input(input) {
  if((() {
    var dart_truthy__52501 = xtd.is_emptyp(input);
    return (null != dart_truthy__52501) && (false != dart_truthy__52501);
  })()){
    return <dynamic>[];
  }
  else if((input.runtimeType).toString().startsWith("List") || (input.runtimeType).toString().startsWith("_GrowableList")){
    return input;
  }
  else{
    return <dynamic>[input];
  }
}

get_tree(schema, table_name, where, returning, opts) {
  var table_fn = (null == opts["table_fn"]) ? ((x) {
    return x;
  }) : opts["table_fn"];
  var column_fn = (null == opts["column_fn"]) ? ((x) {
    return x;
  }) : opts["column_fn"];
  where = as_where_input(where);
  returning = (((returning.runtimeType).toString().startsWith("List") || (returning.runtimeType).toString().startsWith("_GrowableList")) ? returning : <dynamic>["*/data"]);
  var where_pred = (e) {
    return (("Map" == (e.runtimeType).toString()) || (e.runtimeType).toString().startsWith("_Map") || (e.runtimeType).toString().startsWith("LinkedMap")) && (null == e["::"]);
  };
  var custom_pred = (e) {
    return (("Map" == (e.runtimeType).toString()) || (e.runtimeType).toString().startsWith("_Map") || (e.runtimeType).toString().startsWith("LinkedMap")) && ("String" == (e["::"].runtimeType).toString());
  };
  var custom = xtd.arr_filter(returning,custom_pred);
  var data = get_data_columns(schema,table_name,returning);
  var links = get_link_columns(schema,table_name,returning);
  var get_child_tree = (link) {
    var attr = link[0];
    var link_query = xtd.second(link);
    var link_where_query = xtd.arr_filter(link_query,where_pred);
    var link_returning = link_query[link_query.length + -1];
    var link_where_returning = xtd.arr_filter(link_returning,where_pred);
    var link_where = merge_queries(link_where_query,link_where_returning);
    var link_table = attr["ref"]["ns"];
    var link_type = attr["ref"]["type"];
    var link_extra = <dynamic, dynamic>{};
    if("reverse" == link_type){
      link_extra[attr["ref"]["rkey"]] = <dynamic>[
        "eq",
        <dynamic>[
              Function.apply((table_fn as Function),<dynamic>[table_name]) + "." + Function.apply((column_fn as Function),<dynamic>["id"])
            ]
      ];
    }
    else{
      link_extra["id"] = <dynamic>[
        "eq",
        <dynamic>[
              Function.apply((table_fn as Function),<dynamic>[table_name]) + "." + Function.apply(
                    (column_fn as Function),
                    <dynamic>[attr["ref"]["key"] + "_id"]
                  )
            ]
      ];
    }
    return <dynamic>[
      attr["ident"],
      link_type,
      get_tree(
          schema,
          link_table,
          merge_queries(link_where,link_extra),
          link_returning,
          opts
        )
    ];
  };
  return <dynamic>[
    table_name,
    <dynamic, dynamic>{
      "where":where,
      "data":xtd.arr_map(data,(e) {
          return ("ref" == e["type"]) ? (e["ident"] + "_id") : e["ident"];
        }),
      "links":xtd.arr_map(links,get_child_tree),
      "custom":custom
    }
  ];
}