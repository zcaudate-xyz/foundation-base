import 'package:xtalk_substrate/base-json.dart' as node_json;

import 'package:xtalk_substrate/substrate.dart' as main;

event_text(event) {
  return ((("Map" == (event.runtimeType).toString()) || (event.runtimeType).toString().startsWith("_Map") || (event.runtimeType).toString().startsWith("LinkedMap")) && event.containsKey("data")) ? event["data"] : (((("Map" == (event.runtimeType).toString()) || (event.runtimeType).toString().startsWith("_Map") || (event.runtimeType).toString().startsWith("LinkedMap")) && event.containsKey("text")) ? event["text"] : event);
}

network_targets(value) {
  if(null == value){
    return <dynamic>[];
  }
  else if((value.runtimeType).toString().startsWith("List") || (value.runtimeType).toString().startsWith("_GrowableList")){
    return value;
  }
  else{
    return <dynamic>[value];
  }
}

ensure_network_state(network, endpoint_id) {
  var states = network["states"];
  var state = states[endpoint_id];
  if(null == state){
    state = <dynamic, dynamic>{
      "id":endpoint_id,
      "listener":null,
      "peers":<dynamic>[],
      "network":network
    };
    states[endpoint_id] = state;
  }
  return state;
}

ensure_network_targets_loop(network, peer_ids, index) {
  if(index >= peer_ids.length){
    return null;
  }
  ensure_network_state(network,peer_ids[index]);
  return ensure_network_targets_loop(network,peer_ids,index + 1);
}

configure_network_links_loop(network, links, endpoint_ids, index) {
  if(index >= endpoint_ids.length){
    return network;
  }
  var endpoint_id = endpoint_ids[index];
  var peer_ids = network_targets(links[endpoint_id]);
  var state = ensure_network_state(network,endpoint_id);
  state["peers"] = peer_ids;
  ensure_network_targets_loop(network,peer_ids,0);
  return configure_network_links_loop(network,links,endpoint_ids,index + 1);
}

deliver_network_loop(network, state, peer_ids, text, index) {
  if(index >= peer_ids.length){
    return Future.sync(() {
      return true;
    });
  }
  var peer_id = peer_ids[index];
  var peer = (network["states"])[peer_id];
  if(null == peer){
    throw "wire peer not found - " + peer_id;
  }
  var listener = peer["listener"];
  if(null == listener){
    throw "wire peer not started - " + peer_id;
  }
  var output = listener(
    <dynamic, dynamic>{"text":text},
    <dynamic, dynamic>{"wire":state["id"],"peer":peer_id}
  );
  return ((Future.sync(() => ((null != ((null != output) && (("Future" == (output.runtimeType).toString()) || (output.runtimeType).toString().startsWith("Future<")))) && (false != ((null != output) && (("Future" == (output.runtimeType).toString()) || (output.runtimeType).toString().startsWith("Future<"))))) ? output : Future.sync(() {
    return output;
  }))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
    return deliver_network_loop(network,state,peer_ids,text,index + 1);
  },<dynamic>[value]); });
}

memory_endpoint(state) {
  var write_fn = (text) {
    var network = state["network"];
    var peer_ids = state["peers"];
    if(null != network){
      if(0 == peer_ids.length){
        throw "wire endpoint missing peers";
      }
      return deliver_network_loop(network,state,peer_ids,text,0);
    }
    var peer = state["peer"];
    if(null == peer){
      throw "wire endpoint missing peer";
    }
    var listener = peer["listener"];
    if(null == listener){
      throw "wire peer not started";
    }
    return listener(
      <dynamic, dynamic>{"text":text},
      <dynamic, dynamic>{"wire":state["id"],"peer":peer["id"]}
    );
  };
  var start_fn = (listener) {
    state["listener"] = listener;
    return state;
  };
  var stop_fn = (_) {
    state["listener"] = null;
    return true;
  };
  return <dynamic, dynamic>{
    "meta":<dynamic, dynamic>{"kind":"wire.memory","id":state["id"]},
    "write_fn":write_fn,
    "start_fn":start_fn,
    "stop_fn":stop_fn
  };
}

create_network_endpoints_loop(network, endpoint_ids, out, index) {
  if(index >= endpoint_ids.length){
    return out;
  }
  var endpoint_id = endpoint_ids[index];
  out[endpoint_id] = memory_endpoint(ensure_network_state(network,endpoint_id));
  return create_network_endpoints_loop(network,endpoint_ids,out,index + 1);
}

