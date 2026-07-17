import 'package:xtalk_lang/common-data.dart' as xtd;

import 'package:xtalk_lang/common-string.dart' as xts;

var OPERATORS = <dynamic, dynamic>{
  "neq":"!=",
  "gt":">",
  "gte":">=",
  "lt":"<",
  "lte":"<=",
  "eq":"=",
  "is_not_null":"IS NOT NULL",
  "is_null":"IS NULL"
};

var INFIX = <dynamic, dynamic>{"||":true,"+":true,"-":true,"*":true,"/":true};

var PG = <dynamic, dynamic>{
  "map":"jsonb",
  "long":"bigint",
  "enum":"text",
  "image":"jsonb",
  "array":"jsonb"
};

var SQLITE = <dynamic, dynamic>{
  "enum":"text",
  "long":"integer",
  "citext":"text",
  "jsonb":"text",
  "inet":"text",
  "array":"text",
  "image":"text",
  "uuid":"text",
  "map":"text"
};

sqlite_json_values(v) {
  return "(SELECT value from json_each(" + v + "))";
}

sqlite_json_keys(v) {
  return "(SELECT key from json_each(" + v + "))";
}

var SQLITE_FN = <dynamic, dynamic>{
  "jsonb_build_object":<dynamic, dynamic>{"type":"alias","name":"json_object"},
  "jsonb_build_array":<dynamic, dynamic>{"type":"alias","name":"json_array"},
  "jsonb_array_elements_text":<dynamic, dynamic>{"type":"macro","fn":sqlite_json_values},
  "jsonb_array_elements":<dynamic, dynamic>{"type":"macro","fn":sqlite_json_values},
  "jsonb_object_keys":<dynamic, dynamic>{"type":"macro","fn":sqlite_json_keys},
  "\"core/util\".as_array":<dynamic, dynamic>{
    "type":"macro",
    "fn":(x) {
      return x;
    }
  }
};

encode_bool(b) {
  if(b == true){
    return "TRUE";
  }
  else if(b == false){
    return "FALSE";
  }
  else{
    throw "Not Valid";
  }
}

encode_number(v) {
  return "'" + (("int" == (v.runtimeType).toString()) ? v.toStringAsFixed(0) : (v).toString()) + "'";
}

encode_operator(op, opts) {
  if(OPERATORS.containsKey(op)){
    return OPERATORS[op];
  }
  else if((("Map" == (opts.runtimeType).toString()) || (opts.runtimeType).toString().startsWith("_Map") || (opts.runtimeType).toString().startsWith("LinkedMap")) && (("Map" == (opts["operators"].runtimeType).toString()) || (opts["operators"].runtimeType).toString().startsWith("_Map") || (opts["operators"].runtimeType).toString().startsWith("LinkedMap")) && opts["operators"].containsKey(op)){
    return xtd.get_in(opts,<dynamic>["operators",op]);
  }
  else{
    return op;
  }
}

encode_json(v) {
  return "'" + jsonEncode(v).replaceAll("'","''") + "'";
}

encode_value(v) {
  if(null == v){
    return "NULL";
  }
  else if("String" == (v.runtimeType).toString()){
    return "'" + v.replaceAll("'","''") + "'";
  }
  else if("bool" == (v.runtimeType).toString()){
    return encode_bool(v);
  }
  else if(((v.runtimeType).toString().startsWith("List") || (v.runtimeType).toString().startsWith("_GrowableList")) || (("Map" == (v.runtimeType).toString()) || (v.runtimeType).toString().startsWith("_Map") || (v.runtimeType).toString().startsWith("LinkedMap"))){
    return encode_json(v);
  }
  else if(("int" == (v.runtimeType).toString()) || ("double" == (v.runtimeType).toString()) || ("num" == (v.runtimeType).toString())){
    return encode_number(v);
  }
  else{
    return "'" + (v).toString() + "'";
  }
}

encode_sql_arg(v, column_fn, opts, loop_fn) {
  var name = v["name"];
  return encode_value(name);
}

encode_sql_column(v, column_fn, opts, loop_fn) {
  var name = v["name"];
  return Function.apply((column_fn as Function),<dynamic>[name]);
}

