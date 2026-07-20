const xtd = require("@xtalk/lang/common-data.js")

function node(component,props,children){
  return {
    "component":component,
    "props":props || {},
    "children":children || []
  };
}

function text(value,props){
  return node("ui/text",Object.assign({"value":value},props || {}),[]);
}

function slot(slot_id,fallback,props){
  return node(
    "ui/slot",
    Object.assign({"slot_id":slot_id},props || {}),
    fallback || []
  );
}

function extension(extension_id,props,fallback){
  return node(
    extension_id,
    Object.assign({"extension":true},props || {}),
    fallback || []
  );
}

function component_contract(component_id,tier,props,events,slots,fallback){
  return {
    "id":component_id,
    "tier":tier || "portable",
    "props":props || [],
    "events":events || [],
    "slots":slots || [],
    "fallback":fallback
  };
}

function registry_create(id){
  return {"id":id,"contracts":{},"renderers":{}};
}

function registry_register_contract(registry,contract){
  let component_id = contract["id"];
  let contracts = registry["contracts"];
  let existing = contracts[component_id];
  if(existing && (JSON.stringify(existing) != JSON.stringify(contract))){
    throw "ERR - incompatible UI contract - " + component_id;
  }
  contracts[component_id] = contract;
  return registry;
}

function registry_register_renderer(registry,component_id,renderer){
  registry["renderers"][component_id] = renderer;
  return registry;
}

function registry_compose(layers){
  let out = registry_create("composed");
  for(let layer of layers || []){
    for(let [component_id,contract] of Object.entries(layer["contracts"] || {})){
      registry_register_contract(out,contract);
    };
    for(let [component_id,renderer] of Object.entries(layer["renderers"] || {})){
      out["renderers"][component_id] = renderer;
    };
  };
  return out;
}

function registry_contract(registry,component_id){
  return (registry["contracts"])[component_id];
}

function registry_renderer(registry,component_id){
  return (registry["renderers"])[component_id];
}

function validate_props(contract,props){
  let allowed = contract["props"] || [];
  let events = contract["events"] || [];
  for(let prop_id of Object.keys(props || {})){
    if(!xtd.arr_some(allowed,function (allowed_id){
      return allowed_id == prop_id;
    }) && !xtd.arr_some(events,function (event_id){
      return event_id == prop_id;
    })){
      throw "ERR - unsupported UI prop - " + contract["id"] + "." + prop_id;
    }
  };
  return true;
}

function validate_node(registry,ui_node){
  if(null == ui_node){
    return true;
  }
  if(("string" == (typeof ui_node)) || ("number" == (typeof ui_node))){
    return true;
  }
  if(Array.isArray(ui_node)){
    for(let child of ui_node){
      validate_node(registry,child);
    };
    return true;
  }
  let component_id = ui_node["component"];
  let contracts = registry["contracts"];
  if(!(null != contracts[component_id])){
    throw "ERR - unregistered UI component - " + component_id;
  }
  let contract = contracts[component_id];
  validate_props(contract,ui_node["props"]);
  for(let child of ui_node["children"] || []){
    validate_node(registry,child);
  };
  return true;
}

function runtime_create(store,registry,capabilities,services,slots){
  return {
    "store":store,
    "registry":registry,
    "capabilities":capabilities || {},
    "services":services || {},
    "slots":slots || {}
  };
}

function capabilityp(runtime,capability_id){
  return true == (runtime["capabilities"])[capability_id];
}

function service(runtime,service_id){
  return (runtime["services"])[service_id];
}

function effectf(runtime,service_id,args){
  let handler = service(runtime,service_id);
  if(!("function" == (typeof handler))){
    return Promise.resolve().then(function (){
      return {"status":"unavailable","service":service_id};
    });
  }
  return Promise.resolve().then(function (){
    return handler(args || {});
  }).catch(function (err){
    return {
      "status":"error",
      "service":service_id,
      "message":(err instanceof Error) ? err["message"] : null,
      "data":(err instanceof Error) ? err["data"] : null
    };
  });
}

function resolve_slot(runtime,slot_node){
  let slot_id = xtd.get_in(slot_node,["props","slot_id"]);
  let supplied = (runtime["slots"])[slot_id];
  return supplied ? supplied : slot_node["children"];
}

function base_registry(){
  let registry = registry_create("xt.ui/base");
  let structural_props = ["class","style","hidden","key","aria_label"];
  for(let component_id of [
    "ui/fragment",
    "ui/row",
    "ui/column",
    "ui/text",
    "ui/icon",
    "ui/image",
    "ui/slot"
  ]){
    registry_register_contract(registry,component_contract(
      component_id,
      "portable",
      (component_id == "ui/text") ? ["value","class","style","hidden","key","aria_label"] : ((component_id == "ui/slot") ? ["slot_id","class","style","hidden","key"] : structural_props),
      [],
      [],
      null
    ));
  };
  return registry;
}

module.exports = {
  ["node"]:node,
  ["text"]:text,
  ["slot"]:slot,
  ["extension"]:extension,
  ["component_contract"]:component_contract,
  ["registry_create"]:registry_create,
  ["registry_register_contract"]:registry_register_contract,
  ["registry_register_renderer"]:registry_register_renderer,
  ["registry_compose"]:registry_compose,
  ["registry_contract"]:registry_contract,
  ["registry_renderer"]:registry_renderer,
  ["validate_props"]:validate_props,
  ["validate_node"]:validate_node,
  ["runtime_create"]:runtime_create,
  ["capabilityp"]:capabilityp,
  ["service"]:service,
  ["effectf"]:effectf,
  ["resolve_slot"]:resolve_slot,
  ["base_registry"]:base_registry
}