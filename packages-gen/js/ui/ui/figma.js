const React = require("react")

const FigmaUi = require("@xtalk/figma-ui")

const ui = require("@xtalk/ui/core.js")

const xtd = require("@xtalk/lang/common-data.js")

const ui_page = require("@xtalk/ui/page.js")

const catalog = require("@xtalk/ui/catalog.js")

const ui_model = require("@xtalk/ui/model.js")

function normalize_props(props){
  let out = Object.assign({},props || {});
  if(null != out["class"]){
    out["className"] = out["class"];
    delete(out["class"]);
  }
  if(null != out["aria_label"]){
    out["aria-label"] = out["aria_label"];
    delete(out["aria_label"]);
  }
  if(null != out["read_only"]){
    out["readOnly"] = out["read_only"];
    delete(out["read_only"]);
  }
  if(null != out["for"]){
    out["htmlFor"] = out["for"];
    delete(out["for"]);
  }
  if(null != out["tone"]){
    out["variant"] = (("error" == out["tone"]) ? "destructive" : "default");
    delete(out["tone"]);
  }
  if(null != out["on_press"]){
    out["onClick"] = out["on_press"];
    delete(out["on_press"]);
  }
  if(null != out["on_submit"]){
    out["onSubmit"] = out["on_submit"];
    delete(out["on_submit"]);
  }
  if(null != out["on_change"]){
    let callback = out["on_change"];
    out["onChange"] = (function (event){
      return callback(xtd.get_in(event,["target","value"]));
    });
    delete(out["on_change"]);
  }
  delete(out["pending"]);
  delete(out["hidden"]);
  return out;
}

function web_registry(){
  let platform = ui.registry_create("xt.ui/react-figma");
  ui.registry_register_renderer(platform,"ui/fragment",React.Fragment);
  ui.registry_register_renderer(platform,"ui/row","div");
  ui.registry_register_renderer(platform,"ui/column","div");
  ui.registry_register_renderer(platform,"ui/text","span");
  ui.registry_register_renderer(platform,"ui/icon","span");
  ui.registry_register_renderer(platform,"ui/image","img");
  ui.registry_register_renderer(platform,"ui/card",FigmaUi.Card);
  ui.registry_register_renderer(platform,"ui/card-header",FigmaUi.CardHeader);
  ui.registry_register_renderer(platform,"ui/card-content",FigmaUi.CardContent);
  ui.registry_register_renderer(platform,"ui/title",FigmaUi.CardTitle);
  ui.registry_register_renderer(platform,"ui/description",FigmaUi.CardDescription);
  ui.registry_register_renderer(platform,"ui/label",FigmaUi.Label);
  ui.registry_register_renderer(platform,"ui/input",FigmaUi.Input);
  ui.registry_register_renderer(platform,"ui/textarea",FigmaUi.Textarea);
  ui.registry_register_renderer(platform,"ui/button",FigmaUi.Button);
  ui.registry_register_renderer(platform,"ui/alert",FigmaUi.Alert);
  ui.registry_register_renderer(platform,"ui/spinner",FigmaUi.Skeleton);
  return ui.registry_compose([catalog.registry(),platform]);
}

function render_node(runtime,value){
  if(null == value){
    return null;
  }
  if(("string" == (typeof value)) || ("number" == (typeof value))){
    return value;
  }
  if(Array.isArray(value)){
    return xtd.arr_map(value,function (child){
      return render_node(runtime,child);
    });
  }
  let component_id = value["component"];
  if(component_id == "ui/slot"){
    return render_node(runtime,ui.resolve_slot(runtime,value));
  }
  let props = value["props"];
  if(true == props["hidden"]){
    return null;
  }
  let renderer = ui.registry_renderer(runtime["registry"],component_id);
  if(!renderer){
    throw "ERR - missing React UI renderer - " + component_id;
  }
  let children = render_node(runtime,value["children"]);
  let rprops = normalize_props(props);
  if(component_id == "ui/row"){
    rprops["className"] = ("flex flex-row " + (rprops["className"] || ""));
  }
  if(component_id == "ui/column"){
    rprops["className"] = ("flex flex-col " + (rprops["className"] || ""));
  }
  if((component_id == "ui/text") || (component_id == "ui/title") || (component_id == "ui/description") || (component_id == "ui/label")){
    children = [props["value"]];
    delete(rprops["value"]);
  }
  return React.createElement(renderer,rprops,children);
}

function use_model_store(store,subscription_id){
  React.useSyncExternalStore(function (notify){
    ui_model.subscribef(store,subscription_id,notify);
    return function (){
      ui_model.unsubscribef(store,subscription_id);
    };
  },function (){
    return ui_model.store_version(store);
  },function (){
    return ui_model.store_version(store);
  });
  return store;
}

function PortableView({runtime,subscription_id,view_fn}){
  use_model_store(runtime["store"],subscription_id || "xt.ui/react-view");
  return render_node(runtime,view_fn(runtime));
}

function use_page_controller(controller,subscription_id){
  React.useSyncExternalStore(function (notify){
    ui_page.subscribef(controller,subscription_id,function (_state,_revision){
      notify();
    });
    return function (){
      ui_page.unsubscribef(controller,subscription_id);
    };
  },function (){
    return ui_page.revision(controller);
  },function (){
    return ui_page.revision(controller);
  });
  return ui_page.snapshot(controller);
}

function PortableControllerView({actions,controller,runtime,subscription_id,view_fn}){
  let state = use_page_controller(controller,subscription_id || "xt.ui/react-controller-view");
  return render_node(runtime,view_fn(state,actions || {}));
}

module.exports = {
  ["normalize_props"]:normalize_props,
  ["web_registry"]:web_registry,
  ["render_node"]:render_node,
  ["use_model_store"]:use_model_store,
  ["PortableView"]:PortableView,
  ["use_page_controller"]:use_page_controller,
  ["PortableControllerView"]:PortableControllerView
}