const impl_realtime = require("@xtalk/db/system/impl-supabase-realtime.js")

const xtd = require("@xtalk/lang/common-data.js")

const proto = require("@xtalk/lang/common-protocol.js")

const impl_common = require("@xtalk/db/system/impl-common.js")

const pgrest_graph = require("@xtalk/db/text/pgrest-graph.js")

const addon = require("@xtalk/net/addon-supabase.js")

const supabase_ws = require("@xtalk/db/system/impl-supabase-ws.js")

const http_fetch = require("@xtalk/net/http-fetch.js")

const http_util = require("@xtalk/net/http-util.js")

function ImplSupabase(client,schema,lookup,state,listeners,opts,metadata){
  if(!globalThis["xt_lang_common_protocol$$IMPLEMENTATIONS"]["xt.db.system.impl_supabase/ImplSupabase"]){
    globalThis["xt_lang_common_protocol$$IMPLEMENTATIONS"]["xt.db.system.impl_supabase/ImplSupabase"] = true;
    proto.register_protocol_impl(
      impl_common.ISourceRemote["on"],
      "xt.db.system.impl_supabase/ImplSupabase",
      {"pull_async":pull_async,"rpc_call_async":rpc_call_async}
    );
    proto.register_protocol_impl(impl_common.ISourceListener["on"],"xt.db.system.impl_supabase/ImplSupabase",{
      "add_db_listener":impl_common.add_db_listener_default,
      "remove_db_listener":impl_common.remove_db_listener_default,
      "get_db_listener":impl_common.get_db_listener_default
    });
    proto.register_protocol_impl(impl_common.ISourceRealtime["on"],"xt.db.system.impl_supabase/ImplSupabase",{
      "subscribe_db":impl_realtime.subscribe,
      "unsubscribe_db":impl_realtime.unsubscribe
    });
    proto.register_protocol_impl(
      supabase_ws.ISupabaseWebsocketFactory["on"],
      "xt.db.system.impl_supabase/ImplSupabase",
      {"create_ws_client":create_ws_client}
    );
  }
  return {
    "schema":schema,
    "lookup":lookup,
    "opts":opts,
    "::":"xt.db.system.impl_supabase/ImplSupabase",
    "metadata":metadata,
    "::/protocols":[
        impl_common.ISourceRemote["on"],
        impl_common.ISourceListener["on"],
        impl_common.ISourceRealtime["on"],
        supabase_ws.ISupabaseWebsocketFactory["on"]
      ],
    "state":state,
    "client":client,
    "listeners":listeners
  };
}

function cmd_pull_async(impl,tree){
  let {client,lookup,opts,schema} = impl;
  let request = pgrest_graph.select(schema,tree,opts);
  let table_name = tree[0];
  let schema_name = lookup[table_name]["schema"];
  let headers = Object.assign(
    Object.assign({},request["headers"]),
    schema_name ? {"Accept-Profile":schema_name,"Content-Profile":schema_name} : null
  );
  return Object.assign({"path":request["url"],"method":"GET"},{"headers":headers});
}

function pull_async(impl,tree){
  let {client} = impl;
  let input = cmd_pull_async(impl,tree);
  return http_fetch.request_http(client,input).then(http_util.get_body_data);
}

function cmd_rpc_call_async(impl,rpc_spec,args,opts){
  let input_spec = rpc_spec["input"] || [];
  let body = {};
  opts = (opts || {});
  for(let i = 0; i < input_spec.length; ++i){
    let input = input_spec[i];
    let key = input["symbol"] || input["name"] || null;
    if(null != key){
      body[key] = args[i];
    }
  };
  let schema = rpc_spec["schema"];
  let headers = Object.assign({},opts["headers"]);
  if(null != schema){
    headers["Content-Profile"] = schema;
    headers["Accept-Profile"] = schema;
  }
  return addon.cmd_rpc_call(
    rpc_spec["id"],
    body,
    Object.assign(Object.assign({},opts),{"headers":headers})
  );
}

function rpc_call_async(impl,rpc_spec,args,opts){
  let {client} = impl;
  let input = cmd_rpc_call_async(impl,rpc_spec,args,opts);
  return http_fetch.request_http(client,input).then(http_util.get_body_data);
}

function create_ws_client(impl,defaults){
  let create_fn = xtd.get_in(impl,["client","create_ws_client"]);
  if(null == create_fn){
    throw "Supabase websocket client factory is not configured";
  }
  return create_fn(defaults);
}

function impl_supabase(client,schema,lookup){
  let impl = ImplSupabase(
    client,
    schema,
    lookup,
    {"session":null,"auto_refresh":null,"realtimes":{}},
    {},
    {},
    {}
  );
  impl["::/override"] = {"create_ws_client":create_ws_client};
  return impl;
}

module.exports = {
  ["cmd_pull_async"]:cmd_pull_async,
  ["pull_async"]:pull_async,
  ["cmd_rpc_call_async"]:cmd_rpc_call_async,
  ["rpc_call_async"]:rpc_call_async,
  ["create_ws_client"]:create_ws_client,
  ["ImplSupabase"]:ImplSupabase,
  ["impl_supabase"]:impl_supabase
}