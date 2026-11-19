import * as str from '../lang/common-string.mjs'

export function rand_id(prefix,n){
  return (prefix || "") + str.str_rand(n);
}

export function frame(kind,id,space,meta,extra){
  return Object.assign({
    "kind":kind,
    "id":id,
    "space":space || "__NODE__",
    "meta":meta || {}
  },extra || {});
}

export function request_frame(space,action,args,meta){
  meta = (meta || {});
  return frame(
    "request",
    meta["id"] || rand_id("req-",6),
    space,
    meta,
    {"action":action,"args":args || []}
  );
}

export function response_frame(reply_to,space,status,data,error,meta){
  meta = (meta || {});
  return frame(
    "response",
    meta["id"] || rand_id("res-",6),
    space,
    meta,
    {"reply_to":reply_to,"status":status,"data":data,"error":error}
  );
}

export function response_ok_frame(reply_to,space,data,meta){
  return response_frame(reply_to,space,"ok",data,null,meta);
}

export function response_error_frame(reply_to,space,error,meta){
  return response_frame(reply_to,space,"error",null,error,meta);
}

export function stream_frame(space,signal,data,meta,cause){
  meta = (meta || {});
  return frame(
    "stream",
    meta["id"] || rand_id("evt-",6),
    space,
    meta,
    {"signal":signal,"data":data,"cause":cause}
  );
}

export function request_framep(frame){
  return "request" == frame["kind"];
}

export function response_framep(frame){
  return "response" == frame["kind"];
}

export function stream_framep(frame){
  return "stream" == frame["kind"];
}