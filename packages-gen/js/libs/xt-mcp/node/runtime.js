const proxy_util = require("@xtalk/mcp/node/proxy-util.js")

const kernel_base = require("@xtalk/mcp/node/kernel-base.js")

const proxy_base = require("@xtalk/mcp/node/proxy-base.js")

function init_server(node,service_id,opts){
  kernel_base.init_handlers(node);
  kernel_base.ensure_service(node,service_id,opts);
  return node;
}

function init_server_proxy(node,transport_id){
  proxy_base.init_proxy_handlers(node);
  if(transport_id){
    proxy_util.set_default_transport(node,transport_id);
  }
  return node;
}

module.exports = {
  ["init_server"]:init_server,
  ["init_server_proxy"]:init_server_proxy
}