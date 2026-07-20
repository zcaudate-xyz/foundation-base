import 'package:xtalk_mcp/node/proxy-util.dart' as proxy_util;
import 'package:xtalk_mcp/node/kernel-base.dart' as kernel_base;
import 'package:xtalk_mcp/node/proxy-base.dart' as proxy_base;




init_server(node, service_id, opts) {
  kernel_base.init_handlers(node);
  kernel_base.ensure_service(node,service_id,opts);
  return node;
}

init_server_proxy(node, transport_id) {
  proxy_base.init_proxy_handlers(node);
  if((null != transport_id) && (false != transport_id)){
    proxy_util.set_default_transport(node,transport_id);
  }
  return node;
}