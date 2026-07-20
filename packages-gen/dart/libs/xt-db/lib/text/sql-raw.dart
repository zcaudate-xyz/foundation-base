import 'package:xtalk_db/text/sql-util.dart' as ut;


raw_delete(table_name, where_params, opts) {
  var table_fn = (null == opts["table_fn"]) ? ((x) {
    return x;
  }) : opts["table_fn"];
  var where_str = (null == where_params) ? "" : ut.encode_query_string(where_params,"WHERE",opts);
  var out_arr = <dynamic>[
    "DELETE FROM " + Function.apply((table_fn as Function),<dynamic>[table_name])
  ];
  if(0 < where_str.length){
    out_arr.add(where_str);
  }
  return out_arr.join(" ") + ";";
}

raw_insert_array(table_name, columns, values, opts) {
  var table_fn = (null == opts["table_fn"]) ? ((x) {
    return x;
  }) : opts["table_fn"];
  var column_fn = (null == opts["column_fn"]) ? ((x) {
    return x;
  }) : opts["column_fn"];
  var out_arr = <dynamic>[
    "INSERT INTO " + Function.apply((table_fn as Function),<dynamic>[table_name]),
    " (" + xt.lang.common_data.arr_map(columns,column_fn).join(", ") + ")"
  ];
  var val_fn = (data) {
    var s_arr = xt.lang.common_data.arr_map(columns,(k) {
      return ut.encode_value(data[k]);
    });
    return "(" + s_arr.join(",") + ")";
  };
  var val_arr = xt.lang.common_data.arr_map(values,val_fn);
  var val_str = " VALUES\n " + val_arr.join(",\n ");
  out_arr.add(val_str);
  return out_arr;
}

raw_insert(table_name, columns, values, opts) {
  var out_arr = raw_insert_array(table_name,columns,values,opts);
  return out_arr.join("\n") + ";";
}

raw_upsert(table_name, id_column, columns, values, opts) {
  var table_fn = (null == opts["table_fn"]) ? ((x) {
    return x;
  }) : opts["table_fn"];
  var column_fn = (null == opts["column_fn"]) ? ((x) {
    return x;
  }) : opts["column_fn"];
  var upsert_clause = opts["upsert_clause"];
  var out_arr = raw_insert_array(table_name,columns,values,opts);
  var col_arr = xt.lang.common_data.arr_map(xt.lang.common_data.arr_filter(columns,(col) {
    return col != id_column;
  }),(col) {
    return Function.apply((column_fn as Function),<dynamic>[col]) + "=coalesce(\"excluded\"." + Function.apply((column_fn as Function),<dynamic>[col]) + "," + Function.apply((column_fn as Function),<dynamic>[col]) + ")";
  });
  return out_arr.join("\n") + "\n" + ("ON CONFLICT (" + Function.apply((column_fn as Function),<dynamic>[id_column]) + ") DO UPDATE SET\n") + col_arr.join(",\n") + (("String" == (upsert_clause.runtimeType).toString()) ? ("\nWHERE " + upsert_clause) : "") + ";";
}

raw_update(table_name, where_params, data, opts) {
  var table_fn = (null == opts["table_fn"]) ? ((x) {
    return x;
  }) : opts["table_fn"];
  var where_str = (null == where_params) ? "" : ut.encode_query_string(where_params,"WHERE",opts);
  var set_str = ut.encode_query_string(data,"SET",opts);
  var out_arr = <dynamic>[
    "UPDATE " + Function.apply((table_fn as Function),<dynamic>[table_name]),
    set_str,
    where_str
  ];
  return out_arr.join("\n ") + ";";
}

raw_select(table_name, where_params, return_params, opts) {
  var table_fn = (null == opts["table_fn"]) ? ((x) {
    return x;
  }) : opts["table_fn"];
  var column_fn = (null == opts["column_fn"]) ? ((x) {
    return x;
  }) : opts["column_fn"];
  var return_str = ("String" == (return_params.runtimeType).toString()) ? return_params : xt.lang.common_data.arr_map(return_params,column_fn).join(", ");
  var where_str = (null == where_params) ? "" : ut.encode_query_string(where_params,"WHERE",opts);
  var out_arr = <dynamic>[
    "SELECT " + return_str,
    " FROM " + Function.apply((table_fn as Function),<dynamic>[table_name])
  ];
  if(0 < where_str.length){
    out_arr.add(where_str);
  }
  return out_arr.join("\n ") + ";";
}