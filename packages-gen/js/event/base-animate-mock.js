function new_observed(v){
  return {"::":"observed","value":v,"listeners":[]};
}

function is_observed(x){
  return ((null != x) && ("object" == (typeof x)) && !Array.isArray(x)) && ("observed" == x["::"]);
}

function add_listener(obs,f){
  let {listeners} = obs;
  listeners.push(f);
}

function notify_listeners(obs){
  let {listeners,value} = obs;
  for(let listener of listeners){
    listener(value);
  };
}

function get_value(obs){
  let {value} = obs;
  return value;
}

function set_value(obs,v){
  let {listeners} = obs;
  obs["value"] = v;
  notify_listeners(obs);
}

function mock_transition(indicator,tparams,transition,tf){
  let [prev,curr] = transition;
  return function (callback){
    set_value(indicator,tf(curr));
    if(null != callback){
      callback(null);
    }
  };
}

var MOCK = {
  "create_val":new_observed,
  "add_listener":add_listener,
  "get_value":get_value,
  "set_value":set_value,
  "set_props":function (elem,props){
    elem["props"] = props;
  },
  "is_animated":is_observed,
  "create_transition":mock_transition,
  "stop_transition":function (){
    
  }
};

module.exports = {
  ["new_observed"]:new_observed,
  ["is_observed"]:is_observed,
  ["add_listener"]:add_listener,
  ["notify_listeners"]:notify_listeners,
  ["get_value"]:get_value,
  ["set_value"]:set_value,
  ["mock_transition"]:mock_transition,
  ["MOCK"]:MOCK
}