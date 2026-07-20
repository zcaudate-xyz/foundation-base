const xtd = require("@xtalk/lang/common-data.js")

const substrate = require("@xtalk/substrate/substrate.js")

function set_default_transport(node,transport_id){
  xtd.set_in(
    node,
    ["state","mcp_proxy","default_transport_id"],
    transport_id
  );
  return transport_id;
}

function get_default_transport(node){
  return xtd.get_in(node,["state","mcp_proxy","default_transport_id"]);
}

function get_transport_id(node,opts){
  return xtd.get_in(opts,["transport_id"]) || get_default_transport(node) || (substrate.transport_list(node))[0];
}

function request_meta(node,request){
  return {"transport_id":get_transport_id(node,request)};
}

function request_proxy(space,args,request,node){
  return substrate.request(
    node,
    null,
    request["action"],
    args,
    request_meta(node,request["meta"])
  );
}

function request_client(node,action,args,opts){
  return substrate.get_handler(node,action) ? substrate.request(node,null,action,args,{"local":true}) : substrate.request(node,null,action,args,request_meta(node,opts));
}

module.exports = {
  ["set_default_transport"]:set_default_transport,
  ["get_default_transport"]:get_default_transport,
  ["get_transport_id"]:get_transport_id,
  ["request_meta"]:request_meta,
  ["request_proxy"]:request_proxy,
  ["request_client"]:request_client
}