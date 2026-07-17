import 'package:xtalk_substrate/base-util.dart' as base_util;

import 'package:xtalk_substrate/base-space.dart' as base_space;

ping(space, args, request, node) {
  return <dynamic, dynamic>{"pong":true,"node":node["id"]};
}

echo(space, args, request, node) {
  return args;
}

list_handlers(space, args, request, node) {
  return base_util.list_handlers(node);
}

list_triggers(space, args, request, node) {
  return base_util.list_triggers(node);
}

list_spaces(space, args, request, node) {
  return base_space.list_spaces(node);
}

list_transports(space, args, request, node) {
  return base_util.transport_list(node);
}

node_info(space, args, request, node) {
  return <dynamic, dynamic>{"id":node["id"],"meta":node["meta"]};
}

handle_get_service(space, args, request, node) {
  return (node["services"] ?? <dynamic, dynamic>{})[args];
}

install_util_handlers(node) {
  base_util.register_handler(node,"@/ping",ping,<dynamic, dynamic>{"substrate/fn":"@/ping"});
  base_util.register_handler(node,"@/echo",echo,<dynamic, dynamic>{"substrate/fn":"@/echo"});
  base_util.register_handler(
    node,
    "@/list-handlers",
    list_handlers,
    <dynamic, dynamic>{"substrate/fn":"@/list-handlers"}
  );
  base_util.register_handler(
    node,
    "@/list-triggers",
    list_triggers,
    <dynamic, dynamic>{"substrate/fn":"@/list-triggers"}
  );
  base_util.register_handler(
    node,
    "@/list-spaces",
    list_spaces,
    <dynamic, dynamic>{"substrate/fn":"@/list-spaces"}
  );
  base_util.register_handler(
    node,
    "@/list-transports",
    list_transports,
    <dynamic, dynamic>{"substrate/fn":"@/list-transports"}
  );
  base_util.register_handler(
    node,
    "@/get-service",
    handle_get_service,
    <dynamic, dynamic>{"substrate/fn":"@/get-service"}
  );
  base_util.register_handler(
    node,
    "@/node-info",
    node_info,
    <dynamic, dynamic>{"substrate/fn":"@/node-info"}
  );
  return node;
}