import 'package:xtalk_lang/common-protocol.dart' as proto;

var ISqlClient = Function.apply((proto.create_protocol_fn as Function),<dynamic>[
  "xt.net.conn_sql/ISqlClient",
  <dynamic, dynamic>{
  "connect":<dynamic, dynamic>{"name":"connect","arglist":<dynamic>["client","opts"]},
  "disconnect":<dynamic, dynamic>{"name":"disconnect","arglist":<dynamic>["client"]},
  "query":<dynamic, dynamic>{"name":"query","arglist":<dynamic>["client","input"]},
  "query_async":<dynamic, dynamic>{"name":"query_async","arglist":<dynamic>["client","input"]}
}
]);

connect(client, opts) {
  var method_fn = proto.protocol_method(client,"xt.net.conn_sql/ISqlClient","connect");
  return Function.apply((method_fn as Function),<dynamic>[client,opts]);
}

disconnect(client) {
  var method_fn = proto.protocol_method(client,"xt.net.conn_sql/ISqlClient","disconnect");
  return Function.apply((method_fn as Function),<dynamic>[client]);
}

query(client, input) {
  var method_fn = proto.protocol_method(client,"xt.net.conn_sql/ISqlClient","query");
  return Function.apply((method_fn as Function),<dynamic>[client,input]);
}

query_async(client, input) {
  var method_fn = proto.protocol_method(client,"xt.net.conn_sql/ISqlClient","query_async");
  return Function.apply((method_fn as Function),<dynamic>[client,input]);
}

SqlConnection(raw, impl) {
  if(!(() {
    var dart_truthy__41960 = (__globals__["xt.lang.common_protocol/IMPLEMENTATIONS"] ??= <dynamic, dynamic>{})["xt.net.conn_sql/SqlConnection"];
    return (null != dart_truthy__41960) && (false != dart_truthy__41960);
  })()){
    (__globals__["xt.lang.common_protocol/IMPLEMENTATIONS"] ??= <dynamic, dynamic>{})["xt.net.conn_sql/SqlConnection"] = true;
    proto.register_protocol_impl(ISqlClient["on"],"xt.net.conn_sql/SqlConnection",<dynamic, dynamic>{
      "connect":connection_connect,
      "disconnect":connection_disconnect,
      "query":connection_query,
      "query_async":connection_query_async
    });
  }
  return <dynamic, dynamic>{
    "::":"xt.net.conn_sql/SqlConnection",
    "::/protocols":<dynamic>[ISqlClient["on"]],
    "raw":raw,
    "impl":impl
  };
}

connection_disconnect(client) {
  var impl = client["impl"];
  var raw = client["raw"];
  var disconnect_fn = impl["disconnect"];
  return Function.apply((disconnect_fn as Function),<dynamic>[raw]);
}

connection_query(client, input) {
  var impl = client["impl"];
  var raw = client["raw"];
  var query_fn = impl["query"] ?? impl["query_sync"];
  return Function.apply((query_fn as Function),<dynamic>[raw,input]);
}

connection_query_async(client, input) {
  var impl = client["impl"];
  var raw = client["raw"];
  var query_async_fn = impl["query_async"] ?? impl["query"];
  return Function.apply((query_async_fn as Function),<dynamic>[raw,input]);
}

connection_connect(client, opts) {
  var impl = client["impl"];
  var connect_fn = impl["connect"];
  if(null == connect_fn){
    return client;
  }
  else{
    return Function.apply((connect_fn as Function),<dynamic>[client["raw"],opts]);
  }
}

connection_create(raw, impl) {
  return SqlConnection(raw,impl);
}