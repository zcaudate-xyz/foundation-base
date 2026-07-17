const xtd = require("@xtalk/lang/common-data.js")

const sch = require("@xtalk/db/text/base-schema.js")

function flatten_get_links(obj){
  let link_fn = function (e){
    if("string" == (typeof e)){
      return [e,true];
    }
    else{
      if(null == e){
        throw "Invalid link - " + JSON.stringify(obj);
      }
      else{
        return [e["id"],true];
      }
    }
  };
  return xtd.obj_keep(obj,function (v){
    return Array.isArray(v) ? xtd.obj_from_pairs(v.map(link_fn)) : null;
  });
}

function flatten_merge(table_map,data_obj,ref_links,rev_links){
  let {id} = data_obj;
  let rec = table_map[id];
  if(!((null != rec) && ("object" == (typeof rec)) && !Array.isArray(rec))){
    rec = {"id":id,"data":{},"ref_links":{},"rev_links":{}};
    table_map[id] = rec;
  }
  xtd.swap_key(rec,"data",xtd.obj_assign,[data_obj]);
  xtd.swap_key(rec,"ref_links",xtd.obj_assign_with,[
    ref_links,
    function (obj,other){
      return Object.assign(obj,other);
    }
  ]);
  xtd.swap_key(rec,"rev_links",xtd.obj_assign_with,[
    rev_links,
    function (obj,other){
      return Object.assign(obj,other);
    }
  ]);
  return table_map;
}

function flatten_node(schema,table_name,data,parent,acc){
  data = Object.assign(data,xtd.clone_nested(parent));
  let table_map = acc[table_name];
  if(!((null != table_map) && ("object" == (typeof table_map)) && !Array.isArray(table_map))){
    table_map = {};
    acc[table_name] = table_map;
  }
  let data_obj = xtd.obj_pick(data,sch.data_keys(schema,table_name));
  let obj_fn = function (v){
    return ((null != v) && ("object" == (typeof v)) && !Array.isArray(v)) ? [v] : v;
  };
  let rev_obj = xtd.obj_keep(xtd.obj_pick(data,sch.rev_keys(schema,table_name)),obj_fn);
  let rev_links = flatten_get_links(rev_obj);
  let ref_obj = xtd.obj_keep(xtd.obj_pick(data,sch.ref_keys(schema,table_name)),obj_fn);
  let ref_links = flatten_get_links(ref_obj);
  let ref_id_map = sch.ref_id_keys(schema,table_name);
  let ref_id_links = {};
  for(let [id_k,k] of Object.entries(ref_id_map)){
    if("string" == (typeof data[id_k])){
      ref_id_links[k] = {[data[id_k]]:true};
    }
  };
  flatten_merge(table_map,data_obj,xtd.obj_assign_with(ref_links,ref_id_links,function (obj,other){
    return Object.assign(obj,other);
  }),rev_links);
  return {
    "table_map":table_map,
    "data_obj":data_obj,
    "ref_obj":ref_obj,
    "rev_obj":rev_obj
  };
}

function flatten_linked(schema,table_name,link_obj,link_id,acc,flatten_fn){
  let link_fn = function (e){
    let ref = xtd.get_in(schema,[table_name,e,"ref"]);
    return [ref["ns"],ref["rval"]];
  };
  for(let [e,v] of Object.entries(link_obj)){
    if(Array.isArray(v)){
      let [link_key,link_path] = link_fn(e);
      for(let e of v.filter(function (value){
        return (null != value) && ("object" == (typeof value)) && !Array.isArray(value);
      })){
        flatten_fn(schema,link_key,e,{[link_path]:[link_id]},acc);
      };
    }
  };
  return acc;
}

function flatten_obj(schema,table_name,obj,parent,acc){
  let flattened = flatten_node(schema,table_name,obj,parent,acc);
  let {data_obj,ref_obj,rev_obj,table_map} = flattened;
  let link_id = data_obj["id"];
  flatten_linked(schema,table_name,rev_obj,link_id,acc,flatten_obj);
  flatten_linked(schema,table_name,ref_obj,link_id,acc,flatten_obj);
  return acc;
}

function flatten(schema,table_name,data,parent){
  let input = Array.isArray(data) ? data : (((null != data) && ("object" == (typeof data)) && !Array.isArray(data)) ? data : []);
  let parent_obj = ((null != parent) && ("object" == (typeof parent)) && !Array.isArray(parent)) ? parent : {};
  let acc = {};
  if(Array.isArray(input)){
    for(let subdata of input){
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

function flatten_bulk(schema,m){
  let acc = {};
  if(Array.isArray(m)){
    for(let e of m){
      let [table_name,arr] = e;
      let items = Array.isArray(arr) ? arr : [arr];
      for(let obj of items){
        if(null != obj){
          flatten_obj(schema,table_name,obj,{},acc);
        }
      };
    };
  }
  else{
    for(let [table_name,arr] of Object.entries(m)){
      let items = Array.isArray(arr) ? arr : [arr];
      for(let obj of items){
        if(null != obj){
          flatten_obj(schema,table_name,obj,{},acc);
        }
      };
    };
  }
  return acc;
}

function flatten_bulk_ids(schema,lookup,m){
  let flat = flatten_bulk(schema,m);
  return xtd.arr_keep(sch.table_order(lookup),function (table_name){
    return (null != flat[table_name]) ? [
      table_name,
      xtd.arr_sort(Object.keys(flat[table_name]),function (id){
          return id;
        },function (x,y){
          return x < y;
        })
    ] : null;
  });
}

module.exports = {
  ["flatten_get_links"]:flatten_get_links,
  ["flatten_merge"]:flatten_merge,
  ["flatten_node"]:flatten_node,
  ["flatten_linked"]:flatten_linked,
  ["flatten_obj"]:flatten_obj,
  ["flatten"]:flatten,
  ["flatten_bulk"]:flatten_bulk,
  ["flatten_bulk_ids"]:flatten_bulk_ids
}