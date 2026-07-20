import 'package:xtalk_lang/common-data.dart' as xtd;
import 'package:xtalk_lang/common-sort-by.dart' as xtsb;
import 'package:xtalk_db/text/base-graph.dart' as base_graph;
import 'package:xtalk_db/text/base-tree.dart' as base_tree;





check_in_clause(x, expr) {
  return xtd.arr_some(expr[0],(e) {
    return e == x;
  });
}

like_char_at(s, i) {
  if((i < 0) || (i >= s.length)){
    return "";
  }
  return s.substring(i - 0,i + 1);
}

check_like_clause(x, expr) {
  if(!("String" == (x.runtimeType).toString()) || !("String" == (expr.runtimeType).toString())){
    return false;
  }
  var slen = x.length;
  var plen = expr.length;
  var sidx = 0;
  var pidx = 0;
  var star_pidx = -1;
  var star_sidx = -1;
  while(sidx < slen){
    var sch = like_char_at(x,sidx);
    var pch = (pidx < plen) ? like_char_at(expr,pidx) : null;
    if(("\\" == pch) && ((pidx + 1) < plen) && (sch == like_char_at(expr,pidx + 1))){
      sidx = (sidx + 1);
      pidx = (pidx + 2);
    }
    else if(("\\" == pch) && (sch == "\\") && ((pidx + 1) == plen)){
      sidx = (sidx + 1);
      pidx = (pidx + 1);
    }
    else if("%" == pch){
      star_pidx = pidx;
      star_sidx = sidx;
      pidx = (pidx + 1);
    }
    else if(("_" == pch) || (sch == pch)){
      sidx = (sidx + 1);
      pidx = (pidx + 1);
    }
    else if(star_pidx < 0){
      return false;
    }
    else{
      star_sidx = (star_sidx + 1);
      sidx = star_sidx;
      pidx = (star_pidx + 1);
    }
  }
  while(pidx < plen){
    if("%" == like_char_at(expr,pidx)){
      pidx = (pidx + 1);
    }
    else{
      return false;
    }
  }
  return true;
}

var LINK_LOOKUP = <dynamic, dynamic>{"forward":"ref_links","reverse":"rev_links"};

check_ilike_clause(x, expr) {
  if(!("String" == (x.runtimeType).toString()) || !("String" == (expr.runtimeType).toString())){
    return false;
  }
  return check_like_clause((x).toLowerCase(),(expr).toLowerCase());
}

var PULL_CHECK = <dynamic, dynamic>{
  "not_in":(x, expr) {
    return !check_in_clause(x,expr);
  },
  "lt":(x, y) {
    return (("String" == (x.runtimeType).toString()) || ("String" == (y.runtimeType).toString())) ? ((x).toString().compareTo((y).toString()) < 0) : (x < y);
  },
  "neq":(x, expr) {
    return x != expr;
  },
  "eq":(x, y) {
    return x == y;
  },
  "is":(x, expr) {
    return x == expr;
  },
  "like":check_like_clause,
  "gt":(x, y) {
    return (("String" == (x.runtimeType).toString()) || ("String" == (y.runtimeType).toString())) ? ((x).toString().compareTo((y).toString()) > 0) : (x > y);
  },
  "gte":(x, y) {
    return (("String" == (x.runtimeType).toString()) || ("String" == (y.runtimeType).toString())) ? ((x).toString().compareTo((y).toString()) >= 0) : (x >= y);
  },
  "is_not_null":(value) {
    return null != value;
  },
  "is_null":(value) {
    return null == value;
  },
  "between":(x, start_expr, _and, end_expr) {
    return (() {
      var dart_and__51892 = x >= start_expr;
      return ((null != dart_and__51892) && (false != dart_and__51892)) ? ((_and == "and") ? (x <= end_expr) : (x <= _and)) : dart_and__51892;
    })();
  },
  "not_like":(x, expr) {
    return !check_like_clause(x,expr);
  },
  "lte":(x, y) {
    return (("String" == (x.runtimeType).toString()) || ("String" == (y.runtimeType).toString())) ? ((x).toString().compareTo((y).toString()) <= 0) : (x <= y);
  },
  "ilike":check_ilike_clause,
  "in":check_in_clause
};

custom_params(custom) {
  var out = <dynamic, dynamic>{
    "count":false,
    "order_by":null,
    "order_sort":null,
    "limit":null,
    "offset":null
  };
  var arr_51893 = custom ?? <dynamic>[];
  for(var i51894 = 0; i51894 < arr_51893.length; ++i51894){
    var entry = arr_51893[i51894];
    if(entry["::"] == "sql/count"){
      out["count"] = true;
    }
    else if(entry["::"] == "sql/keyword"){
      var name = entry["name"];
      if(name == "ORDER BY"){
        var tuple = (entry["args"] ?? <dynamic>[])[0];
        out["order_by"] = xtd.arr_map(tuple["args"] ?? <dynamic>[],(arg) {
          return arg["name"];
        });
      }
      else if((name == "ASC") || (name == "DESC")){
        out["order_sort"] = (name).toLowerCase();
      }
      else if(name == "LIMIT"){
        out["limit"] = ((entry["args"] ?? <dynamic>[])[0])["name"];
      }
      else if(name == "OFFSET"){
        out["offset"] = ((entry["args"] ?? <dynamic>[])[0])["name"];
      }
    }
  };
  return out;
}