encode_sql_tuple(v, column_fn, opts, loop_fn) {
  var args = v["args"];
  var arg_fn = (arg) {
    return Function.apply((loop_fn as Function),<dynamic>[arg,column_fn,opts,loop_fn]);
  };
  var arg_arr = ((args.runtimeType).toString().startsWith("List") || (args.runtimeType).toString().startsWith("_GrowableList")) ? args : <dynamic>[];
  var fargs = xtd.arr_map(arg_arr,arg_fn);
  return fargs.join(", ");
}

encode_sql_table(v, column_fn, opts, loop_fn) {
  var name = v["name"];
  var schema = v["schema"];
  if((() {
    var dart_truthy__42764 = opts["strict"];
    return (null != dart_truthy__42764) && (false != dart_truthy__42764);
  })()){
    return Function.apply((column_fn as Function),<dynamic>[schema]) + "." + Function.apply((column_fn as Function),<dynamic>[name]);
  }
  else{
    return Function.apply((column_fn as Function),<dynamic>[name]);
  }
}

encode_sql_cast(v, column_fn, opts, loop_fn) {
  var value_42766 = v["args"];
  var out = value_42766[0];
  var cast = value_42766[1];
  if((() {
    var dart_truthy__42765 = opts["strict"];
    return (null != dart_truthy__42765) && (false != dart_truthy__42765);
  })()){
    return Function.apply((loop_fn as Function),<dynamic>[out,column_fn,opts,loop_fn]) + "::" + encode_sql_table(cast,column_fn,opts,loop_fn);
  }
  else{
    return Function.apply((loop_fn as Function),<dynamic>[out,column_fn,opts,loop_fn]);
  }
}

encode_sql_keyword(v, column_fn, opts, loop_fn) {
  var args = v["args"];
  var name = v["name"];
  var arg_fn = (arg) {
    return Function.apply((loop_fn as Function),<dynamic>[arg,column_fn,opts,loop_fn]);
  };
  var arg_arr = ((args.runtimeType).toString().startsWith("List") || (args.runtimeType).toString().startsWith("_GrowableList")) ? args : <dynamic>[];
  var fargs = xtd.arr_map(arg_arr,arg_fn);
  if((() {
    var dart_truthy__42762 = xtd.arr_emptyp(fargs);
    return (null != dart_truthy__42762) && (false != dart_truthy__42762);
  })()){
    return (name).toString();
  }
  else{
    return name + " " + fargs.join(" ");
  }
}

encode_sql_fn(v, column_fn, opts, loop_fn) {
  var args = v["args"];
  var name = v["name"];
  var arg_fn = (arg) {
    return Function.apply((loop_fn as Function),<dynamic>[arg,column_fn,opts,loop_fn]);
  };
  var fargs = xtd.arr_map(args,arg_fn);
  if(INFIX.containsKey(name)){
    return "(" + fargs.join(" " + name + " ") + ")";
  }
  else{
    var lu = opts["values"]["replace"];
    var fspec = lu[name];
    if(null == fspec){
      return name + "(" + fargs.join(", ") + ")";
    }
    else if("alias" == fspec["type"]){
      return fspec["name"] + "(" + fargs.join(", ") + ")";
    }
    else if("macro" == fspec["type"]){
      return Function.apply(fspec["fn"],fargs);
    }
    else{
      throw "Invalid Spec Type - " + fspec["type"];
    }
  }
}

encode_sql_select(v, column_fn, opts, loop_fn) {
  var args = v["args"];
  var querystr_fn = opts["querystr_fn"];
  var arg_fn = (arg) {
    if((("Map" == (arg.runtimeType).toString()) || (arg.runtimeType).toString().startsWith("_Map") || (arg.runtimeType).toString().startsWith("LinkedMap")) && !arg.containsKey("::")){
      return Function.apply((querystr_fn as Function),<dynamic>[arg,"",opts]);
    }
    else{
      return Function.apply((loop_fn as Function),<dynamic>[arg,column_fn,opts,loop_fn]);
    }
  };
  var fargs = xtd.arr_map(args,arg_fn);
  return "(SELECT " + fargs.join(" ") + ")";
}

var ENCODE_SQL = <dynamic, dynamic>{
  "sql/arg":encode_sql_arg,
  "sql/keyword":encode_sql_keyword,
  "sql/defenum":encode_sql_table,
  "sql/deftype":encode_sql_table,
  "sql/fn":encode_sql_fn,
  "sql/column":encode_sql_column,
  "sql/cast":encode_sql_cast,
  "sql/tuple":encode_sql_tuple,
  "sql/select":encode_sql_select
};

