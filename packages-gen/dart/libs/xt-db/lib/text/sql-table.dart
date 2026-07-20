import 'package:xtalk_lang/common-data.dart' as xtd;
import 'package:xtalk_db/text/base-schema.dart' as base_schema;
import 'package:xtalk_db/text/sql-raw.dart' as raw;
import 'package:xtalk_db/text/base-flatten.dart' as f;





table_update_single(schema, table_name, id, m, opts) {
  var cols = base_schema.table_columns(schema,table_name);
  return raw.raw_update(
    table_name,
    <dynamic, dynamic>{"id":id},
    xtd.obj_pick(m,cols),
    opts
  );
}

table_insert_single(schema, table_name, m, opts) {
  var cols = base_schema.table_columns(schema,table_name);
  var ks = xtd.arr_filter(cols,(col) {
    return m.containsKey(col);
  });
  return raw.raw_insert(table_name,ks,<dynamic>[m],opts);
}

table_delete_single(schema, table_name, id, opts) {
  var cols = base_schema.table_columns(schema,table_name);
  return raw.raw_delete(table_name,<dynamic, dynamic>{"id":id},opts);
}

table_upsert_single(schema, table_name, m, opts) {
  var cols = base_schema.table_columns(schema,table_name);
  var ks = xtd.arr_filter(cols,(col) {
    return m.containsKey(col);
  });
  return raw.raw_upsert(table_name,"id",ks,<dynamic>[m],opts);
}

table_filter_id(entry) {
  return !((0 == List<dynamic>.from(( entry["ref_links"] ).keys).length) && (1 == List<dynamic>.from(( entry["data"] ).keys).length));
}

table_get_data(entry) {
  var out = xtd.obj_clone(entry["data"]);
  for(var entry_52216 in entry["ref_links"].entries){
    var link = entry_52216.key;
    var m = entry_52216.value;
    out[link + "_id"] = xtd.obj_first_key(m);
  };
  return out;
}

var table_emit_insert = raw.raw_insert;

var table_emit_upsert = (table_name, cols, out, opts) {
  return raw.raw_upsert(table_name,"id",cols,out,opts);
};

table_emit_flat(emit_fn, schema, lookup, flat, opts) {
  var ordered = xtd.arr_keep(base_schema.table_order(lookup),(col) {
    return flat.containsKey(col) ? <dynamic>[col,flat[col]] : null;
  });
  var column_fn = (null == opts["column_fn"]) ? ((x) {
    return x;
  }) : opts["column_fn"];
  var emit_pair_fn = (pair) {
    var value_52217 = pair;
    var table_name = value_52217[0];
    var data = value_52217[1];
    var cols = base_schema.table_columns(schema,table_name);
    var defaults = base_schema.table_defaults(schema,table_name);
    var out = xtd.arr_keepf(
      List<dynamic>.from(( data ).values),
      table_filter_id,
      table_get_data
    );
    var sout = xtd.arr_map(out,(v) {
      return xtd.obj_assign(xtd.obj_clone(defaults),v);
    });
    var schema_update = lookup[table_name]["schema_update"];
    var update_key = opts["update_key"];
    var sopts;
    if(((null != schema_update) && (false != schema_update)) && (null != update_key)){
      sopts = xtd.obj_assign(<dynamic, dynamic>{
        "upsert_clause":"\"excluded\"." + Function.apply((column_fn as Function),<dynamic>[update_key]) + " < " + Function.apply((column_fn as Function),<dynamic>[update_key])
      },opts);
    }
    else{
      sopts = xtd.obj_clone(opts);
    }
    if(0 < sout.length){
      return Function.apply((emit_fn as Function),<dynamic>[table_name,cols,sout,sopts]);
    }
  };
  return xtd.arr_keep(ordered,emit_pair_fn);
}

table_insert(schema, lookup, table_name, data, opts) {
  var flat = f.flatten(schema,table_name,data,<dynamic, dynamic>{});
  return table_emit_flat(table_emit_insert,schema,lookup,flat,opts);
}

table_upsert(schema, lookup, table_name, data, opts) {
  var flat = f.flatten(schema,table_name,data,<dynamic, dynamic>{});
  return table_emit_flat(table_emit_upsert,schema,lookup,flat,opts);
}

prepare_add_input(data, schema, lookup, opts) {
  var flat = f.flatten_bulk(schema,data);
  var statements = table_emit_flat(table_emit_upsert,schema,lookup,flat,opts);
  return statements.join("\n\n");
}

prepare_remove_input(data, schema, lookup, opts) {
  var ordered = f.flatten_bulk_ids(schema,lookup,data);
  var statements = xtd.arr_mapcat(ordered,(entry) {
    var value_52220 = entry;
    var table_name = value_52220[0];
    var ids = value_52220[1];
    return xtd.arr_map(ids,(id) {
      return raw.raw_delete(table_name,<dynamic, dynamic>{"id":id},opts);
    });
  });
  return statements.join("\n\n");
}