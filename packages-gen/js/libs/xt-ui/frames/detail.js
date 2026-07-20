const ui = require("@xtalk/ui/core.js")

function view(frame,record){
  let fields = (frame["opts"])["fields"] || [];
  let children = [];
  for(let field of fields){
    let id = field["id"];
    children.push(ui.node("ui/row",{"class":"justify-between gap-4","key":id},[
      ui.node("ui/label",{"value":field["label"] || id},[]),
      ui.text(record[id] || "",{})
    ]));
  };
  return ui.node("ui/card-content",{"class":"flex flex-col gap-3"},children);
}

module.exports = {["view"]:view}