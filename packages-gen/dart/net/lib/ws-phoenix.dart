import 'package:xtalk_net/ws-native.dart' as websocket;

var IPhxFrame = <dynamic>[
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

extract_message_data(message) {
  if("String" == (message.runtimeType).toString()){
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

decode_frame(message) {
  var data = extract_message_data(message);
  if(!("String" == (data.runtimeType).toString())){
    return data;
  }
  return jsonDecode(data);
}

get_frame_ref(opts) {
  return (opts["ref"] ?? opts["join_ref"] ?? DateTime.now().millisecondsSinceEpoch).toString();
}

make_frame(topic, event, payload, opts) {
  var ref = get_frame_ref(opts);
  var join_ref = opts["join_ref"] ?? ref;
  return <dynamic, dynamic>{
    "topic":topic,
    "event":event,
    "payload":payload ?? <dynamic, dynamic>{},
    "ref":ref,
    "join_ref":join_ref
  };
}

make_frame_join(payload, opts) {
  var topic = opts["topic"];
  if(null == topic){
    throw "Phoenix channel missing topic";
  }
  return make_frame(
    topic,
    "phx_join",
    payload ?? <dynamic, dynamic>{},
    opts ?? <dynamic, dynamic>{}
  );
}

make_frame_leave(opts) {
  var topic = opts["topic"];
  if(null == topic){
    throw "Phoenix channel missing topic";
  }
  return make_frame(
    topic,
    "phx_leave",
    <dynamic, dynamic>{},
    opts ?? <dynamic, dynamic>{}
  );
}

make_frame_heartbeat(opts) {
  var ref = get_frame_ref(opts ?? <dynamic, dynamic>{});
  return <dynamic, dynamic>{
    "topic":"phoenix",
    "event":"heartbeat",
    "payload":<dynamic, dynamic>{},
    "ref":ref,
    "join_ref":ref
  };
}

encode_frame(frame) {
  return <dynamic, dynamic>{
    "join_ref":frame["join_ref"] ?? frame["ref"],
    "ref":frame["ref"],
    "topic":frame["topic"],
    "event":frame["event"],
    "payload":frame["payload"] ?? <dynamic, dynamic>{}
  };
}

send_frame(client, frame) {
  return websocket.send(client,jsonEncode(encode_frame(frame)));
}

wrap_phoenix(handlers) {
  return (event) {
    var frame = decode_frame(event);
    var handler = handlers[frame["event"]];
    if((handler.runtimeType).toString().contains("Function") || (handler.runtimeType).toString().contains("=>") || (handler).toString().startsWith("Closure")){
      Function.apply((handler as Function),<dynamic>[frame]);
    }
  };
}

start_heartbeat(client) {
  return websocket.start_heartbeat(client,"phoenix.default",(client, name) {
    return send_frame(client,make_frame_heartbeat(<dynamic, dynamic>{}));
  },30000);
}