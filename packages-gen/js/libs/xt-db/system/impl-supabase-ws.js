const proto = require("@xtalk/lang/common-protocol.js")

var ISupabaseWebsocketFactory = proto.create_protocol_fn("xt.db.system.impl_supabase_ws/ISupabaseWebsocketFactory",{
  "create_ws_client":{"name":"create_ws_client","arglist":["impl","defaults"]}
});

function create_ws_client(impl,defaults){
  let method_fn = proto.protocol_method(
    impl,
    "xt.db.system.impl_supabase_ws/ISupabaseWebsocketFactory",
    "create_ws_client"
  );
  return method_fn(impl,defaults);
}

module.exports = {
  ["ISupabaseWebsocketFactory"]:ISupabaseWebsocketFactory,
  ["create_ws_client"]:create_ws_client
}