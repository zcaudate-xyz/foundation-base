import 'package:xtalk_lang/common-protocol.dart' as proto;


var IRedisClient = Function.apply((proto.create_protocol_fn as Function),<dynamic>[
  "xt.net.conn_redis/IRedisClient",
  <dynamic, dynamic>{
  "connect":<dynamic, dynamic>{"name":"connect","arglist":<dynamic>["client","opts"]},
  "disconnect":<dynamic, dynamic>{"name":"disconnect","arglist":<dynamic>["client"]},
  "exec":<dynamic, dynamic>{"name":"exec","arglist":<dynamic>["client","command","args"]}
}
]);

connect(client, opts) {
  var method_fn = proto.protocol_method(client,"xt.net.conn_redis/IRedisClient","connect");
  return Function.apply((method_fn as Function),<dynamic>[client,opts]);
}

disconnect(client) {
  var method_fn = proto.protocol_method(client,"xt.net.conn_redis/IRedisClient","disconnect");
  return Function.apply((method_fn as Function),<dynamic>[client]);
}

exec(client, command, args) {
  var method_fn = proto.protocol_method(client,"xt.net.conn_redis/IRedisClient","exec");
  return Function.apply((method_fn as Function),<dynamic>[client,command,args]);
}