encode_sql(v, column_fn, opts, loop_fn) {
  var tcls = v["::"];
  var arg_fn = (arg) {
    return Function.apply((loop_fn as Function),<dynamic>[arg,column_fn,opts,loop_fn]);
  };
  var f = ENCODE_SQL[tcls];
  if(null == f){
    throw "Unsupported Type - " + tcls;
  }
  return f(v,column_fn,opts,loop_fn);
}

encode_loop_fn(v, column_fn, opts, loop_fn) {
  if((("Map" == (v.runtimeType).toString()) || (v.runtimeType).toString().startsWith("_Map") || (v.runtimeType).toString().startsWith("LinkedMap")) && v.containsKey("::")){
    return encode_sql(v,column_fn,opts,loop_fn);
  }
  else if("String" == (v.runtimeType).toString()){
    return v;
  }
  else{
    return encode_value(v);
  }
}

encode_query_value(v, column_fn, opts) {
  if((("Map" == (v.runtimeType).toString()) || (v.runtimeType).toString().startsWith("_Map") || (v.runtimeType).toString().startsWith("LinkedMap")) && v.containsKey("::")){
    return Function.apply(
      (encode_loop_fn as Function),
      <dynamic>[v,column_fn,opts,encode_loop_fn]
    );
  }
  else if((v.runtimeType).toString().startsWith("List") || (v.runtimeType).toString().startsWith("_GrowableList")){
    if((1 == v.length) && ((v[0].runtimeType).toString().startsWith("List") || (v[0].runtimeType).toString().startsWith("_GrowableList")) && (() {
      var dart_truthy__42763 = xtd.arr_every(v[0],(value) {
        return "String" == (value.runtimeType).toString();
      });
      return (null != dart_truthy__42763) && (false != dart_truthy__42763);
    })()){
      return "(" + xtd.arr_map(v[0],encode_value).join(", ") + ")";
    }
    else if((1 == v.length) && ("String" == (v[0].runtimeType).toString())){
      return v[0];
    }
    else{
      var map_fn = (item) {
        return encode_query_value(item,column_fn,opts);
      };
      return xtd.arr_map(v,map_fn).join(" ");
    }
  }
  else if((v == "and") || (v == "or")){
    return v;
  }
  else{
    return encode_value(v);
  }
}

encode_query_segment(key, v, column_fn, opts) {
  var col = Function.apply((column_fn as Function),<dynamic>[key]);
  if((v.runtimeType).toString().startsWith("List") || (v.runtimeType).toString().startsWith("_GrowableList")){
    var map_fn = (item) {
      return encode_query_value(item,column_fn,opts);
    };
    var tail_values = v.sublist(1 - 0,v.length);
    var encoded = xtd.arr_map(tail_values,map_fn);
    return col + " " + encode_operator(v[0],opts) + " " + encoded.join(" ");
  }
  else{
    return col + " = " + encode_query_value(v,column_fn,opts);
  }
}

encode_query_single_string(params, opts) {
  var column_fn = (null == opts["column_fn"]) ? ((x) {
    return x;
  }) : opts["column_fn"];
  var sort_keys = (null == opts["sort_keys"]) ? true : opts["sort_keys"];
  var out = "";
  var query_pairs = List<List<dynamic>>.from(( params ).entries.map((entry) => [entry.key, entry.value]));
  if((null != sort_keys) && (false != sort_keys)){
    query_pairs = xtd.arr_sort(query_pairs,(arr) {
      return arr[0];
    },(x, y) {
      return (x).toString().compareTo((y).toString()) < 0;
    });
  }
  var arr_42769 = query_pairs;
  for(var i42770 = 0; i42770 < arr_42769.length; ++i42770){
    var e = arr_42769[i42770];
    var value_42791 = e;
    var key = value_42791[0];
    var v = value_42791[1];
    if(0 < out.length){
      out = (out + " AND ");
    }
    out = (out + encode_query_segment(key,v,column_fn,opts));
  };
  return out;
}

