import 'package:xtalk_lang/common-string.dart' as xts;


platform_idp(component_id) {
  return xts.starts_withp(component_id,"fg/");
}

entry(component_id) {
  return   (<dynamic, dynamic>{
      "ui/description":<dynamic, dynamic>{
          "band":"core",
          "kind":"text",
          "props":<dynamic, dynamic>{"value":<dynamic, dynamic>{"type":"string"}}
        },
      "ui/column":<dynamic, dynamic>{"band":"core","kind":"layout"},
      "ui/card-content":<dynamic, dynamic>{"band":"core","kind":"container"},
      "ui/row":<dynamic, dynamic>{"band":"core","kind":"layout"},
      "ui/textarea":<dynamic, dynamic>{
          "band":"core",
          "kind":"input",
          "props":<dynamic, dynamic>{
              "value":<dynamic, dynamic>{"type":"string"},
              "placeholder":<dynamic, dynamic>{"type":"string"},
              "rows":<dynamic, dynamic>{"type":"number"},
              "disabled":<dynamic, dynamic>{"type":"boolean"},
              "read_only":<dynamic, dynamic>{"type":"boolean"}
            },
          "events":<dynamic, dynamic>{"on_change":<dynamic, dynamic>{"path":<dynamic>["value"]}}
        },
      "ui/card-description":<dynamic, dynamic>{
          "band":"core",
          "kind":"text",
          "props":<dynamic, dynamic>{"value":<dynamic, dynamic>{"type":"string"}}
        },
      "ui/icon":<dynamic, dynamic>{
          "band":"core",
          "kind":"display",
          "props":<dynamic, dynamic>{"value":<dynamic, dynamic>{"type":"string"}}
        },
      "ui/text":<dynamic, dynamic>{
          "band":"core",
          "kind":"text",
          "props":<dynamic, dynamic>{"value":<dynamic, dynamic>{"type":"string"}}
        },
      "ui/separator":<dynamic, dynamic>{"band":"core","kind":"layout"},
      "ui/label":<dynamic, dynamic>{
          "band":"core",
          "kind":"text",
          "props":<dynamic, dynamic>{
              "value":<dynamic, dynamic>{"type":"string"},
              "for":<dynamic, dynamic>{"type":"string"}
            }
        },
      "ui/input":<dynamic, dynamic>{
          "band":"core",
          "kind":"input",
          "props":<dynamic, dynamic>{
              "value":<dynamic, dynamic>{"type":"string"},
              "placeholder":<dynamic, dynamic>{"type":"string"},
              "type":<dynamic, dynamic>{"type":"string"},
              "disabled":<dynamic, dynamic>{"type":"boolean"},
              "read_only":<dynamic, dynamic>{"type":"boolean"}
            },
          "events":<dynamic, dynamic>{"on_change":<dynamic, dynamic>{"path":<dynamic>["value"]}}
        },
      "ui/badge":<dynamic, dynamic>{
          "band":"core",
          "kind":"display",
          "props":<dynamic, dynamic>{
              "value":<dynamic, dynamic>{"type":"string"},
              "variant":<dynamic, dynamic>{"type":"string"}
            },
          "variants":<dynamic, dynamic>{
              "default":"bg-slate-900 text-white",
              "secondary":"bg-slate-100 text-slate-900",
              "outline":"border border-slate-300 text-slate-900",
              "destructive":"bg-red-600 text-white"
            }
        },
      "ui/card-title":<dynamic, dynamic>{
          "band":"core",
          "kind":"text",
          "props":<dynamic, dynamic>{"value":<dynamic, dynamic>{"type":"string"}}
        },
      "ui/card-footer":<dynamic, dynamic>{"band":"core","kind":"container"},
      "ui/table-row":<dynamic, dynamic>{"band":"core","kind":"container"},
      "ui/spinner":<dynamic, dynamic>{
          "band":"core",
          "kind":"display",
          "props":<dynamic, dynamic>{"value":<dynamic, dynamic>{"type":"string"}}
        },
      "ui/table":<dynamic, dynamic>{"band":"core","kind":"container"},
      "ui/title":<dynamic, dynamic>{
          "band":"core",
          "kind":"text",
          "props":<dynamic, dynamic>{"value":<dynamic, dynamic>{"type":"string"}}
        },
      "ui/table-body":<dynamic, dynamic>{"band":"core","kind":"container"},
      "ui/card":<dynamic, dynamic>{"band":"core","kind":"container"},
      "ui/table-header":<dynamic, dynamic>{"band":"core","kind":"container"},
      "ui/table-cell":<dynamic, dynamic>{
          "band":"core",
          "kind":"text",
          "props":<dynamic, dynamic>{"value":<dynamic, dynamic>{"type":"string"}}
        },
      "ui/alert":<dynamic, dynamic>{
          "band":"core",
          "kind":"display",
          "props":<dynamic, dynamic>{"variant":<dynamic, dynamic>{"type":"string"}},
          "variants":<dynamic, dynamic>{
              "default":"border-slate-200 bg-white text-slate-900",
              "destructive":"border-red-200 bg-red-50 text-red-700"
            }
        },
      "ui/fragment":<dynamic, dynamic>{"band":"core","kind":"layout"},
      "ui/table-head":<dynamic, dynamic>{
          "band":"core",
          "kind":"text",
          "props":<dynamic, dynamic>{"value":<dynamic, dynamic>{"type":"string"}}
        },
      "ui/card-header":<dynamic, dynamic>{"band":"core","kind":"container"},
      "ui/button":<dynamic, dynamic>{
          "band":"core",
          "kind":"action",
          "props":<dynamic, dynamic>{
              "value":<dynamic, dynamic>{"type":"string"},
              "variant":<dynamic, dynamic>{"type":"string"},
              "size":<dynamic, dynamic>{"type":"string"},
              "disabled":<dynamic, dynamic>{"type":"boolean"},
              "pending":<dynamic, dynamic>{"type":"boolean"}
            },
          "events":<dynamic, dynamic>{"on_press":<dynamic, dynamic>{"path":null}},
          "variants":<dynamic, dynamic>{
              "default":"bg-slate-900 text-white",
              "secondary":"bg-slate-100 text-slate-900",
              "outline":"border border-slate-300 bg-white text-slate-900",
              "ghost":"text-slate-900",
              "destructive":"bg-red-600 text-white",
              "link":"text-blue-600 underline underline-offset-4"
            }
        },
      "ui/image":<dynamic, dynamic>{
          "band":"core",
          "kind":"display",
          "props":<dynamic, dynamic>{
              "src":<dynamic, dynamic>{"type":"string","required":true},
              "alt":<dynamic, dynamic>{"type":"string"}
            }
        },
      "ui/scroll":<dynamic, dynamic>{"band":"core","kind":"layout"}
    })[component_id];
}

