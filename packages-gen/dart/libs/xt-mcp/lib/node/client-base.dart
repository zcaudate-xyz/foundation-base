import 'package:xtalk_mcp/node/proxy-util.dart' as proxy_util;
import 'package:xtalk_mcp/base.dart' as base;



var MESSAGE_ACTION = "@xt.mcp/message";

message(node, service_id, request, context, opts) {
  return proxy_util.request_client(
    node,
    MESSAGE_ACTION,
    <dynamic>[service_id,request,context ?? <dynamic, dynamic>{}],
    opts
  );
}

initialize(node, service_id, request_id, client_info, context, opts) {
  return message(node,service_id,<dynamic, dynamic>{
    "jsonrpc":"2.0",
    "id":request_id,
    "method":"initialize",
    "params":<dynamic, dynamic>{
        "protocolVersion":base.PROTOCOL_VERSION,
        "capabilities":<dynamic, dynamic>{},
        "clientInfo":client_info
      }
  },context,opts);
}

initialized(node, service_id, context, opts) {
  return message(
    node,
    service_id,
    <dynamic, dynamic>{"jsonrpc":"2.0","method":"notifications/initialized"},
    context,
    opts
  );
}

ping(node, service_id, request_id, context, opts) {
  return message(
    node,
    service_id,
    <dynamic, dynamic>{"jsonrpc":"2.0","id":request_id,"method":"ping"},
    context,
    opts
  );
}

list_tools(node, service_id, request_id, context, opts) {
  return message(
    node,
    service_id,
    <dynamic, dynamic>{"jsonrpc":"2.0","id":request_id,"method":"tools/list"},
    context,
    opts
  );
}

call_tool(node, service_id, request_id, tool_name, tool_args, context, opts) {
  return message(node,service_id,<dynamic, dynamic>{
    "jsonrpc":"2.0",
    "id":request_id,
    "method":"tools/call",
    "params":<dynamic, dynamic>{
        "name":tool_name,
        "arguments":tool_args ?? <dynamic, dynamic>{}
      }
  },context,opts);
}