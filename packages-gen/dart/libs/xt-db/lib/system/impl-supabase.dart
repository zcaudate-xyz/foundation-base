import 'package:xtalk_db/system/impl-supabase-realtime.dart' as impl_realtime;
import 'package:xtalk_lang/common-data.dart' as xtd;
import 'package:xtalk_lang/common-protocol.dart' as proto;
import 'package:xtalk_db/system/impl-common.dart' as impl_common;
import 'package:xtalk_db/text/pgrest-graph.dart' as pgrest_graph;
import 'package:xtalk_net/addon-supabase.dart' as addon;
import 'package:xtalk_db/system/impl-supabase-ws.dart' as supabase_ws;
import 'package:xtalk_net/http-fetch.dart' as http_fetch;
import 'package:xtalk_net/http-util.dart' as http_util;
import 'dart:async';










ImplSupabase(client, schema, lookup, state, listeners, opts, metadata) {
  (__globals__["xt.lang.common_protocol/IMPLEMENTATIONS"] ??= <dynamic, dynamic>{})["xt.db.system.impl_supabase/ImplSupabase"] = true;
  proto.register_protocol_impl(
    impl_common.ISourceRemote["on"],
    "xt.db.system.impl_supabase/ImplSupabase",
    <dynamic, dynamic>{"pull_async":pull_async,"rpc_call_async":rpc_call_async}
  );
  proto.register_protocol_impl(impl_common.ISourceListener["on"],"xt.db.system.impl_supabase/ImplSupabase",<dynamic, dynamic>{
    "add_db_listener":impl_common.add_db_listener_default,
    "remove_db_listener":impl_common.remove_db_listener_default,
    "get_db_listener":impl_common.get_db_listener_default
  });
  proto.register_protocol_impl(impl_common.ISourceRealtime["on"],"xt.db.system.impl_supabase/ImplSupabase",<dynamic, dynamic>{
    "subscribe_db":impl_realtime.subscribe,
    "unsubscribe_db":impl_realtime.unsubscribe
  });
  proto.register_protocol_impl(
    supabase_ws.ISupabaseWebsocketFactory["on"],
    "xt.db.system.impl_supabase/ImplSupabase",
    <dynamic, dynamic>{"create_ws_client":create_ws_client}
  );
  return <dynamic, dynamic>{
    "::/protocol-impls":<dynamic, dynamic>{
        impl_common.ISourceRemote["on"]:<dynamic, dynamic>{"pull_async":pull_async,"rpc_call_async":rpc_call_async},
        impl_common.ISourceListener["on"]:<dynamic, dynamic>{
          "add_db_listener":impl_common.add_db_listener_default,
          "remove_db_listener":impl_common.remove_db_listener_default,
          "get_db_listener":impl_common.get_db_listener_default
        },
        impl_common.ISourceRealtime["on"]:<dynamic, dynamic>{
          "subscribe_db":impl_realtime.subscribe,
          "unsubscribe_db":impl_realtime.unsubscribe
        },
        supabase_ws.ISupabaseWebsocketFactory["on"]:<dynamic, dynamic>{"create_ws_client":create_ws_client}
      },
    "schema":schema,
    "lookup":lookup,
    "opts":opts,
    "::":"xt.db.system.impl_supabase/ImplSupabase",
    "metadata":metadata,
    "::/protocols":<dynamic>[
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

cmd_pull_async(impl, tree) {
  var client = impl["client"];
  var lookup = impl["lookup"];
  var opts = impl["opts"];
  var schema = impl["schema"];
  var request = pgrest_graph.select(schema,tree,opts);
  var table_name = tree[0];
  var schema_name = lookup[table_name]["schema"];
  var headers = xtd.obj_assign(
    xtd.obj_assign(<dynamic, dynamic>{},request["headers"]),
    ((null != schema_name) && (false != schema_name)) ? <dynamic, dynamic>{"Accept-Profile":schema_name,"Content-Profile":schema_name} : null
  );
  return xtd.obj_assign(
    <dynamic, dynamic>{"path":request["url"],"method":"GET"},
    <dynamic, dynamic>{"headers":headers}
  );
}

pull_async(impl, tree) {
  var client = impl["client"];
  var input = cmd_pull_async(impl,tree);
  return ((Future.sync(() => http_fetch.request_http(client,input))) as Future<dynamic>).then((value) async { return await Function.apply(http_util.get_body_data,<dynamic>[value]); });
}

cmd_rpc_call_async(impl, rpc_spec, args, opts) {
  var input_spec = rpc_spec["input"] ?? <dynamic>[];
  var body = <dynamic, dynamic>{};
  opts = (opts ?? <dynamic, dynamic>{});
  var arr_51980 = input_spec;
  for(var i = 0; i < arr_51980.length; ++i){
    var input = arr_51980[i];
    var key = input["symbol"] ?? input["name"] ?? null;
    if(null != key){
      body[key] = args[i];
    }
  };
  var schema = rpc_spec["schema"];
  var headers = xtd.obj_clone(opts["headers"]);
  if(null != schema){
    headers["Content-Profile"] = schema;
    headers["Accept-Profile"] = schema;
  }
  return addon.cmd_rpc_call(
    rpc_spec["id"],
    body,
    xtd.obj_assign(xtd.obj_clone(opts),<dynamic, dynamic>{"headers":headers})
  );
}

rpc_call_async(impl, rpc_spec, args, opts) {
  var client = impl["client"];
  var input = cmd_rpc_call_async(impl,rpc_spec,args,opts);
  return ((Future.sync(() => http_fetch.request_http(client,input))) as Future<dynamic>).then((value) async { return await Function.apply(http_util.get_body_data,<dynamic>[value]); });
}

create_ws_client(impl, defaults) {
  var create_fn = xtd.get_in(impl,<dynamic>["client","create_ws_client"]);
  if(null == create_fn){
    throw "Supabase websocket client factory is not configured";
  }
  return Function.apply((create_fn as Function),<dynamic>[defaults]);
}

impl_supabase(client, schema, lookup) {
  var impl = ImplSupabase(client,schema,lookup,<dynamic, dynamic>{
    "session":null,
    "auto_refresh":null,
    "realtimes":<dynamic, dynamic>{}
  },<dynamic, dynamic>{},<dynamic, dynamic>{},<dynamic, dynamic>{});
  impl["::/override"] = <dynamic, dynamic>{"create_ws_client":create_ws_client};
  return impl;
}