import 'package:xtalk_mcp/node/proxy-util.dart' as proxy_util;
import 'package:xtalk_substrate/substrate.dart' as substrate;



var MESSAGE_ACTION = "@xt.mcp/message";

request_proxy(space, args, request, node) {
  return proxy_util.request_proxy(space,args,request,node);
}

init_proxy_handlers(node) {
  substrate.register_handler(node,MESSAGE_ACTION,request_proxy,null);
  return node;
}