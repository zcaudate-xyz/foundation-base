const Postgres = require("pg")

const xtd = require("@xtalk/lang/common-data.js")

const protocol = require("@xtalk/lang/common-protocol.js")

const conn_sql = require("@xtalk/net/conn-sql.js")

function PostgresClient(defaults,raw){
  globalThis["xt_lang_common_protocol$$IMPLEMENTATIONS"]["js.net.conn_postgres/PostgresClient"] = true;
  protocol.register_protocol_impl(conn_sql.ISqlClient["on"],"js.net.conn_postgres/PostgresClient",{
    "connect":client_connect,
    "disconnect":client_disconnect,
    "query":function (client,input){
        throw "Not Allowed";
      },
    "query_async":client_query_async
  });
  return {
    "::":"js.net.conn_postgres/PostgresClient",
    "::/protocols":[conn_sql.ISqlClient["on"]],
    "::/protocol-impls":{
        [conn_sql.ISqlClient["on"]]:{
          "connect":client_connect,
          "disconnect":client_disconnect,
          "query":function (client,input){
            throw "Not Allowed";
          },
          "query_async":client_query_async
        }
      },
    "defaults":defaults,
    "raw":raw
  };
}

function coerce_number_string(value){
  if(!("string" == (typeof value))){
    return value;
  }
  let trimmed = value.trim();
  if(trimmed == ""){
    return value;
  }
  if(trimmed.match("^[+-]?(?:\\d+(?:\\.\\d+)?|\\.\\d+)(?:[eE][+-]?\\d+)?$")){
    return Number(trimmed);
  }
  else{
    return value;
  }
}

function normalise_scalar_output(value){
  if((null == value) || ("boolean" == (typeof value)) || Array.isArray(value) || ((null != value) && ("object" == (typeof value)) && !Array.isArray(value))){
    return value;
  }
  else if("string" == (typeof value)){
    return coerce_number_string(value);
  }
  else{
    return value;
  }
}

function normalise_query_output(res){
  let {rows} = res;
  if((1 == rows.length) && (1 == xtd.obj_keys(xtd.first(rows)).length)){
    return normalise_scalar_output(xtd.obj_first_val(xtd.first(rows)));
  }
  else{
    return rows;
  }
}

function client_connect(client,opts){
  let {defaults} = client;
  let conn = new Postgres.Client(Object.assign(Object.assign({
    "host":"127.0.0.1",
    "port":5432,
    "user":"postgres",
    "password":"postgres",
    "database":"postgres"
  },defaults),opts));
  return conn.connect().then(function (){
    client["raw"] = conn;
    return client;
  });
}

function client_disconnect(client){
  let {raw} = client;
  raw.end();
  delete(client["raw"]);
  return client;
}

function client_query_async(client,input){
  let {raw} = client;
  return raw.query(input).then(normalise_query_output);
}

function create(defaults){
  return PostgresClient(defaults,null);
}

module.exports = {
  ["coerce_number_string"]:coerce_number_string,
  ["normalise_scalar_output"]:normalise_scalar_output,
  ["normalise_query_output"]:normalise_query_output,
  ["client_connect"]:client_connect,
  ["client_disconnect"]:client_disconnect,
  ["client_query_async"]:client_query_async,
  ["PostgresClient"]:PostgresClient,
  ["create"]:create
}