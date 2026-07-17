const websocket = require("@xtalk/net/ws-native.js")

var IPhxFrame = [
  "encode_frame",
  "decode_frame",
  "frame",
  "join_frame",
  "leave_frame",
  "push_frame",
  "send_frame",
  "join",
  "leave",
  "push"
];

function extract_message_data(message){
  if("string" == (typeof message)){
    return message;
  }
  else if(null != message["data"]){
    return message["data"];
  }
  else if(null != message["body"]){
    return message["body"];
  }
  else{
    return message;
  }
}

function decode_frame(message){
  let data = extract_message_data(message);
  if(!("string" == (typeof data))){
    return data;
  }
  return JSON.parse(data);
}

function get_frame_ref(opts){
  return String(opts["ref"] || opts["join_ref"] || Date.now());
}

function make_frame(topic,event,payload,opts){
  let ref = get_frame_ref(opts);
  let join_ref = opts["join_ref"] || ref;
  return {
    "topic":topic,
    "event":event,
    "payload":payload || {},
    "ref":ref,
    "join_ref":join_ref
  };
}

function make_frame_join(payload,opts){
  let topic = opts["topic"];
  if(null == topic){
    throw "Phoenix channel missing topic";
  }
  return make_frame(topic,"phx_join",payload || {},opts || {});
}

function make_frame_leave(opts){
  let topic = opts["topic"];
  if(null == topic){
    throw "Phoenix channel missing topic";
  }
  return make_frame(topic,"phx_leave",{},opts || {});
}

function make_frame_heartbeat(opts){
  let ref = get_frame_ref(opts || {});
  return {
    "topic":"phoenix",
    "event":"heartbeat",
    "payload":{},
    "ref":ref,
    "join_ref":ref
  };
}

function encode_frame(frame){
  return {
    "join_ref":frame["join_ref"] || frame["ref"],
    "ref":frame["ref"],
    "topic":frame["topic"],
    "event":frame["event"],
    "payload":frame["payload"] || {}
  };
}

function send_frame(client,frame){
  return websocket.send(client,JSON.stringify(encode_frame(frame)));
}

function wrap_phoenix(handlers){
  return function (event){
    let frame = decode_frame(event);
    let handler = handlers[frame["event"]];
    if("function" == (typeof handler)){
      handler(frame);
    }
  };
}

function start_heartbeat(client){
  return websocket.start_heartbeat(client,"phoenix.default",function (client,name){
    send_frame(client,make_frame_heartbeat({}));
  },30000);
}

module.exports = {
  ["IPhxFrame"]:IPhxFrame,
  ["extract_message_data"]:extract_message_data,
  ["decode_frame"]:decode_frame,
  ["get_frame_ref"]:get_frame_ref,
  ["make_frame"]:make_frame,
  ["make_frame_join"]:make_frame_join,
  ["make_frame_leave"]:make_frame_leave,
  ["make_frame_heartbeat"]:make_frame_heartbeat,
  ["encode_frame"]:encode_frame,
  ["send_frame"]:send_frame,
  ["wrap_phoenix"]:wrap_phoenix,
  ["start_heartbeat"]:start_heartbeat
}