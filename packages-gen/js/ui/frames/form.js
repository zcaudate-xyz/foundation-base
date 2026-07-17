const ui = require("@xtalk/ui/core.js")

function invoke(actions,action_id,payload){
  let handler = (actions || {})[action_id];
  if("function" == (typeof handler)){
    return handler(payload);
  }
  return null;
}

function view(frame,state,actions){
  let form = state["form"] || state;
  let draft = form["draft"] || {};
  let errors = form["errors"] || {};
  let fields = (frame["opts"])["fields"] || [];
  let children = [];
  for(let field of fields){
    let id = field["id"];
    let component = field["component"] || "ui/input";
    children.push(ui.node("ui/column",{"class":"gap-2","key":id},[
      ui.node("ui/label",{"value":field["label"] || id,"for":id},[]),
      ui.node(component,{
          "id":id,
          "value":draft[id] || "",
          "disabled":true == form["pending"],
          "on_change":function (value){
                return invoke(actions,"set_field",{"field":id,"value":value});
              }
        },[]),
      ui.node(
          "ui/alert",
          {"tone":"error","hidden":null == errors[id]},
          [ui.text(errors[id] || "",{})]
        )
    ]));
  };
  children.push(ui.node("ui/button",{
    "pending":true == form["pending"],
    "disabled":(true != form["valid"]) || (true == form["pending"]),
    "on_press":function (_){
        return invoke(actions,"submit",draft);
      }
  },[ui.text("Save",{})]));
  return ui.node("ui/column",{"class":"gap-4"},children);
}

module.exports = {["invoke"]:invoke,["view"]:view}