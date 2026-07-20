import 'package:xtalk_lang/common-data.dart' as xtd;
import 'package:xtalk_substrate/base-request.dart' as node_request;
import 'package:xtalk_mcp/base.dart' as base;
import 'package:xtalk_substrate/substrate.dart' as substrate;
import 'dart:async';





var DEFAULT_SERVICE = "mcp/default";

var MESSAGE_ACTION = "@xt.mcp/message";

create_service(opts) {
  opts = (opts ?? <dynamic, dynamic>{});
  return <dynamic, dynamic>{
    "::":"mcp.service",
    "protocol_version":base.PROTOCOL_VERSION,
    "server_info":opts["server_info"] ?? <dynamic, dynamic>{"name":"xt.mcp","version":"0.1.0"},
    "instructions":opts["instructions"],
    "authorize_fn":opts["authorize_fn"],
    "tools":<dynamic, dynamic>{},
    "sessions":<dynamic, dynamic>{},
    "meta":opts["meta"] ?? <dynamic, dynamic>{}
  };
}

install_service(node, service_id, opts) {
  var sid = service_id ?? DEFAULT_SERVICE;
  var service = create_service(opts);
  substrate.set_service(node,sid,service);
  return service;
}

get_service(node, service_id) {
  return substrate.get_service(node,service_id ?? DEFAULT_SERVICE);
}

ensure_service(node, service_id, opts) {
  var sid = service_id ?? DEFAULT_SERVICE;
  var service = get_service(node,sid);
  if(null == service){
    service = install_service(node,sid,opts);
  }
  return service;
}

register_tool(node, service_id, tool, handler, meta) {
  if(!(() {
    var dart_truthy__51560 = base.tool_validp(tool);
    return (null != dart_truthy__51560) && (false != dart_truthy__51560);
  })()){
    throw "invalid MCP tool descriptor";
  }
  if(!((handler.runtimeType).toString().contains("Function") || (handler.runtimeType).toString().contains("=>") || (handler).toString().startsWith("Closure"))){
    throw "MCP tool handler must be a function";
  }
  var service = ensure_service(node,service_id,null);
  var tools = service["tools"];
  var name = tool["name"];
  if(null != tools[name]){
    throw "duplicate MCP tool - " + name;
  }
  var entry = <dynamic, dynamic>{
    "tool":tool,
    "handler":handler,
    "meta":meta ?? <dynamic, dynamic>{}
  };
  tools[name] = entry;
  return entry;
}

unregister_tool(node, service_id, tool_name) {
  var service = ensure_service(node,service_id,null);
  var tools = service["tools"];
  var entry = tools[tool_name];
  tools.remove(tool_name);
  return entry;
}

get_tool(node, service_id, tool_name) {
  var service = ensure_service(node,service_id,null);
  return (service["tools"])[tool_name];
}

list_tools(node, service_id) {
  var service = ensure_service(node,service_id,null);
  var names = xtd.arr_sort(List<dynamic>.from(( service["tools"] ).keys),(x) {
    return x;
  },(x, y) {
    return (x).toString().compareTo((y).toString()) < 0;
  });
  return xtd.arr_map(names,(name) {
    return base.tool_wire(((service["tools"])[name])["tool"]);
  });
}

session_id(context) {
  return xtd.get_in(context,<dynamic>["session_id"]) ?? "__DEFAULT__";
}

session_get(service, context) {
  return (service["sessions"])[session_id(context)];
}

session_initializedp(service, context) {
  return true == xtd.get_in(session_get(service,context),<dynamic>["initialized"]);
}

session_mark_initialized(service, context, client_info) {
  var session = session_get(service,context) ?? <dynamic, dynamic>{};
  session["initialized"] = true;
  if(null != client_info){
    session["client_info"] = client_info;
  }
  service["sessions"][session_id(context)] = session;
  return session;
}

session_start(service, context, params) {
  var session = <dynamic, dynamic>{
    "initialized":false,
    "client_info":params["clientInfo"],
    "protocol_version":params["protocolVersion"]
  };
  service["sessions"][session_id(context)] = session;
  return session;
}

