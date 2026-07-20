const xtd = require("@xtalk/lang/common-data.js")

var CACHED_SCHEMA = new Map();

var CACHED_LOOKUP = new Map();

var get_order = function (e){
  return e["order"];
};

var get_ident = function (e){
  return e["ident"];
};

function get_ident_id(e){
  return ("ref" == e["type"]) ? (e["ident"] + "_id") : e["ident"];
}

function list_tables(schema){
  return xtd.arr_sort(Object.keys(schema),function (x){
    return x;
  },function (x,y){
    return 0 > x.localeCompare(y);
  });
}

function get_cached_schema(schema){
  let cached = CACHED_SCHEMA.get(schema);
  if(null == cached){
    cached = {};
    CACHED_SCHEMA.set(schema,cached);
  }
  return cached;
}

function create_data_keys(schema,table_name){
  let table_def = schema[table_name];
  return xtd.arr_sort(Object.values(table_def).filter(function (e){
    return ("number" == (typeof e["order"])) && (e["type"] != "ref");
  }),get_order,function (x,y){
    return x < y;
  }).map(get_ident);
}

function create_ref_keys(schema,table_name){
  let table_def = schema[table_name];
  return xtd.arr_sort(Object.values(table_def).filter(function (e){
    return ("number" == (typeof e["order"])) && (e["type"] == "ref");
  }),get_order,function (x,y){
    return x < y;
  }).map(get_ident);
}

function create_rev_keys(schema,table_name){
  let table_def = schema[table_name];
  return Object.values(table_def).filter(function (e){
    return !("number" == (typeof e["order"]));
  }).map(get_ident);
}

function create_table_entries(schema,table_name){
  let table_def = schema[table_name];
  return xtd.arr_sort(Object.values(table_def).filter(function (e){
    return "number" == (typeof e["order"]);
  }),get_order,function (x,y){
    return x < y;
  });
}

function create_defaults(schema,table_name){
  let table_def = schema[table_name];
  return xtd.obj_keepf(table_def,function (m){
    return ((null != m["sql"]) && ("object" == (typeof m["sql"])) && !Array.isArray(m["sql"])) && (null != m["sql"]["default"]);
  },function (m){
    return m["sql"]["default"];
  });
}

function create_all_keys(schema,table_name){
  let ref_ks = create_ref_keys(schema,table_name);
  let ref_id_ks = xtd.obj_from_pairs(ref_ks.map(function (k){
    return [k + "_id",k];
  }));
  return {
    "data":create_data_keys(schema,table_name),
    "ref":ref_ks,
    "ref_id":ref_id_ks,
    "rev":create_rev_keys(schema,table_name),
    "defaults":create_defaults(schema,table_name),
    "table":create_table_entries(schema,table_name)
  };
}

function get_all_keys(schema,table_name){
  let cached = get_cached_schema(schema);
  let table_keys = cached[table_name];
  if(null == table_keys){
    table_keys = create_all_keys(schema,table_name);
    cached[table_name] = table_keys;
  }
  return table_keys;
}

function data_keys(schema,table_name){
  return (get_all_keys(schema,table_name))["data"];
}

function ref_keys(schema,table_name){
  return (get_all_keys(schema,table_name))["ref"];
}

function ref_id_keys(schema,table_name){
  return (get_all_keys(schema,table_name))["ref_id"];
}

function rev_keys(schema,table_name){
  return (get_all_keys(schema,table_name))["rev"];
}

function table_defaults(schema,table_name){
  return (get_all_keys(schema,table_name))["defaults"];
}

function table_entries(schema,table_name){
  return (get_all_keys(schema,table_name))["table"];
}

function table_columns(schema,table_name){
  return table_entries(schema,table_name).map(get_ident_id);
}

function create_table_order(lookup){
  return xtd.arr_sort(Object.entries(lookup),function (pair){
    return (pair[1])["position"];
  },function (x,y){
    return x < y;
  }).map(function (arr){
    return arr[0];
  });
}

function table_order(lookup){
  let cached = CACHED_LOOKUP.get(lookup);
  if(null == cached){
    cached = create_table_order(lookup);
    CACHED_LOOKUP.set(lookup,cached);
  }
  return cached;
}

function table_coerce(schema,table,data,ctypes){
  let out = {};
  let ref_fn = function (ntable,vdata){
    return table_coerce(schema,ntable,vdata,ctypes);
  };
  if(Array.isArray(data)){
    return data.map(function (vdata){
      return ref_fn(table,vdata);
    });
  }
  for(let [key,v] of Object.entries(data)){
    let rec = xtd.get_in(schema,[table,key]);
    if(null == rec){
      out[key] = v;
    }
    else if("ref" == rec["type"]){
      let ntable = rec["ref"]["ns"];
      out[key] = v.map(function (vdata){
        return ref_fn(ntable,vdata);
      });
    }
    else{
      let f = ctypes[rec["type"]];
      let val = (null == f) ? v : f(v);
      out[key] = val;
    }
  };
  return out;
}

module.exports = {
  ["CACHED_SCHEMA"]:CACHED_SCHEMA,
  ["CACHED_LOOKUP"]:CACHED_LOOKUP,
  ["get_order"]:get_order,
  ["get_ident"]:get_ident,
  ["get_ident_id"]:get_ident_id,
  ["list_tables"]:list_tables,
  ["get_cached_schema"]:get_cached_schema,
  ["create_data_keys"]:create_data_keys,
  ["create_ref_keys"]:create_ref_keys,
  ["create_rev_keys"]:create_rev_keys,
  ["create_table_entries"]:create_table_entries,
  ["create_defaults"]:create_defaults,
  ["create_all_keys"]:create_all_keys,
  ["get_all_keys"]:get_all_keys,
  ["data_keys"]:data_keys,
  ["ref_keys"]:ref_keys,
  ["ref_id_keys"]:ref_id_keys,
  ["rev_keys"]:rev_keys,
  ["table_defaults"]:table_defaults,
  ["table_entries"]:table_entries,
  ["table_columns"]:table_columns,
  ["create_table_order"]:create_table_order,
  ["table_order"]:table_order,
  ["table_coerce"]:table_coerce
}