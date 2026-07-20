const xts = require("@xtalk/lang/common-string.js")

function platform_idp(component_id){
  return xts.starts_withp(component_id,"fg/");
}

function entry(component_id){
  return   ({
      "ui/description":{
          "band":"core",
          "kind":"text",
          "props":{"value":{"type":"string"}}
        },
      "ui/column":{"band":"core","kind":"layout"},
      "ui/card-content":{"band":"core","kind":"container"},
      "ui/row":{"band":"core","kind":"layout"},
      "ui/textarea":{
          "band":"core",
          "kind":"input",
          "props":{
              "value":{"type":"string"},
              "placeholder":{"type":"string"},
              "rows":{"type":"number"},
              "disabled":{"type":"boolean"},
              "read_only":{"type":"boolean"}
            },
          "events":{"on_change":{"path":["value"]}}
        },
      "ui/card-description":{
          "band":"core",
          "kind":"text",
          "props":{"value":{"type":"string"}}
        },
      "ui/icon":{
          "band":"core",
          "kind":"display",
          "props":{"value":{"type":"string"}}
        },
      "ui/text":{
          "band":"core",
          "kind":"text",
          "props":{"value":{"type":"string"}}
        },
      "ui/separator":{"band":"core","kind":"layout"},
      "ui/label":{
          "band":"core",
          "kind":"text",
          "props":{"value":{"type":"string"},"for":{"type":"string"}}
        },
      "ui/input":{
          "band":"core",
          "kind":"input",
          "props":{
              "value":{"type":"string"},
              "placeholder":{"type":"string"},
              "type":{"type":"string"},
              "disabled":{"type":"boolean"},
              "read_only":{"type":"boolean"}
            },
          "events":{"on_change":{"path":["value"]}}
        },
      "ui/badge":{
          "band":"core",
          "kind":"display",
          "props":{"value":{"type":"string"},"variant":{"type":"string"}},
          "variants":{
              "default":"bg-slate-900 text-white",
              "secondary":"bg-slate-100 text-slate-900",
              "outline":"border border-slate-300 text-slate-900",
              "destructive":"bg-red-600 text-white"
            }
        },
      "ui/card-title":{
          "band":"core",
          "kind":"text",
          "props":{"value":{"type":"string"}}
        },
      "ui/card-footer":{"band":"core","kind":"container"},
      "ui/table-row":{"band":"core","kind":"container"},
      "ui/spinner":{
          "band":"core",
          "kind":"display",
          "props":{"value":{"type":"string"}}
        },
      "ui/table":{"band":"core","kind":"container"},
      "ui/title":{
          "band":"core",
          "kind":"text",
          "props":{"value":{"type":"string"}}
        },
      "ui/table-body":{"band":"core","kind":"container"},
      "ui/card":{"band":"core","kind":"container"},
      "ui/table-header":{"band":"core","kind":"container"},
      "ui/table-cell":{
          "band":"core",
          "kind":"text",
          "props":{"value":{"type":"string"}}
        },
      "ui/alert":{
          "band":"core",
          "kind":"display",
          "props":{"variant":{"type":"string"}},
          "variants":{
              "default":"border-slate-200 bg-white text-slate-900",
              "destructive":"border-red-200 bg-red-50 text-red-700"
            }
        },
      "ui/fragment":{"band":"core","kind":"layout"},
      "ui/table-head":{
          "band":"core",
          "kind":"text",
          "props":{"value":{"type":"string"}}
        },
      "ui/card-header":{"band":"core","kind":"container"},
      "ui/button":{
          "band":"core",
          "kind":"action",
          "props":{
              "value":{"type":"string"},
              "variant":{"type":"string"},
              "size":{"type":"string"},
              "disabled":{"type":"boolean"},
              "pending":{"type":"boolean"}
            },
          "events":{"on_press":{"path":null}},
          "variants":{
              "default":"bg-slate-900 text-white",
              "secondary":"bg-slate-100 text-slate-900",
              "outline":"border border-slate-300 bg-white text-slate-900",
              "ghost":"text-slate-900",
              "destructive":"bg-red-600 text-white",
              "link":"text-blue-600 underline underline-offset-4"
            }
        },
      "ui/image":{
          "band":"core",
          "kind":"display",
          "props":{
              "src":{"type":"string","required":true},
              "alt":{"type":"string"}
            }
        },
      "ui/scroll":{"band":"core","kind":"layout"}
    })[component_id];
}

