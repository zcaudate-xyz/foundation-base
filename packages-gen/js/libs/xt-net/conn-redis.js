const proto = require("@xtalk/lang/common-protocol.js")

var IRedisClient = proto.create_protocol_fn("xt.net.conn_redis/IRedisClient",{
  "connect":{"name":"connect","arglist":["client","opts"]},
  "disconnect":{"name":"disconnect","arglist":["client"]},
  "exec":{"name":"exec","arglist":["client","command","args"]}
});

function connect(client,opts){
  let method_fn = proto.protocol_method(client,"xt.net.conn_redis/IRedisClient","connect");
  return method_fn(client,opts);
}

function disconnect(client){
  let method_fn = proto.protocol_method(client,"xt.net.conn_redis/IRedisClient","disconnect");
  return method_fn(client);
}

function exec(client,command,args){
  let method_fn = proto.protocol_method(client,"xt.net.conn_redis/IRedisClient","exec");
  return method_fn(client,command,args);
}

module.exports = {
  ["IRedisClient"]:IRedisClient,
  ["connect"]:connect,
  ["disconnect"]:disconnect,
  ["exec"]:exec
}