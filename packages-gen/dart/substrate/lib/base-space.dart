import 'package:xtalk_lang/common-data.dart' as xtd;

space(space_id, opts) {
  opts = (opts ?? <dynamic, dynamic>{});
  return <dynamic, dynamic>{
    "id":space_id,
    "state":opts["state"] ?? <dynamic, dynamic>{},
    "meta":opts["meta"] ?? <dynamic, dynamic>{}
  };
}

get_space(node, space_id) {
  return (node["spaces"])[space_id ?? "__NODE__"];
}

create_space(node, space_id, opts) {
  var entry = space(space_id ?? "__NODE__",opts);
  node["spaces"][entry["id"]] = entry;
  return entry;
}

ensure_space(node, space_id, opts) {
  var sid = space_id ?? "__NODE__";
  var entry = get_space(node,sid);
  if(null == entry){
    entry = create_space(node,sid,opts);
  }
  return entry;
}

remove_space(node, space_id) {
  var sid = space_id ?? "__NODE__";
  var spaces = node["spaces"];
  var entry = spaces[sid];
  spaces.remove(sid);
  return entry;
}

list_spaces(node) {
  return xtd.arr_sort(List<dynamic>.from(( node["spaces"] ).keys),(x) {
    return x;
  },(x, y) {
    return (x).toString().compareTo((y).toString()) < 0;
  });
}

get_space_state(node, space_id) {
  var entry = ensure_space(node,space_id,null);
  return entry["state"];
}

set_space_state(node, space_id, state) {
  var entry = ensure_space(node,space_id,null);
  entry["state"] = state;
  return state;
}

update_space_state(node, space_id, updater) {
  var entry = ensure_space(node,space_id,null);
  var curr = entry["state"];
  var next = updater(curr,entry,node);
  entry["state"] = next;
  return next;
}