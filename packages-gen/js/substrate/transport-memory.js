const node_json = require("@xtalk/substrate/base-json.js")

const main = require("@xtalk/substrate/substrate.js")

function event_text(event){
  return (((null != event) && ("object" == (typeof event)) && !Array.isArray(event)) && (null != event["data"])) ? event["data"] : ((((null != event) && ("object" == (typeof event)) && !Array.isArray(event)) && (null != event["text"])) ? event["text"] : event);
}

function network_targets(value){
  if(null == value){
    return [];
  }
  else if(Array.isArray(value)){
    return value;
  }
  else{
    return [value];
  }
}

function ensure_network_state(network,endpoint_id){
  let states = network["states"];
  let state = states[endpoint_id];
  if(null == state){
    state = {"id":endpoint_id,"listener":null,"peers":[],"network":network};
    states[endpoint_id] = state;
  }
  return state;
}

function ensure_network_targets_loop(network,peer_ids,index){
  if(index >= peer_ids.length){
    return null;
  }
  ensure_network_state(network,peer_ids[index]);
  return ensure_network_targets_loop(network,peer_ids,index + 1);
}

function configure_network_links_loop(network,links,endpoint_ids,index){
  if(index >= endpoint_ids.length){
    return network;
  }
  let endpoint_id = endpoint_ids[index];
  let peer_ids = network_targets(links[endpoint_id]);
  let state = ensure_network_state(network,endpoint_id);
  state["peers"] = peer_ids;
  ensure_network_targets_loop(network,peer_ids,0);
  return configure_network_links_loop(network,links,endpoint_ids,index + 1);
}

function deliver_network_loop(network,state,peer_ids,text,index){
  if(index >= peer_ids.length){
    return Promise.resolve().then(function (){
      return true;
    });
  }
  let peer_id = peer_ids[index];
  let peer = (network["states"])[peer_id];
  if(null == peer){
    throw "wire peer not found - " + peer_id;
  }
  let listener = peer["listener"];
  if(null == listener){
    throw "wire peer not started - " + peer_id;
  }
  let output = listener({"text":text},{"wire":state["id"],"peer":peer_id});
  return ((output instanceof Promise) ? output : Promise.resolve().then(function (){
    return output;
  })).then(function (_){
    return deliver_network_loop(network,state,peer_ids,text,index + 1);
  });
}

function memory_endpoint(state){
  let write_fn = function (text){
    let network = state["network"];
    let peer_ids = state["peers"];
    if(null != network){
      if(0 == peer_ids.length){
        throw "wire endpoint missing peers";
      }
      return deliver_network_loop(network,state,peer_ids,text,0);
    }
    let peer = state["peer"];
    if(null == peer){
      throw "wire endpoint missing peer";
    }
    let listener = peer["listener"];
    if(null == listener){
      throw "wire peer not started";
    }
    return listener({"text":text},{"wire":state["id"],"peer":peer["id"]});
  };
  let start_fn = function (listener){
    state["listener"] = listener;
    return state;
  };
  let stop_fn = function (_){
    state["listener"] = null;
    return true;
  };
  return {
    "meta":{"kind":"wire.memory","id":state["id"]},
    "write_fn":write_fn,
    "start_fn":start_fn,
    "stop_fn":stop_fn
  };
}

function create_network_endpoints_loop(network,endpoint_ids,out,index){
  if(index >= endpoint_ids.length){
    return out;
  }
  let endpoint_id = endpoint_ids[index];
  out[endpoint_id] = memory_endpoint(ensure_network_state(network,endpoint_id));
  return create_network_endpoints_loop(network,endpoint_ids,out,index + 1);
}

function text_endpoint(endpoint_source){
  let current_endpoint = null;
  let current_listener = null;
  let current_callback = null;
  let source_create_fn = endpoint_source["create_fn"];
  let send_fn = function (frame){
    let endpoint = current_endpoint;
    if(null == endpoint){
      if(!("function" == (typeof source_create_fn))){
        endpoint = endpoint_source;
      }
    }
    if(null == endpoint){
      throw "json endpoint not started";
    }
    let raw_write_fn = endpoint["write_fn"];
    if(!("function" == (typeof raw_write_fn))){
      throw "json endpoint missing write implementation";
    }
    return raw_write_fn(node_json.encode_frame(frame));
  };
  let start_fn = function (listener){
    let callback = function (event,ctx){
      let text = event_text(event);
      let frame = node_json.decode_frame(text);
      ctx = (ctx || {});
      ctx["raw"] = event;
      ctx["payload"] = text;
      return listener(frame,ctx);
    };
    current_callback = callback;
    if("function" == (typeof source_create_fn)){
      current_endpoint = source_create_fn(callback);
      current_listener = current_endpoint;
      return current_endpoint;
    }
    else{
      current_endpoint = endpoint_source;
      let raw_start_fn = current_endpoint["start_fn"];
      if(null == raw_start_fn){
        current_listener = current_endpoint;
        return current_endpoint;
      }
      current_listener = raw_start_fn(callback);
      if(null != current_listener){
        return current_listener;
      }
      return current_endpoint;
    }
  };
  let stop_fn = function (_){
    let endpoint = current_endpoint;
    if(null == endpoint){
      endpoint = endpoint_source;
    }
    let raw_stop_fn = null;
    if((null != endpoint) && ("object" == (typeof endpoint)) && !Array.isArray(endpoint)){
      raw_stop_fn = endpoint["stop_fn"];
    }
    if("function" == (typeof raw_stop_fn)){
      raw_stop_fn(current_listener);
    }
    current_endpoint = null;
    current_listener = null;
    current_callback = null;
    return true;
  };
  return {
    "meta":{"kind":"json"},
    "send_fn":send_fn,
    "start_fn":start_fn,
    "stop_fn":stop_fn
  };
}

function memory_pair(opts){
  let config = opts || {};
  let left_state = {"id":config["left_id"] || "left","listener":null,"peer":null};
  let right_state = {
    "id":config["right_id"] || "right",
    "listener":null,
    "peer":null
  };
  left_state["peer"] = right_state;
  right_state["peer"] = left_state;
  return {
    "left":memory_endpoint(left_state),
    "right":memory_endpoint(right_state)
  };
}

function memory_network(opts){
  let config = opts || {};
  let links = (null != config["links"]) ? config["links"] : config;
  let network = {"states":{}};
  configure_network_links_loop(network,links,Object.keys(links),0);
  return create_network_endpoints_loop(network,Object.keys(network["states"]),{},0);
}

function link_pair(server,client){
  let wire = memory_pair({"left_id":"client","right_id":"server"});
  return Promise.all([
    main.attach_transport(client,"server",text_endpoint(wire["left"])),
    main.attach_transport(server,"client",text_endpoint(wire["right"]))
  ]);
}

module.exports = {
  ["event_text"]:event_text,
  ["network_targets"]:network_targets,
  ["ensure_network_state"]:ensure_network_state,
  ["ensure_network_targets_loop"]:ensure_network_targets_loop,
  ["configure_network_links_loop"]:configure_network_links_loop,
  ["deliver_network_loop"]:deliver_network_loop,
  ["memory_endpoint"]:memory_endpoint,
  ["create_network_endpoints_loop"]:create_network_endpoints_loop,
  ["text_endpoint"]:text_endpoint,
  ["memory_pair"]:memory_pair,
  ["memory_network"]:memory_network,
  ["link_pair"]:link_pair
}