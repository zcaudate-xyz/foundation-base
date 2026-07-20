import 'package:xtalk_lang/common-protocol.dart' as protocol;
import 'package:xtalk_net/http-util.dart' as util;
import 'dart:io';
import 'dart:async';



var IHttpClient = Function.apply((protocol.create_protocol_fn as Function),<dynamic>[
  "xt.net.http_fetch/IHttpClient",
  <dynamic, dynamic>{
  "request_http":<dynamic, dynamic>{"name":"request_http","arglist":<dynamic>["client","input"]}
}
]);

request_http(client, input) {
  var method_fn = protocol.protocol_method(client,"xt.net.http_fetch/IHttpClient","request_http");
  return Function.apply((method_fn as Function),<dynamic>[client,input]);
}

var REQUEST_FIELDS = <dynamic>["method","url","query","headers","body","timeout","opts"];

var RESPONSE_FIELDS = <dynamic>["status","headers","body","error"];

prepare_url(client, input) {
  var path = input["path"];
  var url = input["url"];
  if(!(null == url)){
    return url;
  }
  var defaults = client["defaults"];
  var basepath = defaults["basepath"];
  var host = defaults["host"];
  var port = defaults["port"];
  var secured = defaults["secured"];
  return "http" + (((null != secured) && (false != secured)) ? "s" : "") + "://" + host + ":" + (port ?? 80).toString() + (basepath ?? "") + (path ?? "");
}

prepare_input(client, input) {
  var defaults = client["defaults"];
  var body = input["body"];
  var method = input["method"];
  var headers = xt.lang.common_data.obj_assign(
    xt.lang.common_data.obj_assign(<dynamic, dynamic>{},defaults["headers"]),
    input["headers"]
  );
  var output = <dynamic, dynamic>{
    "url":prepare_url(client,input),
    "method":method ?? "GET",
    "headers":headers
  };
  if(null != body){
    output = xt.lang.common_data.obj_assign(output,<dynamic, dynamic>{"body":body});
  }
  return output;
}

wrap_prepare_input(handler) {
  return (client, input) {
    var prepped = prepare_input(client,input);
    return Function.apply((handler as Function),<dynamic>[client,prepped]);
  };
}

wrap_normalise(handler) {
  return (client, input) {
    return ((Future.sync(() => Function.apply((handler as Function),<dynamic>[client,input]))) as Future<dynamic>).then((value) async { return await Function.apply((response) {
      return util.response_normalize(response);
    },<dynamic>[value]); });
  };
}

then_normalise(promise) {
  return ((Future.sync(() => promise)) as Future<dynamic>).then((value) async { return await Function.apply(util.response_normalize,<dynamic>[value]); });
}

prepare_middleware(client, handler) {
  var middleware = client["middleware"];
  var arr_51563 = middleware ?? <dynamic>[];
  for(var i51564 = 0; i51564 < arr_51563.length; ++i51564){
    var wrapper = arr_51563[i51564];
    handler = Function.apply((wrapper as Function),<dynamic>[handler]);
  };
  return handler;
}

prepare_handler(client, handler) {
  return wrap_normalise(prepare_middleware(client,wrap_prepare_input(handler)));
}