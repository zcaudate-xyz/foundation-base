new_observed(v) {
  return <dynamic, dynamic>{"::":"observed","value":v,"listeners":<dynamic>[]};
}

is_observed(x) {
  return (("Map" == (x.runtimeType).toString()) || (x.runtimeType).toString().startsWith("_Map") || (x.runtimeType).toString().startsWith("LinkedMap")) && ("observed" == x["::"]);
}

add_listener(obs, f) {
  var listeners = obs["listeners"];
  return listeners.add(f);
}

notify_listeners(obs) {
  var listeners = obs["listeners"];
  var value = obs["value"];
  var arr_41365 = listeners;
  for(var i41366 = 0; i41366 < arr_41365.length; ++i41366){
    var listener = arr_41365[i41366];
    listener(value);
  };
}

get_value(obs) {
  var value = obs["value"];
  return value;
}

set_value(obs, v) {
  var listeners = obs["listeners"];
  obs["value"] = v;
  return notify_listeners(obs);
}

mock_transition(indicator, tparams, transition, tf) {
  var value_41387 = transition;
  var prev = value_41387[0];
  var curr = value_41387[1];
  return (callback) {
    set_value(indicator,tf(curr));
    if(null != callback){
      callback(null);
    }
  };
}

var MOCK = <dynamic, dynamic>{
  "create_val":new_observed,
  "add_listener":add_listener,
  "get_value":get_value,
  "set_value":set_value,
  "set_props":(elem, props) {
    return elem["props"] = props;
  },
  "is_animated":is_observed,
  "create_transition":mock_transition,
  "stop_transition":() {
    
  }
};