import 'package:xtalk_event/base-listener.dart' as event_common;

new_log_latest(m) {
  return event_common.blank_container("event.log-latest",xtd.obj_assign(<dynamic, dynamic>{
    "last":null,
    "cache":<dynamic, dynamic>{},
    "interval":30000,
    "callback":null
  },m));
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
  var arr_41268 = List<dynamic>.from(( cache ).keys);
  for(var i41269 = 0; i41269 < arr_41268.length; ++i41269){
    var k = arr_41268[i41269];
    var entry = cache[k];
    if(interval < (t - entry["t"])){
      cache.remove(k);
      out.add(k);
    }
  };
  return out;
}

queue_latest(log, key, latest) {
  var cache = log["cache"];
  var entry = cache[key];
  var t = DateTime.now().millisecondsSinceEpoch;
  if(null == entry){
    cache[key] = <dynamic, dynamic>{"t":t,"latest":latest};
    clear_cache(log,t);
    return true;
  }
  else if(entry["latest"] < latest){
    cache[key] = <dynamic, dynamic>{"t":t,"latest":latest};
    clear_cache(log,t);
    return true;
  }
  else{
    return false;
  }
}