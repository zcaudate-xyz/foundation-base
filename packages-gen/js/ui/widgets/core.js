const ui = require("@xtalk/ui/core.js")

const xtd = require("@xtalk/lang/common-data.js")

function register(registry,component_id,props,events,slots){
  return ui.registry_register_contract(
    registry,
    ui.component_contract(component_id,"portable",props,events,slots,null)
  );
}

function semantic_registry(){
  let registry = ui.registry_create("xt.ui/widgets");
  register(
    registry,
    "ui/card",
    ["class","hidden","key"],
    [],
    ["header","footer"]
  );
  register(registry,"ui/card-header",["class","hidden","key"],[],[]);
  register(registry,"ui/card-content",["class","hidden","key"],[],[]);
  register(registry,"ui/title",["value","class","hidden","key"],[],[]);
  register(
    registry,
    "ui/description",
    ["value","class","hidden","key"],
    [],
    []
  );
  register(
    registry,
    "ui/label",
    ["value","for","class","hidden","key"],
    [],
    []
  );
  register(registry,"ui/input",[
    "id",
    "value",
    "placeholder",
    "type",
    "class",
    "disabled",
    "read_only",
    "aria_label",
    "key"
  ],["on_change","on_submit"],[]);
  register(registry,"ui/textarea",[
    "id",
    "value",
    "placeholder",
    "class",
    "disabled",
    "read_only",
    "rows",
    "aria_label",
    "key"
  ],["on_change"],[]);
  register(
    registry,
    "ui/button",
    ["variant","size","class","disabled","pending","aria_label","key"],
    ["on_press"],
    []
  );
  register(registry,"ui/alert",["tone","class","hidden","key"],[],[]);
  register(registry,"ui/spinner",["class","label","hidden","key"],[],[]);
  register(
    registry,
    "ui/table",
    ["class","hidden","key"],
    [],
    ["header","body"]
  );
  register(registry,"ui/table-header",["class","hidden","key"],[],[]);
  register(registry,"ui/table-body",["class","hidden","key"],[],[]);
  register(
    registry,
    "ui/table-row",
    ["class","hidden","key","selected"],
    ["on_press"],
    []
  );
  register(registry,"ui/table-cell",["class","hidden","key","value"],[],[]);
  return registry;
}

function registry(){
  return ui.registry_compose([ui.base_registry(),semantic_registry()]);
}

function widget(component,props,children){
  return ui.node(component,props || {},children || []);
}

function field(component,id,label,value,props,on_change){
  return ui.node("ui/column",{"class":"gap-2"},[
    ui.node("ui/label",{"value":label,"for":id},[]),
    ui.node(component,xtd.obj_assign(
      {"id":id,"value":value || "","on_change":on_change},
      props || {}
    ),[])
  ]);
}

module.exports = {
  ["register"]:register,
  ["semantic_registry"]:semantic_registry,
  ["registry"]:registry,
  ["widget"]:widget,
  ["field"]:field
}