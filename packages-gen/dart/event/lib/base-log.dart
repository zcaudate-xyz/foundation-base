import 'package:xtalk_event/base-listener.dart' as event_common;

import 'package:xtalk_lang/common-data.dart' as xtd;

new_log(m) {
  return event_common.blank_container("event.log",xtd.obj_assign(<dynamic, dynamic>{
    "last":null,
    "processed":<dynamic>[],
    "cache":<dynamic, dynamic>{},
    "interval":30000,
    "maximum":100,
    "callback":null,
    "listeners":<dynamic, dynamic>{}
  },m));
}

get_count(log) {
  var processed = log["processed"];
  return processed.length;
}

get_last(log) {
  var processed = log["processed"];
  return processed[processed.length + -1];
}

get_head(log, n) {
  var processed = log["processed"];
  var total = processed.length;
  return processed.sublist(0 - 0,math.min(n,total));
}

get_filtered(log, pred) {
  var processed = log["processed"];
  return xtd.arr_filter(processed,pred);
}

get_tail(log, n) {
  var processed = log["processed"];
  var total = processed.length;
  return processed.sublist(math.max(0,total - n) - 0,total);
}

get_slice(log, start, finish) {
  var processed = log["processed"];
  var total = processed.length;
  return processed.sublist(
    math.min(math.max(0,start),total) - 0,
    math.min(math.max(0,finish),total)
  );
}

clear(log) {
  var processed = log["processed"];
  log["processed"] = <dynamic>[];
  return processed;
}

clear_cache(log, t) {
  if(null == t){
    t = DateTime.now().millisecondsSinceEpoch;
  }
  var cache = log["cache"];
  var interval = log["interval"];
  var last = log["last"];
  var out = <dynamic>[];
  if((null != last) && (interval >= (t - last))){
    return out;
  }
  log["last"] = t;
  var stale = <dynamic>[];
  for(var entry_41295 in cache.entries){
    var k = entry_41295.key;
    var kt = entry_41295.value;
    if(interval < (t - kt)){
      stale.add(k);
    }
  };
  var arr_41296 = stale;
  for(var i41297 = 0; i41297 < arr_41296.length; ++i41297){
    var k = arr_41296[i41297];
    cache.remove(k);
    out.add(k);
  };
  return out;
}

var METHODS = <dynamic, dynamic>{
  "count":<dynamic, dynamic>{"handler":get_count,"input":<dynamic>[]},
  "last":<dynamic, dynamic>{"handler":get_last,"input":<dynamic>[]},
  "tail":<dynamic, dynamic>{
    "handler":get_tail,
    "input":<dynamic>[<dynamic, dynamic>{"symbol":"n","type":"integer"}]
  },
  "head":<dynamic, dynamic>{
    "handler":get_head,
    "input":<dynamic>[<dynamic, dynamic>{"symbol":"n","type":"integer"}]
  },
  "slice":<dynamic, dynamic>{
    "handler":get_slice,
    "input":<dynamic>[
      <dynamic, dynamic>{"symbol":"start","type":"integer"},
      <dynamic, dynamic>{"symbol":"finish","type":"integer"}
    ]
  },
  "clear":<dynamic, dynamic>{"handler":clear,"input":<dynamic>[]},
  "clear_cache":<dynamic, dynamic>{
    "handler":clear_cache,
    "input":<dynamic>[<dynamic, dynamic>{"symbol":"t","type":"integer"}]
  }
};

queue_entry(log, input, key_fn, data_fn, t) {
  if(null == t){
    t = DateTime.now().millisecondsSinceEpoch;
  }
  var cache = log["cache"];
  var callback = log["callback"];
  var listeners = log["listeners"];
  var maximum = log["maximum"];
  var processed = log["processed"];
  var key = ((null != key_fn) && (false != key_fn)) ? Function.apply((key_fn as Function),<dynamic>[input,t]) : t;
  var data = Function.apply((data_fn as Function),<dynamic>[input]);
  clear_cache(log,t);
  if((null == key) || !(null == cache[key])){
    return null;
  }
  else{
    cache[key] = t;
    xtd.arr_pushl(processed,xtd.clone_nested(data),maximum);
    if((null != callback) && (false != callback)){
      callback(data,t);
    }
    for(var entry_41318 in listeners.entries){
      var id = entry_41318.key;
      var listener_entry = entry_41318.value;
      var callback = listener_entry["callback"];
      var meta = listener_entry["meta"];
      if((null != callback) && (false != callback)){
        callback(id,data,t,meta);
      }
    };
    return data;
  }
}

add_listener(log, listener_id, callback, meta) {
  return event_common.add_listener(log,listener_id,"log",callback,meta,null);
}

var remove_listener = event_common.remove_listener;

var list_listeners = event_common.list_listeners;