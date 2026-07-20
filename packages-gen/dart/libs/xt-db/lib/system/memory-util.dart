import 'package:xtalk_lang/common-tree.dart' as xtt;
import 'package:xtalk_lang/common-data.dart' as xtd;
import 'package:xtalk_db/text/base-flatten.dart' as f;
import 'dart:convert';




has_entry(rows, table_key, id) {
  return null != xtd.get_in(rows,<dynamic>[table_key,id]);
}

get_entry(rows, table_key, id) {
  return xtd.get_in(rows,<dynamic>[table_key,id]);
}

swap_if_entry(rows, table_key, id, f) {
  var entry = xtd.get_in(rows,<dynamic>[table_key,id]);
  if(null != entry){
    var record = entry["record"];
    f(record);
    var new_entry = <dynamic, dynamic>{"t":DateTime.now().millisecondsSinceEpoch,"record":record};
    xtd.set_in(rows,<dynamic>[table_key,id],new_entry);
    return new_entry;
  }
  return entry;
}

merge_single(rows, table_key, id, new_record, new_fn) {
  var incoming_record = new_record ?? <dynamic, dynamic>{};
  var entry = get_entry(rows,table_key,id);
  if(!(("Map" == (entry.runtimeType).toString()) || (entry.runtimeType).toString().startsWith("_Map") || (entry.runtimeType).toString().startsWith("LinkedMap"))){
    entry = <dynamic, dynamic>{
      "record":<dynamic, dynamic>{
            "id":id,
            "data":<dynamic, dynamic>{},
            "ref_links":<dynamic, dynamic>{},
            "rev_links":<dynamic, dynamic>{}
          }
    };
  }
  var record = entry["record"];
  var data = incoming_record["data"] ?? <dynamic, dynamic>{};
  var ref_links = incoming_record["ref_links"] ?? <dynamic, dynamic>{};
  var rev_links = incoming_record["rev_links"] ?? <dynamic, dynamic>{};
  xtd.swap_key(record,"data",(obj, other) {
    return xtd.obj_assign(obj,other);
  },<dynamic>[data]);
  xtd.swap_key(record,"ref_links",xtd.obj_assign_with,<dynamic>[
    ref_links,
    (obj, other) {
      return xtd.obj_assign(obj,other);
    }
  ]);
  xtd.swap_key(record,"rev_links",xtd.obj_assign_with,<dynamic>[
    rev_links,
    (obj, other) {
      return xtd.obj_assign(obj,other);
    }
  ]);
  var new_entry = Function.apply((new_fn as Function),<dynamic>[
    <dynamic, dynamic>{"t":DateTime.now().millisecondsSinceEpoch,"record":record}
  ]);
  xtd.set_in(rows,<dynamic>[table_key,id],new_entry);
  return new_entry;
}

merge_bulk(rows, fdata, new_fn) {
  var out = <dynamic, dynamic>{};
  for(var entry_51628 in fdata.entries){
    var table_key = entry_51628.key;
    var m = entry_51628.value;
    var entries = m ?? <dynamic, dynamic>{};
    for(var entry_51629 in entries.entries){
      var id = entry_51629.key;
      var new_record = entry_51629.value;
      xtd.set_in(out,<dynamic>[table_key,id],merge_single(rows,table_key,id,new_record,new_fn ?? ((x) {
        return x;
      })));
    };
  };
  return out;
}

get_ids(rows, table_key) {
  return List<dynamic>.from(( rows[table_key] ?? <dynamic, dynamic>{} ).keys);
}

all_records(rows, table_key) {
  if(null == table_key){
    return xtd.obj_filter(xtd.arr_juxt(List<dynamic>.from(( rows ).keys),(x) {
      return x;
    },(k) {
      return all_records(rows,k);
    }),(e) {
      return (null == e) || (0 < List<dynamic>.from(( e ).keys).length);
    });
  }
  else{
    return xtd.obj_map(rows[table_key],(e) {
      return e["record"];
    });
  }
}

get_changed_single(rows, table_key, id, record) {
  var curr = get_entry(rows,table_key,id);
  if(null == curr){
    return record;
  }
  else{
    return xtt.tree_diff_nested(curr["record"],record);
  }
}