has_componentp(component_id) {
  return null !=   (<dynamic, dynamic>{
      "ui/description":<dynamic, dynamic>{
          "band":"core",
          "kind":"text",
          "props":<dynamic, dynamic>{"value":<dynamic, dynamic>{"type":"string"}}
        },
      "ui/column":<dynamic, dynamic>{"band":"core","kind":"layout"},
      "ui/card-content":<dynamic, dynamic>{"band":"core","kind":"container"},
      "ui/row":<dynamic, dynamic>{"band":"core","kind":"layout"},
      "ui/textarea":<dynamic, dynamic>{
          "band":"core",
          "kind":"input",
          "props":<dynamic, dynamic>{
              "value":<dynamic, dynamic>{"type":"string"},
              "placeholder":<dynamic, dynamic>{"type":"string"},
              "rows":<dynamic, dynamic>{"type":"number"},
              "disabled":<dynamic, dynamic>{"type":"boolean"},
              "read_only":<dynamic, dynamic>{"type":"boolean"}
            },
          "events":<dynamic, dynamic>{"on_change":<dynamic, dynamic>{"path":<dynamic>["value"]}}
        },
      "ui/card-description":<dynamic, dynamic>{
          "band":"core",
          "kind":"text",
          "props":<dynamic, dynamic>{"value":<dynamic, dynamic>{"type":"string"}}
        },
      "ui/icon":<dynamic, dynamic>{
          "band":"core",
          "kind":"display",
          "props":<dynamic, dynamic>{"value":<dynamic, dynamic>{"type":"string"}}
        },
      "ui/text":<dynamic, dynamic>{
          "band":"core",
          "kind":"text",
          "props":<dynamic, dynamic>{"value":<dynamic, dynamic>{"type":"string"}}
        },
      "ui/separator":<dynamic, dynamic>{"band":"core","kind":"layout"},
      "ui/label":<dynamic, dynamic>{
          "band":"core",
          "kind":"text",
          "props":<dynamic, dynamic>{
              "value":<dynamic, dynamic>{"type":"string"},
              "for":<dynamic, dynamic>{"type":"string"}
            }
        },
      "ui/input":<dynamic, dynamic>{
          "band":"core",
          "kind":"input",
          "props":<dynamic, dynamic>{
              "value":<dynamic, dynamic>{"type":"string"},
              "placeholder":<dynamic, dynamic>{"type":"string"},
              "type":<dynamic, dynamic>{"type":"string"},
              "disabled":<dynamic, dynamic>{"type":"boolean"},
              "read_only":<dynamic, dynamic>{"type":"boolean"}
            },
          "events":<dynamic, dynamic>{"on_change":<dynamic, dynamic>{"path":<dynamic>["value"]}}
        },
      "ui/badge":<dynamic, dynamic>{
          "band":"core",
          "kind":"display",
          "props":<dynamic, dynamic>{
              "value":<dynamic, dynamic>{"type":"string"},
              "variant":<dynamic, dynamic>{"type":"string"}
            },
          "variants":<dynamic, dynamic>{
              "default":"bg-slate-900 text-white",
              "secondary":"bg-slate-100 text-slate-900",
              "outline":"border border-slate-300 text-slate-900",
              "destructive":"bg-red-600 text-white"
            }
        },
      "ui/card-title":<dynamic, dynamic>{
          "band":"core",
          "kind":"text",
          "props":<dynamic, dynamic>{"value":<dynamic, dynamic>{"type":"string"}}
        },
      "ui/card-footer":<dynamic, dynamic>{"band":"core","kind":"container"},
      "ui/table-row":<dynamic, dynamic>{"band":"core","kind":"container"},
      "ui/spinner":<dynamic, dynamic>{
          "band":"core",
          "kind":"display",
          "props":<dynamic, dynamic>{"value":<dynamic, dynamic>{"type":"string"}}
        },
      "ui/table":<dynamic, dynamic>{"band":"core","kind":"container"},
      "ui/title":<dynamic, dynamic>{
          "band":"core",
          "kind":"text",
          "props":<dynamic, dynamic>{"value":<dynamic, dynamic>{"type":"string"}}
        },
      "ui/table-body":<dynamic, dynamic>{"band":"core","kind":"container"},
      "ui/card":<dynamic, dynamic>{"band":"core","kind":"container"},
      "ui/table-header":<dynamic, dynamic>{"band":"core","kind":"container"},
      "ui/table-cell":<dynamic, dynamic>{
          "band":"core",
          "kind":"text",
          "props":<dynamic, dynamic>{"value":<dynamic, dynamic>{"type":"string"}}
        },
      "ui/alert":<dynamic, dynamic>{
          "band":"core",
          "kind":"display",
          "props":<dynamic, dynamic>{"variant":<dynamic, dynamic>{"type":"string"}},
          "variants":<dynamic, dynamic>{
              "default":"border-slate-200 bg-white text-slate-900",
              "destructive":"border-red-200 bg-red-50 text-red-700"
            }
        },
      "ui/fragment":<dynamic, dynamic>{"band":"core","kind":"layout"},
      "ui/table-head":<dynamic, dynamic>{
          "band":"core",
          "kind":"text",
          "props":<dynamic, dynamic>{"value":<dynamic, dynamic>{"type":"string"}}
        },
      "ui/card-header":<dynamic, dynamic>{"band":"core","kind":"container"},
      "ui/button":<dynamic, dynamic>{
          "band":"core",
          "kind":"action",
          "props":<dynamic, dynamic>{
              "value":<dynamic, dynamic>{"type":"string"},
              "variant":<dynamic, dynamic>{"type":"string"},
              "size":<dynamic, dynamic>{"type":"string"},
              "disabled":<dynamic, dynamic>{"type":"boolean"},
              "pending":<dynamic, dynamic>{"type":"boolean"}
            },
          "events":<dynamic, dynamic>{"on_press":<dynamic, dynamic>{"path":null}},
          "variants":<dynamic, dynamic>{
              "default":"bg-slate-900 text-white",
              "secondary":"bg-slate-100 text-slate-900",
              "outline":"border border-slate-300 bg-white text-slate-900",
              "ghost":"text-slate-900",
              "destructive":"bg-red-600 text-white",
              "link":"text-blue-600 underline underline-offset-4"
            }
        },
      "ui/image":<dynamic, dynamic>{
          "band":"core",
          "kind":"display",
          "props":<dynamic, dynamic>{
              "src":<dynamic, dynamic>{"type":"string","required":true},
              "alt":<dynamic, dynamic>{"type":"string"}
            }
        },
      "ui/scroll":<dynamic, dynamic>{"band":"core","kind":"layout"}
    })[component_id];
}

