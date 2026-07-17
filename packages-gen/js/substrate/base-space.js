const xtd = require("@xtalk/lang/common-data.js")

function space(space_id,opts){
  opts = (opts || {});
  return {
    "id":space_id,
    "state":opts["state"] || {},
    "meta":opts["meta"] || {}
  };
}

function get_space(node,space_id){
  return (node["spaces"])[space_id || "__NODE__"];
}

function create_space(node,space_id,opts){
  let entry = space(space_id || "__NODE__",opts);
  node["spaces"][entry["id"]] = entry;
  return entry;
}

function ensure_space(node,space_id,opts){
  let sid = space_id || "__NODE__";
  let entry = get_space(node,sid);
  if(null == entry){
    entry = create_space(node,sid,opts);
  }
  return entry;
}

function remove_space(node,space_id){
  let sid = space_id || "__NODE__";
  let spaces = node["spaces"];
  let entry = spaces[sid];
  delete(spaces[sid]);
  return entry;
}

function list_spaces(node){
  return xtd.arr_sort(Object.keys(node["spaces"]),function (x){
    return x;
  },function (x,y){
    return 0 > x.localeCompare(y);
  });
}

function get_space_state(node,space_id){
  let entry = ensure_space(node,space_id,null);
  return entry["state"];
}

function set_space_state(node,space_id,state){
  let entry = ensure_space(node,space_id,null);
  entry["state"] = state;
  return state;
}

function update_space_state(node,space_id,updater){
  let entry = ensure_space(node,space_id,null);
  let curr = entry["state"];
  let next = updater(curr,entry,node);
  entry["state"] = next;
  return next;
}

module.exports = {
  ["space"]:space,
  ["get_space"]:get_space,
  ["create_space"]:create_space,
  ["ensure_space"]:ensure_space,
  ["remove_space"]:remove_space,
  ["list_spaces"]:list_spaces,
  ["get_space_state"]:get_space_state,
  ["set_space_state"]:set_space_state,
  ["update_space_state"]:update_space_state
}