has_changed_single(rows, table_key, id, record) {
  var changed = get_changed_single(rows,table_key,id,record);
  return 0 < List<dynamic>.from(( changed ).keys).length;
}

get_link_attrs(schema, table_key, field) {
  var attr = xtd.get_in(schema,<dynamic>[table_key,field,"ref"]);
  if(null == attr){
    throw "Not a valid link type: " + jsonEncode(<dynamic>[table_key,field]);
  }
  var link_ns = attr["ns"];
  var rval = attr["rval"];
  var link_type = attr["type"];
  var value_51642 =   (<dynamic, dynamic>{
      "reverse":<dynamic>["rev_links","ref_links"],
      "forward":<dynamic>["ref_links","rev_links"]
    })[link_type];
  var table_link = value_51642[0];
  var inverse_link = value_51642[1];
  return <dynamic, dynamic>{
    "table_key":table_key,
    "table_link":table_link,
    "table_field":field,
    "inverse_key":link_ns,
    "inverse_link":inverse_link,
    "inverse_field":rval
  };
}

remove_single_link_entry(rows, table_key, id, table_link, table_field, link_id, link_cb) {
  var remove_fn = (record) {
    var link = record[table_link];
    var lrec = link[table_field];
    if((null != lrec) && lrec.containsKey(link_id)){
      lrec.remove(link_id);
      if(0 == List<dynamic>.from(( lrec ).keys).length){
        link.remove(table_field);
      }
      if(null != link_cb){
        link_cb(link_id);
      }
    }
  };
  return swap_if_entry(rows,table_key,id,remove_fn);
}

remove_single_link(rows, schema, table_key, id, field, link_id) {
  var attrs = get_link_attrs(schema,table_key,field);
  var inverse_field = attrs["inverse_field"];
  var inverse_key = attrs["inverse_key"];
  var inverse_link = attrs["inverse_link"];
  var table_field = attrs["table_field"];
  var table_link = attrs["table_link"];
  var l_arr = <dynamic>[false,false];
  var t_has_fn = (_) {
    l_arr[0] = true;
  };
  remove_single_link_entry(rows,table_key,id,table_link,table_field,link_id,t_has_fn);
  var i_has_fn = (_) {
    l_arr[1] = true;
  };
  remove_single_link_entry(rows,inverse_key,link_id,inverse_link,inverse_field,id,i_has_fn);
  return l_arr;
}

remove_single(rows, schema, table_key, id) {
  var entry = get_entry(rows,table_key,id);
  if(null != entry){
    var rec = entry["record"];
    var ref_links = rec["ref_links"];
    var rev_links = rec["rev_links"];
    var links = xtd.arr_assign(
      List<List<dynamic>>.from(( ref_links ).entries.map((entry) => [entry.key, entry.value])),
      List<List<dynamic>>.from(( rev_links ).entries.map((entry) => [entry.key, entry.value]))
    );
    var arr_51651 = links;
    for(var i51652 = 0; i51652 < arr_51651.length; ++i51652){
      var pair = arr_51651[i51652];
      var value_51673 = pair;
      var field = value_51673[0];
      var m = value_51673[1];
      var attrs = get_link_attrs(schema,table_key,field);
      var inverse_field = attrs["inverse_field"];
      var inverse_key = attrs["inverse_key"];
      var inverse_link = attrs["inverse_link"];
      var arr_51674 = List<dynamic>.from(( m ).keys);
      for(var i51675 = 0; i51675 < arr_51674.length; ++i51675){
        var link_id = arr_51674[i51675];
        remove_single_link_entry(rows,inverse_key,link_id,inverse_link,inverse_field,id,null);
      };
    };
    rows[table_key].remove(id);
    return <dynamic>[entry];
  }
}

remove_bulk(rows, schema, table_key, ids) {
  return xtd.arr_mapcat(xtd.arr_keep(ids,(id) {
    return remove_single(rows,schema,table_key,id);
  }),(x) {
    return x;
  });
}