encode_query_string(params, prefix, opts) {
  var out = xtd.arr_filter(xtd.arr_map(xtd.arrayify(params),(p) {
    return encode_query_single_string(p,opts);
  }),(e) {
    return (null != e) && (0 < e.length);
  });
  var joined = xtd.arr_map(out,(s) {
    return "(" + s + ")";
  }).join(" OR ");
  if(0 == out.length){
    return "";
  }
  else if(1 == out.length){
    return ((null != xtd.not_emptyp(prefix)) && (false != xtd.not_emptyp(prefix))) ? (prefix + " " + out[0]) : out[0];
  }
  else{
    return ((null != xtd.not_emptyp(prefix)) && (false != xtd.not_emptyp(prefix))) ? (prefix + " " + joined) : joined;
  }
}

LIMIT(val) {
  return <dynamic, dynamic>{
    "::":"sql/keyword",
    "name":"LIMIT",
    "args":<dynamic>[<dynamic, dynamic>{"::":"sql/keyword","name":val}]
  };
}

OFFSET(val) {
  return <dynamic, dynamic>{
    "::":"sql/keyword",
    "name":"OFFSET",
    "args":<dynamic>[<dynamic, dynamic>{"::":"sql/keyword","name":val}]
  };
}

ORDER_BY(columns) {
  return <dynamic, dynamic>{
    "::":"sql/keyword",
    "name":"ORDER BY",
    "args":<dynamic>[
        <dynamic, dynamic>{
          "::":"sql/tuple",
          "args":xtd.arr_map(columns,(column) {
              return <dynamic, dynamic>{"::":"sql/column","name":column};
            })
        }
      ]
  };
}

ORDER_SORT(order) {
  return <dynamic, dynamic>{"::":"sql/keyword","name":xts.to_uppercase(order)};
}

default_quote_fn(s) {
  return "\"" + s + "\"";
}

default_return_format_fn(input, nest_fn, column_fn, opts) {
  if(("Map" == (input.runtimeType).toString()) || (input.runtimeType).toString().startsWith("_Map") || (input.runtimeType).toString().startsWith("LinkedMap")){
    if(input.containsKey("::")){
      return encode_sql(input,column_fn,opts,encode_loop_fn);
    }
    else{
      return input["expr"] + (input.containsKey("as") ? (" AS " + input["as"]) : "");
    }
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

default_table_fn(table, lookup) {
  return "\"" + lookup[table]["schema"] + "\".\"" + table + "\"";
}

postgres_wrapper_fn(s, indent) {
  return "WITH j_ret AS (\n" + xts.pad_lines(s,2," ") + "\n) SELECT jsonb_agg(j_ret) FROM j_ret";
}

postgres_opts(lookup) {
  return <dynamic, dynamic>{
    "return_join_fn":(arr) {
        return arr.join(", ");
      },
    "strict":true,
    "wrapper_fn":postgres_wrapper_fn,
    "querystr_fn":encode_query_string,
    "types":PG,
    "values":<dynamic, dynamic>{"cast":true,"replace":<dynamic, dynamic>{}},
    "return_link_fn":(s, link_name) {
        return "(" + s + ") AS " + link_name;
      },
    "return_format_fn":default_return_format_fn,
    "coerce":<dynamic, dynamic>{},
    "return_count_fn":() {
        return "count" + "(*)";
      },
    "column_fn":default_quote_fn,
    "table_fn":(table) {
        return Function.apply((default_table_fn as Function),<dynamic>[table,lookup]);
      }
  };
}

sqlite_return_format_fn(input, nest_fn, column_fn, opts) {
  if(("Map" == (input.runtimeType).toString()) || (input.runtimeType).toString().startsWith("_Map") || (input.runtimeType).toString().startsWith("LinkedMap")){
    return "'" + input["as"] + "'" + ", " + input["expr"];
  }
  else if((input.runtimeType).toString().startsWith("List") || (input.runtimeType).toString().startsWith("_GrowableList")){
    return Function.apply((nest_fn as Function),<dynamic>[input]);
  }
  else if("String" == (input.runtimeType).toString()){
    return "'" + input + "'" + ", " + Function.apply((column_fn as Function),<dynamic>[input]);
  }
  else{
    throw "Invalid input - " + (input).toString();
  }
}

sqlite_to_boolean(v) {
  if(("int" == (v.runtimeType).toString()) || ("double" == (v.runtimeType).toString()) || ("num" == (v.runtimeType).toString())){
    return 1 == v;
  }
  return v;
}