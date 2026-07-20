import 'package:xtalk_ui/core.dart' as ui;
import 'package:xtalk_lang/common-data.dart' as xtd;
import 'package:xtalk_ui/widgets/core.dart' as catalog;




flutter_registry() {
  var platform = ui.registry_create("xt.ui/flutter-wind");
  var arr_53025 = <dynamic>[
    <dynamic>["ui/fragment","WDiv"],
    <dynamic>["ui/row","WDiv"],
    <dynamic>["ui/column","WDiv"],
    <dynamic>["ui/text","WText"],
    <dynamic>["ui/title","WText"],
    <dynamic>["ui/description","WText"],
    <dynamic>["ui/label","WText"],
    <dynamic>["ui/icon","WIcon"],
    <dynamic>["ui/image","WImage"],
    <dynamic>["ui/card","WDiv"],
    <dynamic>["ui/card-header","WDiv"],
    <dynamic>["ui/card-content","WDiv"],
    <dynamic>["ui/input","WInput"],
    <dynamic>["ui/textarea","WInput"],
    <dynamic>["ui/button","WButton"],
    <dynamic>["ui/alert","WDiv"],
    <dynamic>["ui/spinner","WDiv"]
  ];
  for(var i53026 = 0; i53026 < arr_53025.length; ++i53026){
    var entry = arr_53025[i53026];
    ui.registry_register_renderer(platform,entry[0],entry[1]);
  };
  return ui.registry_compose(<dynamic>[catalog.registry(),platform]);
}

action_addf(state, event_id, callback, value_event) {
  var action_id = "xt_ui_" + (state["next"]).toString();
  state["next"] = (1 + state["next"]);
  state["actions"][action_id] = ((args) {
    return ((null != value_event) && (false != value_event)) ? callback(args["_value"]) : callback(args);
  });
  return <dynamic, dynamic>{event_id:<dynamic, dynamic>{"action":action_id}};
}

normalize_props(component_id, props, state) {
  var out = <dynamic, dynamic>{};
  for(var entry_53047 in (props ?? <dynamic, dynamic>{}).entries){
    var key = entry_53047.key;
    var value = entry_53047.value;
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
      xtd.obj_assign(out,action_addf(state,"onTap",value,false));
    }
    else if(key == "on_change"){
      xtd.obj_assign(out,action_addf(state,"onChange",value,true));
    }
    else if((key == "hidden") || (key == "on_submit") || (key == "variant") || (key == "size") || (key == "tone")){
      null;
    }
    else{
      out[key] = value;
    }
  };
  if(component_id == "ui/textarea"){
    out["maxLines"] = (props["rows"] ?? 4);
    out.remove("rows");
  }
  if(component_id == "ui/row"){
    out["className"] = ("flex flex-row " + (out["className"] ?? ""));
  }
  if((component_id == "ui/column") || (component_id == "ui/fragment")){
    out["className"] = ("flex flex-col " + (out["className"] ?? ""));
  }
  return out;
}

prepare_node(runtime, value, state) {
  if(null == value){
    return null;
  }
  if(("String" == (value.runtimeType).toString()) || (("int" == (value.runtimeType).toString()) || ("double" == (value.runtimeType).toString()) || ("num" == (value.runtimeType).toString()))){
    return <dynamic, dynamic>{
      "type":"WText",
      "props":<dynamic, dynamic>{"text":(value).toString()}
    };
  }
  if((value.runtimeType).toString().startsWith("List") || (value.runtimeType).toString().startsWith("_GrowableList")){
    return xtd.arr_map(value,(child) {
      return prepare_node(runtime,child,state);
    });
  }
  var component_id = value["component"];
  if(component_id == "ui/slot"){
    return prepare_node(runtime,ui.resolve_slot(runtime,value),state);
  }
  var props = value["props"];
  if(true == props["hidden"]){
    return null;
  }
  var renderer = ui.registry_renderer(runtime["registry"],component_id);
  if(!((null != renderer) && (false != renderer))){
    return <dynamic, dynamic>{
      "type":"WDiv",
      "props":<dynamic, dynamic>{"className":"flex flex-col"},
      "children":prepare_node(runtime,value["children"],state)
    };
  }
  var out_props = normalize_props(component_id,props,state);
  if((renderer == "WText") || (component_id == "ui/label")){
    out_props["text"] = (props["value"] ?? "");
    out_props.remove("value");
    out_props.remove("for");
  }
  return <dynamic, dynamic>{
    "type":renderer,
    "props":out_props,
    "children":prepare_node(runtime,value["children"],state)
  };
}

prepare(runtime, value) {
  var state = <dynamic, dynamic>{"next":0,"actions":<dynamic, dynamic>{}};
  var json = prepare_node(runtime,value,state);
  return <dynamic, dynamic>{"json":json,"actions":state["actions"]};
}