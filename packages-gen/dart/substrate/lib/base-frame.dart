import 'package:xtalk_lang/common-string.dart' as str;

rand_id(prefix, n) {
  return (prefix ?? "") + str.str_rand(n);
}

frame(kind, id, space, meta, extra) {
  return xtd.obj_assign(<dynamic, dynamic>{
    "kind":kind,
    "id":id,
    "space":space ?? "__NODE__",
    "meta":meta ?? <dynamic, dynamic>{}
  },extra ?? <dynamic, dynamic>{});
}

request_frame(space, action, args, meta) {
  meta = (meta ?? <dynamic, dynamic>{});
  return frame(
    "request",
    meta["id"] ?? rand_id("req-",6),
    space,
    meta,
    <dynamic, dynamic>{"action":action,"args":args ?? <dynamic>[]}
  );
}

response_frame(reply_to, space, status, data, error, meta) {
  meta = (meta ?? <dynamic, dynamic>{});
  return frame(
    "response",
    meta["id"] ?? rand_id("res-",6),
    space,
    meta,
    <dynamic, dynamic>{"reply_to":reply_to,"status":status,"data":data,"error":error}
  );
}

response_ok_frame(reply_to, space, data, meta) {
  return response_frame(reply_to,space,"ok",data,null,meta);
}

response_error_frame(reply_to, space, error, meta) {
  return response_frame(reply_to,space,"error",null,error,meta);
}

stream_frame(space, signal, data, meta, cause) {
  meta = (meta ?? <dynamic, dynamic>{});
  return frame(
    "stream",
    meta["id"] ?? rand_id("evt-",6),
    space,
    meta,
    <dynamic, dynamic>{"signal":signal,"data":data,"cause":cause}
  );
}

request_framep(frame) {
  return "request" == frame["kind"];
}

response_framep(frame) {
  return "response" == frame["kind"];
}

stream_framep(frame) {
  return "stream" == frame["kind"];
}