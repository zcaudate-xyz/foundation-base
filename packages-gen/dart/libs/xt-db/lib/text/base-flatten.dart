import 'package:xtalk_lang/common-data.dart' as xtd;
import 'package:xtalk_db/text/base-schema.dart' as sch;
import 'dart:convert';



flatten_get_links(obj) {
  var link_fn = (e) {
    if("String" == (e.runtimeType).toString()){
      return <dynamic>[e,true];
    }
    else{
      if(null == e){
        throw "Invalid link - " + jsonEncode(obj);
      }
      else{
        return <dynamic>[e["id"],true];
      }
    }
  };
  return xtd.obj_keep(obj,(v) {
    return ((v.runtimeType).toString().startsWith("List") || (v.runtimeType).toString().startsWith("_GrowableList")) ? xtd.obj_from_pairs(xtd.arr_map(v,link_fn)) : null;
  });
}

flatten_merge(table_map, data_obj, ref_links, rev_links) {
  var id = data_obj["id"];
  var rec = table_map[id];
  if(!(("Map" == (rec.runtimeType).toString()) || (rec.runtimeType).toString().startsWith("_Map") || (rec.runtimeType).toString().startsWith("LinkedMap"))){
    rec = <dynamic, dynamic>{
      "id":id,
      "data":<dynamic, dynamic>{},
      "ref_links":<dynamic, dynamic>{},
      "rev_links":<dynamic, dynamic>{}
    };
    table_map[id] = rec;
  }
  xtd.swap_key(rec,"data",xtd.obj_assign,<dynamic>[data_obj]);
  xtd.swap_key(rec,"ref_links",xtd.obj_assign_with,<dynamic>[
    ref_links,
    (obj, other) {
      return xtd.obj_assign(obj,other);
    }
  ]);
  xtd.swap_key(rec,"rev_links",xtd.obj_assign_with,<dynamic>[
    rev_links,
    (obj, other) {
      return xtd.obj_assign(obj,other);
    }
  ]);
  return table_map;
}

flatten_node(schema, table_name, data, parent, acc) {
  data = xtd.obj_assign(data,xtd.clone_nested(parent));
  var table_map = acc[table_name];
  if(!(("Map" == (table_map.runtimeType).toString()) || (table_map.runtimeType).toString().startsWith("_Map") || (table_map.runtimeType).toString().startsWith("LinkedMap"))){
    table_map = <dynamic, dynamic>{};
    acc[table_name] = table_map;
  }
  var data_obj = xtd.obj_pick(data,sch.data_keys(schema,table_name));
  var obj_fn = (v) {
    return (("Map" == (v.runtimeType).toString()) || (v.runtimeType).toString().startsWith("_Map") || (v.runtimeType).toString().startsWith("LinkedMap")) ? <dynamic>[v] : v;
  };
  var rev_obj = xtd.obj_keep(xtd.obj_pick(data,sch.rev_keys(schema,table_name)),obj_fn);
  var rev_links = flatten_get_links(rev_obj);
  var ref_obj = xtd.obj_keep(xtd.obj_pick(data,sch.ref_keys(schema,table_name)),obj_fn);
  var ref_links = flatten_get_links(ref_obj);
  var ref_id_map = sch.ref_id_keys(schema,table_name);
  var ref_id_links = <dynamic, dynamic>{};
  for(var entry_52249 in ref_id_map.entries){
    var id_k = entry_52249.key;
    var k = entry_52249.value;
    if("String" == (data[id_k].runtimeType).toString()){
      ref_id_links[k] = <dynamic, dynamic>{data[id_k]:true};
    }
  };
  flatten_merge(table_map,data_obj,xtd.obj_assign_with(ref_links,ref_id_links,(obj, other) {
    return xtd.obj_assign(obj,other);
  }),rev_links);
  return <dynamic, dynamic>{
    "table_map":table_map,
    "data_obj":data_obj,
    "ref_obj":ref_obj,
    "rev_obj":rev_obj
  };
}

