const ui = require("@xtalk/ui/core.js")

function register(registry,component_id,props,events,slots){
  return ui.registry_register_contract(
    registry,
    ui.component_contract(component_id,"portable",props,events,slots,null)
  );
}

function semantic_registry(){
  let registry = ui.registry_create("xt.ui/semantic");
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
  return registry;
}

function registry(){
  return ui.registry_compose([ui.base_registry(),semantic_registry()]);
}

module.exports = {
  ["register"]:register,
  ["semantic_registry"]:semantic_registry,
  ["registry"]:registry
}