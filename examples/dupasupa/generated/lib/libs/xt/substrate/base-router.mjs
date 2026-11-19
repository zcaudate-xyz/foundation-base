import * as xtd from '../lang/common-data.mjs'

import * as frame from './base-frame.mjs'

export function subscribe_frame(space,signal,subscription_id,meta){
  return {
    "kind":"subscribe",
    "id":subscription_id || frame.rand_id("sub-",6),
    "space":space || "__NODE__",
    "signal":signal,
    "meta":meta || {}
  };
}

export function unsubscribe_frame(space,signal,subscription_id,meta){
  return {
    "kind":"unsubscribe",
    "id":subscription_id || frame.rand_id("sub-",6),
    "space":space || "__NODE__",
    "signal":signal,
    "meta":meta || {}
  };
}

export function ensure_router(node){
  let {router} = node;
  if(null == router){
    router = {"connections":{},"subscriptions":{}};
    node["router"] = router;
  }
  return router;
}

export function get_connections(node){
  return (ensure_router(node))["connections"];
}

export function get_subscriptions(node){
  return (ensure_router(node))["subscriptions"];
}

export function register_connection(node,transport_id,meta){
  let entry = {"id":transport_id,"meta":meta || {}};
  get_connections(node)[transport_id] = entry;
  return entry;
}

export function prune_subscription_signal_loop(space_subs,signal_ids,transport_id,index){
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

export function prune_subscription_space_loop(subscriptions,space_ids,transport_id,index){
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

export function unregister_connection(node,transport_id){
  let connections = get_connections(node);
  let prev = connections[transport_id];
  delete(connections[transport_id]);
  let subscriptions = get_subscriptions(node);
  prune_subscription_space_loop(subscriptions,Object.keys(subscriptions),transport_id,0);
  return prev;
}

export function ensure_space_subscriptions(node,space){
  let subscriptions = get_subscriptions(node);
  let space_id = space || "__NODE__";
  let space_subs = subscriptions[space_id];
  if(null == space_subs){
    space_subs = {};
    subscriptions[space_id] = space_subs;
  }
  return space_subs;
}

export function ensure_signal_subscriptions(node,space,signal){
  let space_subs = ensure_space_subscriptions(node,space);
  let signal_subs = space_subs[signal];
  if(null == signal_subs){
    signal_subs = {};
    space_subs[signal] = signal_subs;
  }
  return signal_subs;
}

export function add_subscription(node,transport_id,space,signal,subscription_id,meta){
  let signal_subs = ensure_signal_subscriptions(node,space,signal);
  let entry = {
    "id":subscription_id || frame.rand_id("sub-",6),
    "meta":meta || {}
  };
  signal_subs[transport_id] = entry;
  return entry;
}

export function remove_subscription(node,transport_id,space,signal){
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

export function list_subscriptions(node,space,signal){
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

export function target_ids(node,space,signal){
  return list_subscriptions(node,space,signal);
}

export function receive_subscribe(node,event,ctx){
  let {transport_id} = ctx;
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

export function receive_unsubscribe(node,event,ctx){
  let {transport_id} = ctx;
  if(null != transport_id){
    remove_subscription(node,transport_id,event["space"],event["signal"]);
  }
  return Promise.resolve().then(function (){
    return event;
  });
}