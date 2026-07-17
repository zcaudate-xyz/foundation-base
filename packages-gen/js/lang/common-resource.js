const common_data = require("@xtalk/lang/common-data.js")

const xts = require("@xtalk/lang/common-string.js")

function xt_existsp(){
  return !(null == globalThis["XT"]);
}

function xt_create(){
  return {
    "::":"xt",
    "config":{},
    "spaces":{},
    "hash":{"lookup":new Map(),"counter":0x011c9dc5}
  };
}

function xt_ensure(){
  if(!(null == globalThis["XT"])){
    return globalThis["XT"];
  }
  else{
    globalThis["XT"] = xt_create();
    return globalThis["XT"];
  }
}

function xt_current(){
  if(!(null == globalThis["XT"])){
    return globalThis["XT"];
  }
  else{
    return null;
  }
}

function xt_purge(){
  if(!(null == globalThis["XT"])){
    let out = globalThis["XT"];
    globalThis["XT"] = null;
    return out;
  }
  else{
    return null;
  }
}

function xt_purge_config(){
  let g = xt_ensure();
  let prev = g["config"];
  g["config"] = {};
  return [true,prev];
}

function xt_purge_spaces(){
  let g = xt_ensure();
  let prev = g["spaces"];
  g["spaces"] = {};
  return [true,prev];
}

function xt_lookup_id(obj){
  if(("function" == (typeof obj)) || ((null != obj) && ("object" == (typeof obj)) && !Array.isArray(obj)) || Array.isArray(obj)){
    let {hash} = xt_ensure();
    let {counter,lookup} = hash;
    let hash_id = lookup.get(obj);
    if(null == hash_id){
      hash["counter"] = (1 + counter);
      lookup.set(obj,counter);
      return counter;
    }
    return hash_id;
  }
}

function xt_config_list(){
  let g = xt_ensure();
  let {config} = g;
  return common_data.obj_keys(config);
}

function xt_config_set(module,m){
  let g = xt_ensure();
  let {config} = g;
  let prev = config[module];
  config[module] = m;
  return [true,prev];
}

function xt_config_del(module){
  let g = xt_ensure();
  let {config} = g;
  let prev = config[module];
  delete(config[module]);
  return [true,prev];
}

function xt_config(module){
  let g = xt_ensure();
  let {config} = g;
  return config[module];
}

function xt_space_list(){
  let g = xt_ensure();
  let {spaces} = g;
  return common_data.obj_keys(spaces);
}

function xt_space_del(module){
  let g = xt_ensure();
  let {spaces} = g;
  let prev = spaces[module];
  if(null != prev){
    delete(spaces[module]);
  }
  return [true,prev];
}

function xt_space(module){
  let g = xt_ensure();
  let {spaces} = g;
  let curr = spaces[module];
  if(null == curr){
    curr = {};
    spaces[module] = curr;
  }
  return curr;
}

function xt_space_clear(module){
  let g = xt_ensure();
  let {spaces} = g;
  let prev = spaces[module];
  spaces[module] = {};
  return [true,prev];
}

function xt_item_del(module,key){
  let space = xt_space(module);
  let prev = space[key];
  if(null != prev){
    delete(space[key]);
  }
  return [true,prev];
}

function xt_item_trigger(module,key){
  let space = xt_space(module);
  let prev = space[key];
  if(null != prev){
    let {value,watch} = prev;
    for(let [watch_key,watch_fn] of Object.entries(watch)){
      watch_fn(value,module + "/" + key);
    };
    return common_data.obj_keys(watch);
  }
}

function xt_item_set(module,key,value){
  let space = xt_space(module);
  let prev = space[key];
  if(null == prev){
    prev = {"watch":{}};
    space[key] = prev;
  }
  prev["value"] = value;
  let {watch} = prev;
  for(let [watch_key,watch_fn] of Object.entries(watch)){
    watch_fn(value,module + "/" + key);
  };
  return [true,prev];
}

function xt_item(module,key){
  let space = xt_space(module);
  let curr = space[key];
  if(curr){
    return curr["value"];
  }
  return null;
}

function xt_item_get(module,key,init_fn){
  let space = xt_space(module);
  let curr = space[key];
  if(curr){
    return curr["value"];
  }
  else{
    let value = init_fn();
    space[key] = {"value":value,"watch":{}};
    return value;
  }
}

function xt_var_entry(sym){
  let [module,key] = xts.sym_pair(sym);
  let space = xt_space(module);
  return space[key];
}

function xt_var(sym){
  let [module,key] = xts.sym_pair(sym);
  return xt_item(module,key);
}

function xt_var_set(sym,value){
  let [module,key] = xts.sym_pair(sym);
  if(null == value){
    return xt_item_del(module,key);
  }
  else{
    return xt_item_set(module,key,value);
  }
}

function xt_var_trigger(sym){
  let [module,key] = xts.sym_pair(sym);
  return xt_item_trigger(module,key);
}

function xt_add_watch(sym,watch_key,watch_fn){
  let entry = xt_var_entry(sym);
  if(entry){
    let {watch} = entry;
    watch[watch_key] = watch_fn;
    return true;
  }
  return false;
}

function xt_remove_watch(sym,watch_key){
  let entry = xt_var_entry(sym);
  if(entry){
    let {watch} = entry;
    delete(watch[watch_key]);
    return true;
  }
  return false;
}

module.exports = {
  ["xt_existsp"]:xt_existsp,
  ["xt_create"]:xt_create,
  ["xt_ensure"]:xt_ensure,
  ["xt_current"]:xt_current,
  ["xt_purge"]:xt_purge,
  ["xt_purge_config"]:xt_purge_config,
  ["xt_purge_spaces"]:xt_purge_spaces,
  ["xt_lookup_id"]:xt_lookup_id,
  ["xt_config_list"]:xt_config_list,
  ["xt_config_set"]:xt_config_set,
  ["xt_config_del"]:xt_config_del,
  ["xt_config"]:xt_config,
  ["xt_space_list"]:xt_space_list,
  ["xt_space_del"]:xt_space_del,
  ["xt_space"]:xt_space,
  ["xt_space_clear"]:xt_space_clear,
  ["xt_item_del"]:xt_item_del,
  ["xt_item_trigger"]:xt_item_trigger,
  ["xt_item_set"]:xt_item_set,
  ["xt_item"]:xt_item,
  ["xt_item_get"]:xt_item_get,
  ["xt_var_entry"]:xt_var_entry,
  ["xt_var"]:xt_var,
  ["xt_var_set"]:xt_var_set,
  ["xt_var_trigger"]:xt_var_trigger,
  ["xt_add_watch"]:xt_add_watch,
  ["xt_remove_watch"]:xt_remove_watch
}