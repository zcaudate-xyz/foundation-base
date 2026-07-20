import 'package:xtalk_lang/common-data.dart' as xtd;
import 'dart:convert';


var PROTOCOL_VERSION = "2025-11-25";

schema_wire(schema) {
  if(null == schema){
    return null;
  }
  var out = <dynamic, dynamic>{};
  for(var entry_51511 in schema.entries){
    var k = entry_51511.key;
    var v = entry_51511.value;
    if(k == "properties"){
      var properties = <dynamic, dynamic>{};
      for(var entry_51512 in v.entries){
        var property_name = entry_51512.key;
        var property_schema = entry_51512.value;
        properties[property_name] = schema_wire(property_schema);
      };
      out["properties"] = properties;
    }
    else if(k == "items"){
      out["items"] = schema_wire(v);
    }
    else if(k == "additional_properties"){
      out["additionalProperties"] = v;
    }
    else{
      out[k] = v;
    }
  };
  return out;
}

annotations_wire(annotations) {
  if(null == annotations){
    return null;
  }
  var out = <dynamic, dynamic>{};
  for(var entry_51513 in annotations.entries){
    var k = entry_51513.key;
    var v = entry_51513.value;
    if(k == "read_only_hint"){
      out["readOnlyHint"] = v;
    }
    else if(k == "destructive_hint"){
      out["destructiveHint"] = v;
    }
    else if(k == "idempotent_hint"){
      out["idempotentHint"] = v;
    }
    else if(k == "open_world_hint"){
      out["openWorldHint"] = v;
    }
    else{
      out[k] = v;
    }
  };
  return out;
}

tool_validp(tool) {
  return (("Map" == (tool.runtimeType).toString()) || (tool.runtimeType).toString().startsWith("_Map") || (tool.runtimeType).toString().startsWith("LinkedMap")) && ("String" == (tool["name"].runtimeType).toString()) && (0 < tool["name"].length) && ("String" == (tool["description"].runtimeType).toString()) && (("Map" == (tool["input_schema"].runtimeType).toString()) || (tool["input_schema"].runtimeType).toString().startsWith("_Map") || (tool["input_schema"].runtimeType).toString().startsWith("LinkedMap"));
}

tool_wire(tool) {
  if(!(() {
    var dart_truthy__51510 = tool_validp(tool);
    return (null != dart_truthy__51510) && (false != dart_truthy__51510);
  })()){
    throw "invalid MCP tool descriptor";
  }
  var out = <dynamic, dynamic>{
    "name":tool["name"],
    "description":tool["description"],
    "inputSchema":schema_wire(tool["input_schema"])
  };
  if(null != tool["title"]){
    out["title"] = tool["title"];
  }
  if(null != tool["output_schema"]){
    out["outputSchema"] = schema_wire(tool["output_schema"]);
  }
  if(null != tool["annotations"]){
    out["annotations"] = annotations_wire(tool["annotations"]);
  }
  return out;
}

schema_type_validp(type, value) {
  if(null == type){
    return true;
  }
  else if(type == "object"){
    return ("Map" == (value.runtimeType).toString()) || (value.runtimeType).toString().startsWith("_Map") || (value.runtimeType).toString().startsWith("LinkedMap");
  }
  else if(type == "array"){
    return (value.runtimeType).toString().startsWith("List") || (value.runtimeType).toString().startsWith("_GrowableList");
  }
  else if(type == "string"){
    return "String" == (value.runtimeType).toString();
  }
  else if(type == "number"){
    return ("int" == (value.runtimeType).toString()) || ("double" == (value.runtimeType).toString()) || ("num" == (value.runtimeType).toString());
  }
  else if(type == "integer"){
    return "int" == (value.runtimeType).toString();
  }
  else if(type == "boolean"){
    return "bool" == (value.runtimeType).toString();
  }
  else if(type == "null"){
    return null == value;
  }
  else{
    return false;
  }
}

schema_error(schema, value, path) {
  path = (path ?? "\$");
  if(!(() {
    var dart_truthy__51508 = schema_type_validp(schema["type"],value);
    return (null != dart_truthy__51508) && (false != dart_truthy__51508);
  })()){
    return path + " must be " + schema["type"];
  }
  var enum_values = schema["enum"];
  if(((enum_values.runtimeType).toString().startsWith("List") || (enum_values.runtimeType).toString().startsWith("_GrowableList")) && !(() {
    var dart_truthy__51509 = xtd.arr_some(enum_values,(candidate) {
      return candidate == value;
    });
    return (null != dart_truthy__51509) && (false != dart_truthy__51509);
  })()){
    return path + " must be one of the declared enum values";
  }
  if(("object" == schema["type"]) && (("Map" == (value.runtimeType).toString()) || (value.runtimeType).toString().startsWith("_Map") || (value.runtimeType).toString().startsWith("LinkedMap"))){
    var properties = schema["properties"] ?? <dynamic, dynamic>{};
    var required = schema["required"] ?? <dynamic>[];
    var required_error = null;
    var arr_51514 = required;
    for(var i51515 = 0; i51515 < arr_51514.length; ++i51515){
      var required_key = arr_51514[i51515];
      if((null == required_error) && !value.containsKey(required_key)){
        required_error = (path + "." + required_key + " is required");
      }
    };
    if(null != required_error){
      return required_error;
    }
    var property_error = null;
    for(var entry_51536 in value.entries){
      var property_name = entry_51536.key;
      var property_value = entry_51536.value;
      if(null == property_error){
        var property_schema = properties[property_name];
        if(null != property_schema){
          property_error = schema_error(property_schema,property_value,path + "." + property_name);
        }
        else if(false == schema["additional_properties"]){
          property_error = (path + "." + property_name + " is not allowed");
        }
      }
    };
    if(null != property_error){
      return property_error;
    }
  }
  if(("array" == schema["type"]) && ((value.runtimeType).toString().startsWith("List") || (value.runtimeType).toString().startsWith("_GrowableList")) && (null != schema["items"])){
    var item_error = null;
    var index = 0;
    var arr_51537 = value;
    for(var i51538 = 0; i51538 < arr_51537.length; ++i51538){
      var item = arr_51537[i51538];
      if(null == item_error){
        item_error = schema_error(schema["items"],item,path + "[" + index + "]");
      }
      index = (index + 1);
    };
    if(null != item_error){
      return item_error;
    }
  }
  return null;
}

response(id, result) {
  return <dynamic, dynamic>{"jsonrpc":"2.0","id":id,"result":result};
}

error_response(id, code, message, data) {
  var error = <dynamic, dynamic>{"code":code,"message":message};
  if(null != data){
    error["data"] = data;
  }
  return <dynamic, dynamic>{"jsonrpc":"2.0","id":id,"error":error};
}

tool_result(value) {
  var text = null;
  try{
    text = jsonEncode(value);
  }
  catch(err){
    text = (value).toString();
  }
  var result = <dynamic, dynamic>{
    "content":<dynamic>[<dynamic, dynamic>{"type":"text","text":text}],
    "isError":false
  };
  if(("Map" == (value.runtimeType).toString()) || (value.runtimeType).toString().startsWith("_Map") || (value.runtimeType).toString().startsWith("LinkedMap")){
    result["structuredContent"] = value;
  }
  return result;
}

tool_error_result(error) {
  var message = (((error is Map) && ("xt.exception" == ((error as Map)["__type__"]))) ? ((error as Map)["message"]) : null) ?? (error).toString() ?? "Tool execution failed";
  return <dynamic, dynamic>{
    "content":<dynamic>[<dynamic, dynamic>{"type":"text","text":message}],
    "isError":true
  };
}