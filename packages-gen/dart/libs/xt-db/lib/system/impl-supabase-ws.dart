import 'package:xtalk_lang/common-protocol.dart' as proto;


var ISupabaseWebsocketFactory = Function.apply((proto.create_protocol_fn as Function),<dynamic>[
  "xt.db.system.impl_supabase_ws/ISupabaseWebsocketFactory",
  <dynamic, dynamic>{
  "create_ws_client":<dynamic, dynamic>{
    "name":"create_ws_client",
    "arglist":<dynamic>["impl","defaults"]
  }
}
]);

create_ws_client(impl, defaults) {
  var method_fn = proto.protocol_method(
    impl,
    "xt.db.system.impl_supabase_ws/ISupabaseWebsocketFactory",
    "create_ws_client"
  );
  return Function.apply((method_fn as Function),<dynamic>[impl,defaults]);
}