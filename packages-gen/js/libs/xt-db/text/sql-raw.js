const ut = require("@xtalk/db/text/sql-util.js")

function raw_delete(table_name,where_params,opts){
  let table_fn = (null == opts["table_fn"]) ? (function (x){
    return x;
  }) : opts["table_fn"];
  let where_str = (null == where_params) ? "" : ut.encode_query_string(where_params,"WHERE",opts);
  let out_arr = ["DELETE FROM " + table_fn(table_name)];
  if(0 < where_str.length){
    out_arr.push(where_str);
  }
  return out_arr.join(" ") + ";";
}

function raw_insert_array(table_name,columns,values,opts){
  let table_fn = (null == opts["table_fn"]) ? (function (x){
    return x;
  }) : opts["table_fn"];
  let column_fn = (null == opts["column_fn"]) ? (function (x){
    return x;
  }) : opts["column_fn"];
  let out_arr = [
    "INSERT INTO " + table_fn(table_name),
    " (" + columns.map(column_fn).join(", ") + ")"
  ];
  let val_fn = function (data){
    let s_arr = columns.map(function (k){
      return ut.encode_value(data[k]);
    });
    return "(" + s_arr.join(",") + ")";
  };
  let val_arr = values.map(val_fn);
  let val_str = " VALUES\n " + val_arr.join(",\n ");
  out_arr.push(val_str);
  return out_arr;
}

function raw_insert(table_name,columns,values,opts){
  let out_arr = raw_insert_array(table_name,columns,values,opts);
  return out_arr.join("\n") + ";";
}

function raw_upsert(table_name,id_column,columns,values,opts){
  let table_fn = (null == opts["table_fn"]) ? (function (x){
    return x;
  }) : opts["table_fn"];
  let column_fn = (null == opts["column_fn"]) ? (function (x){
    return x;
  }) : opts["column_fn"];
  let upsert_clause = opts["upsert_clause"];
  let out_arr = raw_insert_array(table_name,columns,values,opts);
  let col_arr = columns.filter(function (col){
    return col != id_column;
  }).map(function (col){
    return column_fn(col) + "=coalesce(\"excluded\"." + column_fn(col) + "," + column_fn(col) + ")";
  });
  return out_arr.join("\n") + "\n" + ("ON CONFLICT (" + column_fn(id_column) + ") DO UPDATE SET\n") + col_arr.join(",\n") + (("string" == (typeof upsert_clause)) ? ("\nWHERE " + upsert_clause) : "") + ";";
}

function raw_update(table_name,where_params,data,opts){
  let table_fn = (null == opts["table_fn"]) ? (function (x){
    return x;
  }) : opts["table_fn"];
  let where_str = (null == where_params) ? "" : ut.encode_query_string(where_params,"WHERE",opts);
  let set_str = ut.encode_query_string(data,"SET",opts);
  let out_arr = ["UPDATE " + table_fn(table_name),set_str,where_str];
  return out_arr.join("\n ") + ";";
}

function raw_select(table_name,where_params,return_params,opts){
  let table_fn = (null == opts["table_fn"]) ? (function (x){
    return x;
  }) : opts["table_fn"];
  let column_fn = (null == opts["column_fn"]) ? (function (x){
    return x;
  }) : opts["column_fn"];
  let return_str = ("string" == (typeof return_params)) ? return_params : return_params.map(column_fn).join(", ");
  let where_str = (null == where_params) ? "" : ut.encode_query_string(where_params,"WHERE",opts);
  let out_arr = ["SELECT " + return_str," FROM " + table_fn(table_name)];
  if(0 < where_str.length){
    out_arr.push(where_str);
  }
  return out_arr.join("\n ") + ";";
}

module.exports = {
  ["raw_delete"]:raw_delete,
  ["raw_insert_array"]:raw_insert_array,
  ["raw_insert"]:raw_insert,
  ["raw_upsert"]:raw_upsert,
  ["raw_update"]:raw_update,
  ["raw_select"]:raw_select
}