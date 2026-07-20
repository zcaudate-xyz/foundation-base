const xtd = require("@xtalk/lang/common-data.js")

const frame = require("@xtalk/substrate/base-frame.js")

function subscribe_frame(space,signal,subscription_id,meta){
  return {
    "kind":"subscribe",
    "id":subscription_id || frame.rand_id("sub-",6),
    "space":space || "__NODE__",
    "signal":signal,
    "meta":meta || {}
  };
}

function unsubscribe_frame(space,signal,subscription_id,meta){
  return {
    "kind":"unsubscribe",
    "id":subscription_id || frame.rand_id("sub-",6),
    "space":space || "__NODE__",
    "signal":signal,
    "meta":meta || {}
  };
}

function ensure_router(node){
  let router = node["router"];
  if(null == router){
    router = {"connections":{},"subscriptions":{}};
    node["router"] = router;
  }
  return router;
}

function get_connections(node){
  return (ensure_router(node))["connections"];
}

function get_subscriptions(node){
  return (ensure_router(node))["subscriptions"];
}

function register_connection(node,transport_id,meta){
  let entry = {"id":transport_id,"meta":meta || {}};
  get_connections(node)[transport_id] = entry;
  return entry;
}

function prune_subscription_signal_loop(space_subs,signal_ids,transport_id,index){
  if(index >= signal_ids.length){
    return null;
  }
  let signal = signal_ids[index];
  let signal_subs = space_subs[signal];
  if(null != signal_subs){
    delete(signal_subs[transport_id]);
    if(0 == Object.keys(signal_subs).length){
      delete(space_subs[signal]);
    }
  }
  return prune_subscription_signal_loop(space_subs,signal_ids,transport_id,index + 1);
}

function prune_subscription_space_loop(subscriptions,space_ids,transport_id,index){
  if(index >= space_ids.length){
    return null;
  }
  let space = space_ids[index];
  let space_subs = subscriptions[space];
  if(null != space_subs){
    prune_subscription_signal_loop(space_subs,Object.keys(space_subs),transport_id,0);
    if(0 == Object.keys(space_subs).length){
      delete(subscriptions[space]);
    }
  }
  return prune_subscription_space_loop(subscriptions,space_ids,transport_id,index + 1);
}

function unregister_connection(node,transport_id){
  let connections = get_connections(node);
  let prev = connections[transport_id];
  delete(connections[transport_id]);
  let subscriptions = get_subscriptions(node);
  prune_subscription_space_loop(subscriptions,Object.keys(subscriptions),transport_id,0);
  return prev;
}

function ensure_space_subscriptions(node,space){
  let subscriptions = get_subscriptions(node);
  let space_id = space || "__NODE__";
  let space_subs = subscriptions[space_id];
  if(null == space_subs){
    space_subs = {};
    subscriptions[space_id] = space_subs;
  }
  return space_subs;
}

function ensure_signal_subscriptions(node,space,signal){
  let space_subs = ensure_space_subscriptions(node,space);
  let signal_subs = space_subs[signal];
  if(null == signal_subs){
    signal_subs = {};
    space_subs[signal] = signal_subs;
  }
  return signal_subs;
}

function add_subscription(node,transport_id,space,signal,subscription_id,meta){
  let signal_subs = ensure_signal_subscriptions(node,space,signal);
  let entry = {
    "id":subscription_id || frame.rand_id("sub-",6),
    "meta":meta || {}
  };
  signal_subs[transport_id] = entry;
  return entry;
}

function remove_subscription(node,transport_id,space,signal){
  let subscriptions = get_subscriptions(node);
  let space_subs = subscriptions[space || "__NODE__"];
  if(null == space_subs){
    return null;
  }
  let signal_subs = space_subs[signal];
  if(null == signal_subs){
    return null;
  }
  let prev = signal_subs[transport_id];
  delete(signal_subs[transport_id]);
  if(0 == Object.keys(signal_subs).length){
    delete(space_subs[signal]);
  }
  if(0 == Object.keys(space_subs).length){
    delete(subscriptions[space || "__NODE__"]);
  }
  return prev;
}

function list_subscriptions(node,space,signal){
  let subscriptions = get_subscriptions(node);
  if(null == space){
    return subscriptions;
  }
  else{
    let space_subs = subscriptions[space || "__NODE__"];
    if(null == signal){
      return space_subs || {};
    }
    else{
      if(null == space_subs){
        return [];
      }
      else{
        return xtd.arr_sort(Object.keys(space_subs[signal] || {}),function (x){
          return x;
        },function (x,y){
          return 0 > x.localeCompare(y);
        });
      }
    }
  }
}

function target_ids(node,space,signal){
  return list_subscriptions(node,space,signal);
}

function receive_subscribe(node,event,ctx){
  let transport_id = ctx["transport_id"];
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
  return Promise.resolve().then(function (){
    return event;
  });
}

function receive_unsubscribe(node,event,ctx){
  let transport_id = ctx["transport_id"];
  if(null != transport_id){
    remove_subscription(node,transport_id,event["space"],event["signal"]);
  }
  return Promise.resolve().then(function (){
    return event;
  });
}

module.exports = {
  ["subscribe_frame"]:subscribe_frame,
  ["unsubscribe_frame"]:unsubscribe_frame,
  ["ensure_router"]:ensure_router,
  ["get_connections"]:get_connections,
  ["get_subscriptions"]:get_subscriptions,
  ["register_connection"]:register_connection,
  ["prune_subscription_signal_loop"]:prune_subscription_signal_loop,
  ["prune_subscription_space_loop"]:prune_subscription_space_loop,
  ["unregister_connection"]:unregister_connection,
  ["ensure_space_subscriptions"]:ensure_space_subscriptions,
  ["ensure_signal_subscriptions"]:ensure_signal_subscriptions,
  ["add_subscription"]:add_subscription,
  ["remove_subscription"]:remove_subscription,
  ["list_subscriptions"]:list_subscriptions,
  ["target_ids"]:target_ids,
  ["receive_subscribe"]:receive_subscribe,
  ["receive_unsubscribe"]:receive_unsubscribe
}