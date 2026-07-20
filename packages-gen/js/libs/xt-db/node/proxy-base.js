const xtd = require("@xtalk/lang/common-data.js")

const page_proxy = require("@xtalk/substrate/page-proxy.js")

const proxy_util = require("@xtalk/db/node/proxy-util.js")

const substrate = require("@xtalk/substrate/substrate.js")

var CALL_ACTIONS = [
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

var ATTACH_ACTIONS = [
  "@xt.db/attach-model",
  "@xt.db/rpc-attach-model",
  "@xt.db/pull-attach-model",
  "@xt.db/dataview-attach-model"
];

var DETACH_ACTIONS = ["@xt.db/detach-model"];

function request_proxy(space,args,request,node){
  return proxy_util.request_proxy(space,args,request,node);
}

function attach_forward_handler(space,args,request,node){
  let page_args = args[1];
  let space_id = xtd.get_in(page_args,["space_id"]);
  let group_id = xtd.get_in(page_args,["group_id"]);
  let transport_id = proxy_util.get_transport_id(node,xtd.get_in(request,["meta"]));
  page_proxy.group_create_proxy(node,space_id,group_id,{},{"transport_id":transport_id});
  return substrate.request(node,null,request["action"],args,{"transport_id":transport_id}).then(function (status){
    return page_proxy.group_open_proxy(node,space_id,group_id,{"transport_id":transport_id}).then(function (_){
      return status;
    });
  });
}

function detach_forward_handler(space,args,request,node){
  let page_args = args[1];
  let space_id = xtd.get_in(page_args,["space_id"]);
  let group_id = xtd.get_in(page_args,["group_id"]);
  let transport_id = proxy_util.get_transport_id(node,xtd.get_in(request,["meta"]));
  return page_proxy.group_close_proxy(node,space_id,group_id,{"transport_id":transport_id}).then(function (_){
    return substrate.request(node,null,request["action"],args,{"transport_id":transport_id});
  });
}

function init_proxy_handlers(node){
  for(let action of CALL_ACTIONS){
    substrate.register_handler(node,action,request_proxy,null);
  };
  for(let action of ATTACH_ACTIONS){
    substrate.register_handler(node,action,attach_forward_handler,null);
  };
  for(let action of DETACH_ACTIONS){
    substrate.register_handler(node,action,detach_forward_handler,null);
  };
  return node;
}

module.exports = {
  ["CALL_ACTIONS"]:CALL_ACTIONS,
  ["ATTACH_ACTIONS"]:ATTACH_ACTIONS,
  ["DETACH_ACTIONS"]:DETACH_ACTIONS,
  ["request_proxy"]:request_proxy,
  ["attach_forward_handler"]:attach_forward_handler,
  ["detach_forward_handler"]:detach_forward_handler,
  ["init_proxy_handlers"]:init_proxy_handlers
}