const xtd = require("@xtalk/lang/common-data.js")

const node_request = require("@xtalk/substrate/base-request.js")

const base = require("@xtalk/mcp/base.js")

const substrate = require("@xtalk/substrate/substrate.js")

var DEFAULT_SERVICE = "mcp/default";

var MESSAGE_ACTION = "@xt.mcp/message";

function create_service(opts){
  opts = (opts || {});
  return {
    "::":"mcp.service",
    "protocol_version":base.PROTOCOL_VERSION,
    "server_info":opts["server_info"] || {"name":"xt.mcp","version":"0.1.0"},
    "instructions":opts["instructions"],
    "authorize_fn":opts["authorize_fn"],
    "tools":{},
    "sessions":{},
    "meta":opts["meta"] || {}
  };
}

function install_service(node,service_id,opts){
  let sid = service_id || DEFAULT_SERVICE;
  let service = create_service(opts);
  substrate.set_service(node,sid,service);
  return service;
}

function get_service(node,service_id){
  return substrate.get_service(node,service_id || DEFAULT_SERVICE);
}

function ensure_service(node,service_id,opts){
  let sid = service_id || DEFAULT_SERVICE;
  let service = get_service(node,sid);
  if(null == service){
    service = install_service(node,sid,opts);
  }
  return service;
}

function register_tool(node,service_id,tool,handler,meta){
  if(!base.tool_validp(tool)){
    throw "invalid MCP tool descriptor";
  }
  if(!("function" == (typeof handler))){
    throw "MCP tool handler must be a function";
  }
  let service = ensure_service(node,service_id,null);
  let tools = service["tools"];
  let name = tool["name"];
  if(null != tools[name]){
    throw "duplicate MCP tool - " + name;
  }
  let entry = {"tool":tool,"handler":handler,"meta":meta || {}};
  tools[name] = entry;
  return entry;
}

function unregister_tool(node,service_id,tool_name){
  let service = ensure_service(node,service_id,null);
  let tools = service["tools"];
  let entry = tools[tool_name];
  delete(tools[tool_name]);
  return entry;
}

function get_tool(node,service_id,tool_name){
  let service = ensure_service(node,service_id,null);
  return (service["tools"])[tool_name];
}

function list_tools(node,service_id){
  let service = ensure_service(node,service_id,null);
  let names = xtd.arr_sort(Object.keys(service["tools"]),function (x){
    return x;
  },function (x,y){
    return 0 > x.localeCompare(y);
  });
  return names.map(function (name){
    return base.tool_wire(((service["tools"])[name])["tool"]);
  });
}

function session_id(context){
  return xtd.get_in(context,["session_id"]) || "__DEFAULT__";
}

function session_get(service,context){
  return (service["sessions"])[session_id(context)];
}

function session_initializedp(service,context){
  return true == xtd.get_in(session_get(service,context),["initialized"]);
}

function session_mark_initialized(service,context,client_info){
  let session = session_get(service,context) || {};
  session["initialized"] = true;
  if(null != client_info){
    session["client_info"] = client_info;
  }
  service["sessions"][session_id(context)] = session;
  return session;
}

function session_start(service,context,params){
  let session = {
    "initialized":false,
    "client_info":params["clientInfo"],
    "protocol_version":params["protocolVersion"]
  };
  service["sessions"][session_id(context)] = session;
  return session;
}