flatten_linked(schema, table_name, link_obj, link_id, acc, flatten_fn) {
  var link_fn = (e) {
    var ref = xtd.get_in(schema,<dynamic>[table_name,e,"ref"]);
    return <dynamic>[ref["ns"],ref["rval"]];
  };
  for(var entry_52250 in link_obj.entries){
    var e = entry_52250.key;
    var v = entry_52250.value;
    if((v.runtimeType).toString().startsWith("List") || (v.runtimeType).toString().startsWith("_GrowableList")){
      var value_52251 = Function.apply((link_fn as Function),<dynamic>[e]);
      var link_key = value_52251[0];
      var link_path = value_52251[1];
      var arr_52252 = xtd.arr_filter(v,(value) {
        return ("Map" == (value.runtimeType).toString()) || (value.runtimeType).toString().startsWith("_Map") || (value.runtimeType).toString().startsWith("LinkedMap");
      });
      for(var i52253 = 0; i52253 < arr_52252.length; ++i52253){
        var e = arr_52252[i52253];
        Function.apply((flatten_fn as Function),<dynamic>[
          schema,
          link_key,
          e,
          <dynamic, dynamic>{link_path:<dynamic>[link_id]},
          acc
        ]);
      };
    }
  };
  return acc;
}

flatten_obj(schema, table_name, obj, parent, acc) {
  var flattened = flatten_node(schema,table_name,obj,parent,acc);
  var data_obj = flattened["data_obj"];
  var ref_obj = flattened["ref_obj"];
  var rev_obj = flattened["rev_obj"];
  var table_map = flattened["table_map"];
  var link_id = data_obj["id"];
  flatten_linked(schema,table_name,rev_obj,link_id,acc,flatten_obj);
  flatten_linked(schema,table_name,ref_obj,link_id,acc,flatten_obj);
  return acc;
}

flatten(schema, table_name, data, parent) {
  var input = ((data.runtimeType).toString().startsWith("List") || (data.runtimeType).toString().startsWith("_GrowableList")) ? data : ((("Map" == (data.runtimeType).toString()) || (data.runtimeType).toString().startsWith("_Map") || (data.runtimeType).toString().startsWith("LinkedMap")) ? data : <dynamic>[]);
  var parent_obj = (("Map" == (parent.runtimeType).toString()) || (parent.runtimeType).toString().startsWith("_Map") || (parent.runtimeType).toString().startsWith("LinkedMap")) ? parent : <dynamic, dynamic>{};
  var acc = <dynamic, dynamic>{};
  if((input.runtimeType).toString().startsWith("List") || (input.runtimeType).toString().startsWith("_GrowableList")){
    var arr_52274 = input;
    for(var i52275 = 0; i52275 < arr_52274.length; ++i52275){
      var subdata = arr_52274[i52275];
      if(null != subdata){
        flatten_obj(schema,table_name,subdata,parent_obj,acc);
      }
    };
  }
  else{
    flatten_obj(schema,table_name,input,parent_obj,acc);
  }
  return acc;
}

flatten_bulk(schema, m) {
  var acc = <dynamic, dynamic>{};
  if((m.runtimeType).toString().startsWith("List") || (m.runtimeType).toString().startsWith("_GrowableList")){
    var arr_52296 = m;
    for(var i52297 = 0; i52297 < arr_52296.length; ++i52297){
      var e = arr_52296[i52297];
      var value_52318 = e;
      var table_name = value_52318[0];
      var arr = value_52318[1];
      var items = ((arr.runtimeType).toString().startsWith("List") || (arr.runtimeType).toString().startsWith("_GrowableList")) ? arr : <dynamic>[arr];
      var arr_52319 = items;
      for(var i52320 = 0; i52320 < arr_52319.length; ++i52320){
        var obj = arr_52319[i52320];
        if(null != obj){
          flatten_obj(schema,table_name,obj,<dynamic, dynamic>{},acc);
        }
      };
    };
  }
  else{
    for(var entry_52341 in m.entries){
      var table_name = entry_52341.key;
      var arr = entry_52341.value;
      var items = ((arr.runtimeType).toString().startsWith("List") || (arr.runtimeType).toString().startsWith("_GrowableList")) ? arr : <dynamic>[arr];
      var arr_52342 = items;
      for(var i52343 = 0; i52343 < arr_52342.length; ++i52343){
        var obj = arr_52342[i52343];
        if(null != obj){
          flatten_obj(schema,table_name,obj,<dynamic, dynamic>{},acc);
        }
      };
    };
  }
  return acc;
}

flatten_bulk_ids(schema, lookup, m) {
  var flat = flatten_bulk(schema,m);
  return xtd.arr_keep(sch.table_order(lookup),(table_name) {
    return flat.containsKey(table_name) ? <dynamic>[
      table_name,
      xtd.arr_sort(List<dynamic>.from(( flat[table_name] ).keys),(id) {
          return id;
        },(x, y) {
          return (("String" == (x.runtimeType).toString()) || ("String" == (y.runtimeType).toString())) ? ((x).toString().compareTo((y).toString()) < 0) : (x < y);
        })
    ] : null;
  });
}