const xtt = require("@xtalk/lang/common-tree.js")

const xtd = require("@xtalk/lang/common-data.js")

const f = require("@xtalk/db/text/base-flatten.js")

function has_entry(rows,table_key,id){
  return null != xtd.get_in(rows,[table_key,id]);
}

function get_entry(rows,table_key,id){
  return xtd.get_in(rows,[table_key,id]);
}

function swap_if_entry(rows,table_key,id,f){
  let entry = xtd.get_in(rows,[table_key,id]);
  if(null != entry){
    let {record} = entry;
    f(record);
    let new_entry = {"t":Date.now(),"record":record};
    xtd.set_in(rows,[table_key,id],new_entry);
    return new_entry;
  }
  return entry;
}

function merge_single(rows,table_key,id,new_record,new_fn){
  let incoming_record = new_record || {};
  let entry = get_entry(rows,table_key,id);
  if(!((null != entry) && ("object" == (typeof entry)) && !Array.isArray(entry))){
    entry = {"record":{"id":id,"data":{},"ref_links":{},"rev_links":{}}};
  }
  let record = entry["record"];
  let data = incoming_record["data"] || {};
  let ref_links = incoming_record["ref_links"] || {};
  let rev_links = incoming_record["rev_links"] || {};
  xtd.swap_key(record,"data",function (obj,other){
    return Object.assign(obj,other);
  },[data]);
  xtd.swap_key(record,"ref_links",xtd.obj_assign_with,[
    ref_links,
    function (obj,other){
      return Object.assign(obj,other);
    }
  ]);
  xtd.swap_key(record,"rev_links",xtd.obj_assign_with,[
    rev_links,
    function (obj,other){
      return Object.assign(obj,other);
    }
  ]);
  let new_entry = new_fn({"t":Date.now(),"record":record});
  xtd.set_in(rows,[table_key,id],new_entry);
  return new_entry;
}

function merge_bulk(rows,fdata,new_fn){
  let out = {};
  for(let [table_key,m] of Object.entries(fdata)){
    let entries = m || {};
    for(let [id,new_record] of Object.entries(entries)){
      xtd.set_in(out,[table_key,id],merge_single(rows,table_key,id,new_record,new_fn || (function (x){
        return x;
      })));
    };
  };
  return out;
}

function get_ids(rows,table_key){
  return Object.keys(rows[table_key] || {});
}

function all_records(rows,table_key){
  if(null == table_key){
    return xtd.obj_filter(xtd.arr_juxt(Object.keys(rows),function (x){
      return x;
    },function (k){
      return all_records(rows,k);
    }),function (e){
      return (null == e) || (0 < Object.keys(e).length);
    });
  }
  else{
    return xtd.obj_map(rows[table_key],function (e){
      return e["record"];
    });
  }
}

function get_changed_single(rows,table_key,id,record){
  let curr = get_entry(rows,table_key,id);
  if(null == curr){
    return record;
  }
  else{
    return xtt.tree_diff_nested(curr["record"],record);
  }
}

function has_changed_single(rows,table_key,id,record){
  let changed = get_changed_single(rows,table_key,id,record);
  return 0 < Object.keys(changed).length;
}

function get_link_attrs(schema,table_key,field){
  let attr = xtd.get_in(schema,[table_key,field,"ref"]);
  if(null == attr){
    throw "Not a valid link type: " + JSON.stringify([table_key,field]);
  }
  let link_ns = attr["ns"];
  let rval = attr["rval"];
  let link_type = attr["type"];
  let [table_link,inverse_link] =   ({
      "reverse":["rev_links","ref_links"],
      "forward":["ref_links","rev_links"]
    })[link_type];
  return {
    "table_key":table_key,
    "table_link":table_link,
    "table_field":field,
    "inverse_key":link_ns,
    "inverse_link":inverse_link,
    "inverse_field":rval
  };
}

function remove_single_link_entry(rows,table_key,id,table_link,table_field,link_id,link_cb){
  let remove_fn = function (record){
    let link = record[table_link];
    let lrec = link[table_field];
    if((null != lrec) && (null != lrec[link_id])){
      delete(lrec[link_id]);
      if(0 == Object.keys(lrec).length){
        delete(link[table_field]);
      }
      if(null != link_cb){
        link_cb(link_id);
      }
    }
  };
  return swap_if_entry(rows,table_key,id,remove_fn);
}