check_clause_value(record, key, clause) {
  if((() {
    var dart_truthy__51891 = xt.lang.common_string.ends_withp(key,"_id");
    return (null != dart_truthy__51891) && (false != dart_truthy__51891);
  })()){
    var base_key = key.substring(0 - 0,key.length - 3);
    return clause ==     (List<dynamic>.from(( xtd.get_in(record,<dynamic>["ref_links",base_key]) ?? <dynamic, dynamic>{} ).keys))[0];
  }
  else{
    return clause == xtd.get_in(record,<dynamic>["data",key]);
  }
}

check_clause_function(record, link_type, key, pred, exprs) {
  if(null == pred){
    return false;
  }
  else if(null == link_type){
    return Function.apply(
      pred,
      <dynamic>[xtd.get_in(record,<dynamic>["data",key]),...exprs]
    );
  }
  else if(link_type == "forward"){
    if(pred == PULL_CHECK["is_null"]){
      return pred(xtd.get_in(record,<dynamic>["ref_links",key]));
    }
    else{
      return xtd.arr_some(List<dynamic>.from(( xtd.get_in(record,<dynamic>["ref_links",key]) ?? <dynamic, dynamic>{} ).keys),(v) {
        return Function.apply(pred,<dynamic>[v,...exprs]);
      });
    }
  }
  else if(link_type == "reverse"){
    return xtd.arr_some(List<dynamic>.from(( xtd.get_in(record,<dynamic>["rev_links",key]) ?? <dynamic, dynamic>{} ).keys),(v) {
      return Function.apply(pred,<dynamic>[v,...exprs]);
    });
  }
}

where_clause(rows, schema, table_name, record, where_fn, key, clause) {
  var link_type = xtd.get_in(schema,<dynamic>[table_name,key,"ref","type"]);
  if((clause.runtimeType).toString().startsWith("List") || (clause.runtimeType).toString().startsWith("_GrowableList")){
    var tag = clause[0];
    var exprs = <dynamic>[...clause];
    exprs.removeAt(0);
    return check_clause_function(record,link_type,key,PULL_CHECK[tag],exprs);
  }
  else if((clause.runtimeType).toString().contains("Function") || (clause.runtimeType).toString().contains("=>") || (clause).toString().startsWith("Closure")){
    return check_clause_function(record,link_type,key,clause,<dynamic>[]);
  }
  else if(("Map" == (clause.runtimeType).toString()) || (clause.runtimeType).toString().startsWith("_Map") || (clause.runtimeType).toString().startsWith("LinkedMap")){
    var ref = xtd.get_in(schema,<dynamic>[table_name,key,"ref"]);
    var link_table = ref["ns"];
    var link_map_key = LINK_LOOKUP[ref["type"]];
    var ids = List<dynamic>.from(( xtd.get_in(record,<dynamic>[link_map_key,key]) ?? <dynamic, dynamic>{} ).keys);
    var entries = List<dynamic>.from(( xtd.obj_pick(rows[link_table] ?? <dynamic, dynamic>{},ids) ).values);
    var found = xtd.arr_filter(entries,(entry) {
      return Function.apply(
        (where_fn as Function),
        <dynamic>[rows,schema,link_table,clause,entry["record"]]
      );
    });
    return 0 < found.length;
  }
  else{
    return check_clause_value(record,key,clause);
  }
}

where(rows, schema, table_name, where, record) {
  var clause_fn = (pair) {
    var value_51925 = pair;
    var k = value_51925[0];
    var clause = value_51925[1];
    return where_clause(rows,schema,table_name,record,where,k,clause);
  };
  if((where.runtimeType).toString().contains("Function") || (where.runtimeType).toString().contains("=>") || (where).toString().startsWith("Closure")){
    return where(record,table_name);
  }
  else if((() {
    var dart_truthy__51889 = xtd.is_emptyp(where);
    return (null != dart_truthy__51889) && (false != dart_truthy__51889);
  })()){
    return true;
  }
  else if((where.runtimeType).toString().startsWith("List") || (where.runtimeType).toString().startsWith("_GrowableList")){
    return xtd.arr_some(where,(or_clause) {
      return where(rows,schema,table_name,or_clause,record);
    });
  }
  else{
    return xtd.arr_every(List<List<dynamic>>.from(( xtd.obj_filter(where,(value) {
      return null != value;
    }) ).entries.map((entry) => [entry.key, entry.value])),clause_fn);
  }
}

