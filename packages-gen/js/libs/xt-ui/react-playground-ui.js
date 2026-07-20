const React = require("react")

const ui = require("@xtalk/ui/core.js")

const r = require("@xtalk/ui/react.js")

const state = require("@xtalk/ui/state/core.js")

function render_ui_node(registry,node){
  if(null == node){
    return null;
  }
  if("string" == (typeof node)){
    return node;
  }
  if("number" == (typeof node)){
    return String(node);
  }
  if(Array.isArray(node)){
    return node.map(function (child){
      return render_ui_node(registry,child);
    });
  }
  let component_id = node["component"];
  let props = node["props"] || {};
  let children = node["children"];
  let renderer = registry[component_id];
  if(null == renderer){
    return null;
  }
  return renderer(props,if(null == children){
    null;
  }
  else{
    children.map(function (child){
      return render_ui_node(registry,child);
    });
  });
}

function react_registry(){
  return {
    "ui/column":function (props,children){
        return React.createElement("div",Object.assign({
          "style":{
                "display":"flex",
                "flexDirection":"column",
                "gap":props["gap"] || "0px",
                "padding":props["padding"] || "0px"
              }
        },props),children);
      },
    "ui/row":function (props,children){
        return React.createElement("div",Object.assign({
          "style":{
                "display":"flex",
                "flexDirection":"row",
                "gap":props["gap"] || "0px",
                "alignItems":props["alignItems"] || "stretch"
              }
        },props),children);
      },
    "ui/text":function (props,_children){
        return React.createElement("span",props,props["value"] || "");
      },
    "ui/title":function (props,_children){
        return React.createElement("h2",props,props["value"] || "");
      },
    "ui/input":function (props,_children){
        return React.createElement("input",{
          "style":{
                "border":"1px solid #ccc",
                "borderRadius":"4px",
                "padding":"8px",
                "fontSize":"14px"
              },
          "type":"text",
          "value":props["value"] || "",
          "placeholder":props["placeholder"] || "",
          "disabled":true == props["disabled"],
          "onChange":function (event){
                let on_change = props["on_change"];
                if("function" == (typeof on_change)){
                  return on_change(event.target.value);
                }
                return undefined;
              }
        },null);
      },
    "ui/button":function (props,_children){
        return React.createElement("button",{
          "style":{
                "border":"1px solid #111",
                "background":"#111",
                "color":"#fff",
                "borderRadius":"4px",
                "padding":"8px 12px",
                "fontSize":"14px",
                "cursor":"pointer"
              },
          "disabled":true == props["disabled"],
          "onClick":function (){
                let on_press = props["on_press"];
                if("function" == (typeof on_press)){
                  return on_press();
                }
                return undefined;
              }
        },props["label"] || "Button");
      }
  };
}

function make_controller(){
  return state.controller_create({"items":["alpha","beta"],"draft":""},{
    "set_draft":function (controller,value,_deps){
        return state.update_statef(controller,function (s){
          return Object.assign(s,{"draft":value});
        });
      },
    "add_item":function (controller,_payload,_deps){
        return state.update_statef(controller,function (s){
          let draft = s["draft"] || "";
          let text = draft.trim();
          if("" == text){
            return s;
          }
          let items = s["items"] || [];
          return Object.assign(Object.assign(s,{"draft":""}),{"items":items.concat([text])});
        });
      },
    "remove_item":function (controller,idx,_deps){
        return state.update_statef(controller,function (s){
          let items = s["items"] || [];
          return Object.assign(s,{
            "items":items.filter(function (item,i){
                    return i != idx;
                  })
          });
        });
      }
  },{},{});
}

function view(state,actions){
  let draft = state["draft"] || "";
  let items = state["items"] || [];
  return ui.node("ui/column",{"gap":"12px","padding":"24px"},[
    ui.node("ui/title",{"value":"xt.ui React Playground"},[]),
    ui.node("ui/input",{
      "value":draft,
      "placeholder":"New item...",
      "on_change":actions["set_draft"]
    },[]),
    ui.node("ui/button",{"label":"Add","on_press":actions["add_item"]},[]),
    ui.node("ui/column",{"gap":"8px"},items.map(function (item,i){
      return ui.node("ui/row",{"key":i,"gap":"8px","alignItems":"center"},[
        ui.node("ui/text",{"value":item},[]),
        ui.node("ui/button",{
            "label":"Remove",
            "on_press":function (){
                  return actions["remove_item"](i);
                }
          },[])
      ]);
    }))
  ]);
}

function App(){
  let refresh = r.useRefresh();
  let controllerRef = React.useRef(make_controller());
  React.useEffect(function (){
    let controller = controllerRef.current;
    state.openf(controller);
    state.subscribef(controller,"react",function (_state,_rev){
      return refresh();
    });
    return function (){
      state.unsubscribef(controller,"react");
      state.closef(controller);
    };
  },[]);
  let controller = controllerRef.current;
  let state = state.snapshot(controller);
  let actions = state.actions_create(controller,["set_draft","add_item","remove_item"]);
  return React.createElement(
    "div",
    {"style":{"fontFamily":"system-ui, sans-serif"}},
    render_ui_node(react_registry(),view(state,actions))
  );
}

function mountf(){
  let el = React.createElement(App,null);
  window.PLAYGROUND.setStage(el);
  return true;
}

module.exports = {
  ["render_ui_node"]:render_ui_node,
  ["react_registry"]:react_registry,
  ["make_controller"]:make_controller,
  ["view"]:view,
  ["App"]:App,
  ["mountf"]:mountf
}