call_tool(node, service_id, tool_name, tool_args, request, context) {
  var service = ensure_service(node,service_id,null);
  var entry = get_tool(node,service_id,tool_name);
  if(null == entry){
    throw "Unknown tool: " + tool_name;
  }
  var tool = entry["tool"];
  tool_args = (tool_args ?? <dynamic, dynamic>{});
  var validation_error = base.schema_error(tool["input_schema"],tool_args,"\$");
  if(null != validation_error){
    return Future.sync(() {
      return base.tool_error_result(validation_error);
    });
  }
  var call_context = <dynamic, dynamic>{
    "node":node,
    "service_id":service_id ?? DEFAULT_SERVICE,
    "tool":tool,
    "request":request,
    "request_id":request["id"],
    "session_id":session_id(context),
    "application":context["application"],
    "meta":entry["meta"] ?? <dynamic, dynamic>{}
  };
  var authorize_fn = service["authorize_fn"];
  try{
    return (() async { try { return await ((Future.sync(() => ((Future.sync(() => ((Future.sync(() => node_request.ensure_promise(((authorize_fn.runtimeType).toString().contains("Function") || (authorize_fn.runtimeType).toString().contains("=>") || (authorize_fn).toString().startsWith("Closure")) ? Function.apply(
      (authorize_fn as Function),
      <dynamic>[tool,tool_args,call_context]
    ) : true))) as Future<dynamic>).then((value) async { return await Function.apply((allowed) {
      if(!((null != allowed) && (false != allowed))){
        throw "MCP tool call was not authorized";
      }
      return node_request.ensure_promise(entry["handler"](tool_args,call_context));
    },<dynamic>[value]); }))) as Future<dynamic>).then((value) async { return await Function.apply(base.tool_result,<dynamic>[value]); }))) as Future<dynamic>); } catch (err) { return await Future.sync(() => Function.apply((error) {
      return base.tool_error_result(error);
    },<dynamic>[err])); } })();
  }
  catch(error){
    return Future.sync(() {
      return base.tool_error_result(error);
    });
  }
}

initialize_result(service) {
  var result = <dynamic, dynamic>{
    "protocolVersion":service["protocol_version"],
    "capabilities":<dynamic, dynamic>{"tools":<dynamic, dynamic>{"listChanged":false}},
    "serverInfo":service["server_info"]
  };
  if(null != service["instructions"]){
    result["instructions"] = service["instructions"];
  }
  return result;
}

handle_message(node, service_id, message, context) {
  context = (context ?? <dynamic, dynamic>{});
  var id = message["id"];
  var method = message["method"];
  var params = message["params"] ?? <dynamic, dynamic>{};
  var service = ensure_service(node,service_id,null);
  var tool_name = null;
  if(!("2.0" == message["jsonrpc"])){
    return Future.sync(() {
      return base.error_response(id,-32600,"Invalid Request",null);
    });
  }
  if(method == "initialize"){
    session_start(service,context,params);
    return Future.sync(() {
      return base.response(id,initialize_result(service));
    });
  }
  else if(method == "notifications/initialized"){
    session_mark_initialized(service,context,null);
    return Future.sync(() {
      return null;
    });
  }
  else if(!(() {
    var dart_truthy__51559 = session_initializedp(service,context);
    return (null != dart_truthy__51559) && (false != dart_truthy__51559);
  })()){
    return Future.sync(() {
      return base.error_response(id,-32002,"MCP session is not initialized",null);
    });
  }
  else if(method == "ping"){
    return Future.sync(() {
      return base.response(id,<dynamic, dynamic>{});
    });
  }
  else if(method == "tools/list"){
    return Future.sync(() {
      return base.response(id,<dynamic, dynamic>{"tools":list_tools(node,service_id)});
    });
  }
  else if(method == "tools/call"){
    tool_name = params["name"];
  }
  else{
    return Future.sync(() {
      return base.error_response(
        id,
        -32601,
        "Method not found",
        <dynamic, dynamic>{"method":method}
      );
    });
  }
  if(null == get_tool(node,service_id,tool_name)){
    return Future.sync(() {
      return base.error_response(id,-32602,"Unknown tool: " + tool_name,null);
    });
  }
  return ((Future.sync(() => call_tool(node,service_id,tool_name,params["arguments"],message,context))) as Future<dynamic>).then((value) async { return await Function.apply((result) {
    return base.response(id,result);
  },<dynamic>[value]); });
}

message_handler(space, args, request, node) {
  var service_id = args[0];
  var message = args[1];
  var context = args[2] ?? <dynamic, dynamic>{};
  return handle_message(node,service_id,message,context);
}

init_handlers(node) {
  substrate.register_handler(node,MESSAGE_ACTION,message_handler,null);
  return node;
}