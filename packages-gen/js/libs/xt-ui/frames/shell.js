const ui = require("@xtalk/ui/core.js")

function view(frame,content){
  let opts = frame["opts"];
  return ui.node("ui/column",{"class":opts["class"] || "mx-auto w-full gap-6 p-4 md:p-8"},[
    ui.node("ui/column",{"class":"gap-1"},[
      ui.node("ui/title",{"value":opts["title"] || ""},[]),
      ui.node("ui/description",{"value":opts["description"] || ""},[])
    ]),
    ui.slot(frame["id"] + "/content",content || [],{})
  ]);
}

module.exports = {["view"]:view}