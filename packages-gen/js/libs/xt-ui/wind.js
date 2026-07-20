const ui = require("@xtalk/ui/core.js")

const xtd = require("@xtalk/lang/common-data.js")

const catalog = require("@xtalk/ui/widgets/core.js")

function flutter_registry(){
  let platform = ui.registry_create("xt.ui/flutter-wind");
  for(let entry of [
    ["ui/fragment","WDiv"],
    ["ui/row","WDiv"],
    ["ui/column","WDiv"],
    ["ui/text","WText"],
    ["ui/title","WText"],
    ["ui/description","WText"],
    ["ui/label","WText"],
    ["ui/icon","WIcon"],
    ["ui/image","WImage"],
    ["ui/card","WDiv"],
    ["ui/card-header","WDiv"],
    ["ui/card-content","WDiv"],
    ["ui/input","WInput"],
    ["ui/textarea","WInput"],
    ["ui/button","WButton"],
    ["ui/alert","WDiv"],
    ["ui/spinner","WDiv"]
  ]){
    ui.registry_register_renderer(platform,entry[0],entry[1]);
  };
  return ui.registry_compose([catalog.registry(),platform]);
}

function action_addf(state,event_id,callback,value_event){
  let action_id = "xt_ui_" + String(state["next"]);
  state["next"] = (1 + state["next"]);
  state["actions"][action_id] = (function (args){
    return value_event ? callback(args["_value"]) : callback(args);
  });
  return {[event_id]:{"action":action_id}};
}

function normalize_props(component_id,props,state){
  let out = {};
  for(let [key,value] of Object.entries(props || {})){
    if(key == "class"){
      out["className"] = value;
    }
    else if(key == "aria_label"){
      out["semanticLabel"] = value;
    }
    else if(key == "read_only"){
      out["readOnly"] = value;
    }
    else if(key == "pending"){
      out["isLoading"] = value;
    }
    else if(key == "on_press"){
      Object.assign(out,action_addf(state,"onTap",value,false));
    }
    else if(key == "on_change"){
      Object.assign(out,action_addf(state,"onChange",value,true));
    }
    else if((key == "hidden") || (key == "on_submit") || (key == "variant") || (key == "size") || (key == "tone")){
      null;
    }
    else{
      out[key] = value;
    }
  };
  if(component_id == "ui/textarea"){
    out["maxLines"] = (props["rows"] || 4);
    delete(out["rows"]);
  }
  if(component_id == "ui/row"){
    out["className"] = ("flex flex-row " + (out["className"] || ""));
  }
  if((component_id == "ui/column") || (component_id == "ui/fragment")){
    out["className"] = ("flex flex-col " + (out["className"] || ""));
  }
  return out;
}

function prepare_node(runtime,value,state){
  if(null == value){
    return null;
  }
  if(("string" == (typeof value)) || ("number" == (typeof value))){
    return {"type":"WText","props":{"text":String(value)}};
  }
  if(Array.isArray(value)){
    return xtd.arr_map(value,function (child){
      return prepare_node(runtime,child,state);
    });
  }
  let component_id = value["component"];
  if(component_id == "ui/slot"){
    return prepare_node(runtime,ui.resolve_slot(runtime,value),state);
  }
  let props = value["props"];
  if(true == props["hidden"]){
    return null;
  }
  let renderer = ui.registry_renderer(runtime["registry"],component_id);
  if(!renderer){
    return {
      "type":"WDiv",
      "props":{"className":"flex flex-col"},
      "children":prepare_node(runtime,value["children"],state)
    };
  }
  let out_props = normalize_props(component_id,props,state);
  if((renderer == "WText") || (component_id == "ui/label")){
    out_props["text"] = (props["value"] || "");
    delete(out_props["value"]);
    delete(out_props["for"]);
  }
  return {
    "type":renderer,
    "props":out_props,
    "children":prepare_node(runtime,value["children"],state)
  };
}

function prepare(runtime,value){
  let state = {"next":0,"actions":{}};
  let json = prepare_node(runtime,value,state);
  return {"json":json,"actions":state["actions"]};
}

module.exports = {
  ["flutter_registry"]:flutter_registry,
  ["action_addf"]:action_addf,
  ["normalize_props"]:normalize_props,
  ["prepare_node"]:prepare_node,
  ["prepare"]:prepare
}