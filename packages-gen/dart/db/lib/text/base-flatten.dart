import 'package:xtalk_lang/common-data.dart' as xtd;

import 'package:xtalk_db/text/base-schema.dart' as sch;

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
  for(var entry_42581 in ref_id_map.entries){
    var id_k = entry_42581.key;
    var k = entry_42581.value;
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
  for(var entry_42582 in link_obj.entries){
    var e = entry_42582.key;
    var v = entry_42582.value;
    if((v.runtimeType).toString().startsWith("List") || (v.runtimeType).toString().startsWith("_GrowableList")){
      var value_42583 = Function.apply((link_fn as Function),<dynamic>[e]);
      var link_key = value_42583[0];
      var link_path = value_42583[1];
      var arr_42584 = xtd.arr_filter(v,(value) {
        return ("Map" == (value.runtimeType).toString()) || (value.runtimeType).toString().startsWith("_Map") || (value.runtimeType).toString().startsWith("LinkedMap");
      });
      for(var i42585 = 0; i42585 < arr_42584.length; ++i42585){
        var e = arr_42584[i42585];
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
    var arr_42606 = input;
    for(var i42607 = 0; i42607 < arr_42606.length; ++i42607){
      var subdata = arr_42606[i42607];
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
    var arr_42628 = m;
    for(var i42629 = 0; i42629 < arr_42628.length; ++i42629){
      var e = arr_42628[i42629];
      var value_42650 = e;
      var table_name = value_42650[0];
      var arr = value_42650[1];
      var items = ((arr.runtimeType).toString().startsWith("List") || (arr.runtimeType).toString().startsWith("_GrowableList")) ? arr : <dynamic>[arr];
      var arr_42651 = items;
      for(var i42652 = 0; i42652 < arr_42651.length; ++i42652){
        var obj = arr_42651[i42652];
        if(null != obj){
          flatten_obj(schema,table_name,obj,<dynamic, dynamic>{},acc);
        }
      };
    };
  }
  else{
    for(var entry_42673 in m.entries){
      var table_name = entry_42673.key;
      var arr = entry_42673.value;
      var items = ((arr.runtimeType).toString().startsWith("List") || (arr.runtimeType).toString().startsWith("_GrowableList")) ? arr : <dynamic>[arr];
      var arr_42674 = items;
      for(var i42675 = 0; i42675 < arr_42674.length; ++i42675){
        var obj = arr_42674[i42675];
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