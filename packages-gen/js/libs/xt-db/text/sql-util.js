const xtd = require("@xtalk/lang/common-data.js")

const xts = require("@xtalk/lang/common-string.js")

var OPERATORS = {
  "neq":"!=",
  "gt":">",
  "gte":">=",
  "lt":"<",
  "lte":"<=",
  "eq":"=",
  "is_not_null":"IS NOT NULL",
  "is_null":"IS NULL"
};

var INFIX = {"||":true,"+":true,"-":true,"*":true,"/":true};

var PG = {
  "map":"jsonb",
  "long":"bigint",
  "enum":"text",
  "image":"jsonb",
  "array":"jsonb"
};

var SQLITE = {
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

function sqlite_json_values(v){
  return "(SELECT value from json_each(" + v + "))";
}

function sqlite_json_keys(v){
  return "(SELECT key from json_each(" + v + "))";
}

var SQLITE_FN = {
  "jsonb_build_object":{"type":"alias","name":"json_object"},
  "jsonb_build_array":{"type":"alias","name":"json_array"},
  "jsonb_array_elements_text":{"type":"macro","fn":sqlite_json_values},
  "jsonb_array_elements":{"type":"macro","fn":sqlite_json_values},
  "jsonb_object_keys":{"type":"macro","fn":sqlite_json_keys},
  "\"core/util\".as_array":{
    "type":"macro",
    "fn":function (x){
      return x;
    }
  }
};

function encode_bool(b){
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

function encode_number(v){
  return "'" + (Number.isInteger(v) ? v.toFixed(0) : String(v)) + "'";
}

function encode_operator(op,opts){
  if(null != OPERATORS[op]){
    return OPERATORS[op];
  }
  else if(((null != opts) && ("object" == (typeof opts)) && !Array.isArray(opts)) && ((null != opts["operators"]) && ("object" == (typeof opts["operators"])) && !Array.isArray(opts["operators"])) && (null != opts["operators"][op])){
    return xtd.get_in(opts,["operators",op]);
  }
  else{
    return op;
  }
}

function encode_json(v){
  return "'" + JSON.stringify(v).replace(new RegExp("'","g"),"''") + "'";
}

function encode_value(v){
  if(null == v){
    return "NULL";
  }
  else if("string" == (typeof v)){
    return "'" + v.replace(new RegExp("'","g"),"''") + "'";
  }
  else if("boolean" == (typeof v)){
    return encode_bool(v);
  }
  else if(Array.isArray(v) || ((null != v) && ("object" == (typeof v)) && !Array.isArray(v))){
    return encode_json(v);
  }
  else if("number" == (typeof v)){
    return encode_number(v);
  }
  else{
    return "'" + String(v) + "'";
  }
}

function encode_sql_arg(v,column_fn,opts,loop_fn){
  let {name} = v;
  return encode_value(name);
}

function encode_sql_column(v,column_fn,opts,loop_fn){
  let {name} = v;
  return column_fn(name);
}

function encode_sql_tuple(v,column_fn,opts,loop_fn){
  let {args} = v;
  let arg_fn = function (arg){
    return loop_fn(arg,column_fn,opts,loop_fn);
  };
  let arg_arr = Array.isArray(args) ? args : [];
  let fargs = arg_arr.map(arg_fn);
  return fargs.join(", ");
}

function encode_sql_table(v,column_fn,opts,loop_fn){
  let {name,schema} = v;
  if(opts["strict"]){
    return column_fn(schema) + "." + column_fn(name);
  }
  else{
    return column_fn(name);
  }
}

function encode_sql_cast(v,column_fn,opts,loop_fn){
  let [out,cast] = v["args"];
  if(opts["strict"]){
    return loop_fn(out,column_fn,opts,loop_fn) + "::" + encode_sql_table(cast,column_fn,opts,loop_fn);
  }
  else{
    return loop_fn(out,column_fn,opts,loop_fn);
  }
}

function encode_sql_keyword(v,column_fn,opts,loop_fn){
  let {args,name} = v;
  let arg_fn = function (arg){
    return loop_fn(arg,column_fn,opts,loop_fn);
  };
  let arg_arr = Array.isArray(args) ? args : [];
  let fargs = arg_arr.map(arg_fn);
  if(xtd.arr_emptyp(fargs)){
    return String(name);
  }
  else{
    return name + " " + fargs.join(" ");
  }
}

function encode_sql_fn(v,column_fn,opts,loop_fn){
  let {args,name} = v;
  let arg_fn = function (arg){
    return loop_fn(arg,column_fn,opts,loop_fn);
  };
  let fargs = args.map(arg_fn);
  if(null != INFIX[name]){
    return "(" + fargs.join(" " + name + " ") + ")";
  }
  else{
    let lu = opts["values"]["replace"];
    let fspec = lu[name];
    if(null == fspec){
      return name + "(" + fargs.join(", ") + ")";
    }
    else if("alias" == fspec["type"]){
      return fspec["name"] + "(" + fargs.join(", ") + ")";
    }
    else if("macro" == fspec["type"]){
      return fspec["fn"].apply(null,fargs);
    }
    else{
      throw "Invalid Spec Type - " + fspec["type"];
    }
  }
}

function encode_sql_select(v,column_fn,opts,loop_fn){
  let {args} = v;
  let {querystr_fn} = opts;
  let arg_fn = function (arg){
    if(((null != arg) && ("object" == (typeof arg)) && !Array.isArray(arg)) && !(null != arg["::"])){
      return querystr_fn(arg,"",opts);
    }
    else{
      return loop_fn(arg,column_fn,opts,loop_fn);
    }
  };
  let fargs = args.map(arg_fn);
  return "(SELECT " + fargs.join(" ") + ")";
}

var ENCODE_SQL = {
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

function encode_sql(v,column_fn,opts,loop_fn){
  let tcls = v["::"];
  let arg_fn = function (arg){
    return loop_fn(arg,column_fn,opts,loop_fn);
  };
  let f = ENCODE_SQL[tcls];
  if(null == f){
    throw "Unsupported Type - " + tcls;
  }
  return f(v,column_fn,opts,loop_fn);
}

function encode_loop_fn(v,column_fn,opts,loop_fn){
  if(((null != v) && ("object" == (typeof v)) && !Array.isArray(v)) && (null != v["::"])){
    return encode_sql(v,column_fn,opts,loop_fn);
  }
  else if("string" == (typeof v)){
    return v;
  }
  else{
    return encode_value(v);
  }
}

function encode_query_value(v,column_fn,opts){
  if(((null != v) && ("object" == (typeof v)) && !Array.isArray(v)) && (null != v["::"])){
    return encode_loop_fn(v,column_fn,opts,encode_loop_fn);
  }
  else if(Array.isArray(v)){
    if((1 == v.length) && Array.isArray(v[0]) && v[0].every(function (value){
      return "string" == (typeof value);
    })){
      return "(" + v[0].map(encode_value).join(", ") + ")";
    }
    else if((1 == v.length) && ("string" == (typeof v[0]))){
      return v[0];
    }
    else{
      let map_fn = function (item){
        return encode_query_value(item,column_fn,opts);
      };
      return v.map(map_fn).join(" ");
    }
  }
  else if((v == "and") || (v == "or")){
    return v;
  }
  else{
    return encode_value(v);
  }
}

function encode_query_segment(key,v,column_fn,opts){
  let col = column_fn(key);
  if(Array.isArray(v)){
    let map_fn = function (item){
      return encode_query_value(item,column_fn,opts);
    };
    let tail_values = v.slice(1,v.length);
    let encoded = tail_values.map(map_fn);
    return col + " " + encode_operator(v[0],opts) + " " + encoded.join(" ");
  }
  else{
    return col + " = " + encode_query_value(v,column_fn,opts);
  }
}

function encode_query_single_string(params,opts){
  let column_fn = (null == opts["column_fn"]) ? (function (x){
    return x;
  }) : opts["column_fn"];
  let sort_keys = (null == opts["sort_keys"]) ? true : opts["sort_keys"];
  let out = "";
  let query_pairs = Object.entries(params);
  if(sort_keys){
    query_pairs = xtd.arr_sort(query_pairs,function (arr){
      return arr[0];
    },function (x,y){
      return 0 > x.localeCompare(y);
    });
  }
  for(let e of query_pairs){
    let [key,v] = e;
    if(0 < out.length){
      out = (out + " AND ");
    }
    out = (out + encode_query_segment(key,v,column_fn,opts));
  };
  return out;
}

function encode_query_string(params,prefix,opts){
  let out = xtd.arrayify(params).map(function (p){
    return encode_query_single_string(p,opts);
  }).filter(function (e){
    return (null != e) && (0 < e.length);
  });
  let joined = out.map(function (s){
    return "(" + s + ")";
  }).join(" OR ");
  if(0 == out.length){
    return "";
  }
  else if(1 == out.length){
    return xtd.not_emptyp(prefix) ? (prefix + " " + out[0]) : out[0];
  }
  else{
    return xtd.not_emptyp(prefix) ? (prefix + " " + joined) : joined;
  }
}

function LIMIT(val){
  return {
    "::":"sql/keyword",
    "name":"LIMIT",
    "args":[{"::":"sql/keyword","name":val}]
  };
}

function OFFSET(val){
  return {
    "::":"sql/keyword",
    "name":"OFFSET",
    "args":[{"::":"sql/keyword","name":val}]
  };
}

function ORDER_BY(columns){
  return {
    "::":"sql/keyword",
    "name":"ORDER BY",
    "args":[
        {
          "::":"sql/tuple",
          "args":columns.map(function (column){
              return {"::":"sql/column","name":column};
            })
        }
      ]
  };
}

function ORDER_SORT(order){
  return {"::":"sql/keyword","name":xts.to_uppercase(order)};
}

function default_quote_fn(s){
  return "\"" + s + "\"";
}

function default_return_format_fn(input,nest_fn,column_fn,opts){
  if((null != input) && ("object" == (typeof input)) && !Array.isArray(input)){
    if(null != input["::"]){
      return encode_sql(input,column_fn,opts,encode_loop_fn);
    }
    else{
      return input["expr"] + ((null != input["as"]) ? (" AS " + input["as"]) : "");
    }
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

function default_table_fn(table,lookup){
  return "\"" + lookup[table]["schema"] + "\".\"" + table + "\"";
}

function postgres_wrapper_fn(s,indent){
  return "WITH j_ret AS (\n" + xts.pad_lines(s,2," ") + "\n) SELECT jsonb_agg(j_ret) FROM j_ret";
}

function postgres_opts(lookup){
  return {
    "return_join_fn":function (arr){
        return arr.join(", ");
      },
    "strict":true,
    "wrapper_fn":postgres_wrapper_fn,
    "querystr_fn":encode_query_string,
    "types":PG,
    "values":{"cast":true,"replace":{}},
    "return_link_fn":function (s,link_name){
        return "(" + s + ") AS " + link_name;
      },
    "return_format_fn":default_return_format_fn,
    "coerce":{},
    "return_count_fn":function (){
        return "count" + "(*)";
      },
    "column_fn":default_quote_fn,
    "table_fn":function (table){
        return default_table_fn(table,lookup);
      }
  };
}

function sqlite_return_format_fn(input,nest_fn,column_fn,opts){
  if((null != input) && ("object" == (typeof input)) && !Array.isArray(input)){
    return "'" + input["as"] + "'" + ", " + input["expr"];
  }
  else if(Array.isArray(input)){
    return nest_fn(input);
  }
  else if("string" == (typeof input)){
    return "'" + input + "'" + ", " + column_fn(input);
  }
  else{
    throw "Invalid input - " + String(input);
  }
}

function sqlite_to_boolean(v){
  if("number" == (typeof v)){
    return 1 == v;
  }
  return v;
}

module.exports = {
  ["OPERATORS"]:OPERATORS,
  ["INFIX"]:INFIX,
  ["PG"]:PG,
  ["SQLITE"]:SQLITE,
  ["sqlite_json_values"]:sqlite_json_values,
  ["sqlite_json_keys"]:sqlite_json_keys,
  ["SQLITE_FN"]:SQLITE_FN,
  ["encode_bool"]:encode_bool,
  ["encode_number"]:encode_number,
  ["encode_operator"]:encode_operator,
  ["encode_json"]:encode_json,
  ["encode_value"]:encode_value,
  ["encode_sql_arg"]:encode_sql_arg,
  ["encode_sql_column"]:encode_sql_column,
  ["encode_sql_tuple"]:encode_sql_tuple,
  ["encode_sql_table"]:encode_sql_table,
  ["encode_sql_cast"]:encode_sql_cast,
  ["encode_sql_keyword"]:encode_sql_keyword,
  ["encode_sql_fn"]:encode_sql_fn,
  ["encode_sql_select"]:encode_sql_select,
  ["ENCODE_SQL"]:ENCODE_SQL,
  ["encode_sql"]:encode_sql,
  ["encode_loop_fn"]:encode_loop_fn,
  ["encode_query_value"]:encode_query_value,
  ["encode_query_segment"]:encode_query_segment,
  ["encode_query_single_string"]:encode_query_single_string,
  ["encode_query_string"]:encode_query_string,
  ["LIMIT"]:LIMIT,
  ["OFFSET"]:OFFSET,
  ["ORDER_BY"]:ORDER_BY,
  ["ORDER_SORT"]:ORDER_SORT,
  ["default_quote_fn"]:default_quote_fn,
  ["default_return_format_fn"]:default_return_format_fn,
  ["default_table_fn"]:default_table_fn,
  ["postgres_wrapper_fn"]:postgres_wrapper_fn,
  ["postgres_opts"]:postgres_opts,
  ["sqlite_return_format_fn"]:sqlite_return_format_fn,
  ["sqlite_to_boolean"]:sqlite_to_boolean
}