text_endpoint(endpoint_source) {
  var current_endpoint = null;
  var current_listener = null;
  var current_callback = null;
  var source_create_fn = endpoint_source["create_fn"];
  var send_fn = (frame) {
    var endpoint = current_endpoint;
    if(null == endpoint){
      if(!((source_create_fn.runtimeType).toString().contains("Function") || (source_create_fn.runtimeType).toString().contains("=>") || (source_create_fn).toString().startsWith("Closure"))){
        endpoint = endpoint_source;
      }
    }
    if(null == endpoint){
      throw "json endpoint not started";
    }
    var raw_write_fn = endpoint["write_fn"];
    if(!((raw_write_fn.runtimeType).toString().contains("Function") || (raw_write_fn.runtimeType).toString().contains("=>") || (raw_write_fn).toString().startsWith("Closure"))){
      throw "json endpoint missing write implementation";
    }
    return Function.apply(
      (raw_write_fn as Function),
      <dynamic>[node_json.encode_frame(frame)]
    );
  };
  var start_fn = (listener) {
    var callback = (event, ctx) {
      var text = event_text(event);
      var frame = node_json.decode_frame(text);
      ctx = (ctx ?? <dynamic, dynamic>{});
      ctx["raw"] = event;
      ctx["payload"] = text;
      return listener(frame,ctx);
    };
    current_callback = callback;
    if((source_create_fn.runtimeType).toString().contains("Function") || (source_create_fn.runtimeType).toString().contains("=>") || (source_create_fn).toString().startsWith("Closure")){
      current_endpoint = Function.apply((source_create_fn as Function),<dynamic>[callback]);
      current_listener = current_endpoint;
      return current_endpoint;
    }
    else{
      current_endpoint = endpoint_source;
      var raw_start_fn = current_endpoint["start_fn"];
      if(null == raw_start_fn){
        current_listener = current_endpoint;
        return current_endpoint;
      }
      current_listener = Function.apply((raw_start_fn as Function),<dynamic>[callback]);
      if(null != current_listener){
        return current_listener;
      }
      return current_endpoint;
    }
  };
  var stop_fn = (_) {
    var endpoint = current_endpoint;
    if(null == endpoint){
      endpoint = endpoint_source;
    }
    var raw_stop_fn = null;
    if(("Map" == (endpoint.runtimeType).toString()) || (endpoint.runtimeType).toString().startsWith("_Map") || (endpoint.runtimeType).toString().startsWith("LinkedMap")){
      raw_stop_fn = endpoint["stop_fn"];
    }
    if((raw_stop_fn.runtimeType).toString().contains("Function") || (raw_stop_fn.runtimeType).toString().contains("=>") || (raw_stop_fn).toString().startsWith("Closure")){
      Function.apply((raw_stop_fn as Function),<dynamic>[current_listener]);
    }
    current_endpoint = null;
    current_listener = null;
    current_callback = null;
    return true;
  };
  return <dynamic, dynamic>{
    "meta":<dynamic, dynamic>{"kind":"json"},
    "send_fn":send_fn,
    "start_fn":start_fn,
    "stop_fn":stop_fn
  };
}

memory_pair(opts) {
  var config = opts ?? <dynamic, dynamic>{};
  var left_state = <dynamic, dynamic>{"id":config["left_id"] ?? "left","listener":null,"peer":null};
  var right_state = <dynamic, dynamic>{
    "id":config["right_id"] ?? "right",
    "listener":null,
    "peer":null
  };
  left_state["peer"] = right_state;
  right_state["peer"] = left_state;
  return <dynamic, dynamic>{
    "left":memory_endpoint(left_state),
    "right":memory_endpoint(right_state)
  };
}

memory_network(opts) {
  var config = opts ?? <dynamic, dynamic>{};
  var links = config.containsKey("links") ? config["links"] : config;
  var network = <dynamic, dynamic>{"states":<dynamic, dynamic>{}};
  configure_network_links_loop(network,links,List<dynamic>.from(( links ).keys),0);
  return create_network_endpoints_loop(
    network,
    List<dynamic>.from(( network["states"] ).keys),
    <dynamic, dynamic>{},
    0
  );
}

link_pair(server, client) {
  var wire = memory_pair(<dynamic, dynamic>{"left_id":"client","right_id":"server"});
  return Future.wait(List<Future<dynamic>>.from(( <dynamic>[
    main.attach_transport(client,"server",text_endpoint(wire["left"])),
    main.attach_transport(server,"client",text_endpoint(wire["right"]))
  ] ).map((entry) => Future.sync(() => entry))));
}