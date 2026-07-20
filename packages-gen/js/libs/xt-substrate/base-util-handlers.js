const base_util = require("@xtalk/substrate/base-util.js")

const base_space = require("@xtalk/substrate/base-space.js")

function ping(space,args,request,node){
  return {"pong":true,"node":node["id"]};
}

function echo(space,args,request,node){
  return args;
}

function list_handlers(space,args,request,node){
  return base_util.list_handlers(node);
}

function list_triggers(space,args,request,node){
  return base_util.list_triggers(node);
}

function list_spaces(space,args,request,node){
  return base_space.list_spaces(node);
}

function list_transports(space,args,request,node){
  return base_util.transport_list(node);
}

function node_info(space,args,request,node){
  return {"id":node["id"],"meta":node["meta"]};
}

function handle_get_service(space,args,request,node){
  return (node["services"] || {})[args];
}

function install_util_handlers(node){
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

module.exports = {
  ["ping"]:ping,
  ["echo"]:echo,
  ["list_handlers"]:list_handlers,
  ["list_triggers"]:list_triggers,
  ["list_spaces"]:list_spaces,
  ["list_transports"]:list_transports,
  ["node_info"]:node_info,
  ["handle_get_service"]:handle_get_service,
  ["install_util_handlers"]:install_util_handlers
}