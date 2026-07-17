import 'package:xtalk_lang/common-data.dart' as xtd;

import 'package:xtalk_substrate/substrate.dart' as substrate;

set_default_transport(node, transport_id) {
  xtd.set_in(
    node,
    <dynamic>["state","adaptor_proxy","default_transport_id"],
    transport_id
  );
  return transport_id;
}

get_default_transport(node) {
  return xtd.get_in(
    node,
    <dynamic>["state","adaptor_proxy","default_transport_id"]
  );
}

get_transport_id(node, opts) {
  return xtd.get_in(opts,<dynamic>["transport_id"]) ?? get_default_transport(node) ?? (substrate.transport_list(node))[0];
}

request_meta(node, request) {
  var transport_id = get_transport_id(node,request);
  return <dynamic, dynamic>{"transport_id":transport_id};
}

request_proxy(space, args, request, node) {
  return substrate.request(node,null,request["action"],args,request_meta(node,request));
}

request_client(node, action, args, opts) {
  var local_meta = <dynamic, dynamic>{};
  local_meta["local"] = true;
  return ((null != substrate.get_handler(node,action)) && (false != substrate.get_handler(node,action))) ? substrate.request(node,null,action,args,local_meta) : substrate.request(node,null,action,args,request_meta(node,opts));
}