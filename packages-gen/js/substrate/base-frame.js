const str = require("@xtalk/lang/common-string.js")

function rand_id(prefix,n){
  return (prefix || "") + str.str_rand(n);
}

function frame(kind,id,space,meta,extra){
  return Object.assign({
    "kind":kind,
    "id":id,
    "space":space || "__NODE__",
    "meta":meta || {}
  },extra || {});
}

function request_frame(space,action,args,meta){
  meta = (meta || {});
  return frame(
    "request",
    meta["id"] || rand_id("req-",6),
    space,
    meta,
    {"action":action,"args":args || []}
  );
}

function response_frame(reply_to,space,status,data,error,meta){
  meta = (meta || {});
  return frame(
    "response",
    meta["id"] || rand_id("res-",6),
    space,
    meta,
    {"reply_to":reply_to,"status":status,"data":data,"error":error}
  );
}

function response_ok_frame(reply_to,space,data,meta){
  return response_frame(reply_to,space,"ok",data,null,meta);
}

function response_error_frame(reply_to,space,error,meta){
  return response_frame(reply_to,space,"error",null,error,meta);
}

function stream_frame(space,signal,data,meta,cause){
  meta = (meta || {});
  return frame(
    "stream",
    meta["id"] || rand_id("evt-",6),
    space,
    meta,
    {"signal":signal,"data":data,"cause":cause}
  );
}

function request_framep(frame){
  return "request" == frame["kind"];
}

function response_framep(frame){
  return "response" == frame["kind"];
}

function stream_framep(frame){
  return "stream" == frame["kind"];
}

module.exports = {
  ["rand_id"]:rand_id,
  ["frame"]:frame,
  ["request_frame"]:request_frame,
  ["response_frame"]:response_frame,
  ["response_ok_frame"]:response_ok_frame,
  ["response_error_frame"]:response_error_frame,
  ["stream_frame"]:stream_frame,
  ["request_framep"]:request_framep,
  ["response_framep"]:response_framep,
  ["stream_framep"]:stream_framep
}