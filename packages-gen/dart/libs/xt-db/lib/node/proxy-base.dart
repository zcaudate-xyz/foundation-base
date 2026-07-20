import 'package:xtalk_lang/common-data.dart' as xtd;
import 'package:xtalk_substrate/page-proxy.dart' as page_proxy;
import 'package:xtalk_db/node/proxy-util.dart' as proxy_util;
import 'package:xtalk_substrate/substrate.dart' as substrate;
import 'dart:async';





var CALL_ACTIONS = <dynamic>[
  "@xt.db/kernel-init",
  "@xt.db/kernel-setup",
  "@xt.db/kernel-teardown",
  "@xt.db/subscribe-db",
  "@xt.db/unsubscribe-db",
  "@xt.db/sync-cached",
  "@xt.db/rpc-call",
  "@xt.db/pull-call",
  "@xt.db/pull-cached",
  "@xt.db/dataview-call",
  "@xt.db/dataview-cached"
];

var ATTACH_ACTIONS = <dynamic>[
  "@xt.db/attach-model",
  "@xt.db/rpc-attach-model",
  "@xt.db/pull-attach-model",
  "@xt.db/dataview-attach-model"
];

var DETACH_ACTIONS = <dynamic>["@xt.db/detach-model"];

request_proxy(space, args, request, node) {
  return proxy_util.request_proxy(space,args,request,node);
}

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

detach_forward_handler(space, args, request, node) {
  var page_args = args[1];
  var space_id = xtd.get_in(page_args,<dynamic>["space_id"]);
  var group_id = xtd.get_in(page_args,<dynamic>["group_id"]);
  var transport_id = proxy_util.get_transport_id(node,xtd.get_in(request,<dynamic>["meta"]));
  return ((Future.sync(() => page_proxy.group_close_proxy(
    node,
    space_id,
    group_id,
    <dynamic, dynamic>{"transport_id":transport_id}
  ))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
    return substrate.request(
      node,
      null,
      request["action"],
      args,
      <dynamic, dynamic>{"transport_id":transport_id}
    );
  },<dynamic>[value]); });
}

init_proxy_handlers(node) {
  var arr_52676 = CALL_ACTIONS;
  for(var i52677 = 0; i52677 < arr_52676.length; ++i52677){
    var action = arr_52676[i52677];
    substrate.register_handler(node,action,request_proxy,null);
  };
  var arr_52698 = ATTACH_ACTIONS;
  for(var i52699 = 0; i52699 < arr_52698.length; ++i52699){
    var action = arr_52698[i52699];
    substrate.register_handler(node,action,attach_forward_handler,null);
  };
  var arr_52720 = DETACH_ACTIONS;
  for(var i52721 = 0; i52721 < arr_52720.length; ++i52721){
    var action = arr_52720[i52721];
    substrate.register_handler(node,action,detach_forward_handler,null);
  };
  return node;
}