function call_tool(node,service_id,tool_name,tool_args,request,context){
  let service = ensure_service(node,service_id,null);
  let entry = get_tool(node,service_id,tool_name);
  if(null == entry){
    throw "Unknown tool: " + tool_name;
  }
  let tool = entry["tool"];
  tool_args = (tool_args || {});
  let validation_error = base.schema_error(tool["input_schema"],tool_args,"$");
  if(null != validation_error){
    return Promise.resolve().then(function (){
      return base.tool_error_result(validation_error);
    });
  }
  let call_context = {
    "node":node,
    "service_id":service_id || DEFAULT_SERVICE,
    "tool":tool,
    "request":request,
    "request_id":request["id"],
    "session_id":session_id(context),
    "application":context["application"],
    "meta":entry["meta"] || {}
  };
  let authorize_fn = service["authorize_fn"];
  try{
    return node_request.ensure_promise(
      ("function" == (typeof authorize_fn)) ? authorize_fn(tool,tool_args,call_context) : true
    ).then(function (allowed){
      if(!allowed){
        throw "MCP tool call was not authorized";
      }
      return node_request.ensure_promise(entry["handler"](tool_args,call_context));
    }).then(base.tool_result).catch(function (error){
      return base.tool_error_result(error);
    });
  }
  catch(error){
    return Promise.resolve().then(function (){
      return base.tool_error_result(error);
    });
  }
}

function initialize_result(service){
  let result = {
    "protocolVersion":service["protocol_version"],
    "capabilities":{"tools":{"listChanged":false}},
    "serverInfo":service["server_info"]
  };
  if(null != service["instructions"]){
    result["instructions"] = service["instructions"];
  }
  return result;
}

function handle_message(node,service_id,message,context){
  context = (context || {});
  let id = message["id"];
  let method = message["method"];
  let params = message["params"] || {};
  let service = ensure_service(node,service_id,null);
  let tool_name = null;
  if(!("2.0" == message["jsonrpc"])){
    return Promise.resolve().then(function (){
      return base.error_response(id,-32600,"Invalid Request",null);
    });
  }
  if(method == "initialize"){
    session_start(service,context,params);
    return Promise.resolve().then(function (){
      return base.response(id,initialize_result(service));
    });
  }
  else if(method == "notifications/initialized"){
    session_mark_initialized(service,context,null);
    return Promise.resolve().then(function (){
      return null;
    });
  }
  else if(!session_initializedp(service,context)){
    return Promise.resolve().then(function (){
      return base.error_response(id,-32002,"MCP session is not initialized",null);
    });
  }
  else if(method == "ping"){
    return Promise.resolve().then(function (){
      return base.response(id,{});
    });
  }
  else if(method == "tools/list"){
    return Promise.resolve().then(function (){
      return base.response(id,{"tools":list_tools(node,service_id)});
    });
  }
  else if(method == "tools/call"){
    tool_name = params["name"];
  }
  else{
    return Promise.resolve().then(function (){
      return base.error_response(id,-32601,"Method not found",{"method":method});
    });
  }
  if(null == get_tool(node,service_id,tool_name)){
    return Promise.resolve().then(function (){
      return base.error_response(id,-32602,"Unknown tool: " + tool_name,null);
    });
  }
  return call_tool(node,service_id,tool_name,params["arguments"],message,context).then(function (result){
    return base.response(id,result);
  });
}

function message_handler(space,args,request,node){
  let service_id = args[0];
  let message = args[1];
  let context = args[2] || {};
  return handle_message(node,service_id,message,context);
}

function init_handlers(node){
  substrate.register_handler(node,MESSAGE_ACTION,message_handler,null);
  return node;
}

module.exports = {
  ["DEFAULT_SERVICE"]:DEFAULT_SERVICE,
  ["MESSAGE_ACTION"]:MESSAGE_ACTION,
  ["create_service"]:create_service,
  ["install_service"]:install_service,
  ["get_service"]:get_service,
  ["ensure_service"]:ensure_service,
  ["register_tool"]:register_tool,
  ["unregister_tool"]:unregister_tool,
  ["get_tool"]:get_tool,
  ["list_tools"]:list_tools,
  ["session_id"]:session_id,
  ["session_get"]:session_get,
  ["session_initializedp"]:session_initializedp,
  ["session_mark_initialized"]:session_mark_initialized,
  ["session_start"]:session_start,
  ["call_tool"]:call_tool,
  ["initialize_result"]:initialize_result,
  ["handle_message"]:handle_message,
  ["message_handler"]:message_handler,
  ["init_handlers"]:init_handlers
}