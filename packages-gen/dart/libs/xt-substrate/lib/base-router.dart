import 'package:xtalk_lang/common-data.dart' as xtd;
import 'package:xtalk_substrate/base-frame.dart' as frame;
import 'dart:async';



subscribe_frame(space, signal, subscription_id, meta) {
  return <dynamic, dynamic>{
    "kind":"subscribe",
    "id":subscription_id ?? frame.rand_id("sub-",6),
    "space":space ?? "__NODE__",
    "signal":signal,
    "meta":meta ?? <dynamic, dynamic>{}
  };
}

unsubscribe_frame(space, signal, subscription_id, meta) {
  return <dynamic, dynamic>{
    "kind":"unsubscribe",
    "id":subscription_id ?? frame.rand_id("sub-",6),
    "space":space ?? "__NODE__",
    "signal":signal,
    "meta":meta ?? <dynamic, dynamic>{}
  };
}

ensure_router(node) {
  var router = node["router"];
  if(null == router){
    router = <dynamic, dynamic>{
      "connections":<dynamic, dynamic>{},
      "subscriptions":<dynamic, dynamic>{}
    };
    node["router"] = router;
  }
  return router;
}

get_connections(node) {
  return (ensure_router(node))["connections"];
}

get_subscriptions(node) {
  return (ensure_router(node))["subscriptions"];
}

register_connection(node, transport_id, meta) {
  var entry = <dynamic, dynamic>{"id":transport_id,"meta":meta ?? <dynamic, dynamic>{}};
  get_connections(node)[transport_id] = entry;
  return entry;
}

prune_subscription_signal_loop(space_subs, signal_ids, transport_id, index) {
  if(index >= signal_ids.length){
    return null;
  }
  var signal = signal_ids[index];
  var signal_subs = space_subs[signal];
  if(null != signal_subs){
    signal_subs.remove(transport_id);
    if(0 == List<dynamic>.from(( signal_subs ).keys).length){
      space_subs.remove(signal);
    }
  }
  return prune_subscription_signal_loop(space_subs,signal_ids,transport_id,index + 1);
}

prune_subscription_space_loop(subscriptions, space_ids, transport_id, index) {
  if(index >= space_ids.length){
    return null;
  }
  var space = space_ids[index];
  var space_subs = subscriptions[space];
  if(null != space_subs){
    prune_subscription_signal_loop(
      space_subs,
      List<dynamic>.from(( space_subs ).keys),
      transport_id,
      0
    );
    if(0 == List<dynamic>.from(( space_subs ).keys).length){
      subscriptions.remove(space);
    }
  }
  return prune_subscription_space_loop(subscriptions,space_ids,transport_id,index + 1);
}

unregister_connection(node, transport_id) {
  var connections = get_connections(node);
  var prev = connections[transport_id];
  connections.remove(transport_id);
  var subscriptions = get_subscriptions(node);
  prune_subscription_space_loop(
    subscriptions,
    List<dynamic>.from(( subscriptions ).keys),
    transport_id,
    0
  );
  return prev;
}

ensure_space_subscriptions(node, space) {
  var subscriptions = get_subscriptions(node);
  var space_id = space ?? "__NODE__";
  var space_subs = subscriptions[space_id];
  if(null == space_subs){
    space_subs = <dynamic, dynamic>{};
    subscriptions[space_id] = space_subs;
  }
  return space_subs;
}

ensure_signal_subscriptions(node, space, signal) {
  var space_subs = ensure_space_subscriptions(node,space);
  var signal_subs = space_subs[signal];
  if(null == signal_subs){
    signal_subs = <dynamic, dynamic>{};
    space_subs[signal] = signal_subs;
  }
  return signal_subs;
}

add_subscription(node, transport_id, space, signal, subscription_id, meta) {
  var signal_subs = ensure_signal_subscriptions(node,space,signal);
  var entry = <dynamic, dynamic>{
    "id":subscription_id ?? frame.rand_id("sub-",6),
    "meta":meta ?? <dynamic, dynamic>{}
  };
  signal_subs[transport_id] = entry;
  return entry;
}

remove_subscription(node, transport_id, space, signal) {
  var subscriptions = get_subscriptions(node);
  var space_subs = subscriptions[space ?? "__NODE__"];
  if(null == space_subs){
    return null;
  }
  var signal_subs = space_subs[signal];
  if(null == signal_subs){
    return null;
  }
  var prev = signal_subs[transport_id];
  signal_subs.remove(transport_id);
  if(0 == List<dynamic>.from(( signal_subs ).keys).length){
    space_subs.remove(signal);
  }
  if(0 == List<dynamic>.from(( space_subs ).keys).length){
    subscriptions.remove(space ?? "__NODE__");
  }
  return prev;
}

list_subscriptions(node, space, signal) {
  var subscriptions = get_subscriptions(node);
  if(null == space){
    return subscriptions;
  }
  else{
    var space_subs = subscriptions[space ?? "__NODE__"];
    if(null == signal){
      return space_subs ?? <dynamic, dynamic>{};
    }
    else{
      if(null == space_subs){
        return <dynamic>[];
      }
      else{
        return xtd.arr_sort(List<dynamic>.from(( space_subs[signal] ?? <dynamic, dynamic>{} ).keys),(x) {
          return x;
        },(x, y) {
          return (x).toString().compareTo((y).toString()) < 0;
        });
      }
    }
  }
}

target_ids(node, space, signal) {
  return list_subscriptions(node,space,signal);
}

receive_subscribe(node, event, ctx) {
  var transport_id = ctx["transport_id"];
  if(null != transport_id){
    add_subscription(
      node,
      transport_id,
      event["space"],
      event["signal"],
      event["id"],
      event["meta"]
    );
  }
  return Future.sync(() {
    return event;
  });
}

receive_unsubscribe(node, event, ctx) {
  var transport_id = ctx["transport_id"];
  if(null != transport_id){
    remove_subscription(node,transport_id,event["space"],event["signal"]);
  }
  return Future.sync(() {
    return event;
  });
}