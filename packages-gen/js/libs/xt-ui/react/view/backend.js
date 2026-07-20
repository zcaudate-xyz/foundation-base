const FigmaUi = require("@xtalk/figma-ui")

const React = require("react")

const catalog = require("@xtalk/substrate/view-catalog.js")

const xts = require("@xtalk/lang/common-string.js")

function native_registry(){
  return {
    "ui/description":{"tag":"p","value_prop":"value"},
    "ui/column":{"tag":"div","column":true},
    "ui/card-content":{"tag":FigmaUi.CardContent},
    "ui/row":{"tag":"div","row":true},
    "ui/textarea":{"tag":FigmaUi.Textarea},
    "ui/card-description":{"tag":FigmaUi.CardDescription,"value_prop":"value"},
    "ui/icon":{"tag":"span","value_prop":"value"},
    "ui/text":{"tag":"span","value_prop":"value"},
    "ui/separator":{"tag":FigmaUi.Separator},
    "ui/label":{"tag":FigmaUi.Label,"value_prop":"value"},
    "ui/input":{"tag":FigmaUi.Input},
    "ui/badge":{"tag":FigmaUi.Badge,"value_prop":"value"},
    "ui/card-title":{"tag":FigmaUi.CardTitle,"value_prop":"value"},
    "ui/card-footer":{"tag":FigmaUi.CardFooter},
    "ui/table-row":{"tag":FigmaUi.TableRow},
    "ui/spinner":{"tag":FigmaUi.Skeleton},
    "ui/table":{"tag":FigmaUi.Table},
    "ui/title":{"tag":"h2","value_prop":"value"},
    "ui/table-body":{"tag":FigmaUi.TableBody},
    "ui/card":{"tag":FigmaUi.Card},
    "ui/table-header":{"tag":FigmaUi.TableHeader},
    "ui/table-cell":{"tag":FigmaUi.TableCell,"value_prop":"value"},
    "ui/alert":{"tag":FigmaUi.Alert},
    "ui/fragment":{"tag":React.Fragment},
    "ui/table-head":{"tag":FigmaUi.TableHead,"value_prop":"value"},
    "ui/card-header":{"tag":FigmaUi.CardHeader},
    "ui/button":{"tag":FigmaUi.Button},
    "ui/image":{"tag":"img"},
    "ui/scroll":{"tag":"div"}
  };
}

function pascal_case(s){
  let out = "";
  for(let part of xts.split(s,"-")){
    out = (out + xts.capitalize(part));
  };
  return out;
}

function native_entry(registry,component_id){
  let entry = (registry["native"])[component_id];
  if(null != entry){
    return entry;
  }
  if(catalog.platform_idp(component_id)){
    let name = pascal_case(xts.substring(component_id,3,component_id.length));
    let tag = FigmaUi[name];
    if(null == tag){
      throw "figma component missing [react] - " + component_id;
    }
    return {"tag":tag};
  }
  return null;
}

function registry(overrides,polyfills){
  return {
    "backend":"react",
    "native":native_registry(),
    "polyfills":polyfills || {},
    "overrides":overrides || {}
  };
}

function dom_props(runtime,props,entry){
  let out = {};
  for(let key of [
    "id",
    "value",
    "placeholder",
    "type",
    "disabled",
    "rows",
    "src",
    "alt",
    "variant",
    "size",
    "href"
  ]){
    if(null != props[key]){
      out[key] = props[key];
    }
  };
  if(null != props["for"]){
    out["htmlFor"] = props["for"];
    delete(out["for"]);
  }
  let class_name = props["class"];
  if(entry["row"]){
    class_name = ("flex flex-row " + (class_name || ""));
  }
  if(entry["column"]){
    class_name = ("flex flex-col " + (class_name || ""));
  }
  if(props["pending"]){
    out["disabled"] = true;
    class_name = ((class_name || "") + " opacity-60 pointer-events-none");
  }
  if(class_name){
    out["className"] = class_name;
  }
  let aria_label = props["aria_label"];
  if(aria_label){
    out["aria-label"] = aria_label;
  }
  if(null != props["read_only"]){
    out["readOnly"] = props["read_only"];
  }
  let on_change = props["on_change"];
  if(on_change){
    out["onChange"] = (function (event){
      return runtime["dispatch"](on_change,{"value":event.target.value});
    });
  }
  let on_press = props["on_press"];
  if(on_press){
    out["onClick"] = (function (event){
      return runtime["dispatch"](on_press,event);
    });
  }
  return out;
}

function render_native(runtime,entry,props,children){
  let tag = entry["tag"];
  let dom_props = dom_props(runtime,props,entry);
  let value_prop = entry["value_prop"];
  let value = value_prop ? props[value_prop] : null;
  return React.createElement(tag,dom_props,value || children);
}

module.exports = {
  ["native_registry"]:native_registry,
  ["pascal_case"]:pascal_case,
  ["native_entry"]:native_entry,
  ["registry"]:registry,
  ["dom_props"]:dom_props,
  ["render_native"]:render_native
}