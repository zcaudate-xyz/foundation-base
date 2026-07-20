import 'package:xtalk_lang/common-data.dart' as xtd;
import 'package:xtalk_db/text/base-schema.dart' as base_schema;



table_create_column(schema, entry, opts) {
  var column_fn = opts["column_fn"];
  var strict = opts["strict"];
  var table_fn = opts["table_fn"];
  var types = opts["types"];
  var ident = entry["ident"];
  var itype = entry["type"];
  var iprimary = entry["primary"];
  var irequired = entry["required"];
  var stype = types.containsKey(itype) ? types[itype] : itype;
  var default_fn = (ident) {
    return Function.apply((column_fn as Function),<dynamic>[ident]) + " " + (("ref" == stype) ? "text" : stype) + ((true == iprimary) ? " PRIMARY KEY" : "") + (((true == irequired) && (true == strict)) ? " NOT NULL" : "");
  };
  if((stype == "ref") && schema.containsKey(entry["ref"]["ns"])){
    var rtable = entry["ref"]["ns"];
    var rtype = xtd.get_in(schema,<dynamic>[rtable,"id","type"]);
    if(!("String" == (rtype.runtimeType).toString())){
      return Function.apply((default_fn as Function),<dynamic>[ident + "_id"]);
    }
    else{
      return Function.apply((column_fn as Function),<dynamic>[ident + "_id"]) + " " + (types.containsKey(rtype) ? types[rtype] : rtype) + " REFERENCES " + Function.apply((table_fn as Function),<dynamic>[rtable]);
    }
  }
  else{
    return Function.apply((default_fn as Function),<dynamic>[ident]);
  }
}

table_create(schema, table_name, opts) {
  var table_fn = (null == opts["table_fn"]) ? ((x) {
    return x;
  }) : opts["table_fn"];
  var columns = base_schema.table_entries(schema,table_name);
  return "CREATE TABLE IF NOT EXISTS " + Function.apply((table_fn as Function),<dynamic>[table_name]) + " (\n  " + xtd.arr_map(columns,(e) {
    return table_create_column(schema,e,opts);
  }).join(",\n  ") + "\n);";
}

table_create_all(schema, lookup, opts) {
  var table_list = base_schema.table_order(lookup);
  return xtd.arr_map(table_list,(table_name) {
    return table_create(schema,table_name,opts);
  });
}

table_drop(schema, table_name, opts) {
  var table_fn = (null == opts["table_fn"]) ? ((x) {
    return x;
  }) : opts["table_fn"];
  return "DROP TABLE IF EXISTS " + Function.apply((table_fn as Function),<dynamic>[table_name]) + ";";
}

table_drop_all(schema, lookup, opts) {
  var ks = xtd.arr_reverse(base_schema.table_order(lookup));
  return xtd.arr_map(ks,(table_name) {
    return table_drop(schema,table_name,opts);
  });
}