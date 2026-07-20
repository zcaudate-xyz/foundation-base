const xtd = require("@xtalk/lang/common-data.js")

var PROTOCOL_VERSION = "2025-11-25";

function schema_wire(schema){
  if(null == schema){
    return null;
  }
  let out = {};
  for(let [k,v] of Object.entries(schema)){
    if(k == "properties"){
      let properties = {};
      for(let [property_name,property_schema] of Object.entries(v)){
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

function annotations_wire(annotations){
  if(null == annotations){
    return null;
  }
  let out = {};
  for(let [k,v] of Object.entries(annotations)){
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

function tool_validp(tool){
  return ((null != tool) && ("object" == (typeof tool)) && !Array.isArray(tool)) && ("string" == (typeof tool["name"])) && (0 < tool["name"].length) && ("string" == (typeof tool["description"])) && ((null != tool["input_schema"]) && ("object" == (typeof tool["input_schema"])) && !Array.isArray(tool["input_schema"]));
}

function tool_wire(tool){
  if(!tool_validp(tool)){
    throw "invalid MCP tool descriptor";
  }
  let out = {
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

function schema_type_validp(type,value){
  if(null == type){
    return true;
  }
  else if(type == "object"){
    return (null != value) && ("object" == (typeof value)) && !Array.isArray(value);
  }
  else if(type == "array"){
    return Array.isArray(value);
  }
  else if(type == "string"){
    return "string" == (typeof value);
  }
  else if(type == "number"){
    return "number" == (typeof value);
  }
  else if(type == "integer"){
    return Number.isInteger(value);
  }
  else if(type == "boolean"){
    return "boolean" == (typeof value);
  }
  else if(type == "null"){
    return null == value;
  }
  else{
    return false;
  }
}

function schema_error(schema,value,path){
  path = (path || "$");
  if(!schema_type_validp(schema["type"],value)){
    return path + " must be " + schema["type"];
  }
  let enum_values = schema["enum"];
  if(Array.isArray(enum_values) && !xtd.arr_some(enum_values,function (candidate){
    return candidate == value;
  })){
    return path + " must be one of the declared enum values";
  }
  if(("object" == schema["type"]) && ((null != value) && ("object" == (typeof value)) && !Array.isArray(value))){
    let properties = schema["properties"] || {};
    let required = schema["required"] || [];
    let required_error = null;
    for(let required_key of required){
      if((null == required_error) && !(null != value[required_key])){
        required_error = (path + "." + required_key + " is required");
      }
    };
    if(null != required_error){
      return required_error;
    }
    let property_error = null;
    for(let [property_name,property_value] of Object.entries(value)){
      if(null == property_error){
        let property_schema = properties[property_name];
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
  if(("array" == schema["type"]) && Array.isArray(value) && (null != schema["items"])){
    let item_error = null;
    let index = 0;
    for(let item of value){
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

function response(id,result){
  return {"jsonrpc":"2.0","id":id,"result":result};
}

function error_response(id,code,message,data){
  let error = {"code":code,"message":message};
  if(null != data){
    error["data"] = data;
  }
  return {"jsonrpc":"2.0","id":id,"error":error};
}

function tool_result(value){
  let text = null;
  try{
    text = JSON.stringify(value);
  }
  catch(err){
    text = String(value);
  }
  let result = {"content":[{"type":"text","text":text}],"isError":false};
  if((null != value) && ("object" == (typeof value)) && !Array.isArray(value)){
    result["structuredContent"] = value;
  }
  return result;
}

function tool_error_result(error){
  let message = ((error instanceof Error) ? error["message"] : null) || String(error) || "Tool execution failed";
  return {"content":[{"type":"text","text":message}],"isError":true};
}

module.exports = {
  ["PROTOCOL_VERSION"]:PROTOCOL_VERSION,
  ["schema_wire"]:schema_wire,
  ["annotations_wire"]:annotations_wire,
  ["tool_validp"]:tool_validp,
  ["tool_wire"]:tool_wire,
  ["schema_type_validp"]:schema_type_validp,
  ["schema_error"]:schema_error,
  ["response"]:response,
  ["error_response"]:error_response,
  ["tool_result"]:tool_result,
  ["tool_error_result"]:tool_error_result
}