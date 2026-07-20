const Redis = require("redis")

const xtd = require("@xtalk/lang/common-data.js")

const conn_redis = require("@xtalk/net/conn-redis.js")

const protocol = require("@xtalk/lang/common-protocol.js")

function JsRedisClient(defaults,raw){
  globalThis["xt_lang_common_protocol$$IMPLEMENTATIONS"]["js.net.conn_redis/JsRedisClient"] = true;
  protocol.register_protocol_impl(conn_redis.IRedisClient["on"],"js.net.conn_redis/JsRedisClient",{
    "connect":client_connect,
    "disconnect":client_disconnect,
    "exec":client_exec
  });
  return {
    "::":"js.net.conn_redis/JsRedisClient",
    "::/protocols":[conn_redis.IRedisClient["on"]],
    "::/protocol-impls":{
        [conn_redis.IRedisClient["on"]]:{
          "connect":client_connect,
          "disconnect":client_disconnect,
          "exec":client_exec
        }
      },
    "defaults":defaults,
    "raw":raw
  };
}

function client_connect(client,opts){
  let {defaults} = client;
  let env = xtd.obj_assign(xtd.obj_clone(defaults),opts);
  let url = "redis://" + (env["host"] || "127.0.0.1") + ":" + (env["port"] || "6379");
  let raw = Redis.createClient({"url":url});
  return raw.connect().then(function (){
    client["raw"] = raw;
    return client;
  });
}

function client_disconnect(client){
  let {raw} = client;
  return raw.quit();
}

function client_exec(client,command,args){
  let {raw} = client;
  let input = xtd.arr_assign([command],args);
  return raw.sendCommand(input);
}

function create(defaults){
  return JsRedisClient(defaults || {},null);
}

module.exports = {
  ["client_connect"]:client_connect,
  ["client_disconnect"]:client_disconnect,
  ["client_exec"]:client_exec,
  ["JsRedisClient"]:JsRedisClient,
  ["create"]:create
}