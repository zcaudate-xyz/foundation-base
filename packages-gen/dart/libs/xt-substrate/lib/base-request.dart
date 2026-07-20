import 'package:xtalk_substrate/base-space.dart' as space;
import 'dart:async';


ensure_promise(value) {
  if((() {
    var dart_truthy__51226 = (null != value) && (("Future" == (value.runtimeType).toString()) || (value.runtimeType).toString().startsWith("Future<"));
    return (null != dart_truthy__51226) && (false != dart_truthy__51226);
  })() || ((("Map" == (value.runtimeType).toString()) || (value.runtimeType).toString().startsWith("_Map") || (value.runtimeType).toString().startsWith("LinkedMap")) && ((value["then"].runtimeType).toString().contains("Function") || (value["then"].runtimeType).toString().contains("=>") || (value["then"]).toString().startsWith("Closure")))){
    return value;
  }
  else{
    return Future.sync(() {
      return value;
    });
  }
}

add_pending(node, request, resolve, reject, meta) {
  var pending = node["pending"];
  var id = request["id"];
  var entry = <dynamic, dynamic>{
    "resolve":resolve,
    "reject":reject,
    "request":request,
    "meta":meta ?? <dynamic, dynamic>{}
  };
  pending[id] = entry;
  return entry;
}

remove_pending(node, request_id) {
  var pending = node["pending"];
  var entry = pending[request_id];
  pending.remove(request_id);
  return entry;
}

settle_pending(node, response) {
  var reply_to = response["reply_to"];
  var entry = remove_pending(node,reply_to);
  if(null == entry){
    return null;
  }
  var resolve = entry["resolve"];
  var reject = entry["reject"];
  if(response["status"] == "ok"){
    Function.apply((resolve as Function),<dynamic>[response["data"]]);
  }
  else{
    Function.apply((reject as Function),<dynamic>[response]);
  }
  return entry;
}

invoke_handler(node, request) {
  var action = request["action"];
  var entry = (node["handlers"])[action];
  if(null == entry){
    throw "handler not found - " + action;
  }
  var handler = entry["fn"];
  var current_space = space.ensure_space(node,request["space"],null);
  return ensure_promise(Function.apply(
    handler,
    <dynamic>[current_space,request["args"],request,node]
  ));
}

response_body(response) {
  return ((Future.sync(() => ensure_promise(response))) as Future<dynamic>).then((value) async { return await Function.apply((frame) {
    if(frame["status"] == "ok"){
      return frame["data"];
    }
    else{
      throw frame;
    }
  },<dynamic>[value]); });
}