band(component_id) {
  if((() {
    var dart_truthy__51501 = platform_idp(component_id);
    return (null != dart_truthy__51501) && (false != dart_truthy__51501);
  })()){
    return "platform";
  }
  var entry = entry(component_id);
  if(null == entry){
    return null;
  }
  return entry["band"];
}

portablep(component_id) {
  if((() {
    var dart_truthy__51500 = platform_idp(component_id);
    return (null != dart_truthy__51500) && (false != dart_truthy__51500);
  })()){
    return false;
  }
  else{
    return null != entry(component_id);
  }
}

variant_classes(component_id, variant) {
  var entry = entry(component_id);
  if(null == entry){
    return null;
  }
  var variants = entry["variants"];
  if(null == variants){
    return null;
  }
  return variants[variant];
}

validate_action(component_id, prop, value) {
  if(!(("Map" == (value.runtimeType).toString()) || (value.runtimeType).toString().startsWith("_Map") || (value.runtimeType).toString().startsWith("LinkedMap"))){
    throw "view event requires an action descriptor - " + component_id + " - " + prop;
  }
  if(!("String" == (value["action"].runtimeType).toString())){
    throw "view event requires an action id - " + component_id + " - " + prop;
  }
  var payload = value["payload"];
  if((("Map" == (payload.runtimeType).toString()) || (payload.runtimeType).toString().startsWith("_Map") || (payload.runtimeType).toString().startsWith("LinkedMap")) && payload.containsKey("\$")){
    if("event" != payload["\$"]){
      throw "invalid view event projection - " + component_id + " - " + prop;
    }
    if(!((payload["path"].runtimeType).toString().startsWith("List") || (payload["path"].runtimeType).toString().startsWith("_GrowableList"))){
      throw "view event projection requires a path - " + component_id + " - " + prop;
    }
  }
  return true;
}

