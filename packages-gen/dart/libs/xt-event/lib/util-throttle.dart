import 'package:xtalk_lang/common-data.dart' as xtd;
import 'dart:async';


throttle_create(handler, now_fn) {
  return <dynamic, dynamic>{
    "now_fn":(null == now_fn) ? (() {
        return DateTime.now().millisecondsSinceEpoch;
      }) : now_fn,
    "handler":handler,
    "active":<dynamic, dynamic>{},
    "queued":<dynamic, dynamic>{}
  };
}

throttle_run_async(throttle, id, args) {
  var active = throttle["active"];
  var handler = throttle["handler"];
  var queued = throttle["queued"];
  var key = (id).toString();
  args = xtd.arrayify(args);
  var inputs = <dynamic>[id];
  var arr_50713 = args;
  for(var i50714 = 0; i50714 < arr_50713.length; ++i50714){
    var arg = arr_50713[i50714];
    inputs.add(arg);
  };
  var base_promise = Future.sync(() {
    return Function.apply(handler,inputs);
  });
  return ((Future.sync(() => base_promise)) as Future<dynamic>).whenComplete(() async { await Function.apply(() {
    active.remove(key);
    var qentry = queued[key];
    if(null != qentry){
      active[key] = qentry;
      queued.remove(key);
      throttle_run_async(throttle,id,qentry["args"]);
    }
  },<dynamic>[]); });
}

throttle_run(throttle, id, args) {
  var active = throttle["active"];
  var now_fn = throttle["now_fn"];
  var queued = throttle["queued"];
  var key = (id).toString();
  args = xtd.arrayify(args);
  var qentry = queued[key];
  if(null != qentry){
    return qentry;
  }
  var aentry = active[key];
  if(null != aentry){
    qentry = <dynamic, dynamic>{
      "promise":aentry["promise"],
      "started":Function.apply((now_fn as Function),<dynamic>[]),
      "args":aentry["args"]
    };
    queued[key] = qentry;
    return qentry;
  }
  aentry = <dynamic, dynamic>{
    "promise":null,
    "started":Function.apply((now_fn as Function),<dynamic>[]),
    "args":args
  };
  active[key] = aentry;
  var promise = throttle_run_async(throttle,id,args);
  aentry["promise"] = promise;
  return aentry;
}

throttle_waiting(throttle) {
  var active = throttle["active"];
  var queued = throttle["queued"];
  return xtd.arr_union(
    List<dynamic>.from(( active ).keys),
    List<dynamic>.from(( queued ).keys)
  );
}

throttle_active(throttle) {
  var active = throttle["active"];
  return List<dynamic>.from(( active ).keys);
}

throttle_queued(throttle) {
  var queued = throttle["queued"];
  return List<dynamic>.from(( queued ).keys);
}