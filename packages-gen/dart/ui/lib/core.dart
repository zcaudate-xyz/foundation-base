import 'package:xtalk_lang/common-data.dart' as xtd;

node(component, props, children) {
  return <dynamic, dynamic>{
    "component":component,
    "props":props ?? <dynamic, dynamic>{},
    "children":children ?? <dynamic>[]
  };
}

text(value, props) {
  return node("ui/text",xtd.obj_assign(
    <dynamic, dynamic>{"value":value},
    props ?? <dynamic, dynamic>{}
  ),<dynamic>[]);
}

slot(slot_id, fallback, props) {
  return node("ui/slot",xtd.obj_assign(
    <dynamic, dynamic>{"slot_id":slot_id},
    props ?? <dynamic, dynamic>{}
  ),fallback ?? <dynamic>[]);
}

extension(extension_id, props, fallback) {
  return node(extension_id,xtd.obj_assign(
    <dynamic, dynamic>{"extension":true},
    props ?? <dynamic, dynamic>{}
  ),fallback ?? <dynamic>[]);
}

component_contract(component_id, tier, props, events, slots, fallback) {
  return <dynamic, dynamic>{
    "id":component_id,
    "tier":tier ?? "portable",
    "props":props ?? <dynamic>[],
    "events":events ?? <dynamic>[],
    "slots":slots ?? <dynamic>[],
    "fallback":fallback
  };
}

registry_create(id) {
  return <dynamic, dynamic>{
    "id":id,
    "contracts":<dynamic, dynamic>{},
    "renderers":<dynamic, dynamic>{}
  };
}

registry_register_contract(registry, contract) {
  var component_id = contract["id"];
  var contracts = registry["contracts"];
  var existing = contracts[component_id];
  if(((null != existing) && (false != existing)) && (jsonEncode(existing) != jsonEncode(contract))){
    throw "ERR - incompatible UI contract - " + component_id;
  }
  contracts[component_id] = contract;
  return registry;
}

registry_register_renderer(registry, component_id, renderer) {
  registry["renderers"][component_id] = renderer;
  return registry;
}

registry_compose(layers) {
  var out = registry_create("composed");
  var arr_43226 = layers ?? <dynamic>[];
  for(var i43227 = 0; i43227 < arr_43226.length; ++i43227){
    var layer = arr_43226[i43227];
    for(var entry_43248 in (layer["contracts"] ?? <dynamic, dynamic>{}).entries){
      var component_id = entry_43248.key;
      var contract = entry_43248.value;
      registry_register_contract(out,contract);
    };
    for(var entry_43249 in (layer["renderers"] ?? <dynamic, dynamic>{}).entries){
      var component_id = entry_43249.key;
      var renderer = entry_43249.value;
      out["renderers"][component_id] = renderer;
    };
  };
  return out;
}

registry_contract(registry, component_id) {
  return (registry["contracts"])[component_id];
}

registry_renderer(registry, component_id) {
  return (registry["renderers"])[component_id];
}

validate_props(contract, props) {
  var allowed = contract["props"] ?? <dynamic>[];
  var events = contract["events"] ?? <dynamic>[];
  for(var prop_id in (props ?? <dynamic, dynamic>{}).keys){
    if(!(() {
      var dart_truthy__43224 = xtd.arr_some(allowed,(allowed_id) {
        return allowed_id == prop_id;
      });
      return (null != dart_truthy__43224) && (false != dart_truthy__43224);
    })() && !(() {
      var dart_truthy__43225 = xtd.arr_some(events,(event_id) {
        return event_id == prop_id;
      });
      return (null != dart_truthy__43225) && (false != dart_truthy__43225);
    })()){
      throw "ERR - unsupported UI prop - " + contract["id"] + "." + prop_id;
    }
  };
  return true;
}

validate_node(registry, ui_node) {
  if(null == ui_node){
    return true;
  }
  if(("String" == (ui_node.runtimeType).toString()) || (("int" == (ui_node.runtimeType).toString()) || ("double" == (ui_node.runtimeType).toString()) || ("num" == (ui_node.runtimeType).toString()))){
    return true;
  }
  if((ui_node.runtimeType).toString().startsWith("List") || (ui_node.runtimeType).toString().startsWith("_GrowableList")){
    var arr_43250 = ui_node;
    for(var i43251 = 0; i43251 < arr_43250.length; ++i43251){
      var child = arr_43250[i43251];
      validate_node(registry,child);
    };
    return true;
  }
  var component_id = ui_node["component"];
  var contracts = registry["contracts"];
  if(!contracts.containsKey(component_id)){
    throw "ERR - unregistered UI component - " + component_id;
  }
  var contract = contracts[component_id];
  validate_props(contract,ui_node["props"]);
  var arr_43272 = ui_node["children"] ?? <dynamic>[];
  for(var i43273 = 0; i43273 < arr_43272.length; ++i43273){
    var child = arr_43272[i43273];
    validate_node(registry,child);
  };
  return true;
}

runtime_create(store, registry, capabilities, services, slots) {
  return <dynamic, dynamic>{
    "store":store,
    "registry":registry,
    "capabilities":capabilities ?? <dynamic, dynamic>{},
    "services":services ?? <dynamic, dynamic>{},
    "slots":slots ?? <dynamic, dynamic>{}
  };
}

capabilityp(runtime, capability_id) {
  return true == (runtime["capabilities"])[capability_id];
}

service(runtime, service_id) {
  return (runtime["services"])[service_id];
}

effectf(runtime, service_id, args) {
  var handler = service(runtime,service_id);
  if(!((handler.runtimeType).toString().contains("Function") || (handler.runtimeType).toString().contains("=>") || (handler).toString().startsWith("Closure"))){
    return Future.sync(() {
      return <dynamic, dynamic>{"status":"unavailable","service":service_id};
    });
  }
  return (() async { try { return await ((Future.sync(() => Future.sync(() {
    return Function.apply(
      (handler as Function),
      <dynamic>[args ?? <dynamic, dynamic>{}]
    );
  }))) as Future<dynamic>); } catch (err) { return await Future.sync(() => Function.apply((err) {
    return <dynamic, dynamic>{
      "status":"error",
      "service":service_id,
      "message":((err is Map) && ("xt.exception" == ((err as Map)["__type__"]))) ? ((err as Map)["message"]) : null,
      "data":((err is Map) && ("xt.exception" == ((err as Map)["__type__"]))) ? ((err as Map)["data"]) : null
    };
  },<dynamic>[err])); } })();
}

resolve_slot(runtime, slot_node) {
  var slot_id = xtd.get_in(slot_node,<dynamic>["props","slot_id"]);
  var supplied = (runtime["slots"])[slot_id];
  return ((null != supplied) && (false != supplied)) ? supplied : slot_node["children"];
}

base_registry() {
  var registry = registry_create("xt.ui/base");
  var structural_props = <dynamic>["class","style","hidden","key","aria_label"];
  var arr_43294 = <dynamic>[
    "ui/fragment",
    "ui/row",
    "ui/column",
    "ui/text",
    "ui/icon",
    "ui/image",
    "ui/slot"
  ];
  for(var i43295 = 0; i43295 < arr_43294.length; ++i43295){
    var component_id = arr_43294[i43295];
    registry_register_contract(registry,component_contract(
      component_id,
      "portable",
      (component_id == "ui/text") ? <dynamic>["value","class","style","hidden","key","aria_label"] : ((component_id == "ui/slot") ? <dynamic>["slot_id","class","style","hidden","key"] : structural_props),
      <dynamic>[],
      <dynamic>[],
      null
    ));
  };
  return registry;
}