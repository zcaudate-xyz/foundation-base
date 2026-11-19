import * as xtd from '../lang/common-data.mjs'

export function space(space_id,opts){
  opts = (opts || {});
  return {
    "id":space_id,
    "state":opts["state"] || {},
    "meta":opts["meta"] || {}
  };
}

export function get_space(node,space_id){
  return (node["spaces"])[space_id || "__NODE__"];
}

export function create_space(node,space_id,opts){
  let entry = space(space_id || "__NODE__",opts);
  node["spaces"][entry["id"]] = entry;
  return entry;
}

export function ensure_space(node,space_id,opts){
  let sid = space_id || "__NODE__";
  let entry = get_space(node,sid);
  if(null == entry){
    entry = create_space(node,sid,opts);
  }
  return entry;
}

export function remove_space(node,space_id){
  let sid = space_id || "__NODE__";
  let {spaces} = node;
  let entry = spaces[sid];
  delete(spaces[sid]);
  return entry;
}

export function list_spaces(node){
  return xtd.arr_sort(Object.keys(node["spaces"]),function (x){
    return x;
  },function (x,y){
    return 0 > x.localeCompare(y);
  });
}

export function get_space_state(node,space_id){
  let entry = ensure_space(node,space_id,null);
  return entry["state"];
}

export function set_space_state(node,space_id,state){
  let entry = ensure_space(node,space_id,null);
  entry["state"] = state;
  return state;
}

export function update_space_state(node,space_id,updater){
  let entry = ensure_space(node,space_id,null);
  let curr = entry["state"];
  let next = updater(curr,entry,node);
  entry["state"] = next;
  return next;
}