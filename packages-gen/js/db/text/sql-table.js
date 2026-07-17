const xtd = require("@xtalk/lang/common-data.js")

const base_schema = require("@xtalk/db/text/base-schema.js")

const raw = require("@xtalk/db/text/sql-raw.js")

const f = require("@xtalk/db/text/base-flatten.js")

function table_update_single(schema,table_name,id,m,opts){
  let cols = base_schema.table_columns(schema,table_name);
  return raw.raw_update(table_name,{"id":id},xtd.obj_pick(m,cols),opts);
}

function table_insert_single(schema,table_name,m,opts){
  let cols = base_schema.table_columns(schema,table_name);
  let ks = cols.filter(function (col){
    return null != m[col];
  });
  return raw.raw_insert(table_name,ks,[m],opts);
}

function table_delete_single(schema,table_name,id,opts){
  let cols = base_schema.table_columns(schema,table_name);
  return raw.raw_delete(table_name,{"id":id},opts);
}

function table_upsert_single(schema,table_name,m,opts){
  let cols = base_schema.table_columns(schema,table_name);
  let ks = cols.filter(function (col){
    return null != m[col];
  });
  return raw.raw_upsert(table_name,"id",ks,[m],opts);
}

function table_filter_id(entry){
  return !((0 == Object.keys(entry["ref_links"]).length) && (1 == Object.keys(entry["data"]).length));
}

function table_get_data(entry){
  let out = Object.assign({},entry["data"]);
  for(let [link,m] of Object.entries(entry["ref_links"])){
    out[link + "_id"] = xtd.obj_first_key(m);
  };
  return out;
}

var table_emit_insert = raw.raw_insert;

var table_emit_upsert = function (table_name,cols,out,opts){
  return raw.raw_upsert(table_name,"id",cols,out,opts);
};

function table_emit_flat(emit_fn,schema,lookup,flat,opts){
  let ordered = xtd.arr_keep(base_schema.table_order(lookup),function (col){
    return (null != flat[col]) ? [col,flat[col]] : null;
  });
  let column_fn = (null == opts["column_fn"]) ? (function (x){
    return x;
  }) : opts["column_fn"];
  let emit_pair_fn = function (pair){
    let [table_name,data] = pair;
    let cols = base_schema.table_columns(schema,table_name);
    let defaults = base_schema.table_defaults(schema,table_name);
    let out = xtd.arr_keepf(Object.values(data),table_filter_id,table_get_data);
    let sout = out.map(function (v){
      return Object.assign(Object.assign({},defaults),v);
    });
    let {schema_update} = lookup[table_name];
    let {update_key} = opts;
    let sopts = null;
    if(schema_update && (null != update_key)){
      sopts = Object.assign({
        "upsert_clause":"\"excluded\"." + column_fn(update_key) + " < " + column_fn(update_key)
      },opts);
    }
    else{
      sopts = Object.assign({},opts);
    }
    if(0 < sout.length){
      return emit_fn(table_name,cols,sout,sopts);
    }
  };
  return xtd.arr_keep(ordered,emit_pair_fn);
}

function table_insert(schema,lookup,table_name,data,opts){
  let flat = f.flatten(schema,table_name,data,{});
  return table_emit_flat(table_emit_insert,schema,lookup,flat,opts);
}

function table_upsert(schema,lookup,table_name,data,opts){
  let flat = f.flatten(schema,table_name,data,{});
  return table_emit_flat(table_emit_upsert,schema,lookup,flat,opts);
}

function prepare_add_input(data,schema,lookup,opts){
  let flat = f.flatten_bulk(schema,data);
  let statements = table_emit_flat(table_emit_upsert,schema,lookup,flat,opts);
  return statements.join("\n\n");
}

function prepare_remove_input(data,schema,lookup,opts){
  let ordered = f.flatten_bulk_ids(schema,lookup,data);
  let statements = xtd.arr_mapcat(ordered,function (entry){
    let [table_name,ids] = entry;
    return ids.map(function (id){
      return raw.raw_delete(table_name,{"id":id},opts);
    });
  });
  return statements.join("\n\n");
}

module.exports = {
  ["table_update_single"]:table_update_single,
  ["table_insert_single"]:table_insert_single,
  ["table_delete_single"]:table_delete_single,
  ["table_upsert_single"]:table_upsert_single,
  ["table_filter_id"]:table_filter_id,
  ["table_get_data"]:table_get_data,
  ["table_emit_insert"]:table_emit_insert,
  ["table_emit_upsert"]:table_emit_upsert,
  ["table_emit_flat"]:table_emit_flat,
  ["table_insert"]:table_insert,
  ["table_upsert"]:table_upsert,
  ["prepare_add_input"]:prepare_add_input,
  ["prepare_remove_input"]:prepare_remove_input
}