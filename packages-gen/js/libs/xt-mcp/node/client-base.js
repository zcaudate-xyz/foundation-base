const proxy_util = require("@xtalk/mcp/node/proxy-util.js")

const base = require("@xtalk/mcp/base.js")

var MESSAGE_ACTION = "@xt.mcp/message";

function message(node,service_id,request,context,opts){
  return proxy_util.request_client(node,MESSAGE_ACTION,[service_id,request,context || {}],opts);
}

function initialize(node,service_id,request_id,client_info,context,opts){
  return message(node,service_id,{
    "jsonrpc":"2.0",
    "id":request_id,
    "method":"initialize",
    "params":{
        "protocolVersion":base.PROTOCOL_VERSION,
        "capabilities":{},
        "clientInfo":client_info
      }
  },context,opts);
}

function initialized(node,service_id,context,opts){
  return message(
    node,
    service_id,
    {"jsonrpc":"2.0","method":"notifications/initialized"},
    context,
    opts
  );
}

function ping(node,service_id,request_id,context,opts){
  return message(
    node,
    service_id,
    {"jsonrpc":"2.0","id":request_id,"method":"ping"},
    context,
    opts
  );
}

function list_tools(node,service_id,request_id,context,opts){
  return message(
    node,
    service_id,
    {"jsonrpc":"2.0","id":request_id,"method":"tools/list"},
    context,
    opts
  );
}

function call_tool(node,service_id,request_id,tool_name,tool_args,context,opts){
  return message(node,service_id,{
    "jsonrpc":"2.0",
    "id":request_id,
    "method":"tools/call",
    "params":{"name":tool_name,"arguments":tool_args || {}}
  },context,opts);
}

module.exports = {
  ["MESSAGE_ACTION"]:MESSAGE_ACTION,
  ["message"]:message,
  ["initialize"]:initialize,
  ["initialized"]:initialized,
  ["ping"]:ping,
  ["list_tools"]:list_tools,
  ["call_tool"]:call_tool
}