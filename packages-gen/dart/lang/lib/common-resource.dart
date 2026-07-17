import 'package:xtalk_lang/common-data.dart' as common_data;

import 'package:xtalk_lang/common-string.dart' as xts;

xt_existsp() {
  return !(null == __globals__["XT"]);
}

xt_create() {
  return <dynamic, dynamic>{
    "::":"xt",
    "config":<dynamic, dynamic>{},
    "spaces":<dynamic, dynamic>{},
    "hash":<dynamic, dynamic>{"lookup":<dynamic, dynamic>{},"counter":0x011c9dc5}
  };
}

xt_ensure() {
  if((() {
    var dart_truthy__39902 = !(null == __globals__["XT"]);
    return (null != dart_truthy__39902) && (false != dart_truthy__39902);
  })()){
    return __globals__["XT"];
  }
  else{
    __globals__["XT"] = xt_create();
    return __globals__["XT"];
  }
}

xt_current() {
  if((() {
    var dart_truthy__39901 = !(null == __globals__["XT"]);
    return (null != dart_truthy__39901) && (false != dart_truthy__39901);
  })()){
    return __globals__["XT"];
  }
  else{
    return null;
  }
}

xt_purge() {
  if((() {
    var dart_truthy__39900 = !(null == __globals__["XT"]);
    return (null != dart_truthy__39900) && (false != dart_truthy__39900);
  })()){
    var out = __globals__["XT"];
    __globals__["XT"] = null;
    return out;
  }
  else{
    return null;
  }
}

xt_purge_config() {
  var g = xt_ensure();
  var prev = g["config"];
  g["config"] = <dynamic, dynamic>{};
  return <dynamic>[true,prev];
}

xt_purge_spaces() {
  var g = xt_ensure();
  var prev = g["spaces"];
  g["spaces"] = <dynamic, dynamic>{};
  return <dynamic>[true,prev];
}

xt_lookup_id(obj) {
  if(((obj.runtimeType).toString().contains("Function") || (obj.runtimeType).toString().contains("=>") || (obj).toString().startsWith("Closure")) || (("Map" == (obj.runtimeType).toString()) || (obj.runtimeType).toString().startsWith("_Map") || (obj.runtimeType).toString().startsWith("LinkedMap")) || ((obj.runtimeType).toString().startsWith("List") || (obj.runtimeType).toString().startsWith("_GrowableList"))){
    var hash = xt_ensure()["hash"];
    var counter = hash["counter"];
    var lookup = hash["lookup"];
    var hash_id = lookup[obj];
    if(null == hash_id){
      hash["counter"] = (1 + counter);
      lookup[obj] = counter;
      return counter;
    }
    return hash_id;
  }
}

xt_config_list() {
  var g = xt_ensure();
  var config = g["config"];
  return common_data.obj_keys(config);
}

xt_config_set(module, m) {
  var g = xt_ensure();
  var config = g["config"];
  var prev = config[module];
  config[module] = m;
  return <dynamic>[true,prev];
}

xt_config_del(module) {
  var g = xt_ensure();
  var config = g["config"];
  var prev = config[module];
  config.remove(module);
  return <dynamic>[true,prev];
}

xt_config(module) {
  var g = xt_ensure();
  var config = g["config"];
  return config[module];
}

xt_space_list() {
  var g = xt_ensure();
  var spaces = g["spaces"];
  return common_data.obj_keys(spaces);
}

xt_space_del(module) {
  var g = xt_ensure();
  var spaces = g["spaces"];
  var prev = spaces[module];
  if(null != prev){
    spaces.remove(module);
  }
  return <dynamic>[true,prev];
}

xt_space(module) {
  var g = xt_ensure();
  var spaces = g["spaces"];
  var curr = spaces[module];
  if(null == curr){
    curr = <dynamic, dynamic>{};
    spaces[module] = curr;
  }
  return curr;
}

xt_space_clear(module) {
  var g = xt_ensure();
  var spaces = g["spaces"];
  var prev = spaces[module];
  spaces[module] = <dynamic, dynamic>{};
  return <dynamic>[true,prev];
}

xt_item_del(module, key) {
  var space = xt_space(module);
  var prev = space[key];
  if(null != prev){
    space.remove(key);
  }
  return <dynamic>[true,prev];
}

xt_item_trigger(module, key) {
  var space = xt_space(module);
  var prev = space[key];
  if(null != prev){
    var value = prev["value"];
    var watch = prev["watch"];
    for(var entry_39903 in watch.entries){
      var watch_key = entry_39903.key;
      var watch_fn = entry_39903.value;
      Function.apply((watch_fn as Function),<dynamic>[value,module + "/" + key]);
    };
    return common_data.obj_keys(watch);
  }
}

xt_item_set(module, key, value) {
  var space = xt_space(module);
  var prev = space[key];
  if(null == prev){
    prev = <dynamic, dynamic>{"watch":<dynamic, dynamic>{}};
    space[key] = prev;
  }
  prev["value"] = value;
  var watch = prev["watch"];
  for(var entry_39904 in watch.entries){
    var watch_key = entry_39904.key;
    var watch_fn = entry_39904.value;
    Function.apply((watch_fn as Function),<dynamic>[value,module + "/" + key]);
  };
  return <dynamic>[true,prev];
}

xt_item(module, key) {
  var space = xt_space(module);
  var curr = space[key];
  if((null != curr) && (false != curr)){
    return curr["value"];
  }
  return null;
}

xt_item_get(module, key, init_fn) {
  var space = xt_space(module);
  var curr = space[key];
  if((null != curr) && (false != curr)){
    return curr["value"];
  }
  else{
    var value = Function.apply((init_fn as Function),<dynamic>[]);
    space[key] = <dynamic, dynamic>{"value":value,"watch":<dynamic, dynamic>{}};
    return value;
  }
}

xt_var_entry(sym) {
  var value_39905 = xts.sym_pair(sym);
  var module = value_39905[0];
  var key = value_39905[1];
  var space = xt_space(module);
  return space[key];
}

xt_var(sym) {
  var value_39906 = xts.sym_pair(sym);
  var module = value_39906[0];
  var key = value_39906[1];
  return xt_item(module,key);
}

xt_var_set(sym, value) {
  var value_39907 = xts.sym_pair(sym);
  var module = value_39907[0];
  var key = value_39907[1];
  if(null == value){
    return xt_item_del(module,key);
  }
  else{
    return xt_item_set(module,key,value);
  }
}

xt_var_trigger(sym) {
  var value_39908 = xts.sym_pair(sym);
  var module = value_39908[0];
  var key = value_39908[1];
  return xt_item_trigger(module,key);
}

xt_add_watch(sym, watch_key, watch_fn) {
  var entry = xt_var_entry(sym);
  if((null != entry) && (false != entry)){
    var watch = entry["watch"];
    watch[watch_key] = watch_fn;
    return true;
  }
  return false;
}

xt_remove_watch(sym, watch_key) {
  var entry = xt_var_entry(sym);
  if((null != entry) && (false != entry)){
    var watch = entry["watch"];
    watch.remove(watch_key);
    return true;
  }
  return false;
}