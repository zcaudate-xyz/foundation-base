const xtd = require("@xtalk/lang/common-data.js")

const base_schema = require("@xtalk/db/text/base-schema.js")

function table_create_column(schema,entry,opts){
  let {column_fn,strict,table_fn,types} = opts;
  let ident = entry["ident"];
  let itype = entry["type"];
  let iprimary = entry["primary"];
  let irequired = entry["required"];
  let stype = (null != types[itype]) ? types[itype] : itype;
  let default_fn = function (ident){
    return column_fn(ident) + " " + (("ref" == stype) ? "text" : stype) + ((true == iprimary) ? " PRIMARY KEY" : "") + (((true == irequired) && (true == strict)) ? " NOT NULL" : "");
  };
  if((stype == "ref") && (null != schema[entry["ref"]["ns"]])){
    let rtable = entry["ref"]["ns"];
    let rtype = xtd.get_in(schema,[rtable,"id","type"]);
    if(!("string" == (typeof rtype))){
      return default_fn(ident + "_id");
    }
    else{
      return column_fn(ident + "_id") + " " + ((null != types[rtype]) ? types[rtype] : rtype) + " REFERENCES " + table_fn(rtable);
    }
  }
  else{
    return default_fn(ident);
  }
}

function table_create(schema,table_name,opts){
  let table_fn = (null == opts["table_fn"]) ? (function (x){
    return x;
  }) : opts["table_fn"];
  let columns = base_schema.table_entries(schema,table_name);
  return "CREATE TABLE IF NOT EXISTS " + table_fn(table_name) + " (\n  " + columns.map(function (e){
    return table_create_column(schema,e,opts);
  }).join(",\n  ") + "\n);";
}

function table_create_all(schema,lookup,opts){
  let table_list = base_schema.table_order(lookup);
  return table_list.map(function (table_name){
    return table_create(schema,table_name,opts);
  });
}

function table_drop(schema,table_name,opts){
  let table_fn = (null == opts["table_fn"]) ? (function (x){
    return x;
  }) : opts["table_fn"];
  return "DROP TABLE IF EXISTS " + table_fn(table_name) + ";";
}

function table_drop_all(schema,lookup,opts){
  let ks = base_schema.table_order(lookup).slice().reverse();
  return ks.map(function (table_name){
    return table_drop(schema,table_name,opts);
  });
}

module.exports = {
  ["table_create_column"]:table_create_column,
  ["table_create"]:table_create,
  ["table_create_all"]:table_create_all,
  ["table_drop"]:table_drop,
  ["table_drop_all"]:table_drop_all
}