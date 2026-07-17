const js_sqlite = require("@xtalk/db/net/conn-sqlite.js")

const js_ws = require("@xtalk/db/net/ws-native.js")

const js_postgres = require("@xtalk/db/net/conn-postgres.js")

const addon = require("@xtalk/net/addon-supabase.js")

const js_fetch = require("@xtalk/db/net/http-fetch.js")

function create_client(type,defaults){
  if(type == "sqlite"){
    return js_sqlite.create(defaults);
  }
  else if(type == "postgres"){
    return js_postgres.create(defaults);
  }
  else if(type == "supabase"){
    let client = js_fetch.create(defaults,addon.middleware_supabase());
    client["create_ws_client"] = js_ws.create;
    return client;
  }
  else{
    return null;
  }
}

module.exports = {["create_client"]:create_client}