add_single_link_entry(rows, table_key, id, table_link, table_field, link_id, link_cb, inverse_key, inverse_field) {
  var add_fn = (record) {
    var link = record[table_link];
    var lrec = link[table_field];
    if(null == lrec){
      lrec = <dynamic, dynamic>{};
      link[table_field] = lrec;
      lrec[link_id] = true;
    }
    else if(table_link == "rev_links"){
      lrec[link_id] = true;
    }
    else{
      var prev_ids = List<dynamic>.from(( lrec ).keys);
      var arr_51700 = prev_ids;
      for(var i51701 = 0; i51701 < arr_51700.length; ++i51701){
        var prev_id = arr_51700[i51701];
        remove_single_link_entry(rows,inverse_key,prev_id,"rev_links",inverse_field,id,null);
      };
      link[table_field] = <dynamic, dynamic>{link_id:true};
    }
    if(null != link_cb){
      link_cb(link_id);
    }
  };
  return swap_if_entry(rows,table_key,id,add_fn);
}

add_single_link(rows, schema, table_key, id, field, link_id) {
  var attrs = get_link_attrs(schema,table_key,field);
  var inverse_field = attrs["inverse_field"];
  var inverse_key = attrs["inverse_key"];
  var inverse_link = attrs["inverse_link"];
  var table_field = attrs["table_field"];
  var table_link = attrs["table_link"];
  var l_arr = <dynamic>[false,false];
  var t_has_fn = (_) {
    l_arr[0] = true;
  };
  var t_entry_fn = () {
    return add_single_link_entry(
      rows,
      table_key,
      id,
      table_link,
      table_field,
      link_id,
      t_has_fn,
      inverse_key,
      inverse_field
    );
  };
  var i_has_fn = (_) {
    l_arr[1] = true;
  };
  var i_entry_fn = () {
    return add_single_link_entry(
      rows,
      inverse_key,
      link_id,
      inverse_link,
      inverse_field,
      id,
      i_has_fn,
      table_key,
      field
    );
  };
  if(table_link == "ref_links"){
    Function.apply((t_entry_fn as Function),<dynamic>[]);
    Function.apply((i_entry_fn as Function),<dynamic>[]);
  }
  else if(table_link == "rev_links"){
    Function.apply((i_entry_fn as Function),<dynamic>[]);
    Function.apply((t_entry_fn as Function),<dynamic>[]);
  }
  return l_arr;
}

add_bulk_links(rows, schema, flat) {
  var out = <dynamic>[];
  var arr_51722 = List<dynamic>.from(( flat ).keys);
  for(var i51723 = 0; i51723 < arr_51722.length; ++i51723){
    var table_key = arr_51722[i51723];
    var bulk = flat[table_key];
    var arr_51746 = List<dynamic>.from(( bulk ).keys);
    for(var i51747 = 0; i51747 < arr_51746.length; ++i51747){
      var row_id = arr_51746[i51747];
      var record = bulk[row_id];
      var ref_links = record["ref_links"];
      var rev_links = record["rev_links"];
      var arr_51770 = List<dynamic>.from(( ref_links ).keys);
      for(var i51771 = 0; i51771 < arr_51770.length; ++i51771){
        var field = arr_51770[i51771];
        var links = ref_links[field];
        var arr_51794 = List<dynamic>.from(( links ).keys);
        for(var i51795 = 0; i51795 < arr_51794.length; ++i51795){
          var link_id = arr_51794[i51795];
          out.add(
            <dynamic, dynamic>{"table":table_key,"id":row_id,"field":field,"link_id":link_id}
          );
        };
      };
      var arr_51818 = List<dynamic>.from(( rev_links ).keys);
      for(var i51819 = 0; i51819 < arr_51818.length; ++i51819){
        var field = arr_51818[i51819];
        var links = rev_links[field];
        var arr_51842 = List<dynamic>.from(( links ).keys);
        for(var i51843 = 0; i51843 < arr_51842.length; ++i51843){
          var link_id = arr_51842[i51843];
          out.add(
            <dynamic, dynamic>{"table":table_key,"id":row_id,"field":field,"link_id":link_id}
          );
        };
      };
    };
  };
  var arr_51866 = out;
  for(var i51867 = 0; i51867 < arr_51866.length; ++i51867){
    var link_spec = arr_51866[i51867];
    add_single_link(
      rows,
      schema,
      link_spec["table"],
      link_spec["id"],
      link_spec["field"],
      link_spec["link_id"]
    );
  };
  return out;
}

add_bulk(rows, schema, data) {
  var flat = f.flatten_bulk(schema,data);
  merge_bulk(rows,flat,null);
  return add_bulk_links(rows,schema,flat);
}