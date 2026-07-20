import 'package:xtalk_substrate/view-catalog.dart' as catalog;


native_registry() {
  return <dynamic, dynamic>{
    "ui/description":<dynamic, dynamic>{"type":"WText","value_prop":"value"},
    "ui/column":<dynamic, dynamic>{"type":"WDiv","layout":"column"},
    "ui/row":<dynamic, dynamic>{"type":"WDiv","layout":"row"},
    "ui/textarea":<dynamic, dynamic>{"type":"WInput","input":true},
    "ui/icon":<dynamic, dynamic>{"type":"WIcon","value_prop":"value"},
    "ui/text":<dynamic, dynamic>{"type":"WText","value_prop":"value"},
    "ui/label":<dynamic, dynamic>{"type":"WText","value_prop":"value"},
    "ui/input":<dynamic, dynamic>{"type":"WInput","input":true},
    "ui/spinner":<dynamic, dynamic>{"type":"WText","value_prop":"value"},
    "ui/title":<dynamic, dynamic>{"type":"WText","value_prop":"value","role":"title"},
    "ui/alert":<dynamic, dynamic>{"type":"WDiv","layout":"column"},
    "ui/fragment":<dynamic, dynamic>{"type":"WDiv","layout":"column"},
    "ui/button":<dynamic, dynamic>{"type":"WButton","press":true},
    "ui/image":<dynamic, dynamic>{"type":"WImage"},
    "ui/scroll":<dynamic, dynamic>{"type":"WDiv","layout":"column"}
  };
}

native_entry(registry, component_id) {
  if((() {
    var dart_truthy__53181 = catalog.platform_idp(component_id);
    return (null != dart_truthy__53181) && (false != dart_truthy__53181);
  })()){
    throw "platform view component not portable [wind] - " + component_id;
  }
  return (registry["native"])[component_id];
}

registry(overrides, polyfills) {
  return <dynamic, dynamic>{
    "backend":"wind",
    "native":native_registry(),
    "polyfills":polyfills ?? <dynamic, dynamic>{},
    "overrides":overrides ?? <dynamic, dynamic>{}
  };
}

action_add(runtime, state, event_id, action_desc, value_event) {
  var action_id = "substrate_view_" + (state["next"]).toString();
  state["next"] = (1 + state["next"]);
  state["actions"][action_id] = ((args) {
    var event = ((null != value_event) && (false != value_event)) ? <dynamic, dynamic>{"value":args["_value"]} : (args ?? <dynamic, dynamic>{});
    return runtime["dispatch"](action_desc,event);
  });
  return <dynamic, dynamic>{event_id:<dynamic, dynamic>{"action":action_id}};
}

props(runtime, component_id, input, state, entry) {
  var out = <dynamic, dynamic>{};
  for(var entry_53182 in (input ?? <dynamic, dynamic>{}).entries){
    var key = entry_53182.key;
    var value = entry_53182.value;
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
    else if(key == "hidden"){
      null;
    }
    else if(key == "variant"){
      null;
    }
    else if(key == "on_press"){
      xt.lang.common_data.obj_assign(out,action_add(runtime,state,"onTap",value,false));
    }
    else if(key == "on_change"){
      xt.lang.common_data.obj_assign(out,action_add(runtime,state,"onChange",value,true));
    }
    else{
      out[key] = value;
    }
  };
  var variant = input["variant"];
  if(null != variant){
    var classes = catalog.variant_classes(component_id,variant);
    if(null != classes){
      var current = out["className"] ?? "";
      if(0 < current.length){
        current = (current + " ");
      }
      out["className"] = (current + classes);
    }
  }
  var layout = entry["layout"];
  if((null != layout) && (false != layout)){
    var base = (layout == "row") ? "flex flex-row" : "flex flex-col";
    out["className"] = (base + " " + (out["className"] ?? ""));
  }
  var value_prop = entry["value_prop"];
  if((null != value_prop) && (false != value_prop)){
    out["text"] = (input[value_prop] ?? "");
    out.remove(value_prop);
  }
  return out;
}

prepare_native(runtime, component_id, entry, props, children, state) {
  return <dynamic, dynamic>{
    "type":entry["type"],
    "props":props(runtime,component_id,props,state,entry),
    "children":children
  };
}