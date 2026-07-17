const xtd = require("@xtalk/lang/common-data.js")

function create(opts){
  return {
    "items":opts["items"] || [],
    "query":opts["query"] || {},
    "page":opts["page"] || 0,
    "page_size":opts["page_size"] || 25,
    "total":opts["total"] || 0,
    "selected":{},
    "pending":false,
    "error":null
  };
}

function set_itemsf(state,items,total){
  state["items"] = (items || []);
  state["total"] = (total || (items || []).length);
  state["pending"] = false;
  state["error"] = null;
  return state;
}

function set_queryf(state,path,value){
  let query = xtd.clone_nested(state["query"] || {});
  xtd.set_in(query,path,value);
  state["query"] = query;
  state["page"] = 0;
  return state;
}

function set_pagef(state,page){
  state["page"] = (page || 0);
  return state;
}

function selectf(state,id,selected){
  if(true == selected){
    state["selected"][id] = true;
  }
  else{
    delete(state["selected"][id]);
  }
  return state;
}

function selected_ids(state){
  return Object.keys(state["selected"]);
}

module.exports = {
  ["create"]:create,
  ["set_itemsf"]:set_itemsf,
  ["set_queryf"]:set_queryf,
  ["set_pagef"]:set_pagef,
  ["selectf"]:selectf,
  ["selected_ids"]:selected_ids
}