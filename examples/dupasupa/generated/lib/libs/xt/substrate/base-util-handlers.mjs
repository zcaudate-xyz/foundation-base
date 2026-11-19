import * as base_util from './base-util.mjs'

import * as base_space from './base-space.mjs'

export function ping(space,args,request,node){
  return {"pong":true,"node":node["id"]};
}

export function echo(space,args,request,node){
  return args;
}

export function list_handlers(space,args,request,node){
  return base_util.list_handlers(node);
}

export function list_triggers(space,args,request,node){
  return base_util.list_triggers(node);
}

export function list_spaces(space,args,request,node){
  return base_space.list_spaces(node);
}

export function list_transports(space,args,request,node){
  return base_util.transport_list(node);
}

export function node_info(space,args,request,node){
  return {"id":node["id"],"meta":node["meta"]};
}

export function handle_get_service(space,args,request,node){
  return (node["services"] || {})[args];
}

export function install_util_handlers(node){
  base_util.register_handler(node,"@/ping",ping,{"substrate/fn":"@/ping"});
  base_util.register_handler(node,"@/echo",echo,{"substrate/fn":"@/echo"});
  base_util.register_handler(
    node,
    "@/list-handlers",
    list_handlers,
    {"substrate/fn":"@/list-handlers"}
  );
  base_util.register_handler(
    node,
    "@/list-triggers",
    list_triggers,
    {"substrate/fn":"@/list-triggers"}
  );
  base_util.register_handler(
    node,
    "@/list-spaces",
    list_spaces,
    {"substrate/fn":"@/list-spaces"}
  );
  base_util.register_handler(
    node,
    "@/list-transports",
    list_transports,
    {"substrate/fn":"@/list-transports"}
  );
  base_util.register_handler(
    node,
    "@/get-service",
    handle_get_service,
    {"substrate/fn":"@/get-service"}
  );
  base_util.register_handler(node,"@/node-info",node_info,{"substrate/fn":"@/node-info"});
  return node;
}