import 'package:xtalk_lang/common-data.dart' as xtd;


var CACHED_SCHEMA = <dynamic, dynamic>{};

var CACHED_LOOKUP = <dynamic, dynamic>{};

var get_order = (e) {
  return e["order"];
};

var get_ident = (e) {
  return e["ident"];
};

get_ident_id(e) {
  return ("ref" == e["type"]) ? (e["ident"] + "_id") : e["ident"];
}

list_tables(schema) {
  return xtd.arr_sort(List<dynamic>.from(( schema ).keys),(x) {
    return x;
  },(x, y) {
    return (x).toString().compareTo((y).toString()) < 0;
  });
}

get_cached_schema(schema) {
  var cached = CACHED_SCHEMA[schema];
  if(null == cached){
    cached = <dynamic, dynamic>{};
    CACHED_SCHEMA[schema] = cached;
  }
  return cached;
}

create_data_keys(schema, table_name) {
  var table_def = schema[table_name];
  return xtd.arr_map(xtd.arr_sort(xtd.arr_filter(List<dynamic>.from(( table_def ).values),(e) {
    return (("int" == (e["order"].runtimeType).toString()) || ("double" == (e["order"].runtimeType).toString()) || ("num" == (e["order"].runtimeType).toString())) && (e["type"] != "ref");
  }),get_order,(x, y) {
    return (("String" == (x.runtimeType).toString()) || ("String" == (y.runtimeType).toString())) ? ((x).toString().compareTo((y).toString()) < 0) : (x < y);
  }),get_ident);
}

create_ref_keys(schema, table_name) {
  var table_def = schema[table_name];
  return xtd.arr_map(xtd.arr_sort(xtd.arr_filter(List<dynamic>.from(( table_def ).values),(e) {
    return (("int" == (e["order"].runtimeType).toString()) || ("double" == (e["order"].runtimeType).toString()) || ("num" == (e["order"].runtimeType).toString())) && (e["type"] == "ref");
  }),get_order,(x, y) {
    return (("String" == (x.runtimeType).toString()) || ("String" == (y.runtimeType).toString())) ? ((x).toString().compareTo((y).toString()) < 0) : (x < y);
  }),get_ident);
}

create_rev_keys(schema, table_name) {
  var table_def = schema[table_name];
  return xtd.arr_map(xtd.arr_filter(List<dynamic>.from(( table_def ).values),(e) {
    return !(("int" == (e["order"].runtimeType).toString()) || ("double" == (e["order"].runtimeType).toString()) || ("num" == (e["order"].runtimeType).toString()));
  }),get_ident);
}

create_table_entries(schema, table_name) {
  var table_def = schema[table_name];
  return xtd.arr_sort(xtd.arr_filter(List<dynamic>.from(( table_def ).values),(e) {
    return ("int" == (e["order"].runtimeType).toString()) || ("double" == (e["order"].runtimeType).toString()) || ("num" == (e["order"].runtimeType).toString());
  }),get_order,(x, y) {
    return (("String" == (x.runtimeType).toString()) || ("String" == (y.runtimeType).toString())) ? ((x).toString().compareTo((y).toString()) < 0) : (x < y);
  });
}

create_defaults(schema, table_name) {
  var table_def = schema[table_name];
  return xtd.obj_keepf(table_def,(m) {
    return (("Map" == (m["sql"].runtimeType).toString()) || (m["sql"].runtimeType).toString().startsWith("_Map") || (m["sql"].runtimeType).toString().startsWith("LinkedMap")) && m["sql"].containsKey("default");
  },(m) {
    return m["sql"]["default"];
  });
}

create_all_keys(schema, table_name) {
  var ref_ks = create_ref_keys(schema,table_name);
  var ref_id_ks = xtd.obj_from_pairs(xtd.arr_map(ref_ks,(k) {
    return <dynamic>[k + "_id",k];
  }));
  return <dynamic, dynamic>{
    "data":create_data_keys(schema,table_name),
    "ref":ref_ks,
    "ref_id":ref_id_ks,
    "rev":create_rev_keys(schema,table_name),
    "defaults":create_defaults(schema,table_name),
    "table":create_table_entries(schema,table_name)
  };
}

get_all_keys(schema, table_name) {
  var cached = get_cached_schema(schema);
  var table_keys = cached[table_name];
  if(null == table_keys){
    table_keys = create_all_keys(schema,table_name);
    cached[table_name] = table_keys;
  }
  return table_keys;
}

data_keys(schema, table_name) {
  return (get_all_keys(schema,table_name))["data"];
}

ref_keys(schema, table_name) {
  return (get_all_keys(schema,table_name))["ref"];
}

ref_id_keys(schema, table_name) {
  return (get_all_keys(schema,table_name))["ref_id"];
}

rev_keys(schema, table_name) {
  return (get_all_keys(schema,table_name))["rev"];
}

table_defaults(schema, table_name) {
  return (get_all_keys(schema,table_name))["defaults"];
}

table_entries(schema, table_name) {
  return (get_all_keys(schema,table_name))["table"];
}

table_columns(schema, table_name) {
  return xtd.arr_map(table_entries(schema,table_name),get_ident_id);
}

create_table_order(lookup) {
  return xtd.arr_map(xtd.arr_sort(List<List<dynamic>>.from(( lookup ).entries.map((entry) => [entry.key, entry.value])),(pair) {
    return (pair[1])["position"];
  },(x, y) {
    return (("String" == (x.runtimeType).toString()) || ("String" == (y.runtimeType).toString())) ? ((x).toString().compareTo((y).toString()) < 0) : (x < y);
  }),(arr) {
    return arr[0];
  });
}

table_order(lookup) {
  var cached = CACHED_LOOKUP[lookup];
  if(null == cached){
    cached = create_table_order(lookup);
    CACHED_LOOKUP[lookup] = cached;
  }
  return cached;
}

table_coerce(schema, table, data, ctypes) {
  var out = <dynamic, dynamic>{};
  var ref_fn = (ntable, vdata) {
    return table_coerce(schema,ntable,vdata,ctypes);
  };
  if((data.runtimeType).toString().startsWith("List") || (data.runtimeType).toString().startsWith("_GrowableList")){
    return xtd.arr_map(data,(vdata) {
      return Function.apply((ref_fn as Function),<dynamic>[table,vdata]);
    });
  }
  for(var entry_52470 in data.entries){
    var key = entry_52470.key;
    var v = entry_52470.value;
    var rec = xtd.get_in(schema,<dynamic>[table,key]);
    if(null == rec){
      out[key] = v;
    }
    else if("ref" == rec["type"]){
      var ntable = rec["ref"]["ns"];
      out[key] = xtd.arr_map(v,(vdata) {
        return Function.apply((ref_fn as Function),<dynamic>[ntable,vdata]);
      });
    }
    else{
      var f = ctypes[rec["type"]];
      var val = (null == f) ? v : f(v);
      out[key] = val;
    }
  };
  return out;
}