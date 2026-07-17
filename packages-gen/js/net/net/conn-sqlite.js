const sqlite3InitModule = require("@sqlite.org/sqlite-wasm")

const conn_sql = require("@xtalk/net/conn-sql.js")

function SqliteClient(defaults,raw){
  if(!globalThis["xt_lang_common_protocol$$IMPLEMENTATIONS"]["js.net.conn_sqlite/SqliteClient"]){
    globalThis["xt_lang_common_protocol$$IMPLEMENTATIONS"]["js.net.conn_sqlite/SqliteClient"] = true;
    xt.lang.common_protocol.register_protocol_impl(conn_sql.ISqlClient["on"],"js.net.conn_sqlite/SqliteClient",{
      "connect":client_connect,
      "disconnect":client_disconnect,
      "query":client_query,
      "query_async":client_query_async
    });
  }
  return {
    "::":"js.net.conn_sqlite/SqliteClient",
    "::/protocols":[conn_sql.ISqlClient["on"]],
    "defaults":defaults,
    "raw":raw
  };
}

function decode_json_scalar(value){
  if(("string" == (typeof value)) && (value.startsWith("[") || value.startsWith("{") || (value == "true") || (value == "false") || (value == "null"))){
    return JSON.parse(value);
  }
  else{
    return value;
  }
}

function raw_query(db,query){
  let columns = [];
  let values = db.exec({
    "sql":query,
    "rowMode":"array",
    "columnNames":columns,
    "returnValue":"resultRows"
  });
  if((1 == values.length) && (1 == values[0].length)){
    return decode_json_scalar(values[0][0]);
  }
  return columns.length ? [{"columns":columns,"values":values}] : values;
}

function raw_init(sqlite3,opts){
  let config = opts || {};
  let filename = config["filename"] || ":memory:";
  let flags = config["flags"] || "c";
  let conn = new sqlite3["oo1"]["DB"](filename,flags);
  return conn;
}

function client_connect(client,opts){
  let {defaults} = client;
  let init_module = sqlite3InitModule["default"] || sqlite3InitModule;
  return init_module().then(function (sqlite3){
    return raw_init(sqlite3,Object.assign(Object.assign({},defaults),opts));
  }).then(function (raw){
    client["raw"] = raw;
    return client;
  });
}

function client_disconnect(client){
  let {raw} = client;
  raw.close();
  return true;
}

function client_query(client,query){
  let {raw} = client;
  return raw_query(raw,query);
}

function client_query_async(client,query){
  let {raw} = client;
  return Promise.resolve().then(function (){
    return raw_query(raw,query);
  });
}

function create(defaults){
  return SqliteClient(defaults,null);
}

module.exports = {
  ["decode_json_scalar"]:decode_json_scalar,
  ["raw_query"]:raw_query,
  ["raw_init"]:raw_init,
  ["client_connect"]:client_connect,
  ["client_disconnect"]:client_disconnect,
  ["client_query"]:client_query,
  ["client_query_async"]:client_query_async,
  ["SqliteClient"]:SqliteClient,
  ["create"]:create
}