data_field(record, key) {
  if((() {
    var dart_truthy__51890 = xt.lang.common_string.ends_withp(key,"_id");
    return (null != dart_truthy__51890) && (false != dart_truthy__51890);
  })()){
    var base_key = key.substring(0 - 0,key.length - 3);
    return     (List<dynamic>.from(( xtd.get_in(record,<dynamic>["ref_links",base_key]) ?? <dynamic, dynamic>{} ).keys))[0];
  }
  else{
    return xtd.get_in(record,<dynamic>["data",key]);
  }
}

project_record(rows, schema, tree, record, opts, pull_entries_fn) {
  var params = xtd.second(tree);
  var data = params["data"] ?? <dynamic>[];
  var links = params["links"] ?? <dynamic>[];
  var out = <dynamic, dynamic>{};
  var arr_51930 = data;
  for(var i51931 = 0; i51931 < arr_51930.length; ++i51931){
    var key = arr_51930[i51931];
    out[key] = data_field(record,key);
  };
  var arr_51952 = links;
  for(var i51953 = 0; i51953 < arr_51952.length; ++i51953){
    var link = arr_51952[i51953];
    var link_name = link[0];
    var link_type = xtd.second(link);
    var child_tree = xtd.nth(link,2);
    var link_map_key = LINK_LOOKUP[link_type];
    var ids = List<dynamic>.from(( xtd.get_in(record,<dynamic>[link_map_key,link_name]) ?? <dynamic, dynamic>{} ).keys);
    var child_table = child_tree[0];
    var child_entries = List<dynamic>.from(( xtd.obj_pick(rows[child_table] ?? <dynamic, dynamic>{},ids) ).values);
    var child_output = Function.apply(
      (pull_entries_fn as Function),
      <dynamic>[rows,schema,child_tree,child_entries,opts]
    );
    out[link_name] = ((((child_output.runtimeType).toString().startsWith("List") || (child_output.runtimeType).toString().startsWith("_GrowableList")) && (0 < child_output.length)) ? child_output : null);
  };
  return out;
}

apply_custom(out, custom) {
  if(null != custom["order_by"]){
    out = xtsb.sort_by(out,custom["order_by"]);
  }
  if(custom["order_sort"] == "desc"){
    out = xtd.arr_reverse(out);
  }
  if((null != custom["offset"]) || (null != custom["limit"])){
    var sidx = custom["offset"] ?? 0;
    var total = out.length;
    var eidx = sidx + (custom["limit"] ?? (total - sidx));
    eidx = ((eidx < total) ? eidx : total);
    out = out.sublist(sidx - 0,eidx);
  }
  return out;
}

pull_entries(rows, schema, tree, entries, opts) {
  var table_name = tree[0];
  var params = xtd.second(tree);
  var where_clause = params["where"];
  var custom = custom_params(params["custom"]);
  var matched = xtd.arr_filter(entries ?? <dynamic>[],(entry) {
    return where(rows,schema,table_name,where_clause,entry["record"]);
  });
  if((() {
    var dart_truthy__51888 = custom["count"];
    return (null != dart_truthy__51888) && (false != dart_truthy__51888);
  })()){
    return matched.length;
  }
  var out = xtd.arr_map(matched,(entry) {
    return project_record(rows,schema,tree,entry["record"],opts,pull_entries);
  });
  return apply_custom(out,custom);
}

pull(rows, schema, tree, opts) {
  tree = base_graph.select_tree(schema,tree,opts);
  var table_name = tree[0];
  var entries = List<dynamic>.from(( rows[table_name] ?? <dynamic, dynamic>{} ).values);
  return pull_entries(rows,schema,tree,entries,opts);
}

view_select(rows, schema, entry, args, opts) {
  var tree = base_tree.plan_select(schema,entry,args,opts);
  return pull(rows,schema,tree,opts);
}

view_count(rows, schema, entry, args, opts) {
  var tree = base_tree.plan_count(schema,entry,args,opts);
  return pull(rows,schema,tree,opts);
}

view_return(rows, schema, entry, id, args, opts) {
  var tree = base_tree.plan_return(schema,entry,id,args,opts);
  return pull(rows,schema,tree,opts);
}

view_return_bulk(rows, schema, entry, ids, args, opts) {
  var tree = base_tree.plan_return_bulk(schema,entry,ids,args,opts);
  return pull(rows,schema,tree,opts);
}

view_combined(rows, schema, sel_entry, sel_args, ret_entry, ret_args, ret_omit, opts) {
  var tree = base_tree.plan_combined(schema,sel_entry,sel_args,ret_entry,ret_args,ret_omit,opts,false);
  return pull(rows,schema,tree,opts);
}