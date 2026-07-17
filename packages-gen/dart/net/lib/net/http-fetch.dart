import 'dart:io' as io;

import 'dart:convert' as convert;

import 'package:xtalk_lang/common-protocol.dart' as protocol;

import 'package:xtalk_net/http-fetch.dart' as fetch;

import 'package:xtalk_net/http-util.dart' as util;

DartHttpFetchClient(defaults, middleware) {
  if(!(() {
    var dart_truthy__42028 = (__globals__["xt.lang.common_protocol/IMPLEMENTATIONS"] ??= <dynamic, dynamic>{})["dart.net.http_fetch/DartHttpFetchClient"];
    return (null != dart_truthy__42028) && (false != dart_truthy__42028);
  })()){
    (__globals__["xt.lang.common_protocol/IMPLEMENTATIONS"] ??= <dynamic, dynamic>{})["dart.net.http_fetch/DartHttpFetchClient"] = true;
    protocol.register_protocol_impl(
      fetch.IHttpClient["on"],
      "dart.net.http_fetch/DartHttpFetchClient",
      <dynamic, dynamic>{"request_http":request_http}
    );
  }
  return <dynamic, dynamic>{
    "::":"dart.net.http_fetch/DartHttpFetchClient",
    "::/protocols":<dynamic>[fetch.IHttpClient["on"]],
    "defaults":defaults,
    "middleware":middleware
  };
}

request_http_raw(client, input) {
  var raw = client["raw"];
  raw = (raw ?? <dynamic, dynamic>{});
  var request = fetch.prepare_input(client,input);
  var request_fn = raw["request"];
  if((request_fn.runtimeType).toString().contains("Function") || (request_fn.runtimeType).toString().contains("=>") || (request_fn).toString().startsWith("Closure")){
    var output = Function.apply(
      (request_fn as Function),
      <dynamic>[request,<dynamic, dynamic>{}]
    );
    return util.response_normalize(output);
  }
  else{
    var http_client = io.HttpClient();
    var uri = Uri.parse(request["url"]);
    return http_client.openUrl(request["method"],uri).then((req) {
      for(var entry_42029 in request["headers"].entries){
        var k = entry_42029.key;
        var v = entry_42029.value;
        req.headers.set(k,v);
      };
      if(null != request["body"]){
        req.write(request["body"]);
      }
      var response_future = req.close();
      return response_future.then((res) {
        var body_future = res.transform(convert.utf8.decoder).join();
        return body_future.then((body) {
          return <dynamic, dynamic>{
            "status":res.statusCode,
            "headers":<dynamic, dynamic>{},
            "body":util.decode_body(body)
          };
        });
      });
    });
  }
}

request_http(client, input) {
  var handler = fetch.prepare_middleware(client,request_http_raw);
  return Function.apply((handler as Function),<dynamic>[client,input]);
}

create(defaults, middleware) {
  return DartHttpFetchClient(
    defaults ?? <dynamic, dynamic>{},
    middleware ?? <dynamic>[fetch.wrap_prepare_input]
  );
}