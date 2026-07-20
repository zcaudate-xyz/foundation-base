const proxy_util = require("@xtalk/mcp/node/proxy-util.js")

const substrate = require("@xtalk/substrate/substrate.js")

var MESSAGE_ACTION = "@xt.mcp/message";

function request_proxy(space,args,request,node){
  return proxy_util.request_proxy(space,args,request,node);
}

function init_proxy_handlers(node){
  substrate.register_handler(node,MESSAGE_ACTION,request_proxy,null);
  return node;
}

module.exports = {
  ["MESSAGE_ACTION"]:MESSAGE_ACTION,
  ["request_proxy"]:request_proxy,
  ["init_proxy_handlers"]:init_proxy_handlers
}