function has_componentp(component_id){
  return null !=   ({
      "ui/description":{
          "band":"core",
          "kind":"text",
          "props":{"value":{"type":"string"}}
        },
      "ui/column":{"band":"core","kind":"layout"},
      "ui/card-content":{"band":"core","kind":"container"},
      "ui/row":{"band":"core","kind":"layout"},
      "ui/textarea":{
          "band":"core",
          "kind":"input",
          "props":{
              "value":{"type":"string"},
              "placeholder":{"type":"string"},
              "rows":{"type":"number"},
              "disabled":{"type":"boolean"},
              "read_only":{"type":"boolean"}
            },
          "events":{"on_change":{"path":["value"]}}
        },
      "ui/card-description":{
          "band":"core",
          "kind":"text",
          "props":{"value":{"type":"string"}}
        },
      "ui/icon":{
          "band":"core",
          "kind":"display",
          "props":{"value":{"type":"string"}}
        },
      "ui/text":{
          "band":"core",
          "kind":"text",
          "props":{"value":{"type":"string"}}
        },
      "ui/separator":{"band":"core","kind":"layout"},
      "ui/label":{
          "band":"core",
          "kind":"text",
          "props":{"value":{"type":"string"},"for":{"type":"string"}}
        },
      "ui/input":{
          "band":"core",
          "kind":"input",
          "props":{
              "value":{"type":"string"},
              "placeholder":{"type":"string"},
              "type":{"type":"string"},
              "disabled":{"type":"boolean"},
              "read_only":{"type":"boolean"}
            },
          "events":{"on_change":{"path":["value"]}}
        },
      "ui/badge":{
          "band":"core",
          "kind":"display",
          "props":{"value":{"type":"string"},"variant":{"type":"string"}},
          "variants":{
              "default":"bg-slate-900 text-white",
              "secondary":"bg-slate-100 text-slate-900",
              "outline":"border border-slate-300 text-slate-900",
              "destructive":"bg-red-600 text-white"
            }
        },
      "ui/card-title":{
          "band":"core",
          "kind":"text",
          "props":{"value":{"type":"string"}}
        },
      "ui/card-footer":{"band":"core","kind":"container"},
      "ui/table-row":{"band":"core","kind":"container"},
      "ui/spinner":{
          "band":"core",
          "kind":"display",
          "props":{"value":{"type":"string"}}
        },
      "ui/table":{"band":"core","kind":"container"},
      "ui/title":{
          "band":"core",
          "kind":"text",
          "props":{"value":{"type":"string"}}
        },
      "ui/table-body":{"band":"core","kind":"container"},
      "ui/card":{"band":"core","kind":"container"},
      "ui/table-header":{"band":"core","kind":"container"},
      "ui/table-cell":{
          "band":"core",
          "kind":"text",
          "props":{"value":{"type":"string"}}
        },
      "ui/alert":{
          "band":"core",
          "kind":"display",
          "props":{"variant":{"type":"string"}},
          "variants":{
              "default":"border-slate-200 bg-white text-slate-900",
              "destructive":"border-red-200 bg-red-50 text-red-700"
            }
        },
      "ui/fragment":{"band":"core","kind":"layout"},
      "ui/table-head":{
          "band":"core",
          "kind":"text",
          "props":{"value":{"type":"string"}}
        },
      "ui/card-header":{"band":"core","kind":"container"},
      "ui/button":{
          "band":"core",
          "kind":"action",
          "props":{
              "value":{"type":"string"},
              "variant":{"type":"string"},
              "size":{"type":"string"},
              "disabled":{"type":"boolean"},
              "pending":{"type":"boolean"}
            },
          "events":{"on_press":{"path":null}},
          "variants":{
              "default":"bg-slate-900 text-white",
              "secondary":"bg-slate-100 text-slate-900",
              "outline":"border border-slate-300 bg-white text-slate-900",
              "ghost":"text-slate-900",
              "destructive":"bg-red-600 text-white",
              "link":"text-blue-600 underline underline-offset-4"
            }
        },
      "ui/image":{
          "band":"core",
          "kind":"display",
          "props":{
              "src":{"type":"string","required":true},
              "alt":{"type":"string"}
            }
        },
      "ui/scroll":{"band":"core","kind":"layout"}
    })[component_id];
}

