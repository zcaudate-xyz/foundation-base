import 'package:xtalk_lang/common-data.dart' as xtd;
import 'package:xtalk_substrate/page-proxy.dart' as page_proxy;
import 'package:xtalk_db/node/proxy-util.dart' as proxy_util;
import 'package:xtalk_substrate/substrate.dart' as substrate;
import 'dart:async';





var ACTIONS = <dynamic>[
  "@xt.supabase/sign-up",
  "@xt.supabase/sign-in",
  "@xt.supabase/sign-out",
  "@xt.supabase/refresh",
  "@xt.supabase/signed-in?",
  "@xt.supabase/current-session",
  "@xt.supabase/rpc-call",
  "@xt.supabase/query-table",
  "@xt.supabase/health",
  "@xt.supabase/admin-create-user",
  "@xt.supabase/admin-delete-user",
  "@xt.supabase/admin-generate-link",
  "@xt.supabase/admin-get-user",
  "@xt.supabase/admin-list-users",
  "@xt.supabase/admin-update-user",
  "@xt.supabase/authorize",
  "@xt.supabase/callback",
  "@xt.supabase/invite",
  "@xt.supabase/otp",
  "@xt.supabase/recovery",
  "@xt.supabase/settings",
  "@xt.supabase/token-refresh",
  "@xt.supabase/user-get",
  "@xt.supabase/user-info",
  "@xt.supabase/user-put",
  "@xt.supabase/verify-get",
  "@xt.supabase/verify-post"
];

var ATTACH_ACTIONS = <dynamic>["@xt.supabase/attach-model"];

attach_forward_handler(space, args, request, node) {
  var page_args = args[1];
  var space_id = xtd.get_in(page_args,<dynamic>["space_id"]);
  var group_id = xtd.get_in(page_args,<dynamic>["group_id"]);
  var transport_id = proxy_util.get_transport_id(node,xtd.get_in(request,<dynamic>["meta"]));
  page_proxy.group_create_proxy(
    node,
    space_id,
    group_id,
    <dynamic, dynamic>{},
    <dynamic, dynamic>{"transport_id":transport_id}
  );
  return ((Future.sync(() => substrate.request(
    node,
    null,
    request["action"],
    args,
    <dynamic, dynamic>{"transport_id":transport_id}
  ))) as Future<dynamic>).then((value) async { return await Function.apply((status) {
    return ((Future.sync(() => page_proxy.group_open_proxy(
      node,
      space_id,
      group_id,
      <dynamic, dynamic>{"transport_id":transport_id}
    ))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
      return status;
    },<dynamic>[value]); });
  },<dynamic>[value]); });
}

init_proxy_handlers(node) {
  var arr_52626 = ACTIONS;
  for(var i52627 = 0; i52627 < arr_52626.length; ++i52627){
    var action = arr_52626[i52627];
    substrate.register_handler(node,action,proxy_util.request_proxy,null);
  };
  var arr_52648 = ATTACH_ACTIONS;
  for(var i52649 = 0; i52649 < arr_52648.length; ++i52649){
    var action = arr_52648[i52649];
    substrate.register_handler(node,action,attach_forward_handler,null);
  };
  return node;
}