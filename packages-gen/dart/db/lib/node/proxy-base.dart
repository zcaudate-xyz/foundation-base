import 'package:xtalk_lang/common-data.dart' as xtd;

import 'package:xtalk_substrate/page-proxy.dart' as page_proxy;

import 'package:xtalk_db/node/proxy-util.dart' as proxy_util;

import 'package:xtalk_substrate/substrate.dart' as substrate;

var call_actions = <dynamic>[
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

var attach_actions = <dynamic>[
  "@xt.db/attach-model",
  "@xt.db/rpc-attach-model",
  "@xt.db/pull-attach-model",
  "@xt.db/dataview-attach-model"
];

var detach_actions = <dynamic>["@xt.db/detach-model"];

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
  var arr_43084 = call_actions;
  for(var i43085 = 0; i43085 < arr_43084.length; ++i43085){
    var action = arr_43084[i43085];
    substrate.register_handler(node,action,request_proxy,null);
  };
  var arr_43106 = attach_actions;
  for(var i43107 = 0; i43107 < arr_43106.length; ++i43107){
    var action = arr_43106[i43107];
    substrate.register_handler(node,action,attach_forward_handler,null);
  };
  var arr_43128 = detach_actions;
  for(var i43129 = 0; i43129 < arr_43128.length; ++i43129){
    var action = arr_43128[i43129];
    substrate.register_handler(node,action,detach_forward_handler,null);
  };
  return node;
}