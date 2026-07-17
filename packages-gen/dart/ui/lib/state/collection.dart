import 'package:xtalk_lang/common-data.dart' as xtd;

create(opts) {
  return <dynamic, dynamic>{
    "items":opts["items"] ?? <dynamic>[],
    "query":opts["query"] ?? <dynamic, dynamic>{},
    "page":opts["page"] ?? 0,
    "page_size":opts["page_size"] ?? 25,
    "total":opts["total"] ?? 0,
    "selected":<dynamic, dynamic>{},
    "pending":false,
    "error":null
  };
}

set_itemsf(state, items, total) {
  state["items"] = (items ?? <dynamic>[]);
  state["total"] = (total ?? (items ?? <dynamic>[]).length);
  state["pending"] = false;
  state["error"] = null;
  return state;
}

set_queryf(state, path, value) {
  var query = xtd.clone_nested(state["query"] ?? <dynamic, dynamic>{});
  xtd.set_in(query,path,value);
  state["query"] = query;
  state["page"] = 0;
  return state;
}

set_pagef(state, page) {
  state["page"] = (page ?? 0);
  return state;
}

selectf(state, id, selected) {
  if(true == selected){
    state["selected"][id] = true;
  }
  else{
    state["selected"].remove(id);
  }
  return state;
}

selected_ids(state) {
  return List<dynamic>.from(( state["selected"] ).keys);
}