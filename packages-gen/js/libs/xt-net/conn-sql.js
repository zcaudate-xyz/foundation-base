const proto = require("@xtalk/lang/common-protocol.js")

var ISqlClient = proto.create_protocol_fn("xt.net.conn_sql/ISqlClient",{
  "connect":{"name":"connect","arglist":["client","opts"]},
  "disconnect":{"name":"disconnect","arglist":["client"]},
  "query":{"name":"query","arglist":["client","input"]},
  "query_async":{"name":"query_async","arglist":["client","input"]}
});

function connect(client,opts){
  let method_fn = proto.protocol_method(client,"xt.net.conn_sql/ISqlClient","connect");
  return method_fn(client,opts);
}

function disconnect(client){
  let method_fn = proto.protocol_method(client,"xt.net.conn_sql/ISqlClient","disconnect");
  return method_fn(client);
}

function query(client,input){
  let method_fn = proto.protocol_method(client,"xt.net.conn_sql/ISqlClient","query");
  return method_fn(client,input);
}

function query_async(client,input){
  let method_fn = proto.protocol_method(client,"xt.net.conn_sql/ISqlClient","query_async");
  return method_fn(client,input);
}

function SqlConnection(raw,impl){
  globalThis["xt_lang_common_protocol$$IMPLEMENTATIONS"]["xt.net.conn_sql/SqlConnection"] = true;
  proto.register_protocol_impl(ISqlClient["on"],"xt.net.conn_sql/SqlConnection",{
    "connect":connection_connect,
    "disconnect":connection_disconnect,
    "query":connection_query,
    "query_async":connection_query_async
  });
  return {
    "::":"xt.net.conn_sql/SqlConnection",
    "::/protocols":[ISqlClient["on"]],
    "::/protocol-impls":{
        [ISqlClient["on"]]:{
          "connect":connection_connect,
          "disconnect":connection_disconnect,
          "query":connection_query,
          "query_async":connection_query_async
        }
      },
    "raw":raw,
    "impl":impl
  };
}

function connection_disconnect(client){
  let impl = client["impl"];
  let raw = client["raw"];
  let disconnect_fn = impl["disconnect"];
  return disconnect_fn(raw);
}

function connection_query(client,input){
  let impl = client["impl"];
  let raw = client["raw"];
  let query_fn = impl["query"] || impl["query_sync"];
  return query_fn(raw,input);
}

function connection_query_async(client,input){
  let impl = client["impl"];
  let raw = client["raw"];
  let query_async_fn = impl["query_async"] || impl["query"];
  return query_async_fn(raw,input);
}

function connection_connect(client,opts){
  let impl = client["impl"];
  let connect_fn = impl["connect"];
  if(null == connect_fn){
    return client;
  }
  else{
    return connect_fn(client["raw"],opts);
  }
}

function connection_create(raw,impl){
  return SqlConnection(raw,impl);
}

module.exports = {
  ["ISqlClient"]:ISqlClient,
  ["connect"]:connect,
  ["disconnect"]:disconnect,
  ["query"]:query,
  ["query_async"]:query_async,
  ["connection_disconnect"]:connection_disconnect,
  ["connection_query"]:connection_query,
  ["connection_query_async"]:connection_query_async,
  ["connection_connect"]:connection_connect,
  ["SqlConnection"]:SqlConnection,
  ["connection_create"]:connection_create
}