validate_prop_type(component_id, prop, spec, value) {
  var type = spec["type"];
  var ok = true;
  if(type == "string"){
    ok = ("String" == (value.runtimeType).toString());
  }
  else if(type == "number"){
    ok = (("int" == (value.runtimeType).toString()) || ("double" == (value.runtimeType).toString()) || ("num" == (value.runtimeType).toString()));
  }
  else if(type == "boolean"){
    ok = ("bool" == (value.runtimeType).toString());
  }
  else{
    ok = true;
  }
  if(!((null != ok) && (false != ok))){
    throw "invalid view prop type - " + component_id + " - " + prop + " - " + type;
  }
  return true;
}

validate_props(component_id, props) {
  var entry = entry(component_id);
  var eprops = entry["props"];
  var events = entry["events"];
  for(var entry_51502 in (props ?? <dynamic, dynamic>{}).entries){
    var prop = entry_51502.key;
    var value = entry_51502.value;
    if((events ?? <dynamic, dynamic>{}).containsKey(prop)){
      validate_action(component_id,prop,value);
    }
    else if(<dynamic, dynamic>{
      "id":<dynamic, dynamic>{"type":"string"},
      "class":<dynamic, dynamic>{"type":"string"},
      "hidden":<dynamic, dynamic>{"type":"boolean"},
      "aria_label":<dynamic, dynamic>{"type":"string"}
    }.containsKey(prop)){
      validate_prop_type(component_id,prop,      (<dynamic, dynamic>{
              "id":<dynamic, dynamic>{"type":"string"},
              "class":<dynamic, dynamic>{"type":"string"},
              "hidden":<dynamic, dynamic>{"type":"boolean"},
              "aria_label":<dynamic, dynamic>{"type":"string"}
            })[prop],value);
    }
    else if((eprops ?? <dynamic, dynamic>{}).containsKey(prop)){
      validate_prop_type(component_id,prop,eprops[prop],value);
      if(prop == "variant"){
        var variants = entry["variants"];
        if((null != variants) && !variants.containsKey(value)){
          throw "unknown view variant - " + component_id + " - " + value;
        }
      }
    }
    else{
      throw "unknown view prop - " + component_id + " - " + prop;
    }
  };
  for(var entry_51503 in (eprops ?? <dynamic, dynamic>{}).entries){
    var prop = entry_51503.key;
    var spec = entry_51503.value;
    if((true == spec["required"]) && !(props ?? <dynamic, dynamic>{}).containsKey(prop)){
      throw "missing required view prop - " + component_id + " - " + prop;
    }
  };
  return true;
}