function band(component_id){
  if(platform_idp(component_id)){
    return "platform";
  }
  let entry = entry(component_id);
  if(null == entry){
    return null;
  }
  return entry["band"];
}

function portablep(component_id){
  if(platform_idp(component_id)){
    return false;
  }
  else{
    return null != entry(component_id);
  }
}

function variant_classes(component_id,variant){
  let entry = entry(component_id);
  if(null == entry){
    return null;
  }
  let variants = entry["variants"];
  if(null == variants){
    return null;
  }
  return variants[variant];
}

function validate_action(component_id,prop,value){
  if(!((null != value) && ("object" == (typeof value)) && !Array.isArray(value))){
    throw "view event requires an action descriptor - " + component_id + " - " + prop;
  }
  if(!("string" == (typeof value["action"]))){
    throw "view event requires an action id - " + component_id + " - " + prop;
  }
  let payload = value["payload"];
  if(((null != payload) && ("object" == (typeof payload)) && !Array.isArray(payload)) && (null != payload["$"])){
    if("event" != payload["$"]){
      throw "invalid view event projection - " + component_id + " - " + prop;
    }
    if(!Array.isArray(payload["path"])){
      throw "view event projection requires a path - " + component_id + " - " + prop;
    }
  }
  return true;
}

function validate_prop_type(component_id,prop,spec,value){
  let type = spec["type"];
  let ok = true;
  if(type == "string"){
    ok = ("string" == (typeof value));
  }
  else if(type == "number"){
    ok = ("number" == (typeof value));
  }
  else if(type == "boolean"){
    ok = ("boolean" == (typeof value));
  }
  else{
    ok = true;
  }
  if(!ok){
    throw "invalid view prop type - " + component_id + " - " + prop + " - " + type;
  }
  return true;
}

function validate_props(component_id,props){
  let entry = entry(component_id);
  let eprops = entry["props"];
  let events = entry["events"];
  for(let [prop,value] of Object.entries(props || {})){
    if(null != (events || {})[prop]){
      validate_action(component_id,prop,value);
    }
    else if(null != {
      "id":{"type":"string"},
      "class":{"type":"string"},
      "hidden":{"type":"boolean"},
      "aria_label":{"type":"string"}
    }[prop]){
      validate_prop_type(component_id,prop,      ({
              "id":{"type":"string"},
              "class":{"type":"string"},
              "hidden":{"type":"boolean"},
              "aria_label":{"type":"string"}
            })[prop],value);
    }
    else if(null != (eprops || {})[prop]){
      validate_prop_type(component_id,prop,eprops[prop],value);
      if(prop == "variant"){
        let variants = entry["variants"];
        if((null != variants) && !(null != variants[value])){
          throw "unknown view variant - " + component_id + " - " + value;
        }
      }
    }
    else{
      throw "unknown view prop - " + component_id + " - " + prop;
    }
  };
  for(let [prop,spec] of Object.entries(eprops || {})){
    if((true == spec["required"]) && !(null != (props || {})[prop])){
      throw "missing required view prop - " + component_id + " - " + prop;
    }
  };
  return true;
}

module.exports = {
  ["platform_idp"]:platform_idp,
  ["entry"]:entry,
  ["has_componentp"]:has_componentp,
  ["band"]:band,
  ["portablep"]:portablep,
  ["variant_classes"]:variant_classes,
  ["validate_action"]:validate_action,
  ["validate_prop_type"]:validate_prop_type,
  ["validate_props"]:validate_props
}