function remove_single_link(rows,schema,table_key,id,field,link_id){
  let attrs = get_link_attrs(schema,table_key,field);
  let {inverse_field,inverse_key,inverse_link,table_field,table_link} = attrs;
  let l_arr = [false,false];
  let t_has_fn = function (_){
    l_arr[0] = true;
  };
  remove_single_link_entry(rows,table_key,id,table_link,table_field,link_id,t_has_fn);
  let i_has_fn = function (_){
    l_arr[1] = true;
  };
  remove_single_link_entry(rows,inverse_key,link_id,inverse_link,inverse_field,id,i_has_fn);
  return l_arr;
}

function remove_single(rows,schema,table_key,id){
  let entry = get_entry(rows,table_key,id);
  if(null != entry){
    let rec = entry["record"];
    let {ref_links,rev_links} = rec;
    let links = xtd.arr_assign(Object.entries(ref_links),Object.entries(rev_links));
    for(let pair of links){
      let [field,m] = pair;
      let attrs = get_link_attrs(schema,table_key,field);
      let {inverse_field,inverse_key,inverse_link} = attrs;
      for(let link_id of Object.keys(m)){
        remove_single_link_entry(rows,inverse_key,link_id,inverse_link,inverse_field,id,null);
      };
    };
    delete(rows[table_key][id]);
    return [entry];
  }
}

function remove_bulk(rows,schema,table_key,ids){
  return xtd.arr_mapcat(xtd.arr_keep(ids,function (id){
    return remove_single(rows,schema,table_key,id);
  }),function (x){
    return x;
  });
}

function add_single_link_entry(rows,table_key,id,table_link,table_field,link_id,link_cb,inverse_key,inverse_field){
  let add_fn = function (record){
    let link = record[table_link];
    let lrec = link[table_field];
    if(null == lrec){
      lrec = {};
      link[table_field] = lrec;
      lrec[link_id] = true;
    }
    else if(table_link == "rev_links"){
      lrec[link_id] = true;
    }
    else{
      let prev_ids = Object.keys(lrec);
      for(let prev_id of prev_ids){
        remove_single_link_entry(rows,inverse_key,prev_id,"rev_links",inverse_field,id,null);
      };
      link[table_field] = {[link_id]:true};
    }
    if(null != link_cb){
      link_cb(link_id);
    }
  };
  return swap_if_entry(rows,table_key,id,add_fn);
}

function add_single_link(rows,schema,table_key,id,field,link_id){
  let attrs = get_link_attrs(schema,table_key,field);
  let {inverse_field,inverse_key,inverse_link,table_field,table_link} = attrs;
  let l_arr = [false,false];
  let t_has_fn = function (_){
    l_arr[0] = true;
  };
  let t_entry_fn = function (){
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
  let i_has_fn = function (_){
    l_arr[1] = true;
  };
  let i_entry_fn = function (){
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
    t_entry_fn();
    i_entry_fn();
  }
  else if(table_link == "rev_links"){
    i_entry_fn();
    t_entry_fn();
  }
  return l_arr;
}

function add_bulk_links(rows,schema,flat){
  let out = [];
  for(let table_key of Object.keys(flat)){
    let bulk = flat[table_key];
    for(let row_id of Object.keys(bulk)){
      let record = bulk[row_id];
      let {ref_links,rev_links} = record;
      for(let field of Object.keys(ref_links)){
        let links = ref_links[field];
        for(let link_id of Object.keys(links)){
          out.push(
            {"table":table_key,"id":row_id,"field":field,"link_id":link_id}
          );
        };
      };
      for(let field of Object.keys(rev_links)){
        let links = rev_links[field];
        for(let link_id of Object.keys(links)){
          out.push(
            {"table":table_key,"id":row_id,"field":field,"link_id":link_id}
          );
        };
      };
    };
  };
  for(let link_spec of out){
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

function add_bulk(rows,schema,data){
  let flat = f.flatten_bulk(schema,data);
  merge_bulk(rows,flat,null);
  return add_bulk_links(rows,schema,flat);
}

module.exports = {
  ["has_entry"]:has_entry,
  ["get_entry"]:get_entry,
  ["swap_if_entry"]:swap_if_entry,
  ["merge_single"]:merge_single,
  ["merge_bulk"]:merge_bulk,
  ["get_ids"]:get_ids,
  ["all_records"]:all_records,
  ["get_changed_single"]:get_changed_single,
  ["has_changed_single"]:has_changed_single,
  ["get_link_attrs"]:get_link_attrs,
  ["remove_single_link_entry"]:remove_single_link_entry,
  ["remove_single_link"]:remove_single_link,
  ["remove_single"]:remove_single,
  ["remove_bulk"]:remove_bulk,
  ["add_single_link_entry"]:add_single_link_entry,
  ["add_single_link"]:add_single_link,
  ["add_bulk_links"]:add_bulk_links,
  ["add_bulk"]:add_bulk
}